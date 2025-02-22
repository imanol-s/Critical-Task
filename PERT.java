package ixs190023;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

import ______.Graph.Edge;
import ______.Graph.Factory;
import ______.Graph.GraphAlgorithm;
import ______.Graph.Vertex;

/**
 * Implementation of PERT (Program Evaluation and Review Technique) algorithm
 * for project scheduling and critical path analysis.
 */
public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    /** List of vertices in topological order */
    private LinkedList<Vertex> finishList;

    /** Constant representing infinity for LF initialization */
    public static final int INF = Integer.MAX_VALUE;

    /**
     * The PERTVertex class represents a vertex in a PERT (Program Evaluation Review Technique) chart.
     * It includes attributes for task duration and various time-related values used in PERT analysis.
     *
     * Attributes:
     * - duration: The duration of the task.
     * - ES (Early Start): The earliest time the task can start.
     * - EF (Early Finish): The earliest time the task can finish.
     * - LS (Late Start): The latest time the task can start without delaying the project.
     * - LF (Late Finish): The latest time the task can finish without delaying the project.
     * - slack: The amount of time that the task can be delayed without delaying the project.
     *
     * Methods:
     * - PERTVertex(Vertex u): Constructs a PERTVertex with the specified vertex and initializes all time-related values to 0.
     * - make(Vertex u): Factory method to create a new PERTVertex for a given Vertex.
     */
    public static class PERTVertex implements Factory {
        int duration, ES, EF, LS, LF, slack;

        // Constructor for PERTVertex
        public PERTVertex(Vertex u) {
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

    /**
     * Performs the PERT (Program Evaluation Review Technique) analysis on the graph.
     *
     * This method checks if the graph is a Directed Acyclic Graph (DAG) and then
     * performs the forward and backward passes to calculate the earliest and latest
     * start times for each task. It also calculates the slack time for each task.
     *
     * @return true if the graph is a DAG and the PERT analysis is successfully performed,
     *         false otherwise.
     */
    public boolean pert() {
        if (g.size() == 0) {
            System.out.println("Graph is empty. Trivially a DAG.");
            return true;
        }

        finishList = topologicalOrder();
        if (finishList == null || finishList.size() != g.size()) {
            System.out.println("Graph is not a DAG.");
            return false;
        }

        forwardPass();
        int projectCompletionTime = calculateProjectCompletionTime();
        backwardPass(projectCompletionTime);
        calculateSlack();
        return true;
    }

    /**
     * Performs the forward pass in the PERT algorithm to calculate the earliest start (ES)
     * and earliest finish (EF) times for each vertex in the graph.
     *
     * The forward pass iterates over the vertices in the finish list and updates the ES and EF
     * times based on the maximum EF of their predecessor vertices.
     *
     * ES (Earliest Start) is calculated as the maximum EF of all predecessor vertices.
     * EF (Earliest Finish) is calculated as ES plus the duration of the task.
     */
    private void forwardPass() {
        // Forward pass: calculate ES and EF
        for (Vertex u : finishList) {
            PERTVertex pu = get(u);
            for (Edge e : g.inEdges(u)) {
                Vertex v = e.fromVertex();
                PERTVertex pv = get(v);
                pu.ES = Math.max(pu.ES, pv.EF); // ES = max EF of predecessors
            }
            pu.EF = pu.ES + pu.duration; // EF = ES + duration
        }
    }

    /**
     * Calculates the project completion time.
     * The project completion time is determined as the maximum Early Finish (EF) time
     * across all vertices in the graph.
     *
     * @return the project completion time, which is the maximum EF value among all vertices.
     */
    private int calculateProjectCompletionTime() {
        int projectCompletionTime = 0;
        for (Vertex u : g) {
            PERTVertex pu = get(u);
            projectCompletionTime = Math.max(projectCompletionTime, pu.EF);
        }
        return projectCompletionTime;
    }

    /**
     * Performs the backward pass in the PERT algorithm to calculate the latest start (LS)
     * and latest finish (LF) times for each vertex in the project graph.
     *
     * @param projectCompletionTime The total time required to complete the project,
     *                              used to initialize the LF for all vertices.
     */
    private void backwardPass(int projectCompletionTime) {
        // Initialize LF for all vertices
        for (Vertex u : g) {
            get(u).LF = projectCompletionTime;
        }

        // Process vertices in reverse topological order
        for (int i = finishList.size() - 1; i >= 0; i--) {
            Vertex u = finishList.get(i);
            PERTVertex pu = get(u);
            for (Edge e : g.outEdges(u)) {
                Vertex v = e.toVertex();
                PERTVertex pv = get(v);
                pu.LF = Math.min(pu.LF, pv.LS); // LF = min LS of successors
            }
            pu.LS = pu.LF - pu.duration; // LS = LF - duration
        }
    }


    /**
     * Calculates the slack time for each vertex in the finish list.
     * Slack time is the difference between the latest finish time (LF)
     * and the earliest finish time (EF) of a vertex.
     * This method uses parallel streams to perform the calculation concurrently.
     */
    private void calculateSlack() {
        finishList.parallelStream().forEach(u -> {
            PERTVertex pu = get(u);
            pu.slack = pu.LF - pu.EF;
        });
    }

    // Topological sort to determine task ordering
    /**
     * Computes the topological order of the vertices in the graph.
     *
     * This method performs a Depth-First Search (DFS) on the graph to determine
     * the topological order of the vertices. It also checks for cycles in the graph.
     * If a cycle is detected, the method returns null, indicating that no topological
     * order exists.
     *
     * @return A LinkedList of vertices in topological order if no cycle is detected,
     *         otherwise null.
     */
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
    /**
     * Performs a Depth-First Search (DFS) to detect cycles and generate a topological order.
     *
     * @param u The current vertex being visited.
     * @param explored An array indicating whether each vertex has been visited.
     * @param onStack An array indicating whether each vertex is currently on the recursion stack.
     * @return true if no cycle is detected, false if a cycle is detected.
     */
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
    /**
     * Computes the PERT (Program Evaluation and Review Technique) for the given graph and duration array.
     *
     * @param g the graph representing the project tasks and dependencies
     * @param duration an array where each element represents the duration of the corresponding task in the graph
     * @return a PERT object if the PERT calculation is successful, or null if the graph or duration array is invalid
     * @throws IllegalArgumentException if the graph is null, the duration array is null, or the size of the graph does not match the length of the duration array
     */
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
