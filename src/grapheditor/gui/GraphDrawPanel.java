package grapheditor.gui;

import grapheditor.algos.UDGComputer;
import graphs.graph.Edge;
import graphs.graph.Graph;
import graphs.graph.GraphVertex;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GraphDrawPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    
    protected final double HIT_PRECISION = 7; // How close you must click to a vertex or edge in order to select it. Higher values mean you can be further away. Note that this makes it harder to select the right vertex when several are very close.
    protected final int VERTEX_SIZE = 5; // Radius in pixels of the vertices
    protected Graph graph; // The current graph
    protected GraphVertex selectedVertex = null; // The currently selected vertex
    protected Edge selectedEdge = null; // The currently selected edge. Invariant: (selectedVertex == null) || (selectedEdge == null), meaning that you can't select both a vertex and and edge.
    protected double zoomfactor = 0.01;
    protected int panX = 0;
    protected int panY = 0;
    protected int mouseX = 0;
    protected int mouseY = 0;
    protected Collection<SelectionListener> listeners;
    protected Collection<ChangeListener> graphChangeListeners;
    private double radius = 1;
    private Set<Edge> freeEdges;
    private boolean highlightFreeEdges = true;

    public GraphDrawPanel() {
        initialize();
    }

    protected void initialize() {
        setFocusable(true);
        setOpaque(true);
        setBackground(Color.white);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        graph = new Graph();
        listeners = new ArrayList<>();
        graphChangeListeners = new ArrayList<>();
    }

    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    public void addGraphChangeListener(ChangeListener listener) {
        graphChangeListeners.add(listener);
    }

    public void removeGraphChangeListener(ChangeListener listener) {
        graphChangeListeners.remove(listener);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        setSelectedVertex(null);
        zoomToGraph();
        updateGraph();
    }
    
    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        updateGraph();
    }

    public boolean isHighlightFreeEdges() {
        return highlightFreeEdges;
    }

    public void setHighlightFreeEdges(boolean highlightFreeEdges) {
        this.highlightFreeEdges = highlightFreeEdges;
        repaint();
    }
    
    private void updateGraph() {
        UDGComputer.buildUDG(graph, radius);
        computeFreeEdges();
        repaint();
        fireGraphChangedEvent();
    }

    protected void fireGraphChangedEvent() {
        for (ChangeListener changeListener : graphChangeListeners) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    public Edge getSelectedEdge() {
        return selectedEdge;
    }

    public GraphVertex getSelectedVertex() {
        return selectedVertex;
    }

    public void zoomToGraph() {
        if (!graph.getVertices().isEmpty()) {
            int margin = 20;
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (GraphVertex vertex : graph.getVertices()) {
                minX = Math.min(minX, vertex.getX());
                minY = Math.min(minY, vertex.getY());
                maxX = Math.max(maxX, vertex.getX());
                maxY = Math.max(maxY, vertex.getY());
            }
            double zoomfactorX = (maxX - minX) / (getWidth() - 2 * margin);
            double zoomfactorY = (maxY - minY) / (getHeight() - 2 * margin);
            if (zoomfactorY > zoomfactorX) {
                zoomfactor = zoomfactorY;
                panX = (int) Math.round((maxX + minX) / (2 * zoomfactor)) - getWidth() / 2;
                panY = (int) Math.round(maxY / zoomfactor) - getHeight() + margin;
            } else {
                zoomfactor = zoomfactorX;
                panX = (int) Math.round(minX / zoomfactor) - margin;
                panY = (int) Math.round((maxY + minY) / (2 * zoomfactor)) - getHeight() / 2;
            }
        }
        repaint();
    }

    protected double xScreenToWorld(int x) {
        return (x + panX) * zoomfactor;
    }

    protected double yScreenToWorld(int y) {
        return (getHeight() - y + panY) * zoomfactor;
    }

    protected int xWorldToScreen(double x) {
        return (int) Math.round((x / zoomfactor) - panX);
    }

    protected int yWorldToScreen(double y) {
        return getHeight() - (int) Math.round((y / zoomfactor) - panY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (Edge e : graph.getEdges()) {
            if (e.isVisible()) {
                if (e == selectedEdge) {
                    g.setColor(Color.RED);
                } else if (freeEdges.contains(e) && highlightFreeEdges) {
                    g.setColor(new Color(255, 146, 0));
                } else {
                    g.setColor(Color.BLACK);
                }
                GraphVertex vA = e.getVA();
                GraphVertex vB = e.getVB();
                g.drawLine(xWorldToScreen(vA.getX()), yWorldToScreen(vA.getY()), xWorldToScreen(vB.getX()), yWorldToScreen(vB.getY()));
            }
        }
        
        for (GraphVertex v : graph.getVertices()) {
            if (v.isVisible()) {
                g.setColor(Color.blue);
                g.fillOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
                if (v == selectedVertex) {
                    g.setColor(Color.RED);
                    int circleDiameter = (int) Math.round(2 * radius / zoomfactor);
                    g.drawOval(xWorldToScreen(v.getX() - radius), yWorldToScreen(v.getY() + radius), circleDiameter, circleDiameter);
                    ((Graphics2D) g).setStroke(new BasicStroke(2));
                } else {
                    g.setColor(Color.BLACK);
                    ((Graphics2D) g).setStroke(new BasicStroke());
                }
                g.drawOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            double wX = xScreenToWorld(e.getX());
            double wY = yScreenToWorld(e.getY());
            GraphVertex v = graph.getVertexAt(wX, wY, zoomfactor * HIT_PRECISION);
            if (v == null) {
                // Check if we selected an edge
                Edge edge = graph.getEdgeAt(wX, wY, zoomfactor * HIT_PRECISION);
                if (edge == null) {
                    GraphVertex newVertex = new GraphVertex(wX, wY);
                    graph.addVertex(newVertex);
                    updateGraph();
                    setSelectedVertex(newVertex);
                } else {
                    setSelectedEdge(edge);
                }
            } else {
                setSelectedVertex(v);
            }
            repaint();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // start panning, store the current mouse position
            mouseX = e.getX();
            mouseY = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
            // pan
            panX += mouseX - e.getX();
            panY += e.getY() - mouseY;
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        } else if (selectedVertex != null) {
            selectedVertex.setX(xScreenToWorld(e.getX()));
            selectedVertex.setY(yScreenToWorld(e.getY()));
            updateGraph();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double factor = (e.getWheelRotation() < 0 ? 10.0 / 11.0 : 11.0 / 10.0);
        zoomfactor *= factor;
        int centerX = e.getX();
        int centerY = getHeight() - e.getY();
        panX = (int) Math.round((centerX + panX) / factor - centerX);
        panY = (int) Math.round((centerY + panY) / factor - centerY);
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (selectedVertex != null) {
                graph.removeVertex(selectedVertex);
                updateGraph();
                deselectVertex();
            }
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            zoomToGraph();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    protected void setSelectedVertex(GraphVertex v) {
        deselectEdge();
        if (v != selectedVertex) {
            selectedVertex = v;
            for (SelectionListener list : listeners) {
                list.vertexSelected(this, v);
            }
            requestFocus();
        }
    }

    protected void setSelectedEdge(Edge e) {
        deselectVertex();
        if (e != selectedEdge) {
            selectedEdge = e;
            for (SelectionListener list : listeners) {
                list.edgeSelected(this, e);
            }
            requestFocus();
        }
    }

    protected void deselectVertex() {
        // Deselect the current selected vertex
        if (selectedVertex != null) {
            selectedVertex = null;
            for (SelectionListener list : listeners) {
                list.edgeSelected(this, null);
            }
        }
    }

    protected void deselectEdge() {
        // Deselect the current selected edge
        if (selectedEdge != null) {
            selectedEdge = null;
            for (SelectionListener list : listeners) {
                list.edgeSelected(this, null);
            }
        }
    }
    
    private void computeFreeEdges() {
        if (freeEdges == null) {
            freeEdges = new HashSet<>();
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
