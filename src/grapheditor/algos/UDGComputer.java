package grapheditor.algos;

import graphs.graph.Graph;
import graphs.graph.GraphVertex;

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
        
        for (GraphVertex v1 : g.getVertices()) {
            for (GraphVertex v2 : g.getVertices()) {
                if (distanceSquared(v1, v2) <= radiusSquared && !g.containsEdge(v1, v2)) {
                    g.addEdge(v1, v2);
                }
            }
        }
    }

    private static double distanceSquared(GraphVertex v1, GraphVertex v2) {
        double dx = v1.getX() - v2.getX();
        double dy = v1.getY() - v2.getY();
        return dx * dx + dy * dy;
    }
}
