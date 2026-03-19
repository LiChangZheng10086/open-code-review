package cn.bugstack.sdk;

import cn.bugstack.sdk.domain.service.impl.OpenAiCodeReviewService;
import cn.bugstack.sdk.infrastructure.git.GitCommand;
import cn.bugstack.sdk.infrastructure.openai.IOpenAi;
import cn.bugstack.sdk.infrastructure.openai.impl.ChatGLM;
import cn.bugstack.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-17 11:03
 **/


public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    // weixin配置
    private String weixin_appid = "wx8c26a7a3e28a6584";
    private String weixin_secret = "7109903c2ea0730806865b29f351a3e7";
    private String weixin_touser = "oj2LG2wjOHkEoDPpMoGI6jI4Uz3g";
    private String weixin_template_id = "bZ_aNNDikPGXmzX4Zrmu87Es1Sttosm3V3_UagRHHyI";

    // CahtGLM配置
    private String chatglm_apiHost = "";
    private String chatglm_apiKeySecret = "";

    // GitHub配置
    private String github_review_log_uri ;
    private String github_token;

    // 工程配置
    private String github_project;
    private String github_branch;
    private String github_author;



    public static void main(String[] args) throws Exception {
        logger.info("open ai 代码评审测试执行");

        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        IOpenAi openAi = new ChatGLM(
                getEnv("CHATGLM_APIHOST"),
                getEnv("CHATGLM_APIKEYSECRET"));

        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAi, weiXin);
        openAiCodeReviewService.exec();
        logger.info("openai-code-review done!");
    }


    private static String getEnv(String key){
        String value = System.getenv(key);
        if (null==value||value.isEmpty()) {
            throw new RuntimeException("token is null");
        }
        return value;
    }
}
