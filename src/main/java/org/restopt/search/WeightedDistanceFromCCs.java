package org.restopt.search;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.subgraph.NodeSubGraphVar;
import org.chocosolver.solver.variables.subgraph.SubGraphConnectedComponents;
import org.chocosolver.solver.variables.subgraph.SubGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.restopt.RestoptProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;


/**
 */
public class WeightedDistanceFromCCs extends AbstractRestoptSearchStrategy {

    int nbCCLB;
    int[][] distances;
    double[] weights;
    SubGraphVar g;
    boolean increasingOrder;
    boolean UB;
    public List<Integer> a;
    int distThreshold;

    public WeightedDistanceFromCCs(RestoptProblem problem, boolean increasingOrder, boolean UB, int distThreshold) {
        super(problem);
        this.increasingOrder = increasingOrder;
        this.UB = UB;
        this.distThreshold = distThreshold;
        g = problem.getHabitatGraphVar();
        g.updateConnectivity();
        SubGraphConnectedComponents ccUB = g.getConnectedComponentsUB();
        SubGraphConnectedComponents ccLB = g.getConnectedComponentsUB();
        UndirectedGraph gub = g.getUBAsGraph();
        nbCCLB = ccLB.getNbCC();
        weights = new double[g.getNodeVars().length];
        distances = new int[g.getNodeVars().length][];
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            int node = g.getPotentialNode(i);
            if (ccUB.isAlsoInLB(node)) {
                weights[node] = 0;
            } else {
                distances[node] = new int[nbCCLB];
                Arrays.fill(distances[node], g.getNbPotentialNodes());
                int[] dist = bfsDists(node, gub);
                for (int k = 0; k < dist.length; k++) {
                    if (ccUB.isAlsoInLB(k)) {
                        int cc = ccLB.getNodeCC(k);
                        if (dist[k] >= 0 && dist[k] < distances[node][cc]) {
                            distances[node][cc] = dist[k];
                        }
                    }
                }
                weights[node] = 0;
                for (int cc = 0; cc < nbCCLB; cc++) {
                    double factor = distances[node][cc] <= distThreshold ? 1.0 / distances[node][cc] : 0;
                    weights[node] += factor * ccLB.getAttributeCC(cc);
                }
            }
        }
    }

    public int[] bfsDists(int source, UndirectedGraph gub) {
        int n = gub.getNodes().size();
        boolean[] visited = new boolean[n];
        int[] queue = new int[n];
        int front = 0;
        int rear = 0;
        int[] dist = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = -1;
        }
        int current;
        visited[source] = true;
        queue[front] = source;
        rear++;
        dist[source] = 0;
        while (front != rear) {
            current = queue[front++];
            for (int i : gub.getNeighborsOf(current)) {
                if (!visited[i]) {
                    dist[i] = dist[current] + 1;
                    queue[rear++] = i;
                    visited[i] = true;
                }
            }
        }
        return dist;
    }

    public void setSearch() {
        a = new ArrayList<>();
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            a.add(i);
        }
        Collections.shuffle(a);
        a.sort((i, j) -> {
            double iw = weights[i];
            double jw = weights[j];
            if (iw == jw) {
                return 0;
            }
            if (increasingOrder) {
                return iw <= jw ? 1 : -1;
            } else {
                return jw <= iw ? 1 : -1;
            }
        });
        BoolVar[] bools = IntStream.range(0, a.size())
                .filter(i -> problem.getRestoreGraphVar().getNodeVars()[a.get(i)] instanceof NodeSubGraphVar)
                .mapToObj(i -> problem.getRestoreGraphVar().getNodeVars()[a.get(i)])
                .toArray(BoolVar[]::new);
        if (UB) {
            problem.getModel().getSolver().setSearch(Search.inputOrderUBSearch(bools));
        } else {
            problem.getModel().getSolver().setSearch(Search.inputOrderLBSearch(bools));
        }
    }
}
