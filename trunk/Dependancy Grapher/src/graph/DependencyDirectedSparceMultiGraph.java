/*
 * Created on Oct 17, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.AbstractSparseGraph;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;



@SuppressWarnings("serial")
public class DependencyDirectedSparceMultiGraph<V,E> 
    extends AbstractSparseGraph<V,E>
    implements DirectedGraph<V,E>, Serializable {

	public static <V,E> Factory<DirectedGraph<V,E>> getFactory() {
		return new Factory<DirectedGraph<V,E>> () {
			public DirectedGraph<V,E> create() {
				return new DependencyDirectedSparceMultiGraph<V,E>();
			}
		};
	}

/**
 * 
 * @author biggie
 *
 * @param <V>
 */
	class Edge<V>
	{
		private Attributes _attr;
		private Pair<V> _edge;
		
		// Constructors
		protected Edge(V fromVertex_, V toVertex_)
		{
			_edge = new Pair<V>(fromVertex_, toVertex_);
		}
		
		protected Edge(V fromVertex_, V toVertex_, Attributes attr_)
		{
			_edge = new Pair<V>(fromVertex_, toVertex_);
			_attr = attr_;
		}
		protected Edge(Pair<V> edge_, Attributes attr_)
		{
			_edge = edge_;
			_attr = attr_;
		}
		
		protected Pair<V> getNodes()
		{
			return _edge;
		}
		
		protected Attributes getAttributes()
		{
			return _attr;
		}
		
	    public boolean equals(Object o)
	    {
	      boolean compare = false;

	      if (o instanceof Edge)
	      {
	    	  Edge<V> edgeToCompare = (Edge<V>)o;
	        if (!equals(edgeToCompare))
	        {
	          compare = _edge.equals(edgeToCompare.getNodes());
	        }
	      }
	      return compare;
		
	
	    }
	}
	
	
	protected Map<V, Pair<Set<E>>> vertices; // Map of vertices to Pair of adjacency sets {incoming, outgoing}
    protected Map<E, Edge<V>> edges;            // Map of edges to incident vertex pairs

    public DependencyDirectedSparceMultiGraph() {
        vertices = new HashMap<V, Pair<Set<E>>>();
        edges = new HashMap<E, Edge<V>>();
    }
    
    public Collection<E> getEdges() {
        return Collections.unmodifiableCollection(edges.keySet());
    }

    public Collection<V> getVertices() {
        return Collections.unmodifiableCollection(vertices.keySet());
    }

    public boolean containsVertex(V vertex) {
    	return vertices.keySet().contains(vertex);
    }
    
    public boolean containsEdge(E edge) {
    	return edges.keySet().contains(edge);
    }

    protected Collection<E> getIncoming_internal(V vertex)
    {
        return vertices.get(vertex).getFirst();
    }
    
    protected Collection<E> getOutgoing_internal(V vertex)
    {
        return vertices.get(vertex).getSecond();
    }
    
    public boolean addVertex(V vertex) {
    	if(vertex == null) {
    		throw new IllegalArgumentException("vertex may not be null");
    	}
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, new Pair<Set<E>>(new HashSet<E>(), new HashSet<E>()));
            return true;
        } else {
            return false;
        }
    }

    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;
        
        // copy to avoid concurrent modification in removeEdge
        Set<E> incident = new HashSet<E>(getIncoming_internal(vertex));
        incident.addAll(getOutgoing_internal(vertex));
        
        for (E edge : incident)
            removeEdge(edge);
        
        vertices.remove(vertex);
        
        return true;
    }
    
    public boolean removeEdge(E edge) {
        if (!containsEdge(edge))
            return false;
        
        Pair<V> endpoints = this.getEndpoints(edge);
        V source = endpoints.getFirst();
        V dest = endpoints.getSecond();
        
        // remove edge from incident vertices' adjacency sets
        getOutgoing_internal(source).remove(edge);
        getIncoming_internal(dest).remove(edge);
        
        edges.remove(edge);
        return true;
    }

    
    public Collection<E> getInEdges(V vertex) {
        return Collections.unmodifiableCollection(getIncoming_internal(vertex));
    }

    public Collection<E> getOutEdges(V vertex) {
        return Collections.unmodifiableCollection(getOutgoing_internal(vertex));
    }

    public Collection<V> getPredecessors(V vertex) {
        Set<V> preds = new HashSet<V>();
        for (E edge : getIncoming_internal(vertex))
            preds.add(this.getSource(edge));
        
        return Collections.unmodifiableCollection(preds);
    }

    public Collection<V> getSuccessors(V vertex) {
        Set<V> succs = new HashSet<V>();
        for (E edge : getOutgoing_internal(vertex))
            succs.add(this.getDest(edge));
        
        return Collections.unmodifiableCollection(succs);
    }

    public Collection<V> getNeighbors(V vertex) {
        Collection<V> neighbors = new HashSet<V>();
        for (E edge : getIncoming_internal(vertex))
            neighbors.add(this.getSource(edge));
        for (E edge : getOutgoing_internal(vertex))
            neighbors.add(this.getDest(edge));
        return Collections.unmodifiableCollection(neighbors);
    }

    public Collection<E> getIncidentEdges(V vertex) {
        Collection<E> incident = new HashSet<E>();
        incident.addAll(getIncoming_internal(vertex));
        incident.addAll(getOutgoing_internal(vertex));
        return incident;
    }

    public E findEdge(V v1, V v2) {
        for (E edge : getOutgoing_internal(v1))
            if (this.getDest(edge).equals(v2))
                return edge;
        
        return null;
    }
    
	@Override
	public boolean addEdge(E edge, Pair<? extends V> endpoints) {
		return addEdge(edge, endpoints, EdgeType.DIRECTED, null);
	}

	@Override
	public boolean addEdge(E edge, Pair<? extends V> endpoints,
			EdgeType edgeType) {
		return addEdge(edge, endpoints, EdgeType.DIRECTED, null);
	}   
    
    public boolean addEdge(E edge, V source, V dest, Attributes attr_) {
        return addEdge(edge, source, dest, EdgeType.DIRECTED, attr_);
    }
    
    public boolean addEdge(E edge, V source, V dest) {
        return addEdge(edge, source, dest, EdgeType.DIRECTED);
    }

    /**
     * Adds <code>edge</code> to the graph.  Also adds 
     * <code>source</code> and <code>dest</code> to the graph if they
     * are not already present.  Returns <code>false</code> if 
     * the specified edge (with the specified source and destination) are already
     * in the graph.
     */
    public boolean addEdge(E edge, V source, V dest, EdgeType edgeType) {
    	return addEdge(edge, new Pair<V>(source, dest), edgeType, null);
    }
    public boolean addEdge(E edge, V source, V dest, EdgeType edgeType, Attributes attr) {
    	return addEdge(edge, new Pair<V>(source, dest), edgeType, attr);
    }
	public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType, Attributes attr) {
    	if(edgeType != EdgeType.DIRECTED) 
            throw new IllegalArgumentException("This graph does not accept edges of type " + edgeType);
    	return addEdge(edge, endpoints, attr);
	}

	public boolean addEdge(E edge, Pair<? extends V> endpoints, Attributes attr) {
        Pair<V> new_endpoints = getValidatedEndpoints(edge, endpoints);
        if (new_endpoints == null)
            return false;
        
        edges.put(edge, new Edge<V>(new_endpoints, attr));
        
        V source = new_endpoints.getFirst();
        V dest = new_endpoints.getSecond();

        if (!vertices.containsKey(source))
            this.addVertex(source);
        
        if (!vertices.containsKey(dest))
            this.addVertex(dest);
        
        getIncoming_internal(dest).add(edge);
        getOutgoing_internal(source).add(edge);

        return true;
	}

    
    public V getSource(E edge) {
        return this.getEndpoints(edge).getFirst();
    }

    public V getDest(E edge) {
        return this.getEndpoints(edge).getSecond();
    }

    public boolean isSource(V vertex, E edge) {
        return vertex.equals(this.getEndpoints(edge).getFirst());
    }

    public boolean isDest(V vertex, E edge) {
        return vertex.equals(this.getEndpoints(edge).getSecond());
    }

    public Pair<V> getEndpoints(E edge) {
        return edges.get(edge).getNodes();
    }
    
    public Attributes getEdgeAttributes(E edge)
    {
    	return edges.get(edge).getAttributes();
    }
    public EdgeType getEdgeType(E edge) {
        if (containsEdge(edge))
            return EdgeType.DIRECTED;
        else 
            return null;
    }

	public Collection<E> getEdges(EdgeType edgeType) {
        if (edgeType == EdgeType.DIRECTED)
            return getEdges();
        else
            return null;
	}

	public int getEdgeCount() {
		return edges.size();
	}

	public int getVertexCount() {
		return vertices.size();
	}
	
}
