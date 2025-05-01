import java.util.*;

public class OSPFRoutingTable {
    static final int INF = Integer.MAX_VALUE;
    private Map<Integer, Router> routers; //Map of routers
    private Router localRouter; //creation of a local router on startup in case no routers are added

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
            this.routerId = routerId;
            this.links = new HashMap<>();
            this.routingTable = new HashMap<>();
        }

        public int getRouterId(){
            return routerId;
        }

        public String getRouterName(){
            routerName = "R" + routerId;
            return routerName;
        }

        public void addLink(int neighbourId, String Interface, int cost) {
            links.put(neighbourId, new Link(neighbourId, Interface, cost)); //addition of a link to a neighbouring node
        }

        public Map<Integer, Link> getLinks() {
            return links;
        }

        public Map<Integer, Route> getRoutingTable() {
            return routingTable;
        }

        public void updateRoutingTable(int destination, String nextHop, int metric) {
            routingTable.put(destination, new Route(destination, nextHop)); //addition or update a new route
        }

        public void displayRouterInfo() {
            //all display info will be added (wip)
        }
    }

        //link class for representing neighbours and their cost/metric
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

        //Route class representing overall routes across all links to a destination node: an overall cost might be added for easy reference
        public static class Route {
            private final int destination;
            private final String nextHop;

            public Route(int destination, String nextHop) {
                this.destination = destination;
                this.nextHop = nextHop;
            }

            public int getDestination() {
                return destination;
            }

            public String getNextHop() {
                return nextHop;
            }


            @Override
            public String toString() {
                return "Destination: " + destination +
                        ", NextHop: " + nextHop;
            }
        }
        //creation of a new router if not exists
        public void addRouter(int routerId) {
            if (!routers.containsKey(routerId)) {
                routers.put(routerId, new Router(routerId));
            }
        }
        //creation of a new link from a source to a destination. universally traversal between both
        public void addLink(int Source_routerId, int Dest_routerId, String Interface, int cost) {
            addRouter(Source_routerId);
            addRouter(Dest_routerId);

            routers.get(Source_routerId).addLink(Dest_routerId, Interface, cost);
            routers.get(Dest_routerId).addLink(Source_routerId, Interface, cost);
        }
        //creation of the adjacency matrix
        public int[][] createAdjTable(){
            List<Integer> routerIDs = new ArrayList<>(routers.keySet());
            Collections.sort(routerIDs);

            int size = routers.size();
            int[][] graph = new int[size][size]; //overall size of the matrix

            Map<Integer, Integer> routerIdToIndex = new HashMap<>(); //router to index of i
            for(int i = 0; i < size; i++){
                routerIdToIndex.put(routerIDs.get(i),i);
            }

            for(int i = 0; i < size; i++){
                for(int j = 0; j < size; j++){
                    graph[i][j] = (i==j) ? 0 : INF;
                } //set to 0 if index router of i == j. r1 = r1. set else to infinity
            }

            for(Integer sourceId : routerIDs){
                Router sourceRouter = routers.get(sourceId);
                int sourceIndex = routerIdToIndex.get(sourceId);

                for(Map.Entry<Integer, Link> entry : sourceRouter.getLinks().entrySet()){
                    Integer destId = entry.getKey(); //links through all the connections of the source router

                    if(routerIdToIndex.containsKey(destId)){
                        int destIndex = routerIdToIndex.get(destId);
                        int cost = entry.getValue().getCost();
                        graph[sourceIndex][destIndex] = cost; //updating the cost as the observational link
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
                System.out.printf("%-4d", i); //formatting axis of 2d array
            }
            System.out.println();

            System.out.print("    "); //formatting axis of 2d array
            for (int i = 0; i < size; i++) {
                System.out.print("----");
            }
            System.out.println();

            for(int i = 0; i < size; i++) {
                System.out.printf("%-3d|", i);
                for (int j = 0; j < size; j++) {
                    if (graph[i][j] == INF) {
                        System.out.printf("%-4d", 0); //printing 0 if unreachable
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

            Map<Integer, List<Edge>> adjList = new HashMap<>(); //creation of a hash map that takes the class of Edge values resembling links

            for(int i = 0; i < graph.length; i++){
                adjList.put(i, new ArrayList<>()); //creates an empty array for each router index
            }

            for(int i = 0; i < graph.length; i++){
                for(int j = 0; j < graph[i].length; j++){
                    if(i !=j && graph[i][j] != INF){ //if not self or unreachable edge
                        adjList.get(i).add(new Edge(j, graph[i][j]));
                    }
                }
            }
            return adjList;
        }

        //Edge class to determine the neighbour and cost: most likely will be updated to Link class
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
            } //check if source and destination exists

            int sourceIndex = routerToIndex.get(source);
            int destIndex = routerToIndex.get(destination); //converting to index for easier use

            Map<Integer, List<Edge>> adjList = createAdjList(); //calling createAdjList
            int[] distances = new int[routerIds.size()];
            int[] predecessor = new int[routerIds.size()];
            boolean[] visited = new boolean[routerIds.size()];

            Arrays.fill(distances, INF);
            Arrays.fill(predecessor, -1);
            distances[sourceIndex] = 0; //starting the source distance at 0

            for(int count = 0; count < routerIds.size(); count ++){
                int minIndex =-1;
                int minDist = INF;

                for(int i = 0; i < distances.length; i++){
                    if(!visited[i] && distances[i] < minDist){
                        minDist = distances[i]; //loop through distances length until the min distance is discovered and updated
                        minIndex = i; //setting the index of minimum distance to i
                    }
                }

                if(minIndex == -1 || minDist == INF){
                    break;
                } //minimum Index or distance was not found
                if(minIndex == destIndex){
                    break;
                }
                visited[minIndex] = true;

                for(Edge edge: adjList.get(minIndex)){
                    int neighbour = edge.getDestination(); //getting neighbour of minimum index
                    int weight = edge.getWeight();

                    if(!visited[neighbour]){
                        int newDist = distances[minIndex] + weight; //calculates the new distance of the neighbouring node
                        if(newDist < distances[neighbour]){
                            distances[neighbour] = newDist;
                            predecessor[neighbour] = minIndex; //setting new distance and updating fields if new shortest path is discovered
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
            for(Map.Entry<Integer, List<Edge>> entry : adjList.entrySet()){ //loop through the entries of List<Edge>
                int routerIndex = entry.getKey();
                int routerId = indexToRouter.get(routerIndex);

                System.out.print("Router " + routerId + " -> ");
                List<Edge> neighbours = entry.getValue();
                if(neighbours.isEmpty()){
                    System.out.println("No out going Links");
                } else {
                    for(int i = 0; i < neighbours.size(); i ++){
                        Edge edge = neighbours.get(i); //neighbours of position i and assigned object edge for destination and weight
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

        public void printRoutingTable(int routerId){
            System.out.println("OSPF Routing Table for router: " + routers.get(routerId).getRouterName());
            System.out.println("=================================================");
            System.out.printf("%-15s %-15s %-10s\n", "Destination", "Next Hop", "Metric");
            System.out.println("=================================================");
            Router router = routers.get(routerId);
            router.getRoutingTable();
        } //no use for routing table yet

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
        ospf.addLink(2, 4, "Fa0/2 -> Fa0/2", 6); //adding links between routers.
        // this should automatically create the routers if they don't exit so either use this or the add router function

        System.out.println("\n--- Adjacency List Representation ---");
        Map<Integer, List<Edge>> adjList = ospf.createAdjList();
        ospf.printAdjList(adjList);

        System.out.println("\n--- Path calculation ---");
        int cost = ospf.dijkstra(1, 5); //input source and destination node for dijkstra here

        if(cost == INF){
            System.out.println("There is no path from the source to the destination router");
        } else {
            System.out.println("The shortest path cost from these routers is: " + cost);
        }
        int[][] adjTable = ospf.createAdjTable(); //creates the adj matrix for view. comment these lines out if you just want to see the adjacency list
        ospf.printAdjMatrix(adjTable);

    }

}
