/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grapheditor.algos;

import graphs.graph.Edge;
import graphs.graph.Graph;
import graphs.graph.GraphVertex;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sander
 */
public class IntersectionGraphComputer {

    public static Graph computeIntersectionGraph(List<GraphVertex> points) {
        Map<Edge, GraphVertex> diagonals = createVertices(points);
        Graph qg = createEdges(diagonals);
        //drawGraph(qg);
        return qg;
    }

    private static Map<Edge, GraphVertex> createVertices(List<GraphVertex> points) {
        Map<Edge, GraphVertex> vertices = new HashMap<Edge, GraphVertex>(2 * points.size() * points.size());
        int n = 0;

        // Add a vertex for every diagonal
        for (int i = 0; i < points.size(); i++) {
            GraphVertex u = points.get(i);

            for (int j = i + 1; j < points.size(); j++) {
                GraphVertex v = points.get(j);

                vertices.put(new Edge(u, v), new GraphVertex(n, getDistance(u, v) * points.size() * points.size() / 4));
                n++;
            }
        }

        return vertices;
    }

    private static double getDistance(GraphVertex v1, GraphVertex v2) {
        double dx = v1.getX() - v2.getX();
        double dy = v1.getY() - v2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static Graph createEdges(Map<Edge, GraphVertex> diagonals) {
        Graph quadrilateralGraph = new Graph();

        // Add all vertices
        for (GraphVertex labelledVertex : diagonals.values()) {
            quadrilateralGraph.addVertex(labelledVertex);
        }

        // Check every pair of diagonals
        List<Edge> segments = new ArrayList<Edge>(diagonals.keySet());

        for (int i = 0; i < segments.size(); i++) {
            Edge d1 = segments.get(i);

            for (int j = i + 1; j < segments.size(); j++) {
                Edge d2 = segments.get(j);

                if (d1.intersectsProperly(d2)) {
                    addEdge(quadrilateralGraph, diagonals, d1, d2);
                }
            }
        }

        return quadrilateralGraph;
    }

    private static void addEdge(Graph quadrilateralGraph, Map<Edge, GraphVertex> diagonals, Edge d1, Edge d2) {
        // Figure out which of d1 or d2 is longer
        double length1 = diagonals.get(d1).getY();
        double length2 = diagonals.get(d2).getY();

        if (length1 < length2) {
            quadrilateralGraph.addEdge(diagonals.get(d1), diagonals.get(d2), true);
        } else {
            quadrilateralGraph.addEdge(diagonals.get(d2), diagonals.get(d1), true);
        }
    }

    private static void drawGraph(Graph qg) {
        // Gather connected components
        List<Set<GraphVertex>> components = getConnectedComponents(qg);

        // Classify components
        List<GraphVertex> isolated = new ArrayList<GraphVertex>();
        List<Set<GraphVertex>> paths = new ArrayList<Set<GraphVertex>>();
        List<Set<GraphVertex>> other = new ArrayList<Set<GraphVertex>>();

        // Keep track of height
        int height = 0;

        for (Set<GraphVertex> c : components) {
            if (c.size() == 1) {
                isolated.add(c.iterator().next());
            } else if (isPath(c)) {
                paths.add(c);
                height++;
            } else {
                other.add(c);
                height++;
            }
        }

        // Draw isolated vertices
        for (int i = 0; i < isolated.size(); i++) {
            isolated.get(i).setX(i * height / (double) isolated.size());
            isolated.get(i).setY(0);
        }

        // Draw paths
        for (int i = 0; i < paths.size(); i++) {
            Set<GraphVertex> path = paths.get(i);

            // Find a leaf
            GraphVertex prev = null;
            GraphVertex current = null;

            for (GraphVertex v : path) {
                if (v.getDegree() == 1) {
                    current = v;
                    break;
                }
            }

            int x = 0;

            current.setX(x);
            current.setY(i + 1);

            do {
                List<GraphVertex> nv = current.getNeighbours();

                if (prev == null) {
                    prev = current;
                    current = nv.get(0);
                } else if (prev == nv.get(0)) {
                    prev = current;
                    current = nv.get(1);
                } else {
                    prev = current;
                    current = nv.get(0);
                }

                x++;
                current.setX(x);
                current.setY(i + 1);
            } while (current.getDegree() > 1);
        }

        // Move other components up
        for (int i = 0; i < other.size(); i++) {
            Set<GraphVertex> component = other.get(i);

            for (GraphVertex v : component) {
                v.setX(v.getX() * height);
                v.setY(v.getY() + paths.size() + 1.5 * i + 1);
            }

            //drawComponent(component);
        }
    }

    private static List<Set<GraphVertex>> getConnectedComponents(Graph qg) {
        // Gather connected components
        List<Set<GraphVertex>> components = new ArrayList<Set<GraphVertex>>();
        Set<GraphVertex> visited = new HashSet<GraphVertex>(qg.getVertices().size() * 2);

        // Run a DFS
        for (GraphVertex root : qg.getVertices()) {
            if (!visited.contains(root)) {
                // We found a new component
                Set<GraphVertex> currentComponent = new HashSet<GraphVertex>();
                LinkedList<GraphVertex> open = new LinkedList<GraphVertex>();

                // Handle the root
                open.push(root);
                visited.add(root);
                currentComponent.add(root);

                while (!open.isEmpty()) {
                    GraphVertex v = open.pop();

                    // Explore all neighbours
                    for (GraphVertex nv : v.getNeighbours()) {
                        if (!visited.contains(nv)) {
                            open.push(nv);
                            visited.add(nv);
                            currentComponent.add(nv);
                        }
                    }
                }

                components.add(currentComponent);
            }
        }

        return components;
    }

    private static boolean isPath(Set<GraphVertex> component) {
        int nLeaves = 0;

        for (GraphVertex v : component) {
            if (v.getDegree() > 2) {
                return false;
            } else if (v.getDegree() == 1) {
                nLeaves++;

                if (nLeaves > 2) {
                    return false;
                }
            }
        }

        return nLeaves == 2;
    }
}
