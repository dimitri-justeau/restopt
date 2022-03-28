package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.RestoptProblem;
import org.restopt.SolutionExporter;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.io.IOException;
import java.util.Map;

/**
 * Class representing a solution to a restopt problem
 */
public class RestoptSolution {

    private RestoptProblem problem;
    private AbstractRestoptObjective objective;
    private Solution solution;
    private Map<String, String> characteristics;

    public RestoptSolution(RestoptProblem problem, AbstractRestoptObjective objective, Solution solution) {
        this.problem = problem;
        this.objective = objective;
        this.solution = solution;
        this.characteristics = objective.getSolutionCharacteristics(solution);
    }

    public void printSolutionInfos() {
        System.out.println("\n--- Best solution ---\n");
        for (String key : objective.KEYS) {
            System.out.println(objective.messages.get(key) + characteristics.get(key));
        }
        for (String key : objective.getAdditionalKeys()) {
            System.out.println(objective.messages.get(key) + characteristics.get(key));
        }
        System.out.println();
    }

    public void export(String outputPath, boolean verbose) throws IOException {
        PartialRegularGroupedGrid grid = problem.getGrid();
        int[] sites = new int[grid.getNbUngroupedCells()];
        ISet set = SetFactory.makeConstantSet(solution.getSetVal(problem.getRestoreSetVar()));
        for (int i = 0; i < grid.getNbUngroupedCells(); i++) {
            if (grid.getGroupIndexFromPartialIndex(i) < grid.getNbGroups()) {
                sites[i] = 2;
            } else if (set.contains(grid.getGroupIndexFromPartialIndex(i))) {
                sites[i] = 3;
            } else {
                sites[i] = 1;
            }
        }
        SolutionExporter exporter = new SolutionExporter(
                problem,
                sites,
                problem.getData().getHabitatRasterPath(),
                outputPath + ".csv",
                outputPath + ".tif",
                problem.getData().noDataHabitat
        );
        String[][] orderedCharacteristics = new String[2][];
        String[] allKeys = ArrayUtils.append(objective.KEYS, objective.getAdditionalKeys());
        orderedCharacteristics[0] = allKeys;
        orderedCharacteristics[1] = new String[allKeys.length];
        for (int i = 0; i < allKeys.length; i++) {
            orderedCharacteristics[1][i] = characteristics.get(allKeys[i]);
        }
        exporter.exportCharacteristics(orderedCharacteristics);
        exporter.generateRaster();
        if (verbose) {
            printSolutionInfos();
            System.out.println("\nRaster exported at " + outputPath + ".tif");
            System.out.println("Solution characteristics exported at " + outputPath + ".csv\n");
        }
    }

    public int getMinRestoreArea() {
        return problem.getMinRestoreValue(solution);
    }

    public int getTotalRestorableArea() {
        return problem.getTotalRestorableValue(solution);
    }
    public int[] getRestorationPlanningUnits() {
        return solution.getSetVal(problem.getRestoreSetVar());
    }

    public UndirectedGraph getRestorationGraph() {
        int[] pus = getRestorationPlanningUnits();
        UndirectedGraph g = GraphFactory.makeUndirectedGraph(
                problem.grid.getNbCells(),
                SetType.BIPARTITESET,
                SetType.BIPARTITESET,
                pus,
                new int[][] {}
        );
        for (int i : pus) {
            for (int j : problem.getNeighborhood().getNeighbors(problem.getGrid(), i)) {
                if (g.containsNode(j)) {
                    g.addEdge(i, j);
                }
            }
        }
        return g;
    }
}
