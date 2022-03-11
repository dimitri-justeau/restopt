package org.restopt;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.choco.*;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

public class BaseProblem {

    public DataLoader data;
    public PartialRegularGroupedGrid grid;

    public int accessibleVal;

    Model model;
    UndirectedGraphVar habitatGraph;
    UndirectedGraphVar restoreGraph;
    public UndirectedGraph habGraph;
    SetVar restoreSet;

    public int nonHabNonAcc;
    public int[] accessibleNonHabitatPixels;

    public IntVar minRestore;
    public IntVar maxRestorable;
    public IntVar MESH;
    public IntVar IIC;

    PropSmallestEnclosingCircleSpatialGraph propCompact;

    public BaseProblem() {}

    public BaseProblem(DataLoader data, int accessibleVal) {

        this.data = data;
        this.accessibleVal = accessibleVal;

        // ------------------ //
        // PREPARE INPUT DATA //
        // ------------------ //

        System.out.println("Height = " + data.getHeight());
        System.out.println("Width = " + data.getWidth());

        int[] outPixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] <= -1 || data.getHabitatData()[i] == data.noDataHabitat)
                .toArray();

        int[] nonHabitatNonAccessiblePixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == 0 && data.getAccessibleData()[i] != accessibleVal)
                .toArray();

        int[] habitatPixelsComp = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == 1)
                .toArray();

        habGraph = Neighborhoods.FOUR_CONNECTED.getPartialGraph(new RegularSquareGrid(data.getHeight(), data.getWidth()), habitatPixelsComp, SetType.RANGESET, SetType.RANGESET);

        nonHabNonAcc = nonHabitatNonAccessiblePixels.length;

        this.grid = new PartialRegularGroupedGrid(data.getHeight(), data.getWidth(), ArrayUtils.concat(outPixels, nonHabitatNonAccessiblePixels), habGraph);

        int[] nonHabitatPixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == 0)
                .toArray();

        int[] habitatPixels = IntStream.range(0, grid.getNbGroups()).toArray();

        accessibleNonHabitatPixels = IntStream.range(0, data.getAccessibleData().length)
                .filter(i -> data.getAccessibleData()[i] == accessibleVal && data.getHabitatData()[i] == 0)
                .map(i -> grid.getGroupIndexFromCompleteIndex(i))
                .toArray();

        System.out.println("Current landscape state loaded");
        System.out.println("    Habitat cells = " + habitatPixelsComp.length + " ");
        System.out.println("    Non habitat cells = " + nonHabitatPixels.length + " ");
        System.out.println("    Accessible non habitat cells = " + accessibleNonHabitatPixels.length + " ");
        System.out.println("    Out cells = " + outPixels.length);

        // ------------------ //
        // INITIALIZE PROBLEM //
        // ------------------ //

        model = new Model();

        UndirectedGraph hab_LB = Neighborhoods.PARTIAL_GROUPED_FOUR_CONNECTED.getPartialGraph(grid, model, habitatPixels, SetType.BIPARTITESET, SetType.BIPARTITESET);
        UndirectedGraph hab_UB = Neighborhoods.PARTIAL_GROUPED_FOUR_CONNECTED.getPartialGraph(grid, model, ArrayUtils.concat(habitatPixels, accessibleNonHabitatPixels), SetType.BIPARTITESET, SetType.BIPARTITESET);

        habitatGraph = model.nodeInducedGraphVar(
                "habitatGraph",
                hab_LB,
                hab_UB
        );
        restoreGraph = model.nodeInducedSubgraphView(habitatGraph, SetFactory.makeConstantSet(IntStream.range(0, grid.getNbGroups()).toArray()), true);
        restoreSet = model.graphNodeSetView(restoreGraph);

        setDefaultSearch();
    }

    public void postNbComponentsConstraint(int minNbCC, int maxNbCC) {
        model.nbConnectedComponents(restoreGraph, model.intVar(minNbCC, maxNbCC)).post();
    }

    public void postCompactnessConstraint(double maxDiameter) {

        double[][] coords = new double[grid.getNbCells()][];
        double[][] compCoords = grid.getCartesianCoordinates();
        for (int i = 0; i < accessibleNonHabitatPixels.length; i++) {
            coords[accessibleNonHabitatPixels[i]] = compCoords[grid.getUngroupedPartialIndex(accessibleNonHabitatPixels[i])];
        }

        propCompact = new PropSmallestEnclosingCircleSpatialGraph(
                restoreGraph,
                coords,
                model.realVar("radius", 0, 0.5 * maxDiameter, 1e-5),
                model.realVar("centerX",
                        Arrays.stream(grid.getCartesianCoordinates())
                                .mapToDouble(c -> c[0]).min().getAsDouble(),
                        Arrays.stream(grid.getCartesianCoordinates())
                                .mapToDouble(c -> c[0]).max().getAsDouble(),
                        1e-5
                ),
                model.realVar("centerY",
                        Arrays.stream(grid.getCartesianCoordinates())
                                .mapToDouble(c -> c[0]).min().getAsDouble(),
                        Arrays.stream(grid.getCartesianCoordinates())
                                .mapToDouble(c -> c[0]).max().getAsDouble(),
                        1e-5
                )
        );
        Constraint cons = new Constraint("maxDiam", propCompact);
        model.post(cons);
    }

    public boolean maximizeMESH(int precision, String outputPath, int timeLimit) throws IOException {
        return maximizeMESH(precision, outputPath, timeLimit, true);
    }

    public boolean maximizeMESH(int precision, String outputPath, int timeLimit, boolean verbose) throws IOException {
        MESH = model.intVar(
                "MESH",
                0, (int) ((data.getHeight() * data.getWidth() - grid.getDiscardSet().size()) * Math.pow(10, precision))
        );
        int landscapeArea = grid.getNbUngroupedCells() + nonHabNonAcc;
        Constraint meshCons = new Constraint(
                "MESH_constraint",
                new PropEffectiveMeshSize(
                        habitatGraph,
                        MESH,
                        grid.getSizeCells(),
                        landscapeArea,
                        precision,
                        true
                )
        );
        model.post(meshCons);
        double MESH_initial = LandscapeIndicesUtils.effectiveMeshSize(
                habGraph,
                (grid.getNbUngroupedCells() + nonHabNonAcc)
        );
        Solver solver = model.getSolver();
        if (verbose) {
            System.out.println("\nMESH initial = " + MESH_initial + "\n");
            solver.showShortStatistics();
        }
        long t = System.currentTimeMillis();
        Solution solution;
        if (timeLimit > 0) {
            TimeCounter timeCounter = new TimeCounter(model, (long) (timeLimit * 1e9));
            solution = solver.findOptimalSolution(MESH, true, timeCounter);
        } else {
            solution = solver.findOptimalSolution(MESH, true);
        }
        if (solution == null) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return false;
        }
        String[][] solCharacteristics = new String[][]{
                {
                    "Minimum area to restore",
                    "Maximum restorable area",
                    "no. planning units",
                    "MESH_initial",
                    "MESH_best",
                    "optimality_proven",
                    "solving time (ms)"
                },
                {
                        String.valueOf(getMinRestoreValue(solution)),
                        String.valueOf(getMaxRestorableValue(solution)),
                        String.valueOf(solution.getSetVal(restoreSet).length),
                        String.valueOf(1.0 * Math.round(MESH_initial * Math.pow(10, precision)) / Math.pow(10, precision)),
                        String.valueOf((1.0 * solution.getIntVal(MESH)) / Math.pow(10, precision)),
                        String.valueOf(getSearchState() == "TERMINATED"),
                        String.valueOf((System.currentTimeMillis() - t))
                }
        };
        exportSolution(outputPath, solution, solCharacteristics);
        if (verbose) {
            System.out.println("\n--- Best solution ---\n");
            System.out.println("Minimum area to restore : " + solCharacteristics[1][0]);
            System.out.println("Maximum restorable area : " + solCharacteristics[1][1]);
            System.out.println("No. planning units : " + solCharacteristics[1][2]);
            System.out.println("Initial MESH value : " + solCharacteristics[1][3]);
            System.out.println("Optimal MESH value : " + solCharacteristics[1][4]);
            System.out.println("Solving time (ms) : " + solCharacteristics[1][5]);
            System.out.println("\nRaster exported at " + outputPath + ".tif");
            System.out.println("Solution characteristics exported at " + outputPath + ".csv");
        }
        return true;
    }

    public boolean maximizeIIC(int precision, String outputPath, int timeLimit) throws IOException {
        return maximizeIIC(precision, outputPath, timeLimit, true);
    }

    public boolean maximizeIIC(int precision, String outputPath, int timeLimit, boolean verbose) throws IOException {
        int landscapeArea = grid.getNbUngroupedCells() + nonHabNonAcc;
        IIC = model.intVar(
                "IIC",
                0, (int) (Math.pow(10, precision))
        );
        Constraint consIIC = new Constraint(
                "IIC_constraint",
                new PropIIC(
                        habitatGraph,
                        IIC,
                        grid,
                        landscapeArea,
                        Neighborhoods.PARTIAL_GROUPED_TWO_WIDE_FOUR_CONNECTED,
                        precision,
                        true
                )
        );
        model.post(consIIC);
        double IIC_initial = ((PropIIC) consIIC.getPropagator(0)).getIICLB();
        Solver solver = model.getSolver();
        if (verbose) {
            System.out.println("\nIIC initial = " + IIC_initial + "\n");
            solver.showShortStatistics();
        }
        long t = System.currentTimeMillis();
        Solution solution;
        if (timeLimit > 0) {
            TimeCounter timeCounter = new TimeCounter(model, (long) (timeLimit * 1e9));
            solution = solver.findOptimalSolution(IIC, true, timeCounter);
        } else {
            solution = solver.findOptimalSolution(IIC, true);
        }
        if (solution == null) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return false;
        }
        String[][] solCharacteristics = new String[][]{
                {
                    "Minimum area to restore",
                    "Maximum restorable area",
                    "no. planning units",
                    "IIC_initial",
                    "IIC_best",
                    "optimality_proven",
                    "solving time (ms)"},
                {
                        String.valueOf(getMinRestoreValue(solution)),
                        String.valueOf(getMaxRestorableValue(solution)),
                        String.valueOf(solution.getSetVal(restoreSet).length),
                        String.valueOf(1.0 * Math.round(IIC_initial * Math.pow(10, precision)) / Math.pow(10, precision)),
                        String.valueOf((1.0 * solution.getIntVal(IIC)) / Math.pow(10, precision)),
                        String.valueOf(getSearchState() == "TERMINATED"),
                        String.valueOf((System.currentTimeMillis() - t))
                }
        };
        exportSolution(outputPath, solution, solCharacteristics);
        if (verbose) {
            System.out.println("\n--- Best solution ---\n");
            System.out.println("Minimum area to restore : " + solCharacteristics[1][0]);
            System.out.println("Maximum restorable area : " + solCharacteristics[1][1]);
            System.out.println("No. planning units : " + solCharacteristics[1][2]);
            System.out.println("Initial IIC value : " + solCharacteristics[1][3]);
            System.out.println("Optimal IIC value : " + solCharacteristics[1][4]);
            System.out.println("Solving time (ms) : " + solCharacteristics[1][5]);
            System.out.println("\nRaster exported at " + outputPath + ".tif");
            System.out.println("Solution characteristics exported at " + outputPath + ".csv");
        }
        return true;
    }

    public void setDefaultSearch() {
        model.getSolver().setSearch(Search.setVarSearch(restoreSet));
    }

    public void setRandomSearch() {
        model.getSolver().setSearch(Search.setVarSearch(
                new Random<SetVar>(model.getSeed()),
                new SetDomainRandom(model.getSeed()),
                true, restoreSet)
        );
    }

    public String getSearchState() {
        return model.getSolver().getSearchState().toString();
    }

    public boolean findSolution(String outputPath, int timeLimit) throws IOException {
        return findSolution(outputPath, timeLimit, true);
    }

    /**
     * Returns the first solution found satisfying the constraints, without optimization objective.
     * @return True if a solution was found
     */
    public boolean findSolution(String outputPath, int timeLimit, boolean verbose) throws IOException {
        Solver solver = model.getSolver();
        if (verbose) {
            solver.showShortStatistics();
        }
        long t = System.currentTimeMillis();
        Solution solution;
        if (timeLimit > 0) {
            TimeCounter timeCounter = new TimeCounter(model, (long) (timeLimit * 1e9));
            solution = solver.findSolution(timeCounter);
        } else {
            solution = solver.findSolution();
        }
        if (solution == null) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return false;
        }
        String[][] solCharacteristics = new String[][]{
                {"Minimum area to restore", "Maximum restorable area", "no. planning units", "solving time (ms)"},
                {
                        String.valueOf(getMinRestoreValue(solution)),
                        String.valueOf(getMaxRestorableValue(solution)),
                        String.valueOf(solution.getSetVal(restoreSet).length),
                        String.valueOf((System.currentTimeMillis() - t))
                }
        };
        exportSolution(outputPath, solution, solCharacteristics);
        if (verbose) {
            System.out.println("\n--- First solution ---\n");
            System.out.println("Minimum area to restore : " + solCharacteristics[1][0]);
            System.out.println("Maximum restorable area : " + solCharacteristics[1][1]);
            System.out.println("No. planning units : " + solCharacteristics[1][2]);
            System.out.println("Solving time (ms) : " + solCharacteristics[1][3]);
            System.out.println("\nRaster exported at " + outputPath + ".tif");
            System.out.println("Solution characteristics exported at " + outputPath + ".csv");
        }
        return true;
    }

    private int getMinRestoreValue(Solution solution) {
        if (minRestore != null) {
            return solution.getIntVal(minRestore);
        } else {
            return 0;
        }
    }

    private int getMaxRestorableValue(Solution solution) {
        if (maxRestorable != null) {
            return solution.getIntVal(maxRestorable);
        } else {
            int maxRestore = 0;
            for (int i : solution.getSetVal(restoreSet)) {
                maxRestore += Math.round(data.getRestorableData()[grid.getUngroupedCompleteIndex(i)]);
            }
            return maxRestore;
        }
    }

    public void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, int cellArea, double minProportion) {
        // Minimum area to ensure every site to >= proportion
        assert minProportion >= 0 && minProportion <= 1;

        int[] minArea = new int[grid.getNbCells()];
        int[] maxRestorableArea = new int[grid.getNbCells()];
        int threshold = (int) Math.ceil(cellArea - cellArea * minProportion);
        for (int i = 0; i < accessibleNonHabitatPixels.length; i++) {
            int cell = accessibleNonHabitatPixels[i];
            int value = (int) Math.round(data.getRestorableData()[grid.getUngroupedCompleteIndex(cell)]);
            maxRestorableArea[cell] = value;
            int restorable = (int) Math.round(data.getRestorableData()[grid.getUngroupedCompleteIndex(cell)]);
            minArea[cell] = restorable <= threshold ? 0 : restorable - threshold;
        }
        minRestore = model.intVar(minAreaToRestore, maxAreaToRestore);
        maxRestorable = model.intVar(0, maxAreaToRestore * cellArea);
        model.sumElements(restoreSet, minArea, minRestore).post();
        model.sumElements(restoreSet, maxRestorableArea, maxRestorable).post();
    }

    public void exportSolution(String exportPath, Solution solution, String[][] characteristics) throws IOException {
        int[] sites = new int[grid.getNbUngroupedCells()];
        ISet set = SetFactory.makeConstantSet(solution.getSetVal(restoreSet));
        for (int i = 0; i < grid.getNbUngroupedCells(); i++) {
            if (grid.getGroupIndexFromPartialIndex(i) < grid.getNbGroups()) {
                sites[i] = 1;
            } else if (set.contains(grid.getGroupIndexFromPartialIndex(i))) {
                sites[i] = 2;
            } else {
                sites[i] = 0;
            }
        }

        SolutionExporter exporter = new SolutionExporter(
                this,
                sites,
                data.getHabitatRasterPath(),
                exportPath + ".csv",
                exportPath + ".tif",
                data.noDataHabitat
        );
        exporter.exportCharacteristics(characteristics);
        exporter.generateRaster();
    }
}
