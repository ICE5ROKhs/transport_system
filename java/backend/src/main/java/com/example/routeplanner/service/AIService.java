package com.example.routeplanner.service;

import com.example.routeplanner.model.AIRequest;
import com.example.routeplanner.model.AIResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    private static final String QIANFAN_API_URL = "https://qianfan.baidubce.com/v2/chat/completions";

    @Value("${qianfan.api.key:}")
    private String apiKey;

    @Value("${qianfan.api.timeout:30}")
    private int timeoutSeconds;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送同步文本生成请求
     */
    public AIResponse chatCompletion(AIRequest request) {
        try {
            // 验证请求
            validateRequest(request);

            // 构建HTTP请求
            HttpHeaders headers = buildHeaders();
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    QIANFAN_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            return objectMapper.readValue(response.getBody(), AIResponse.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化/反序列化失败", e);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("客户端错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("服务器错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败", e);
        }
    }

    /**
     * 发送异步文本生成请求
     */
    public CompletableFuture<AIResponse> chatCompletionAsync(AIRequest request) {
        return CompletableFuture.supplyAsync(() -> chatCompletion(request))
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 简单的文本生成方法
     */
    public String generateText(String prompt) {
        return generateText(prompt, "ernie-3.5-8k");
    }

    /**
     * 指定模型的文本生成方法
     */
    public String generateText(String prompt, String model) {
        AIRequest request = createSimpleRequest(prompt, model);
        AIResponse response = chatCompletion(request);
        return response.getFirstChoiceContent();
    }

    /**
     * 多轮对话
     */
    public AIResponse multiRoundChat(List<AIRequest.Message> messages, String model) {
        AIRequest request = new AIRequest();
        request.setModel(model);
        request.setMessages(messages);
        return chatCompletion(request);
    }

    /**
     * 带Function Calling的对话
     */
    public AIResponse chatWithFunctions(List<AIRequest.Message> messages,
                                        List<AIRequest.Tool> tools,
                                        String model) {
        AIRequest request = new AIRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setTools(tools);
        request.setToolChoice("auto");
        return chatCompletion(request);
    }

    /**
     * 流式对话（需要单独的流式处理实现）
     */
    public void streamChat(AIRequest request, StreamCallback callback) {

        request.setStream(true);


        try {
            AIResponse response = chatCompletion(request);
            callback.onMessage(response);
            callback.onComplete();
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    /**
     * 创建简单的请求对象
     */
    private AIRequest createSimpleRequest(String prompt, String model) {
        AIRequest request = new AIRequest();
        request.setModel(model);

        List<AIRequest.Message> messages = new ArrayList<>();
        messages.add(new AIRequest.Message("user", prompt));
        request.setMessages(messages);

        return request;
    }

    /**
     * 创建系统消息
     */
    public AIRequest.Message createSystemMessage(String content) {
        return new AIRequest.Message("system", content);
    }

    /**
     * 创建用户消息
     */
    public AIRequest.Message createUserMessage(String content) {
        return new AIRequest.Message("user", content);
    }

    /**
     * 创建助手消息
     */
    public AIRequest.Message createAssistantMessage(String content) {
        return new AIRequest.Message("assistant", content);
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(AIRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求对象不能为空");
        }

        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("模型ID不能为空");
        }

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key未配置");
        }

        // 验证消息内容
        for (AIRequest.Message message : request.getMessages()) {
            if (message.getRole() == null || message.getRole().trim().isEmpty()) {
                throw new IllegalArgumentException("消息角色不能为空");
            }
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
        }
    }

    /**
     * 流式回调接口
     */
    public interface StreamCallback {
        void onMessage(AIResponse response);
        void onComplete();
        void onError(Exception e);
    }

    /**
     * 获取支持的模型列表
     */
    public List<String> getSupportedModels() {
        return List.of(
                "ernie-3.5-8k",
                "ernie-4.0-turbo-8k",
                "ernie-4.0-turbo-128k",
                "ernie-speed-128k",
                "ernie-speed-8k",
                "ernie-lite-8k",
                "ernie-tiny-8k"
        );
    }

    /**
     * 检查API连接状态
     */
    public boolean isApiHealthy() {
        try {
            String testResponse = generateText("测试", "ernie-3.5-8k");
            return testResponse != null && !testResponse.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}