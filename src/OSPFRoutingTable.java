import java.util.*;

public class OSPFRoutingTable {
    static final int INF = Integer.MAX_VALUE;
    private Map<Integer, Router> routers;
    private List<Integer> routerIndex;
    private Router localRouter;

    public OSPFRoutingTable(Integer routerId){
        routers = new HashMap<>();
        localRouter = new Router(routerId);
        routers.put(routerId, localRouter);
    }
    public static class Router {
        private int routerId;
        private String routerName;
        private Map<Integer, Link> links;
        private Map<Integer, Route> routingTable;

        public Router(Integer routerId) {
            this.links = new HashMap<>();
            this.routingTable = new HashMap<>();
        }

        public int getRouterId(){
            return routerId;
        }

        public String getRouterName(){
            return "R" + routerId;
        }

        public void addLink(int neighbourId, String Interface, int cost) {
            links.put(neighbourId, new Link(neighbourId, Interface, cost));
        }

        public Map<Integer, Link> getLinks() {
            return links;
        }

        public Map<Integer, Route> getRoutingTable() {
            return routingTable;
        }

        public void updateRoutingTable(int destination, String nextHop, int metric) {
            routingTable.put(destination, new Route(destination, nextHop, metric));
        }

        public void displayRouterInfo() {

        }
    }


        public static class Link {
            private final int neighbourId;
            private final String Interface;
            private final int cost;

            public Link(int neighborId, String Interface, int cost) {
                this.neighbourId = neighborId;
                this.cost = cost;
                this.Interface = Interface;
            }

            public int getNeighborId() {
                return neighbourId;
            }

            public int getCost() {
                return cost;
            }

            public String getInterface() {
                return Interface;
            }
        }

        public static class Route {
            private final int destination;
            private final String nextHop;
            private final int metric;

            public Route(int destination, String nextHop, int metric) {
                this.destination = destination;
                this.nextHop = nextHop;
                this.metric = metric;
            }

            public int getDestination() {
                return destination;
            }

            public String getNextHop() {
                return nextHop;
            }

            public int getMetric() {
                return metric;
            }

            @Override
            public String toString() {
                return "Destination: " + destination +
                        ", NextHop: " + nextHop +
                        ", Metric: " + metric;
            }
        }

        public void addRouter(int routerId) {
            if (!routers.containsKey(routerId)) {
                routers.put(routerId, new Router(routerId));
            }
        }

        public void addLink(int Source_routerId, int Dest_routerId, String Interface, int cost) {
            addRouter(Source_routerId);
            addRouter(Dest_routerId);

            routers.get(Source_routerId).addLink(Dest_routerId, Interface, cost);
            routers.get(Dest_routerId).addLink(Source_routerId, Interface, cost);
        }

        public int[][] createAdjTable(){
            List<Integer> routerIDs = new ArrayList<>(routers.keySet());
            Collections.sort(routerIDs);

            int size = routers.size();
            int[][] graph = new int[size][size];

            Map<Integer, Integer> routerIdToIndex = new HashMap<>();
            for(int i = 0; i < size; i++){
                routerIdToIndex.put(routerIDs.get(i),i);
            }

            for(int i = 0; i < size; i++){
                for(int j = 0; j < size; j++){
                    graph[i][j] = (i==j) ? 0 : INF;
                }
            }

            for(Integer sourceId : routerIDs){
                Router sourceRouter = routers.get(sourceId);
                int sourceIndex = routerIdToIndex.get(sourceId);

                for(Map.Entry<Integer, Link> entry : sourceRouter.getLinks().entrySet()){
                    Integer destId = entry.getKey();

                    if(routerIdToIndex.containsKey(destId)){
                        int destIndex = routerIdToIndex.get(destId);
                        int cost = entry.getValue().getCost();
                        graph[sourceIndex][destIndex] = cost;
                    }
                }
            }

            return graph;
        }

