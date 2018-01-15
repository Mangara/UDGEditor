/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package grapheditor.gui;

import graphs.graph.Edge;
import graphs.graph.GraphVertex;

/**
 *
 * @author s051182
 */
public interface SelectionListener {

    public void edgeSelected(GraphDrawPanel source, Edge edge);
    public void vertexSelected(GraphDrawPanel source, GraphVertex vertex);

}
