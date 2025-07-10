import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.commons.csv.*;

public class NodeLoader {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * ��ȡ CSV �ļ��������ڵ���Ϣ�������ݾ�����ֵ����ͼ
     * CSV �ļ���ʽʾ��������ͷ����
     * sensor_id,latitude,longitude
     * 0,33.57593488,-116.5630278
     * 1,33.64841237,-116.52706
     * ...
     *
     * @param csvFilePath CSV �ļ�·��
     * @param distanceThresholdKm ������Ӿ�����ֵ����λ��km��
     * @return �����õ�ͼ
     * @throws IOException ��ȡ�ļ��쳣
     */
    public static Graph loadGraphFromCSV(String csvFilePath, double distanceThresholdKm) throws IOException {
        // �ȶ����нڵ�
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

        // ����ͼ
        Graph graph = new Graph();

        // ������нڵ㵽ͼ�У�Ϊ�˼��ݣ���� Graph �� addNode �����Ļ������Ե��ã�
        for (Node node : nodes) {
            // ����ֻ��ȷ���ڵ���ڣ��߻����������
            graph.addEdge(node.id, node.id, 0);  // �Լ����Լ�����0������ͼ��Ӹ� addNode
        }

        // ���������ڵ����룬����С����ֵ������
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

    // �ڵ�ʵ����
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

    // ��������������룬��λ��km
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
