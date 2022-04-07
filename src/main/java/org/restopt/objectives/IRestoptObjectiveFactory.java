package org.restopt.objectives;

import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.exception.RestoptException;

import java.io.IOException;
import java.util.List;

public interface IRestoptObjectiveFactory {

    RestoptProblem self();

    // --------------- //
    // SINGLE SOLUTION //
    // --------------- //

    default RestoptSolution findSolution(int timeLimit, boolean verbose) throws IOException {
        return findSolutions(1, timeLimit, verbose).get(0);
    }

    default RestoptSolution maximizeMESH(int precision, int timeLimit, boolean verbose) {
        return maximizeMESH(1, precision, timeLimit, verbose).get(0);
    }

    default RestoptSolution maximizeIIC(int precision, int distanceThreshold, int timeLimit, boolean verbose) throws RestoptException {
        return maximizeIIC(1, precision, distanceThreshold, timeLimit, verbose).get(0);
    }

    default RestoptSolution maximizeMinRestore(int timeLimit, boolean verbose) throws Exception {
        return maximizeMinRestore(1, timeLimit, verbose).get(0);
    }

    default RestoptSolution maximizeNbPUS(int timeLimit, boolean verbose) {
        return maximizeNbPUS(1, timeLimit, verbose).get(0);
    }

    // --------------- //
    // MULTI SOLUTIONS //
    // --------------- //

    default List<RestoptSolution> findSolutions(int nbSolutions, int timeLimit, boolean verbose) {
        NoOptimizationObjective obj = new NoOptimizationObjective(self(), timeLimit, verbose);
        return obj.findOptimalSolution(nbSolutions);
    }

    default List<RestoptSolution> maximizeMESH(int nbSolutions, int precision, int timeLimit, boolean verbose) {
        EffectiveMeshSizeObjective obj = new EffectiveMeshSizeObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution(nbSolutions);
    }

    default List<RestoptSolution> maximizeIIC(int nbSolutions, int precision, int distanceThreshold, int timeLimit, boolean verbose) throws RestoptException {
        IntegralIndexOfConnectivityObjective obj = new IntegralIndexOfConnectivityObjective(self(), timeLimit, verbose, true, precision, distanceThreshold);
        return obj.findOptimalSolution(nbSolutions);
    }

    default List<RestoptSolution> maximizeMinRestore(int nbSolutions, int timeLimit, boolean verbose) throws Exception {
        MinRestoreObjective obj = new MinRestoreObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution(nbSolutions);
    }

    default List<RestoptSolution> maximizeNbPUS(int nbSolutions, int timeLimit, boolean verbose) {
        NbPlanningUnitsObjective obj = new NbPlanningUnitsObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution(nbSolutions);
    }
}
