
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.nio.graph6.Graph6Sparse6Exporter;
import org.jgrapht.util.SupplierUtil;
import org .jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;


import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class GeneratorbyNumberofGraphs {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Koľko vnútorných vrcholov chcete? (k): ");
        int k = scanner.nextInt();
        System.out.print("Koľko rôznych grafov chcete vygenerovať? (konštanta): ");
        int konstanta = scanner.nextInt();
        System.out.print("Ako chcete nazvať súbor kde uložíme grafy? : ");
        String nazovSuboru = scanner.next();
        generateGraphs(k, konstanta, nazovSuboru);
        scanner.close();
    }
    public static void generateGraphs(int k,int konstanta, String nazovSuboru){
        int x = k+1;
        int y = k+1;
        Set<Graph> mnozina = new HashSet<>();
        boolean prvy =true;
        while(mnozina.size()<konstanta){
            // Vytvorenie grafu
            Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(SupplierUtil.createStringSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
            createCornerVertices(graph,x,y);
            // Generovanie vnútorných vrcholov
            Set<String> internalVertices = generateInternalVertices(k, x, y);
            // Zoradenie vrcholov podľa x  y súradníc
            List<String> sortedVertices = sortVerticesByCoordinates(internalVertices);
            for (String vertex : sortedVertices) {
                graph.addVertex(vertex);
//                System.out.println(vertex);
            }
            Map<String,Integer> mapa = new HashMap<String,Integer>();
            // generovanie hrán pre každý vrchol
            for (String vertex : sortedVertices) {
                generateEdges(graph, vertex, x, y, new HashSet<>(), mapa);
            }
            if (!allInternalVerticesHaveDegree3(graph, internalVertices)) {
                continue;
            }
            connectEdgeVertices(graph,x,y);
            if(prvy){
                prvy=false;
                mnozina.add(graph);
                System.out.print("\rPočet grafov v množine: " + mnozina.size());
                System.out.flush();
            }
            boolean izomorfny=false;
            for(Graph g:mnozina){
                VF2GraphIsomorphismInspector<String, DefaultEdge> inspector = new VF2GraphIsomorphismInspector<>(g, graph);
                if(inspector.isomorphismExists()){
                    izomorfny=true;
                    break;
                }
            }
            if(!izomorfny){
                mnozina.add(graph);
                System.out.print("\rPočet grafov v množine: " + mnozina.size());
                System.out.flush();
            }
        }
        try (FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/" + nazovSuboru + ".txt")) {
            for (Graph<String, DefaultEdge> g : mnozina) {
                Graph6Sparse6Exporter<String, DefaultEdge> exporter = new Graph6Sparse6Exporter<>();
                StringWriter writer = new StringWriter();
                exporter.exportGraph(g, writer);
                String graph6String = writer.toString();
                fileWriter.write(graph6String + System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void createCornerVertices(Graph<String, DefaultEdge> graph, int x, int y) {
        String bottomLeft = "(0,0)";
        String bottomRight = "(" + x + ",0)";
        String topLeft = "(0," + y + ")";
        String topRight = "(" + x + "," + y + ")";
        graph.addVertex(bottomLeft);
        graph.addVertex(bottomRight);
        graph.addVertex(topLeft);
        graph.addVertex(topRight);
    }

    private static Set<String> generateInternalVertices(int k, int x, int y) {
        Random random = new Random();
        Set<String> internalVertices = new HashSet<>();
        while (internalVertices.size() < k) {
            int xi = random.nextInt(x - 1) + 1;
            int yi = random.nextInt(y - 1) + 1;
            String vertex = "(" + xi + "," + yi + ")";
            internalVertices.add(vertex);
        }
        return internalVertices;
    }

    private static List<String> sortVerticesByCoordinates(Set<String> vertices) {
        List<String> sortedList = new ArrayList<>(vertices);
        sortedList.sort((v1, v2) -> {
            int x1 = Integer.parseInt(v1.split(",")[0].replace("(", ""));
            int y1 = Integer.parseInt(v1.split(",")[1].replace(")", ""));
            int x2 = Integer.parseInt(v2.split(",")[0].replace("(", ""));
            int y2 = Integer.parseInt(v2.split(",")[1].replace(")", ""));

            if (x1 == x2) {
                return Integer.compare(y1, y2);
            } else {
                return Integer.compare(x1, x2);
            }
        });
        return sortedList;
    }

    private static String findNeighbor(Graph<String, DefaultEdge> graph, String vertex, String direction, int maxX, int maxY) {
        switch (direction) {
            case "left":
                return findLeftNeighbor(graph, vertex, maxX, maxY);
            case "right":
                return findRightNeighbor(graph, vertex, maxX, maxY);
            case "up":
                return findTopNeighbor(graph, vertex, maxX, maxY);
            case "down":
                return findBottomNeighbor(graph, vertex, maxX, maxY);
            default:
                return null;
        }
    }

    private static void generateEdges(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY, Set<String> visited, Map<String, Integer> edgeCounts) {
        visited.add(vertex);
        int currentEdges = edgeCounts.getOrDefault(vertex,0);
        if (currentEdges >= 3) {
            return;
        }
        // Zoznam možných smerov
        List<String> directions = Arrays.asList("left", "right", "up", "down");
        Collections.shuffle(directions);
        for (String direction : directions) {
            String neighbor = findNeighbor(graph, vertex, direction, maxX, maxY);
            if (neighbor != null) {
                if (!graph.containsEdge(vertex, neighbor)) {
                    int neighborEdges = edgeCounts.getOrDefault(neighbor, 0);
                    if (currentEdges < 3 && neighborEdges < 3) {
                        // Ak sused ešte neexistuje v grafe, pridáme ho
                        if (!graph.containsVertex(neighbor)) {
                            graph.addVertex(neighbor);
                        }
                        graph.addEdge(vertex, neighbor);
                        currentEdges++;
                        edgeCounts.put(vertex, currentEdges+1);
                        edgeCounts.put(neighbor, neighborEdges + 1);
                        if (currentEdges >= 3) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private static String findLeftNeighbor(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        String[] coords = vertex.replace("(", "").replace(")", "").split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        // Vľavo: hľadáme najbližšieho suseda s rovnakým y a menším x
        for (int i = x - 1; i >= 0; i--) {
            String leftNeighbor = "(" + i + "," + y + ")";
            if (graph.containsVertex(leftNeighbor)) {
                return leftNeighbor;
            } else if (i == 0) {
                return "(" + 0 + "," + y + ")"; // Potenciálny vrchol na okraji
            }
        }
        return null; // Ak sa nenájde žiadny sused
    }
    private static String findRightNeighbor(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        String[] coords = vertex.replace("(", "").replace(")", "").split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        // Vpravo: hľadáme najbližšieho suseda s rovnakým y a väčším x
        for (int i = x + 1; i <= maxX; i++) {
            String rightNeighbor = "(" + i + "," + y + ")";
            if (graph.containsVertex(rightNeighbor)) {
                return rightNeighbor;
            } else if (i == maxX) {
                return "(" + maxX + "," + y + ")"; // Potenciálny vrchol na okraji
            }
        }

        return null; // Ak sa nenájde žiadny sused
    }
    private static String findTopNeighbor(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        String[] coords = vertex.replace("(", "").replace(")", "").split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        // Hore: hľadáme najbližšieho suseda s rovnakým x a väčším y
        for (int i = y + 1; i <= maxY; i++) {
            String topNeighbor = "(" + x + "," + i + ")";
            if (graph.containsVertex(topNeighbor)) {
                return topNeighbor;
            } else if (i == maxY) {
                return "(" + x + "," + maxY + ")"; // Potenciálny vrchol na okraji
            }
        }
        return null; // Ak sa nenájde žiadny sused
    }
    private static String findBottomNeighbor(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        String[] coords = vertex.replace("(", "").replace(")", "").split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        // Dole: hľadáme najbližšieho suseda s rovnakým x a menším y
        for (int i = y - 1; i >= 0; i--) {
            String bottomNeighbor = "(" + x + "," + i + ")";
            if (graph.containsVertex(bottomNeighbor)) {
                return bottomNeighbor;
            } else if (i == 0) {
                return "(" + x + "," + 0 + ")"; // Potenciálny vrchol na okraji
            }
        }
        return null; // Ak sa nenájde žiadny sused
    }
    private static Map<String, String> mapNeighbors(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        Map<String, String> neighborMap = new HashMap<>();

        String leftNeighbor = findLeftNeighbor(graph, vertex, maxX, maxY);
        if (leftNeighbor != null) {
            neighborMap.put("left", leftNeighbor);
        }

        String rightNeighbor = findRightNeighbor(graph, vertex, maxX, maxY);
        if (rightNeighbor != null) {
            neighborMap.put("right", rightNeighbor);
        }

        String topNeighbor = findTopNeighbor(graph, vertex, maxX, maxY);
        if (topNeighbor != null) {
            neighborMap.put("up", topNeighbor);
        }

        String bottomNeighbor = findBottomNeighbor(graph, vertex, maxX, maxY);
        if (bottomNeighbor != null) {
            neighborMap.put("down", bottomNeighbor);
        }
        return neighborMap;
    }

    private static void connectEdgeVertices(Graph<String, DefaultEdge> graph, int maxX, int maxY) {
        Set<String> edgeVertices = new HashSet<>(graph.vertexSet());
        for (String vertex : edgeVertices) {
            String[] coords = vertex.replace("(", "").replace(")", "").split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            if ((x == 0 || x == maxX) && (y == 0 || y == maxY)) {
                connectCornerVertex(graph, vertex, maxX, maxY);
            }

            else if (x == 0 || x == maxX) {
                connectVerticalEdge(graph, vertex, maxX, maxY);
            }

            else if (y == 0 || y == maxY) {
                connectHorizontalEdge(graph, vertex, maxX, maxY);
            }
        }
    }

    private static void connectCornerVertex(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        Map<String, String> neighbors = mapNeighbors(graph, vertex, maxX, maxY);
        for (String direction : Arrays.asList("left", "right", "up", "down")) {
            if (neighbors.containsKey(direction)) {
                if(!graph.containsEdge(neighbors.get(direction), vertex)) {
                    graph.addEdge(vertex, neighbors.get(direction));
//                    System.out.println("Connected " + vertex + " to " + neighbors.get(direction) + " at corner.");
                }

            }
        }
    }

    private static void connectVerticalEdge(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        Map<String, String> neighbors = mapNeighbors(graph, vertex, maxX, maxY);
        for (String direction : Arrays.asList("up", "down")) {
            if (neighbors.containsKey(direction)) {
                if(!graph.containsEdge(neighbors.get(direction), vertex)) {
                    graph.addEdge(vertex, neighbors.get(direction));
                }
            }
        }
    }

    private static void connectHorizontalEdge(Graph<String, DefaultEdge> graph, String vertex, int maxX, int maxY) {
        Map<String, String> neighbors = mapNeighbors(graph, vertex, maxX, maxY);
        for (String direction : Arrays.asList("left", "right")) {
            if (neighbors.containsKey(direction)) {
                if(!graph.containsEdge(neighbors.get(direction), vertex)) {
                    graph.addEdge(vertex, neighbors.get(direction));

                }
            }
        }
    }
    private static boolean allInternalVerticesHaveDegree3(Graph<String, DefaultEdge> graph, Set<String> internalVertices) {
        for (String vertex : internalVertices) {
            if (graph.degreeOf(vertex) < 3) {
                return false;
            }
        }
        return true;
    }
}