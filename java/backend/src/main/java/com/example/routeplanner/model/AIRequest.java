package com.example.routeplanner.model;

import java.util.List;
import java.util.Map;

public class AIRequest {

    // 必选参数
    private String model;
    private List<Message> messages;

    // 可选参数
    private Boolean stream = false;
    private StreamOptions streamOptions;
    private Double temperature;
    private Double topP;
    private Double penaltyScore;
    private Integer maxTokens;
    private Integer seed;
    private List<String> stop;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Double repetitionPenalty;
    private List<Tool> tools;
    private Object toolChoice; // 可以是String或ToolChoice对象
    private Boolean parallelToolCalls = true;
    private WebSearch webSearch;
    private ResponseFormat responseFormat;
    private Map<String, String> metadata;
    private Boolean enableThinking = false;
    private String user;

    // 内部类：消息
    public static class Message {
        private String role; // "system", "user", "assistant"
        private String content;
        private List<ToolCall> toolCalls; // 用于assistant消息
        private String toolCallId; // 用于tool消息

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<ToolCall> getToolCalls() { return toolCalls; }
        public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

        public String getToolCallId() { return toolCallId; }
        public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }
    }

    // 内部类：流式选项
    public static class StreamOptions {
        private Boolean includeUsage = false;

        public Boolean getIncludeUsage() { return includeUsage; }
        public void setIncludeUsage(Boolean includeUsage) { this.includeUsage = includeUsage; }
    }

    // 内部类：工具定义
    public static class Tool {
        private String type = "function";
        private Function function;

        public static class Function {
            private String name;
            private String description;
            private Object parameters; // JSON Schema对象

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }

            public Object getParameters() { return parameters; }
            public void setParameters(Object parameters) { this.parameters = parameters; }
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Function getFunction() { return function; }
        public void setFunction(Function function) { this.function = function; }
    }

    // 内部类：工具调用
    public static class ToolCall {
        private String id;
        private String type = "function";
        private FunctionCall function;

        public static class FunctionCall {
            private String name;
            private String arguments;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getArguments() { return arguments; }
            public void setArguments(String arguments) { this.arguments = arguments; }
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public FunctionCall getFunction() { return function; }
        public void setFunction(FunctionCall function) { this.function = function; }
    }

    // 内部类：工具选择
    public static class ToolChoice {
        private String type = "function";
        private Function function;

        public static class Function {
            private String name;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Function getFunction() { return function; }
        public void setFunction(Function function) { this.function = function; }
    }

    // 内部类：网络搜索
    public static class WebSearch {
        private Boolean enable = true;
        private String searchQuery;

        public Boolean getEnable() { return enable; }
        public void setEnable(Boolean enable) { this.enable = enable; }

        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    }

    // 内部类：响应格式
    public static class ResponseFormat {
        private String type = "text"; // "text" 或 "json_object"
        private Object jsonSchema; // JSON Schema对象

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Object getJsonSchema() { return jsonSchema; }
        public void setJsonSchema(Object jsonSchema) { this.jsonSchema = jsonSchema; }
    }

    // 构造函数
    public AIRequest() {}

    public AIRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }

    public StreamOptions getStreamOptions() { return streamOptions; }
    public void setStreamOptions(StreamOptions streamOptions) { this.streamOptions = streamOptions; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }

    public Double getPenaltyScore() { return penaltyScore; }
    public void setPenaltyScore(Double penaltyScore) { this.penaltyScore = penaltyScore; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Integer getSeed() { return seed; }
    public void setSeed(Integer seed) { this.seed = seed; }

    public List<String> getStop() { return stop; }
    public void setStop(List<String> stop) { this.stop = stop; }

    public Double getFrequencyPenalty() { return frequencyPenalty; }
    public void setFrequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }

    public Double getPresencePenalty() { return presencePenalty; }
    public void setPresencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; }

    public Double getRepetitionPenalty() { return repetitionPenalty; }
    public void setRepetitionPenalty(Double repetitionPenalty) { this.repetitionPenalty = repetitionPenalty; }

    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools; }

    public Object getToolChoice() { return toolChoice; }
    public void setToolChoice(Object toolChoice) { this.toolChoice = toolChoice; }

    public Boolean getParallelToolCalls() { return parallelToolCalls; }
    public void setParallelToolCalls(Boolean parallelToolCalls) { this.parallelToolCalls = parallelToolCalls; }

    public WebSearch getWebSearch() { return webSearch; }
    public void setWebSearch(WebSearch webSearch) { this.webSearch = webSearch; }

    public ResponseFormat getResponseFormat() { return responseFormat; }
    public void setResponseFormat(ResponseFormat responseFormat) { this.responseFormat = responseFormat; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public Boolean getEnableThinking() { return enableThinking; }
    public void setEnableThinking(Boolean enableThinking) { this.enableThinking = enableThinking; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}