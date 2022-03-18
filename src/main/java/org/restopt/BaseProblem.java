package org.restopt;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.choco.*;
import org.restopt.constraints.IRestoptConstraintFactory;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;
import org.restopt.objectives.IRestoptObjectiveFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

public class BaseProblem implements IRestoptObjectiveFactory, IRestoptConstraintFactory {

    public DataLoader data;

    public PartialRegularGroupedGrid grid;

    public int accessibleVal;

    private Model model;
    private UndirectedGraphVar habitatGraphVar;

    private UndirectedGraphVar restoreGraph;

    public UndirectedGraph habGraph;

    private SetVar restoreSet;

    public int nonHabNonAcc;

    private int[] accessibleNonHabitatPixels;

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

        habitatGraphVar = model.nodeInducedGraphVar(
                "habitatGraph",
                hab_LB,
                hab_UB
        );
        restoreGraph = model.nodeInducedSubgraphView(habitatGraphVar, SetFactory.makeConstantSet(IntStream.range(0, grid.getNbGroups()).toArray()), true);
        restoreSet = model.graphNodeSetView(restoreGraph);

        setDefaultSearch();
    }

    public Model getModel() {
        return model;
    }

    public SetVar getRestoreSetVar() {
        return restoreSet;
    }

    public PartialRegularGroupedGrid getGrid() {
        return grid;
    }

    public DataLoader getData() {
        return data;
    }

    public UndirectedGraphVar getHabitatGraphVar() {
        return habitatGraphVar;
    }

    public UndirectedGraph getHabitatGraph() {
        return habGraph;
    }

    public int getNbLockedUpNonHabitatCells() {
        return nonHabNonAcc;
    }

    public UndirectedGraphVar getRestoreGraphVar() {
        return restoreGraph;
    }

    public int[] getAvailablePlanningUnits() {
        return accessibleNonHabitatPixels;
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

    public int getMinRestoreValue(Solution solution) {
        if (minRestore != null) {
            return solution.getIntVal(minRestore);
        } else {
            return 0;
        }
    }

    public int getMaxRestorableValue(Solution solution) {
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

    @Override
    public BaseProblem self() {
        return this;
    }

    public void testArrayPerformances(int[] array, boolean verbose) {
        System.out.println(array.length);
        if (verbose) {
            System.out.println(Arrays.toString(array));
        }
    }
}
