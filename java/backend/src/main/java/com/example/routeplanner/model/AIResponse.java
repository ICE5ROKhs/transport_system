package com.example.routeplanner.model;

import java.util.List;

public class AIResponse {

    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private SearchResults searchResults;

    // 内部类：选择项
    public static class Choice {
        private Integer index;
        private Message message;
        private Delta delta; // 用于流式响应
        private String finishReason;
        private Integer flag;

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }

        public Delta getDelta() { return delta; }
        public void setDelta(Delta delta) { this.delta = delta; }

        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }

        public Integer getFlag() { return flag; }
        public void setFlag(Integer flag) { this.flag = flag; }
    }

    // 内部类：消息（与请求中的Message类似，但用于响应）
    public static class Message {
        private String role;
        private String content;
        private List<ToolCall> toolCalls;
        private String thinking; // 用于思考模式

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<ToolCall> getToolCalls() { return toolCalls; }
        public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

        public String getThinking() { return thinking; }
        public void setThinking(String thinking) { this.thinking = thinking; }
    }

    // 内部类：增量（用于流式响应）
    public static class Delta {
        private String role;
        private String content;
        private List<ToolCall> toolCalls;
        private String thinking;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<ToolCall> getToolCalls() { return toolCalls; }
        public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

        public String getThinking() { return thinking; }
        public void setThinking(String thinking) { this.thinking = thinking; }
    }

    // 内部类：工具调用
    public static class ToolCall {
        private String id;
        private String type;
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

    // 内部类：使用情况统计
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;

        public Integer getPromptTokens() { return promptTokens; }
        public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

        public Integer getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

        public Integer getTotalTokens() { return totalTokens; }
        public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    }

    // 内部类：搜索结果
    public static class SearchResults {
        private List<SearchResult> results;
        private String searchQuery;

        public static class SearchResult {
            private String title;
            private String url;
            private String snippet;

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }

            public String getSnippet() { return snippet; }
            public void setSnippet(String snippet) { this.snippet = snippet; }
        }

        public List<SearchResult> getResults() { return results; }
        public void setResults(List<SearchResult> results) { this.results = results; }

        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    }

    // 内部类：错误信息
    public static class ErrorInfo {
        private String code;
        private String message;
        private String type;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // 构造函数
    public AIResponse() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }

    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public SearchResults getSearchResults() { return searchResults; }
    public void setSearchResults(SearchResults searchResults) { this.searchResults = searchResults; }

    // 便利方法
    public String getFirstChoiceContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }

    public boolean isSuccess() {
        return choices != null && !choices.isEmpty();
    }
}