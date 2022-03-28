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
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;
import org.restopt.objectives.IRestoptObjectiveFactory;

import java.util.stream.IntStream;

/**
 * Base restopt problem. Instantiated with a `DataLoader` object (which contains access to all the necessary input
 * data), it computes the necessary data structure and implements a base restoration planning problem which can be
 * further constrained, and solved with various optimization objectives.
 */
public class BaseProblem implements IRestoptObjectiveFactory, IRestoptConstraintFactory {

    public DataLoader data;

    public PartialRegularGroupedGrid grid;

    private INeighborhood neighborhood;

    public int accessibleVal;

    private Model model;
    private UndirectedGraphVar habitatGraphVar;

    private UndirectedGraphVar restoreGraph;

    public UndirectedGraph habGraph;

    private SetVar restoreSet;

    public int nonHabNonAcc;

    private int[] accessibleNonHabitatPixels;

    public IntVar minRestore;
    public IntVar totalRestorable;

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

        this.neighborhood = Neighborhoods.PARTIAL_GROUPED_FOUR_CONNECTED;

        model = new Model();

        UndirectedGraph hab_LB = neighborhood.getPartialGraph(grid, model, habitatPixels, SetType.BIPARTITESET, SetType.BIPARTITESET);
        UndirectedGraph hab_UB = neighborhood.getPartialGraph(grid, model, ArrayUtils.concat(habitatPixels, accessibleNonHabitatPixels), SetType.BIPARTITESET, SetType.BIPARTITESET);

        habitatGraphVar = model.nodeInducedGraphVar(
                "habitatGraph",
                hab_LB,
                hab_UB
        );
        restoreGraph = model.nodeInducedSubgraphView(habitatGraphVar, SetFactory.makeConstantSet(IntStream.range(0, grid.getNbGroups()).toArray()), true);
        restoreSet = model.graphNodeSetView(restoreGraph);

        setDefaultSearch();
    }

    /**
     * @return The Choco model associated with this restopt problem.
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return The set variable representing the set of planning units selected for restoration.
     */
    public SetVar getRestoreSetVar() {
        return restoreSet;
    }

    /**
     * @return The grid corresponding to the problem.
     */
    public PartialRegularGroupedGrid getGrid() {
        return grid;
    }

    /**
     * @return The data loader object that was used to instantiate this problem.
     */
    public DataLoader getData() {
        return data;
    }

    /**
     * @return The graph variable representing the existing habitat plus the restored area.
     */
    public UndirectedGraphVar getHabitatGraphVar() {
        return habitatGraphVar;
    }

    /**
     * @return The existing habitat (without any restoration) as a graph.
     */
    public UndirectedGraph getHabitatGraph() {
        return habGraph;
    }

    /**
     * @return The number of planning units (non habitat, non NA) that are locked up.
     */
    public int getNbLockedUpNonHabitatCells() {
        return nonHabNonAcc;
    }

    /**
     * @return The graph variable representing the set of planning units selected for restoration.
     */
    public UndirectedGraphVar getRestoreGraphVar() {
        return restoreGraph;
    }

    /**
     * @return The IntVar corresponding to the minimum area that need to be restored.
     */
    public IntVar getMinRestore() {
        return minRestore;
    }

    /**
     * @return The planning units of the problem.
     */
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

    /**
     * @return The search state of the solver as a String.
     */
    public String getSearchState() {
        return model.getSolver().getSearchState().toString();
    }

    /**
     * @param solution A solution to the problem.
     * @return The minimum amount of habitat that needs to be restored in the selected restoration area.
     */
    public int getMinRestoreValue(Solution solution) {
        if (minRestore != null) {
            return solution.getIntVal(minRestore);
        } else {
            return 0;
        }
    }

    /**
     * @param solution A solution to the problem.
     * @return The total amount of habitat that can be restored in the selected restoration area.
     */
    public int getTotalRestorableValue(Solution solution) {
        if (totalRestorable != null) {
            return solution.getIntVal(totalRestorable);
        } else {
            int maxRestore = 0;
            for (int i : solution.getSetVal(restoreSet)) {
                maxRestore += Math.round(data.getRestorableData()[grid.getUngroupedCompleteIndex(i)]);
            }
            return maxRestore;
        }
    }

    /**
     * @return The neighborhood relation used in the problem.
     */
    public INeighborhood getNeighborhood() {
        return neighborhood;
    }

    @Override
    public BaseProblem self() {
        return this;
    }
}
