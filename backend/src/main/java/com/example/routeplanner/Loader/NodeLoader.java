import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.commons.csv.*;

public class NodeLoader {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 读取 CSV 文件，解析节点信息，并根据距离阈值构建图
     * CSV 文件格式示例（带表头）：
     * sensor_id,latitude,longitude
     * 0,33.57593488,-116.5630278
     * 1,33.64841237,-116.52706
     * ...
     *
     * @param csvFilePath CSV 文件路径
     * @param distanceThresholdKm 最大连接距离阈值（单位：km）
     * @return 构建好的图
     * @throws IOException 读取文件异常
     */
    public static Graph loadGraphFromCSV(String csvFilePath, double distanceThresholdKm) throws IOException {
        // 先读所有节点
        List<Node> nodes = new ArrayList<>();

        try (FileReader reader = new FileReader(csvFilePath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : records) {
                int sensorId = Integer.parseInt(record.get("sensor_id"));
                double latitude = Double.parseDouble(record.get("latitude"));
                double longitude = Double.parseDouble(record.get("longitude"));
                nodes.add(new Node(sensorId, latitude, longitude));
            }
        }

        // 构造图
        Graph graph = new Graph();

        // 添加所有节点到图中（为了兼容，如果 Graph 有 addNode 函数的话，可以调用）
        for (Node node : nodes) {
            // 这里只是确保节点存在，边会在下面添加
            graph.addEdge(node.id, node.id, 0);  // 自己连自己距离0，或者图类加个 addNode
        }

        // 计算两两节点间距离，距离小于阈值则连边
        for (int i = 0; i < nodes.size(); i++) {
            Node n1 = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node n2 = nodes.get(j);
                double dist = haversine(n1.longitude, n1.latitude, n2.longitude, n2.latitude);
                if (dist <= distanceThresholdKm) {
                    graph.addEdge(n1.id, n2.id, dist);
                }
            }
        }

        return graph;
    }

    // 节点实体类
    private static class Node {
        int id;
        double latitude;
        double longitude;

        public Node(int id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    // 计算地球两点间距离，单位：km
    private static double haversine(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = radLat2 - radLat1;
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_KM * c;
    }
}