        public void printAdjMatrix(int[][] graph){
            int size = graph.length;

            List<Integer> routerIDs = new ArrayList<>(routers.keySet());
            Collections.sort(routerIDs);

            Map<Integer, Integer> indexToRouterId = new HashMap<>();
            for (int i = 0; i < routerIDs.size(); i++){
                indexToRouterId.put(i, routerIDs.get(i));
            }

            System.out.print("\t");
            for(int i = 0; i < size; i++){
                System.out.printf("%-4d", i);
            }
            System.out.println();

            System.out.print("    ");
            for (int i = 0; i < size; i++) {
                System.out.print("----");
            }
            System.out.println();

            for(int i = 0; i < size; i++) {
                System.out.printf("%-3d|", i);
                for (int j = 0; j < size; j++) {
                    if (graph[i][j] == INF) {
                        System.out.printf("%-4d", 0);
                    } else {
                        System.out.printf("%-4d", graph[i][j]);
                    }
                }
                System.out.println();
            }
        }

        public Map<Integer, List<Edge>> createAdjList(){
            int[][] graph = createAdjTable();

            List<Integer> routerIds = new ArrayList<>(routers.keySet());
            Collections.sort(routerIds);

            Map<Integer, Integer> indexToRouter = new HashMap<>();
            for(int i = 0; i < routerIds.size(); i++){
                indexToRouter.put(i, routerIds.get(i));
            }

            Map<Integer, List<Edge>> adjList = new HashMap<>();

            for(int i = 0; i < graph.length; i++){
                adjList.put(i, new ArrayList<>());
            }

            for(int i = 0; i < graph.length; i++){
                for(int j = 0; j < graph[i].length; j++){
                    if(i !=j && graph[i][j] != INF){
                        adjList.get(i).add(new Edge(j, graph[i][j]));
                    }
                }
            }
            return adjList;
        }

        public static class Edge{
            private final int destination;
            private final int weight;

            public Edge(int destination, int weight){
                this.destination = destination;
                this.weight = weight;
            }

            public int getDestination(){return destination;}
            public int getWeight(){return weight;}

            @Override public String toString(){
                return destination + "(" + weight + ")";
            }

        }

        public int dijkstra(int source, int destination){
            List<Integer> routerIds = new ArrayList<>(routers.keySet());
            Collections.sort(routerIds);

            Map<Integer, Integer> routerToIndex = new HashMap<>();
            Map<Integer, Integer> indexToRouter = new HashMap<>();

            for(int i = 0; i < routerIds.size(); i ++){
                routerToIndex.put(routerIds.get(i), i);
                indexToRouter.put(i, routerIds.get(i));
            }

            if(!routerToIndex.containsKey(source) || !routerToIndex.containsKey(destination)){
                return INF;
            }

            int sourceIndex = routerToIndex.get(source);
            int destIndex = routerToIndex.get(destination);

            Map<Integer, List<Edge>> adjList = createAdjList();
            int[] distances = new int[routerIds.size()];
            int[] predecessor = new int[routerIds.size()];
            boolean[] visited = new boolean[routerIds.size()];

            Arrays.fill(distances, INF);
            Arrays.fill(predecessor, -1);
            distances[sourceIndex] = 0;

            for(int count = 0; count < routerIds.size(); count ++){
                int minIndex =-1;
                int minDist = INF;

                for(int i = 0; i < distances.length; i++){
                    if(!visited[i] && distances[i] < minDist){
                        minDist = distances[i];
                        minIndex = i;
                    }
                }

                if(minIndex == -1 || minDist == INF){
                    break;
                }
                if(minIndex == destIndex){
                    break;
                }
                visited[minIndex] = true;

                for(Edge edge: adjList.get(minIndex)){
                    int neighbour = edge.getDestination();
                    int weight = edge.getWeight();

                    if(!visited[neighbour]){
                        int newDist = distances[minIndex] + weight;
                        if(newDist < distances[neighbour]){
                            distances[neighbour] = newDist;
                            predecessor[neighbour] = minIndex;
                        }
                    }
                }
            }

            if(distances[destIndex] != INF){
                System.out.println("Path from Router " + source + " to Router " + destination + ":");
                printPath(predecessor, indexToRouter, sourceIndex, destIndex);
                System.out.println("Total cost: " + distances[destIndex]);
            }
            return distances[destIndex];
        }

