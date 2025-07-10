import java.util.*;

public class Graph {
    // 邻接表
    private Map<Integer, List<Edge>> adjList = new HashMap<>();

    public void addEdge(int from, int to, double distance) {
        adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(from, to, distance));
        adjList.computeIfAbsent(to, k -> new ArrayList<>()).add(new Edge(to, from, distance)); // 无向图
    }

    public List<Edge> getEdges(int node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public Set<Integer> getAllNodes() {
        return adjList.keySet();
    }

    public static class Edge {
        public int from;
        public int to;
        public double distance;
        public double weight;

        public Edge(int from, int to, double distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
