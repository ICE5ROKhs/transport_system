package com.example.routeplanner.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class TrafficPredictor {

    private static final String PREDICT_API_URL = "http://127.0.0.1:5000/predict";

    public static double getPredictedVolume(int node, int time) throws Exception {
        // ƴ�� URL ����
        String urlString = PREDICT_API_URL + "?node=" + node + "&time=" + time;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // ���� GET ����
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("����ʧ�ܣ�HTTP�����룺" + conn.getResponseCode());
        }

        // ��ȡ��Ӧ
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        // ���� JSON ���ؽ��
        JSONObject json = new JSONObject(response.toString());
        return json.getDouble("volume");  // ����Ԥ����
    }
}
