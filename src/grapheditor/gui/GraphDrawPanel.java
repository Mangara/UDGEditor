package grapheditor.gui;

import grapheditor.algos.UDGComputer;
import graphs.graph.Edge;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sander Verdonschot <sander.verdonschot at gmail.com>
 */
public class GraphDrawPanel extends BasicGraphDrawPanel {

    private double radius = 1;
    private Set<Edge> freeEdges;

    public GraphDrawPanel() {
        initialize();
    }

    private void updateGraph() {
        UDGComputer.buildUDG(graph, radius);
        computeFreeEdges();
        repaint();
        fireGraphChangedEvent();
    }


    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        updateGraph();
    }


    private void computeFreeEdges() {
        if (freeEdges == null) {
            freeEdges = new HashSet<Edge>();
        }

        freeEdges.clear();
        
        if (graph == null) {
            return;
        }

        freeEdges.addAll(graph.getEdges());
        int n = graph.getEdges().size();

        for (int i = 0; i < n; i++) {
            Edge e1 = graph.getEdges().get(i);

            for (int j = i+1; j < n; j++) {
                Edge e2 = graph.getEdges().get(j);

                if (e1.intersectsProperly(e2)) {
                    freeEdges.remove(e1);
                    freeEdges.remove(e2);
                }
            }
        }
    }

}
