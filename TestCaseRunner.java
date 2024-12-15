package ixs190023;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestCaseRunner {
  public static void main(String[] args) {
    // Directory containing the test cases
    String testCaseDir = "p4-testcases/";

    // Display test cases
    File folder = new File(testCaseDir);
    File[] testCases = folder.listFiles((dir, name) -> name.endsWith(".txt")); // Filter .txt files

    if (testCases == null || testCases.length == 0) {
      System.out.println("No test cases found in the directory: " + testCaseDir);
      return;
    }

    System.out.println("Available Test Cases:");
    for (int i = 0; i < testCases.length; i++) {
      System.out.printf("[%d] %s%n", i + 1, testCases[i].getName());
    }

    // Prompt user to select a test case
    System.out.print("Select a test case to run (enter the number): ");
    Scanner inputScanner = new Scanner(System.in);
    int choice = inputScanner.nextInt();

    if (choice < 1 || choice > testCases.length) {
      System.out.println("Invalid selection. Exiting.");
      return;
    }

    File selectedTestCase = testCases[choice - 1];
    System.out.println("Selected Test Case: " + selectedTestCase.getName());

    // Run the selected test case
    try {
      runTestCase(selectedTestCase);
    } catch (FileNotFoundException e) {
      System.err.println("Error: Test case file not found - " + e.getMessage());
    }
  }

  private static void runTestCase(File testCaseFile) throws FileNotFoundException {
    // Read the selected test case
    Scanner in = new Scanner(testCaseFile);

    // Read the graph structure from the file
    Graph g = Graph.readDirectedGraph(in);
    g.printGraph(false);

    // Read durations for the vertices
    int[] duration = new int[g.size()];
    for (int i = 0; i < g.size(); i++) {
      if (in.hasNextInt()) {
        duration[i] = in.nextInt();
      } else {
        throw new IllegalArgumentException("Duration array size mismatch in test case.");
      }
    }

    // Run PERT
    PERT p = PERT.pert(g, duration);
    if (p == null) {
      System.out.println("Invalid graph: not a DAG.");
    } else {
      System.out.println("Number of critical vertices: " + p.numCritical());
      System.out.println("u\tEC\tLC\tSlack\tCritical");
      for (Graph.Vertex u : g) {
        System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
      }
      System.out.println("Critical Path Length: " + p.criticalPath());
    }
  }
}
