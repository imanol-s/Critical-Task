/* Starter code for PERT algorithm (Project 4)
 * @author rbk
 */

// change to your netid
package sxa173731;

// replace sxa173731 with your netid below
import sxa173731.Graph;
import sxa173731.Graph.Vertex;
import sxa173731.Graph.Edge;
import sxa173731.Graph.GraphAlgorithm;
import sxa173731.Graph.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
	
    public static class PERTVertex implements Factory {
	// Add fields to represent attributes of vertices here
	
	public PERTVertex(Vertex u) {
	}
	public PERTVertex make(Vertex u) { return new PERTVertex(u); }
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
	super(g, new PERTVertex(null));
    }

    public void setDuration(Vertex u, int d) {
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
	return false;
    }

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
	return finishList;
    }


    // The following methods are called after calling pert().

    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
	return 0;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
	return 0;
    }

    // Slack of u
    public int slack(Vertex u) {
	return 0;
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
	return 0;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
	return false;
    }

    // Number of critical vertices of g
    public int numCritical() {
	return 0;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
	PERT p = new PERT(g);
	for(Vertex u: g) {
	    p.setDuration(u, duration[u.getIndex()]);
	}
	// Run PERT algorithm.  Returns false if g is not a DAG
	if(p.pert()) {
	    return p;
	} else {
	    return null;
	}
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
	for(int i=0; i<g.size(); i++) {
	    duration[i] = in.nextInt();
	}
	PERT p = pert(g, duration);
	if(p == null) {
	    System.out.println("Invalid graph: not a DAG");
	} else {
	    System.out.println("Number of critical vertices: " + p.numCritical());
	    System.out.println("u\tEC\tLC\tSlack\tCritical");
	    for(Vertex u: g) {
		System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
	    }
	}
    }
}
