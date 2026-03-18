package cn.bugstack.sdk;

import cn.bugstack.sdk.domain.model.ChatCompletionRequest;
import cn.bugstack.sdk.domain.model.Model;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-17 11:03
 **/


public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");

        // 1.代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine())!=null){
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:"+exitCode);

        System.out.println("diff code："+diffCode.toString());

        // 2. chatglm 代码评审
        String log = codeReview(diffCode.toString());
        System.out.println("code review："+log.toString());

    }

    private static String codeReview(String diffCode)  throws Exception{
        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization","Bearer 2737be4b7a894e85a96d76f8e081087e.yhswnaTdUMMrS8BB");
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String jsonInputString = "{\n" +
                "          \"model\":\"glm-4\",\n" +
                "          \"stream\": \"true\",\n" +
                "          \"messages\": [\n" +
                "              {\n" +
                "                  \"role\": \"user\",\n" +
                "                  \"content\": \"你是一个Java高级编程架构师，精通各类场景方案、架构设计和编程语言，请您分析下 "+diffCode+" 看看代码是否符合规范\"\n" +
                "              }\n" +
                "          ]\n" +
                "        }";
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());

        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>(){{
            add(new ChatCompletionRequest.Prompt("user","你是一个Java高级编程架构师，精通各类场景方案、架构设计和编程语言，请你根据git diff记录，对代码做出评审。代码为："));
            add(new ChatCompletionRequest.Prompt("user",diffCode));

        }});

        try (OutputStream os = connection.getOutputStream()){
//            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine())!=null){
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        // 正则匹配每个 data: 后面的内容（直到下一个 data: 或结尾）
        Pattern pattern = Pattern.compile("data: (.*?)(?=data: |$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder fullContent = new StringBuilder();

        while (matcher.find()) {
            String chunk = matcher.group(1).trim(); // 获取 data: 之后的内容
            if (chunk.equals("[DONE]")) {
                continue; // 忽略结束标记
            }
            // 解析 JSON
            JsonNode root = mapper.readTree(chunk);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    String contents = delta.get("content").asText();
                    if (contents != null && !contents.isEmpty()) {
                        fullContent.append(contents);
                    }
                }
            }
        }
        return fullContent.toString();
    }
}
