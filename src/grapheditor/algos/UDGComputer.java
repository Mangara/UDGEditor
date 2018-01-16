package grapheditor.algos;

import graphs.graph.Graph;
import graphs.graph.GraphVertex;
import java.util.List;

/**
 *
 * @author sverdons
 */
public class UDGComputer {
    public static void buildUDG(Graph g) {
        buildUDG(g, 1);
    }

    public static void buildUDG(Graph g, double radius) {
        double radiusSquared = radius * radius;
        g.clearEdges();
        
        List<GraphVertex> vertices = g.getVertices();
        
        for (int i = 0; i < vertices.size(); i++) {
            GraphVertex v1 = vertices.get(i);
            
            for (int j = i + 1; j < vertices.size(); j++) {
                GraphVertex v2 = vertices.get(j);
                
                if (distanceSquared(v1, v2) <= radiusSquared) {
                    g.addEdge(v1, v2);
                }
            }
        }
        
        /*for (GraphVertex v1 : g.getVertices()) {
            for (GraphVertex v2 : g.getVertices()) {
                if (distanceSquared(v1, v2) <= radiusSquared && !g.containsEdge(v1, v2)) {
                    g.addEdge(v1, v2);
                }
            }
        }*/
    }

    private static double distanceSquared(GraphVertex v1, GraphVertex v2) {
        double dx = v1.getX() - v2.getX();
        double dy = v1.getY() - v2.getY();
        return dx * dx + dy * dy;
    }
}
