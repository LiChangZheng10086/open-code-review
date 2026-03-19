package cn.bugstack.sdk.domain.service.impl;

import cn.bugstack.sdk.domain.model.Model;
import cn.bugstack.sdk.domain.service.AbstractOpenAICodeReviewService;
import cn.bugstack.sdk.infrastructure.git.GitCommand;
import cn.bugstack.sdk.infrastructure.openai.IOpenAi;
import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import cn.bugstack.sdk.infrastructure.weixin.WeiXin;
import cn.bugstack.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-18 18:52
 **/


public class OpenAiCodeReviewService extends AbstractOpenAICodeReviewService   {
    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAi openAi, WeiXin weiXin) {
        super(gitCommand, openAi, weiXin);
    }

    @Override
    protected String gitDiffCode() throws Exception {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });
        ChatCompletionSyncResponseDTO completions = openAi.completions(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String,String>> data = new HashMap<>();
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.REPO_NAME,gitCommand.getProject());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.BRANCH_NAME,gitCommand.getBranch());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR,gitCommand.getAuthor());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE,gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl,data);

    }
}
