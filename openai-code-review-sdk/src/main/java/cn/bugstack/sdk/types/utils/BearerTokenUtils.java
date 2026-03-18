package cn.bugstack.sdk.types.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: openai_code_review
 * @description: 工具类
 * @author: lcz
 * @create: 2026-03-18 11:20
 **/


public class BearerTokenUtils {

    // 过期时间
    private static final long expireMillis = 30 * 60 * 1000L;

    // 缓存服务
    private static Cache<String,String> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(expireMillis - (60 * 1000l), TimeUnit.MILLISECONDS)
            .build();

    public static String getToken(String apiKeySecret){
        String[] split = apiKeySecret.split("\\.");
        return getToken(split[0],split[1]);
    }


    public static String getToken(String apiKey,String apiSercet){
        // 缓存 token
        String token = cache.getIfPresent(apiKey);
        if (null!=token)return token;
        // 创建token
        Algorithm algorithm = Algorithm.HMAC256(apiSercet.getBytes(StandardCharsets.UTF_8));
        Map<String,Object> payload = new HashMap<>();
        payload.put("api_key",apiKey);
        payload.put("exp",System.currentTimeMillis()+expireMillis);
        payload.put("timestamp", Calendar.getInstance().getTimeInMillis());
        Map<String,Object> headerClaims = new HashMap<>();
        headerClaims.put("alg","HS256");
        headerClaims.put("sign_type","SIGN");
        token = JWT.create().withPayload(payload).withHeader(headerClaims).sign(algorithm);
        cache.put(apiKey,token);
        return token;

    }
}
