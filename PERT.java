/* Starter code for PERT algorithm (Project 4)
 * @author rbk
 */

// change to your netid
package ixs190023;

// replace ixs190023 with your netid below
import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

import ixs190023.Graph.Edge;
import ixs190023.Graph.Factory;
import ixs190023.Graph.GraphAlgorithm;
import ixs190023.Graph.Vertex;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
	LinkedList<Vertex> finishList;
	public static final int INF = Integer.MAX_VALUE;

	// class to store
	public static class PERTVertex implements Factory {
		// Add fields to represent attributes of vertices here
		int duration;
		int ES, EF, LS, LF, slack;

		public PERTVertex(Vertex u) {
			// Initialize all fields
			this.duration = 0;
			this.ES = this.EF = this.LS = this.LF = this.slack = 0;

		}

		public PERTVertex make(Vertex u) {
			return new PERTVertex(u);
		}
	}

	// Constructor for PERT is private. Create PERT instances with static method
	// pert().
	private PERT(Graph g) {
		super(g, new PERTVertex(null));
	}

	public void setDuration(Vertex u, int d) {
		get(u).duration = d;
	}

	// Implement the PERT algorithm. Returns false if the graph g is not a DAG.
	public boolean pert() {
		if (g.size() == 0) {
			System.out.println("Graph is empty. Trivially a DAG.");
			return true;
		}

		// Order tasks using topological order
		finishList = topologicalOrder();
		// If the graph is not a DAG, return false
		if (finishList == null) {
			System.out.println("Cycle detected. Graph is not a DAG.");
			return false;
		}

		// otherwise we can pipe values to determine ES, EF, LS, LF, slack
		for (Vertex u : finishList) {
			// access the attributes of vertex
			PERTVertex uVertex = get(u);
			// if no predecessors, ES = 0
			if (!g.inEdges(u).iterator().hasNext()) {
				uVertex.ES = 0;
			}

			for (Edge e : g.inEdges(u)) {
				Vertex v = e.fromVertex();
				// Slide 35. DFS S(v) = max{EF(u)} where (u,v) in Graph
				uVertex.ES = Math.max(uVertex.ES, get(v).EF);
			}
			uVertex.EF = uVertex.ES + uVertex.duration;
		}

		// Backwards for latest start and finish times
		LinkedList<Vertex> reverseFinishList = new LinkedList<>(finishList);
		while (!reverseFinishList.isEmpty()) {
			Vertex u = reverseFinishList.pollLast();
			PERTVertex uVertex = get(u);

			// If no successors, LF = EF
			if(!g.outEdges(u).iterator().hasNext()) {
				uVertex.LF = uVertex.EF;
			} else {
				// Set default value to MAX
				uVertex.LF = Integer.MAX_VALUE;
				// iterate through the out edges of the vertex
				for (Edge e : g.outEdges(u)) {
					Vertex v = e.toVertex();
					// Slide 35. DFS S(v) = min{LS(u)} where (u,v) in Graph
					uVertex.LF = Math.min(uVertex.LF, get(v).LS);
				}
			}
			uVertex.LS = uVertex.LF - uVertex.duration; // LS = LF - duration
		}

		// Determine slack for u: Slide31: SL(u) : LF(u) - EF(u)
		for (Vertex u : finishList) {
			PERTVertex uVertex = get(u);
			uVertex.slack = uVertex.LF - uVertex.EF; // Slack = LF - EF
		}

		return true;
	}


	// Find a topological order of g using DFS
	LinkedList<Vertex> topologicalOrder() {
		// Init list to store the topological order
		finishList = new LinkedList<>();
		boolean[] explored = new boolean[g.size()];
		boolean[] onStack = new boolean[g.size()];

		// DFS on graph to determine if it is acyclic
		for (Vertex u : g) {
			if (!explored[u.getIndex()]) {
				if (!dfs(u, explored, onStack)) {
					finishList.clear(); // cycle detected clear
					return null;
				}
			}
		}
		return finishList;
	}

	// DFS to explore vertices and determine if acyclic
	// stack is evidence in directed path
	private boolean dfs(Vertex u, boolean[] explored, boolean[] onStack) {
		// Mark the current vertex as explored and on the stack
		explored[u.getIndex()] = true;
		onStack[u.getIndex()] = true;
		// Iterate through the out edges of the vertex
		for (Edge e : g.outEdges(u)) {
			Vertex v = e.toVertex();
			if (!explored[v.getIndex()]) {
				if (!dfs(v, explored, onStack)) {
					return false;
				}
			} else if (onStack[v.getIndex()]) {
				// back edge detected in the graph (Absence of back-edge implies acyclic)
				return false;
			}
		}

		// remove vertex from recursion stack and add vertex to the finishList
		onStack[u.getIndex()] = false;
		finishList.addFirst(u);
		return true;
	}

	/*
	 * Create a PERT instance on g, runs the algorithm.
	 * Returns PERT instance if successful. Returns null if G is not a DAG.
	 */
	public static PERT pert(Graph g, int[] duration) {
		PERT p = new PERT(g);
		for (Vertex u : g) {
			p.setDuration(u, duration[u.getIndex()]);
		}
		// Run PERT algorithm. Returns false if g is not a DAG
		return p.pert() ? p : null;
	}

	// The following methods are called after calling pert().

	// Earliest time at which task u can be completed
	public int ec(Vertex u) {
		return get(u).ES;
	}

	// Latest completion time of u
	public int lc(Vertex u) {
		return get(u).LF;
	}

	// Slack of u Slack for u the maximum time that the task can be delayed without
	// delaying the project
	public int slack(Vertex u) {
		return get(u).slack;
	}

	/*
	 * Length of a critical path (time taken to complete project)
	 * SL(u) : LF(u) - EF(u);
	 * SL(u) = 0 for critical vertices
	 */
	public int criticalPath() {
		int maxEF = 0;
		for (Vertex u : g) {
			maxEF = Math.max(maxEF, get(u).EF);
		}
		return maxEF;
	}

	// Is u a critical vertex?
	public boolean critical(Vertex u) {
		return get(u).slack == 0;
	}

	// Number of critical vertices of g
	// SL(u) = 0 for critical vertices
	public int numCritical() {
		int count = 0;
		for (Vertex u : g) {
			if (get(u).slack == 0) {
				count++;
			}
		}
		return count;
	}


	public static void main(String[] args) throws Exception {
		String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
		Scanner in;
		// If there is a command line argument, use it as file from which
		// input is read, otherwise use input from string.
		in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
		Graph g = Graph.readDirectedGraph(in);
		g.printGraph(false);

		int[] duration = new int[g.size()];
		for (int i = 0; i < g.size(); i++) {
			duration[i] = in.nextInt();
		}
		PERT p = pert(g, duration);
		if (p == null) {
			System.out.println("Invalid graph: not a DAG");
		} else {
			System.out.println("Number of critical vertices: " + p.numCritical());
			System.out.println("u\tEC\tLC\tSlack\tCritical");
			for (Vertex u : g) {
				System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
			}
		}
	}
}
