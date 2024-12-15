package ixs190023;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ixs190023.Graph.Edge;
import ixs190023.Graph.Factory;
import ixs190023.Graph.GraphAlgorithm;
import ixs190023.Graph.Vertex;

/**
 * Implementation of PERT (Program Evaluation and Review Technique) algorithm
 * for project scheduling and critical path analysis.
 */
public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    /** List of vertices in topological order */
    private LinkedList<Vertex> finishList;

    /** Constant representing infinity for LF initialization */
    public static final int INF = Integer.MAX_VALUE;

    // Class representing PERT-specific attributes for each vertex
    public static class PERTVertex implements Factory {
        int duration; // Duration of the task
        int ES, EF; // Earliest start and finish times
        int LS, LF; // Latest start and finish times
        int slack; // Slack time: LF - EF

        public PERTVertex(Vertex u) {
            // Initialize all values to 0
            this.duration = 0;
            this.ES = this.EF = this.LS = this.LF = this.slack = 0;
        }

        // Factory method to create a new PERTVertex for a given Vertex
        public PERTVertex make(Vertex u) {
            return new PERTVertex(u);
        }
    }

    // Private constructor for PERT
    private PERT(Graph g) {
        super(g, new PERTVertex(null));
    }

    // Set the duration of a task (vertex)
    public void setDuration(Vertex u, int d) {
        get(u).duration = d;
    }

    // Main method to execute the PERT algorithm
    /**
     * Performs the PERT (Program Evaluation and Review Technique) analysis on the graph.
     *
     * The method calculates the earliest start (ES), earliest finish (EF), latest start (LS),
     * latest finish (LF), and slack times for each vertex in the graph. It also determines
     * if the graph is a Directed Acyclic Graph (DAG) and computes the project completion time.
     *
     * @return true if the graph is a DAG and the PERT analysis is successfully performed, false otherwise.
     */
    public boolean pert() {
        // Handle empty graphs
        if (g.size() == 0) {
            System.out.println("Graph is empty. Trivially a DAG.");
            return true;
        }

        // Get topological order
        finishList = topologicalOrder();
        if (finishList == null || finishList.size() != g.size()) {
            // Graph has cycles, so it is not a DAG
            System.out.println("Graph is not a DAG.");
            return false;
        }

        // Forward pass: Calculate ES (Earliest Start) and EF (Earliest Finish)
        for (Vertex u : finishList) {
            PERTVertex pu = get(u);
            for (Edge e : g.inEdges(u)) {
                Vertex v = e.fromVertex(); // Predecessor vertex
                PERTVertex pv = get(v);
                pu.ES = Math.max(pu.ES, pv.EF); // Set ES as max of predecessors' EF
            }
            pu.EF = pu.ES + pu.duration; // EF = ES + duration
        }

        // Determine the project completion time (max EF)
        int projectCompletionTime = 0;
        for (Vertex u : g) {
            PERTVertex pu = get(u);
            projectCompletionTime = Math.max(projectCompletionTime, pu.EF);
        }

        // Initialize LF for all vertices to the project completion time
        for (Vertex u : g) {
            PERTVertex pu = get(u);
            pu.LF = projectCompletionTime;
        }

        // Backward pass: Calculate LF (Latest Finish) and LS (Latest Start)
        List<Vertex> reverseFinishList = new ArrayList<>(finishList);
        Collections.reverse(reverseFinishList); // Reverse topological order

        for (Vertex u : reverseFinishList) {
            PERTVertex currentPertVertex = get(u);
            if (!g.outEdges(u).iterator().hasNext()) {
                // Terminal vertex: set LF to project completion time
                currentPertVertex.LF = projectCompletionTime;
            } else {
                for (Edge e : g.outEdges(u)) {
                    Vertex v = e.toVertex(); // Successor vertex
                    PERTVertex pv = get(v);
                    currentPertVertex.LF = Math.min(currentPertVertex.LF, pv.LS); // Set LF as min of successors' LS
                }
            }
            currentPertVertex.LS = currentPertVertex.LF - currentPertVertex.duration; // LS = LF - duration
        }

        // Calculate slack for all vertices
        for (Vertex u : g) {
            PERTVertex pu = get(u);
            pu.slack = pu.LF - pu.EF; // Slack = LF - EF
        }

        return true;
    }

    // Topological sort to determine task ordering
    private LinkedList<Vertex> topologicalOrder() {
        finishList = new LinkedList<>();
        boolean[] explored = new boolean[g.size()]; // Tracks visited vertices
        boolean[] onStack = new boolean[g.size()]; // Tracks recursion stack for cycle detection

        for (Vertex u : g) {
            if (!explored[u.getIndex()]) {
                if (!dfs(u, explored, onStack)) {
                    finishList.clear();
                    return null; // Cycle detected, no topological order
                }
            }
        }
        return finishList; // Return vertices in topological order
    }

    // Depth-first search for cycle detection and topological sorting
    private boolean dfs(Vertex u, boolean[] explored, boolean[] onStack) {
        explored[u.getIndex()] = true; // Mark the vertex as visited
        onStack[u.getIndex()] = true; // Add to recursion stack

        for (Edge e : g.outEdges(u)) {
            Vertex v = e.toVertex();
            if (!explored[v.getIndex()]) {
                if (!dfs(v, explored, onStack)) {
                    return false; // Cycle detected
                }
            } else if (onStack[v.getIndex()]) {
                return false; // Cycle detected
            }
        }

        onStack[u.getIndex()] = false; // Remove from recursion stack
        finishList.addFirst(u); // Add vertex to topological order
        return true;
    }

    // Static method to create a PERT instance and execute the algorithm
    public static PERT pert(Graph g, int[] duration) {
        if (g == null || duration == null || g.size() != duration.length) {
            throw new IllegalArgumentException("Invalid graph or duration array.");
        }

        PERT p = new PERT(g);
        for (Vertex u : g) {
            p.setDuration(u, duration[u.getIndex()]);
        }

        return p.pert() ? p : null;
    }

    // Getter for earliest completion time of a task
    public int ec(Vertex u) {
        return get(u).ES;
    }

    // Getter for latest completion time of a task
    public int lc(Vertex u) {
        return get(u).LF;
    }

    // Getter for slack of a task
    public int slack(Vertex u) {
        return get(u).slack;
    }

    // Determines the critical path length (project completion time)
    public int criticalPath() {
        int maxEF = 0;
        for (Vertex u : g) {
            maxEF = Math.max(maxEF, get(u).EF);
        }
        return maxEF;
    }

    // Checks if a task is critical (no slack)
    public boolean critical(Vertex u) {
        return get(u).slack == 0;
    }

    // Counts the number of critical tasks
    public int numCritical() {
        int count = 0;
        for (Vertex u : g) {
            if (critical(u)) {
                count++;
            }
        }
        return count;
    }

    // Main method to test the PERT algorithm
    public static void main(String[] args) throws Exception {
        // Input graph and durations
        String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
        Scanner in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);

        Graph g = Graph.readDirectedGraph(in);
        g.printGraph(false);

        // Read durations
        int[] duration = new int[g.size()];
        for (int i = 0; i < g.size(); i++) {
            if (in.hasNextInt()) {
                duration[i] = in.nextInt();
            } else {
                throw new IllegalArgumentException("Duration array size mismatch.");
            }
        }

        // Execute PERT algorithm
        PERT p = pert(g, duration);
        if (p == null) {
            System.out.println("Invalid graph: not a DAG.");
        } else {
            System.out.println("Number of critical vertices: " + p.numCritical());
            System.out.println("u\tEC\tLC\tSlack\tCritical");
            for (Vertex u : g) {
                System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
            }
            System.out.println("Critical Path Length: " + p.criticalPath());
        }
    }
}
