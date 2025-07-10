package com.example.routeplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Python 模型服务接口
 */
@Service
public class PythonModelService {

    private static final Logger logger = LoggerFactory.getLogger(PythonModelService.class);

    @Value("${route.planner.model-service-url}")
    private String modelServiceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 预测指定节点在指定时间的流量
     * @param nodeId 节点ID
     * @param timePoint 时间点
     * @return 预测的流量值
     */
    public double predictVolume(int nodeId, int timePoint) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URIBuilder(modelServiceUrl + "/predict")
                    .setParameter("node", String.valueOf(nodeId))
                    .setParameter("time", String.valueOf(timePoint))
                    .build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    double volume = jsonNode.get("volume").asDouble();

                    logger.debug("预测节点 {} 在时间点 {} 的流量: {}", nodeId, timePoint, volume);
                    return volume;
                } else {
                    logger.error("Python 模型服务返回错误状态码: {}, 响应: {}",
                            response.getStatusLine().getStatusCode(), responseBody);
                    return 1.0; // 默认返回最小流量
                }
            }
        } catch (URISyntaxException | IOException e) {
            logger.error("调用 Python 模型服务失败: {}", e.getMessage());
            return 1.0; // 默认返回最小流量
        }
    }

    /**
     * 检查 Python 模型服务是否可用
     * @return 是否可用
     */
    public boolean isServiceAvailable() {
        try {
            double testVolume = predictVolume(1, 100);
            return testVolume > 0;
        } catch (Exception e) {
            logger.error("Python 模型服务不可用: {}", e.getMessage());
            return false;
        }
    }
}