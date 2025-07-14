package com.example.routeplanner.controller;

import com.example.routeplanner.model.AIRequest;
import com.example.routeplanner.model.AIResponse;
import com.example.routeplanner.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * 文本生成 - 完整API
     */
    @PostMapping("/chat/completions")
    public ResponseEntity<AIResponse> chatCompletions(@Valid @RequestBody AIRequest request) {
        try {
            AIResponse response = aiService.chatCompletion(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 异步文本生成
     */
    @PostMapping("/chat/completions/async")
    public CompletableFuture<ResponseEntity<AIResponse>> chatCompletionsAsync(@Valid @RequestBody AIRequest request) {
        return aiService.chatCompletionAsync(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * 简单文本生成
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateText(@RequestBody Map<String, String> requestBody) {
        try {
            String prompt = requestBody.get("prompt");
            String model = requestBody.getOrDefault("model", "ernie-3.5-8k");

            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String response = aiService.generateText(prompt, model);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "生成失败: " + e.getMessage()));
        }
    }

    /**
     * 多轮对话
     */
    @PostMapping("/chat")
    public ResponseEntity<AIResponse> multiRoundChat(@RequestBody ChatRequest chatRequest) {
        try {
            AIResponse response = aiService.multiRoundChat(
                    chatRequest.getMessages(),
                    chatRequest.getModel()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Function Calling对话
     */
    @PostMapping("/chat/functions")
    public ResponseEntity<AIResponse> chatWithFunctions(@RequestBody FunctionChatRequest functionRequest) {
        try {
            AIResponse response = aiService.chatWithFunctions(
                    functionRequest.getMessages(),
                    functionRequest.getTools(),
                    functionRequest.getModel()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 流式对话
     */
    @PostMapping("/chat/stream")
    public SseEmitter streamChat(@RequestBody AIRequest request) {
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时

        try {
            aiService.streamChat(request, new AIService.StreamCallback() {
                @Override
                public void onMessage(AIResponse response) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(response));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete() {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("complete")
                                .data("stream_complete"));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    emitter.completeWithError(e);
                }
            });
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 获取支持的模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<List<String>> getSupportedModels() {
        try {
            List<String> models = aiService.getSupportedModels();
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean isHealthy = aiService.isApiHealthy();
            Map<String, Object> status = Map.of(
                    "status", isHealthy ? "healthy" : "unhealthy",
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * 快速问答接口
     */
    @PostMapping("/quick-ask")
    public ResponseEntity<Map<String, String>> quickAsk(@RequestBody Map<String, String> requestBody) {
        try {
            String question = requestBody.get("question");
            String context = requestBody.get("context");

            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "问题不能为空"));
            }

            // 构建提示词
            String prompt = context != null && !context.trim().isEmpty()
                    ? "基于以下上下文回答问题：\n上下文：" + context + "\n问题：" + question
                    : question;

            String response = aiService.generateText(prompt);
            return ResponseEntity.ok(Map.of("answer", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "回答生成失败: " + e.getMessage()));
        }
    }

    /**
     * 批量文本生成
     */
    @PostMapping("/batch-generate")
    public ResponseEntity<List<Map<String, String>>> batchGenerate(@RequestBody BatchGenerateRequest batchRequest) {
        try {
            List<Map<String, String>> results = new ArrayList<>();

            for (String prompt : batchRequest.getPrompts()) {
                try {
                    String response = aiService.generateText(prompt, batchRequest.getModel());
                    results.add(Map.of("prompt", prompt, "response", response, "status", "success"));
                } catch (Exception e) {
                    results.add(Map.of("prompt", prompt, "error", e.getMessage(), "status", "failed"));
                }
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 内部类：聊天请求
    public static class ChatRequest {
        private List<AIRequest.Message> messages;
        private String model = "ernie-3.5-8k";

        public List<AIRequest.Message> getMessages() { return messages; }
        public void setMessages(List<AIRequest.Message> messages) { this.messages = messages; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    // 内部类：Function Calling请求
    public static class FunctionChatRequest {
        private List<AIRequest.Message> messages;
        private List<AIRequest.Tool> tools;
        private String model = "ernie-3.5-8k";

        public List<AIRequest.Message> getMessages() { return messages; }
        public void setMessages(List<AIRequest.Message> messages) { this.messages = messages; }

        public List<AIRequest.Tool> getTools() { return tools; }
        public void setTools(List<AIRequest.Tool> tools) { this.tools = tools; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    // 内部类：批量生成请求
    public static class BatchGenerateRequest {
        private List<String> prompts;
        private String model = "ernie-3.5-8k";

        public List<String> getPrompts() { return prompts; }
        public void setPrompts(List<String> prompts) { this.prompts = prompts; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "参数错误", "message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "服务器错误", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "未知错误", "message", "系统异常，请稍后重试"));
    }
}