        private void printPath(int[] predecessor, Map<Integer, Integer> indexToRouterId, int sourceIndex, int destIndex) {
            if (sourceIndex == destIndex) {
                System.out.print(indexToRouterId.get(sourceIndex));
                return;
            }

            if (predecessor[destIndex] == -1) {
                System.out.println("No path exists");
                return;
            }

            List<Integer> path = new ArrayList<>();
            int current = destIndex;

            while (current != -1) {
                path.add(indexToRouterId.get(current));
                current = predecessor[current];
            }

            // Print in correct order (source to destination)
            for (int i = path.size() - 1; i >= 0; i--) {
                System.out.print(path.get(i));
                if (i > 0) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
        }

        public void printAdjList(Map<Integer, List<Edge>> adjList){
            List<Integer> routersIds = new ArrayList<>(routers.keySet());
            Collections.sort(routersIds);

            Map<Integer, Integer> indexToRouter = new HashMap<>();
            for (int i = 0; i < routers.size(); i++){
                indexToRouter.put(i, routersIds.get(i));
            }
            System.out.println("Adjacency List Representation:");
            for(Map.Entry<Integer, List<Edge>> entry : adjList.entrySet()){
                int routerIndex = entry.getKey();
                int routerId = indexToRouter.get(routerIndex);

                System.out.print("Router " + routerId + " -> ");
                List<Edge> neighbours = entry.getValue();
                if(neighbours.isEmpty()){
                    System.out.println("No out going Links");
                } else {
                    for(int i = 0; i < neighbours.size(); i ++){
                        Edge edge = neighbours.get(i);
                        int neighbourId = indexToRouter.get(edge.getDestination());
                        System.out.print("Router " + neighbourId + " (cost :" + edge.getWeight() + ")");

                        if(i < neighbours.size() - 1){
                            System.out.print(", ");
                        }
                    }
                    System.out.println();
                }
            }
        }

        public void printRoutingTable(){
            System.out.println("OSPF Routing Table for router:" + localRouter.getRouterId());
            System.out.println("=================================================");
            System.out.printf("%-15s %-15s %-10s\n", "Destination", "Next Hop", "Metric");
            System.out.println("=================================================");

        }

    public static void main(String[] args) {
        OSPFRoutingTable ospf = new OSPFRoutingTable(1);

        ospf.addRouter(2);
        ospf.addRouter(3);
        ospf.addRouter(4);
        ospf.addRouter(5);

        ospf.addLink(1, 2, "Fa0/1 -> Fa0/1", 3);
        ospf.addLink(1, 3, "Fa0/2 -> Fa0/1", 5);
        ospf.addLink(3, 5, "Fa0/1 -> Fa0/2", 3);
        ospf.addLink(5, 4, "Fa0/1 -> Fa0/1", 7);
        ospf.addLink(2, 4, "Fa0/2 -> Fa0/2", 6);

        System.out.println("\n--- Adjacency List Representation ---");
        Map<Integer, List<Edge>> adjList = ospf.createAdjList();
        ospf.printAdjList(adjList);

        System.out.println("\n--- Path calculation ---");
        int cost = ospf.dijkstra(1, 5);

        if(cost == INF){
            System.out.println("There is no path from the source to the destination router");
        } else {
            System.out.println("The shortest path cost from these routers is: " + cost);
        }
        int[][] adjTable = ospf.createAdjTable();
        ospf.printAdjMatrix(adjTable);

        //ospf.calculateRoutingTable();
        //ospf.printRoutingTable();
    }

}
