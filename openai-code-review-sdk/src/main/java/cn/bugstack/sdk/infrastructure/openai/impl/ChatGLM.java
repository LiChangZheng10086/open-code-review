package cn.bugstack.sdk.infrastructure.openai.impl;

import cn.bugstack.sdk.domain.model.Model;
import cn.bugstack.sdk.infrastructure.openai.IOpenAi;
import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-18 18:05
 **/


public class ChatGLM implements IOpenAi {

    private static Logger logger = LoggerFactory.getLogger(ChatGLM.class);

    private final String apiHost;
    private final String apiKeySecret;


    public ChatGLM(String apiHost, String apiKeySecret) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKeySecret;
    }



    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        URL url = new URL(apiHost);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization","Bearer "+apiKeySecret);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        try(OutputStream os = connection.getOutputStream()){
            byte[] input = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input,0,input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine=in.readLine())!=null){
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        logger.info("评审结果:"+content.toString());
        return JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
