package org.restopt;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.choco.LandscapeIndicesUtils;
import org.restopt.exception.RestoptException;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.objectives.AbstractRestoptObjective;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Class representing a solution to a restopt problem
 */
public class RestoptSolution {

    public static final String KEY_MIN_RESTORE = "min_restore";
    public static final String KEY_TOTAL_RESTORABLE = "total_restorable";
    public static final String KEY_NB_PUS = "nb_planning_units";
    public static final String KEY_NB_COMPONENTS = "nb_components";
    public static final String KEY_DIAMETER = "diameter";
    public static final String KEY_OPTIMALITY_PROVEN = "optimality_proven";
    public static final String KEY_SEARCH_STATE = "search_state";
    public static final String KEY_SOLVING_TIME = "solving_time";
    public static final String[] KEYS = {
            KEY_MIN_RESTORE, KEY_TOTAL_RESTORABLE, KEY_NB_PUS, KEY_NB_COMPONENTS, KEY_DIAMETER, KEY_OPTIMALITY_PROVEN, KEY_SEARCH_STATE, KEY_SOLVING_TIME
    };

    private final RestoptProblem problem;
    private final AbstractRestoptObjective objective;
    private final Solution solution;
    private final Map<String, String> characteristics;
    private Map<String, String> messages;

    public RestoptSolution(RestoptProblem problem, AbstractRestoptObjective objective, Solution solution) {
        this.problem = problem;
        this.objective = objective;
        this.solution = solution;
        this.characteristics = makeCharacteristics();
        initMessages();
    }

    public void initMessages() {
        this.messages = new HashMap<>();
        messages.put(KEY_MIN_RESTORE, "Minimum area to restore: ");
        messages.put(KEY_TOTAL_RESTORABLE, "Total restorable area: ");
        messages.put(KEY_NB_PUS, "Number of planning units: ");
        messages.put(KEY_NB_COMPONENTS, "Number of connected components: ");
        messages.put(KEY_DIAMETER, "Diameter: ");
        messages.put(KEY_OPTIMALITY_PROVEN, "Optimality proven: ");
        messages.put(KEY_SEARCH_STATE, "Search state: ");
        messages.put(KEY_SOLVING_TIME, "Solving time (seconds): ");
        for (String[] s : objective.appendMessages()) {
            messages.put(s[0], s[1]);
        }
    }

    private Map<String, String> makeCharacteristics() {
        Model model = problem.getModel();
        Map<String, String> solCharacteristics = new HashMap<>();
        solCharacteristics.put(KEY_MIN_RESTORE, String.valueOf(getMinRestoreArea()));
        solCharacteristics.put(KEY_TOTAL_RESTORABLE, String.valueOf(getTotalRestorableArea()));
        solCharacteristics.put(KEY_NB_PUS, String.valueOf(getRestorationPlanningUnits().length));
        solCharacteristics.put(KEY_NB_COMPONENTS, String.valueOf(getNbComponents()));
        solCharacteristics.put(KEY_DIAMETER, String.valueOf(getDiameter()));
        solCharacteristics.put(KEY_SOLVING_TIME, String.valueOf(1.0 * objective.getTotalRuntime() / 1000));
        solCharacteristics.put(KEY_SEARCH_STATE, String.valueOf(problem.getSearchState()));
        solCharacteristics.put(KEY_OPTIMALITY_PROVEN, String.valueOf(objective.isProvenOptimal()));
        solCharacteristics.putAll(objective.appendCharacteristics(solution));
        return solCharacteristics;
    }

    public Map<String, String> getCharacteristics() {
        return characteristics;
    }

    public String getCharacteristicsAsCsv() {
        String[][] orderedCharacteristics = new String[2][];
        String[] allKeys = ArrayUtils.append(KEYS, objective.getAdditionalKeys());
        orderedCharacteristics[0] = allKeys;
        orderedCharacteristics[1] = new String[allKeys.length];
        for (int i = 0; i < allKeys.length; i++) {
            orderedCharacteristics[1][i] = characteristics.get(allKeys[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (String[] line : orderedCharacteristics) {
            int i = 0;
            for (String s : line) {
                i++;
                sb.append(s);
                if (i < line.length) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void printSolutionInfos() {
        System.out.println("\n--- Best solution ---\n");
        for (String key : KEYS) {
            System.out.println(messages.get(key) + characteristics.get(key));
        }
        for (String key : objective.getAdditionalKeys()) {
            System.out.println(messages.get(key) + characteristics.get(key));
        }
        System.out.println();
    }

    public void export(String outputPath, boolean verbose) throws IOException, RestoptException {
        if (problem.getData() instanceof RasterDataLoader) {
            RasterDataLoader dataLoader = (RasterDataLoader) problem.getData();
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
                    dataLoader.getHabitatRasterPath(),
                    outputPath + ".csv",
                    outputPath + ".tif",
                    problem.getData().getNoDataValue()
            );
            String[][] orderedCharacteristics = new String[2][];
            String[] allKeys = ArrayUtils.append(KEYS, objective.getAdditionalKeys());
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
        } else {
            throw new RestoptException("The export function can only be used if the data was loaded from a raster");
        }
    }

    /**
     * @return The minimum amount of habitat that needs to be restored in the selected restoration area.
     */
    public int getMinRestoreArea() {
        if (problem.getMinRestore() != null) {
            return solution.getIntVal(problem.getMinRestore());
        } else {
            return 0;
        }
    }

    /**
     * @return The total amount of habitat that can be restored in the selected restoration area.
     */
    public int getTotalRestorableArea() {
        if (problem.getTotalRestorable() != null) {
            return solution.getIntVal(problem.getTotalRestorable());
        } else {
            int maxRestore = 0;
            for (int i : getRestorationPlanningUnits()) {
                maxRestore += problem.getRestorableArea(i);
            }
            return maxRestore;
        }
    }

    /**
     * @return The number of connected components of the solution.
     */
    public int getNbComponents() {
        ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(getRestorationGraph());
        cf.findAllCC();
        return cf.getNBCC();
    }

    /**
     * @return The diameter of the solution.
     */
    public double getDiameter() {
        int[] pus = getRestorationPlanningUnits();
        double[][] coordinates = new double[pus.length][];
        for (int i = 0; i < pus.length; i++) {
            try {
                coordinates[i] = problem.getGrid().getCartesianCoordinates(pus[i]);
            } catch (RestoptException e) {
                e.printStackTrace();
            }
        }
        double[] minidisk = LandscapeIndicesUtils.getSmallestEnclosingCircle(coordinates);
        if (minidisk.length == 0) {
            return 0;
        }
        return minidisk[2] * 2;
     }

    public int[] getRestorationPlanningUnits() {
        return solution.getSetVal(problem.getRestoreSetVar());
    }

    public int[] getRestorationPlanningUnitsCompleteIndex() {
        return IntStream.of(getRestorationPlanningUnits())
                .map(i -> problem.getGrid().getUngroupedCompleteIndex(i))
                .toArray();
    }

    public UndirectedGraph getRestorationGraph() {
        int[] pus = getRestorationPlanningUnits();
        UndirectedGraph g = GraphFactory.makeUndirectedGraph(
                problem.grid.getNbCells(),
                SetType.BIPARTITESET,
                SetType.BIPARTITESET,
                pus,
                new int[][]{}
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
