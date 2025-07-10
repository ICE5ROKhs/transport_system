import java.util.*;

public class RoutePlanner {
    private Graph graph;
    private double alpha = 0.05;  // 拥堵系数中流量权重

    public RoutePlanner(Graph graph) {
        this.graph = graph;
    }

    public void updateEdgeWeights(int time) {
        for (int node : graph.getAllNodes()) {
            for (Graph.Edge edge : graph.getEdges(node)) {
                if (edge.from < edge.to) { // 防止重复处理无向边
                    double volumeU = predictVolume(edge.from, time);
                    double volumeV = predictVolume(edge.to, time);
                    double avgFlow = (volumeU + volumeV) / 2.0;
                    double congestion = edge.distance * (1 + alpha * avgFlow);
                    edge.setWeight(congestion);
                }
            }
        }
    }

    public List<Integer> shortestPath(int start, int end) {
        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));

        for (int node : graph.getAllNodes()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(start, 0.0);
        pq.offer(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0];

            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                double alt = dist.get(u) + edge.weight;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.offer(new int[]{v, (int) alt});
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        if (!path.isEmpty() && path.get(0) == start) {
            return path;
        } else {
            return Collections.emptyList(); // 无路径
        }
    }

    private double predictVolume(int node, int time) {
        // 发 HTTP 请求到 Flask 服务接口
        try {
            String url = String.format("http://127.0.0.1:5000/predict?node=%d&time=%d", node, time);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");

            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                String json = scanner.useDelimiter("\\A").next();
                return parseVolumeFromJson(json); // 提取 volume 字段
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1.0; // 默认流量值
        }
    }

    private double parseVolumeFromJson(String json) {
        // 简单解析 volume 值
        int index = json.indexOf("volume");
        if (index != -1) {
            String numStr = json.substring(index).replaceAll("[^0-9.]", "");
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException ignored) {}
        }
        return 1.0;
    }
}
