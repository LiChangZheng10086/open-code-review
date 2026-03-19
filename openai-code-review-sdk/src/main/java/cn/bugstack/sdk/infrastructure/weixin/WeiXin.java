package cn.bugstack.sdk.infrastructure.weixin;

import cn.bugstack.sdk.infrastructure.weixin.dto.TemplateMessageDTO;
import cn.bugstack.sdk.types.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-18 18:24
 **/


public class WeiXin {
    private final Logger logger = LoggerFactory.getLogger(WeiXin.class);

    private final String appId;
    private final String secret;
    private final String touser;
    private final String templateId;

    public WeiXin(String appId, String secret, String touser, String templateId) {
        this.appId = appId;
        this.secret = secret;
        this.touser = touser;
        this.templateId = templateId;
    }

    public void sendTemplateMessage(String logUrl,Map<String, Map<String,String>> data) throws Exception{
        String accessToken = WXAccessTokenUtils.getAccessToken(appId,secret);

        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser,templateId);
        templateMessageDTO.setUrl(logUrl);
        templateMessageDTO.setData(data);

        URL url = new URL(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json; utf-8");
        conn.setRequestProperty("Accept","application/json");
        conn.setDoOutput(true);

        try(OutputStream os = conn.getOutputStream()){
            byte[] input = JSON.toJSONString(templateMessageDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input,0,input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(),StandardCharsets.UTF_8.name())){
            String response = scanner.useDelimiter("\\A").next();
            logger.info("openai-code-review weixin template message! {}", response);
        }
    }
}
