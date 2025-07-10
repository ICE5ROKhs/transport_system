package com.example.routeplanner.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class TrafficPredictor {

    private static final String PREDICT_API_URL = "http://127.0.0.1:5000/predict";

    public static double getPredictedVolume(int node, int time) throws Exception {
        // 拼接 URL 参数
        String urlString = PREDICT_API_URL + "?node=" + node + "&time=" + time;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 配置 GET 请求
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("请求失败，HTTP错误码：" + conn.getResponseCode());
        }

        // 读取响应
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        // 解析 JSON 返回结果
        JSONObject json = new JSONObject(response.toString());
        return json.getDouble("volume");  // 返回预测结果
    }
}
