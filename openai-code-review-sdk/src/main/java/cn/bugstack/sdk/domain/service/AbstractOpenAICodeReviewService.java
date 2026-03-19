package cn.bugstack.sdk.domain.service;

import cn.bugstack.sdk.infrastructure.git.GitCommand;
import cn.bugstack.sdk.infrastructure.openai.IOpenAi;
import cn.bugstack.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-18 18:44
 **/


public abstract class AbstractOpenAICodeReviewService implements IOpenAICodeReviewService{
    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAICodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAi openAi;
    protected final WeiXin weiXin;

    public AbstractOpenAICodeReviewService(GitCommand gitCommand, IOpenAi openAi, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAi = openAi;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() {
        try{
            // 1. 获取代码
            String diffCode = gitDiffCode();
            // 2。 开始评审代码
            String recommend = codeReview(diffCode);
            // 3. 记录评审结果：返回日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发生消息通知，日志地址，通知内容
            pushMessage(logUrl);
        }catch (Exception e){
            logger.info("open-ai-code-review error",e);
        }
    }

    protected abstract String gitDiffCode() throws Exception;
    protected abstract String codeReview(String diffCode) throws Exception;
    protected abstract String recordCodeReview(String recommend) throws Exception;
    protected abstract void pushMessage(String logUrl) throws Exception;



}
