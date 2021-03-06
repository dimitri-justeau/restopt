package org.restopt;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.constraints.EffectiveMeshSizeConstraint;
import org.restopt.constraints.IRestoptConstraintFactory;
import org.restopt.constraints.IntegralIndexOfConnectivityConstraint;
import org.restopt.constraints.RestorableAreaConstraint;
import org.restopt.exception.RestoptException;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;
import org.restopt.objectives.IRestoptObjectiveFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Base restopt problem. Instantiated with a `DataLoader` object (which contains access to all the necessary input
 * data), it computes the necessary data structure and implements a base restoration planning problem which can be
 * further constrained, and solved with various optimization objectives.
 */
public class RestoptProblem implements IRestoptObjectiveFactory, IRestoptConstraintFactory {

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
    private int[] availablePlanningUnits;

    public IntVar minRestore;
    public IntVar totalRestorable;

    private EffectiveMeshSizeConstraint effectiveMeshSizeConstraint;
    private IntegralIndexOfConnectivityConstraint integralIndexOfConnectivityConstraint;
    private RestorableAreaConstraint restorableAreaConstraint;

    /**
     * Map to share variables between constraints and objectives
     */
    private Map<String, IntVar> additionalVariables;

    public RestoptProblem() {
    }

    public RestoptProblem(DataLoader data, int accessibleVal) {

        this.data = data;
        this.accessibleVal = accessibleVal;
        this.additionalVariables = new HashMap<>();

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

        availablePlanningUnits = IntStream.range(0, data.getAccessibleData().length)
                .filter(i -> data.getAccessibleData()[i] == accessibleVal && data.getHabitatData()[i] == 0)
                .map(i -> grid.getGroupIndexFromCompleteIndex(i))
                .toArray();

        System.out.println("Current landscape state loaded");
        System.out.println("    Habitat cells = " + habitatPixelsComp.length + " ");
        System.out.println("    Non habitat cells = " + nonHabitatPixels.length + " ");
        System.out.println("    Accessible non habitat cells = " + availablePlanningUnits.length + " ");
        System.out.println("    Out cells = " + outPixels.length);

        // ------------------ //
        // INITIALIZE PROBLEM //
        // ------------------ //

        this.neighborhood = Neighborhoods.PARTIAL_GROUPED_FOUR_CONNECTED;

        model = new Model();

        UndirectedGraph hab_LB = neighborhood.getPartialGraph(grid, model, habitatPixels, SetType.BIPARTITESET, SetType.BIPARTITESET);
        UndirectedGraph hab_UB = neighborhood.getPartialGraph(grid, model, ArrayUtils.concat(habitatPixels, availablePlanningUnits), SetType.BIPARTITESET, SetType.BIPARTITESET);

        habitatGraphVar = model.nodeInducedGraphVar(
                "habitatGraph",
                hab_LB,
                hab_UB
        );
        restoreGraph = model.nodeInducedSubgraphView(habitatGraphVar, SetFactory.makeConstantSet(IntStream.range(0, grid.getNbGroups()).toArray()), true);
        restoreSet = model.graphNodeSetView(restoreGraph);
    }

    public Map<String, IntVar> getAdditionalVariables() {
        return additionalVariables;
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
     * @return The IntVar corresponding to the total area that can be restored.
     */
    public IntVar getTotalRestorable() {
        return totalRestorable;
    }

    /**
     * @return The planning units of the problem.
     */
    public int[] getAvailablePlanningUnits() {
        return availablePlanningUnits;
    }

    /**
     * @param pu A planning unit index (partial grouped index)
     * @return The amount of restorable habitat in pu.
     */
    public int getRestorableArea(int pu) {
        return (int) Math.round(data.getRestorableData()[grid.getUngroupedCompleteIndex(pu)]);
    }

    /**
     * @return The search state of the solver as a String.
     */
    public String getSearchState() {
        return model.getSolver().getSearchState().toString();
    }

    /**
     * @return The neighborhood relation used in the problem.
     */
    public INeighborhood getNeighborhood() {
        return neighborhood;
    }

    /**
     * @return True if a restorable area constraint was associated with this problem.
     */
    public boolean hasRestorableAreaConstraint() {
        return restorableAreaConstraint != null;
    }

    /**
     * @return The restorable area constraint associated with this problem.
     */
    public RestorableAreaConstraint getRestorableAreaConstraint() {
        return restorableAreaConstraint;
    }

    /**
     * Associate a restorable area constraint with this problem.
     *
     * @param restorableAreaConstraint
     */
    public void setRestorableAreaConstraint(RestorableAreaConstraint restorableAreaConstraint) throws RestoptException {
        if (this.hasRestorableAreaConstraint()) {
            throw new RestoptException("Only one restorable area constraint can be associated with a restopt problem");
        }
        this.restorableAreaConstraint = restorableAreaConstraint;
    }

    /**
     * @return True if a mesh constraint was associated with this problem.
     */
    public boolean hasMeshConstraint() {
        return effectiveMeshSizeConstraint != null;
    }

    /**
     * @return The mesh constraint associated with this problem.
     */
    public EffectiveMeshSizeConstraint getMeshConstraint() {
        return effectiveMeshSizeConstraint;
    }

    /**
     * Associate a mesh constraint with this problem.
     *
     * @param effectiveMeshSizeConstraint
     */
    public void setMeshConstraint(EffectiveMeshSizeConstraint effectiveMeshSizeConstraint) throws RestoptException {
        if (this.hasMeshConstraint()) {
            throw new RestoptException("Only one mesh constraint can be associated with a restopt problem");
        }
        this.effectiveMeshSizeConstraint = effectiveMeshSizeConstraint;
    }

    /**
     * @return True if an iic constraint was associated with this problem.
     */
    public boolean hasIICConstraint() {
        return integralIndexOfConnectivityConstraint != null;
    }

    /**
     * @return The iic constraint associated with this problem.
     */
    public IntegralIndexOfConnectivityConstraint getIICConstraint() {
        return integralIndexOfConnectivityConstraint;
    }

    /**
     * Associate an iic constraint with this problem.
     *
     * @param integralIndexOfConnectivityConstraint
     */
    public void setIICConstraint(IntegralIndexOfConnectivityConstraint integralIndexOfConnectivityConstraint) throws RestoptException {
        if (this.hasIICConstraint()) {
            throw new RestoptException("Only one iic constraint can be associated with a restopt problem");
        }
        this.integralIndexOfConnectivityConstraint = integralIndexOfConnectivityConstraint;
    }

    /**
     * @return The total landscape area.
     */
    public int getLandscapeArea() {
        return grid.getNbUngroupedCells() + getNbLockedUpNonHabitatCells();
    }

    @Override
    public RestoptProblem self() {
        return this;
    }
}
