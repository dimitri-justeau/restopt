package org.restopt.objectives;

import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.exception.RestoptException;

import java.util.List;

public interface IRestoptObjectiveFactory {

    RestoptProblem self();

    // --------------- //
    // SINGLE SOLUTION //
    // --------------- //

    default RestoptSolution findSolution(int timeLimit, boolean verbose) throws RestoptException {
        return findSolutions(1, timeLimit, verbose).get(0);
    }

    default RestoptSolution maximizeMESH(int precision, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        return maximizeMESH(1, precision, timeLimit, optimalityGap, verbose).get(0);
    }

    default RestoptSolution maximizeIIC(int precision, int distanceThreshold, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        return maximizeIIC(1, precision, distanceThreshold, timeLimit, optimalityGap, verbose).get(0);
    }

    default RestoptSolution maximizeMinRestore(int timeLimit, double optimalityGap, boolean verbose) throws Exception {
        return maximizeMinRestore(1, timeLimit, optimalityGap, verbose).get(0);
    }

    default RestoptSolution maximizeNbPUS(int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        return maximizeNbPUS(1, timeLimit, optimalityGap, verbose).get(0);
    }

    default RestoptSolution minimizeNbPUS(int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        return minimizeNbPUS(1, timeLimit, optimalityGap, verbose).get(0);
    }

    // --------------- //
    // MULTI SOLUTIONS //
    // --------------- //

    default List<RestoptSolution> findSolutions(int nbSolutions, int timeLimit, boolean verbose) throws RestoptException {
        NoOptimizationObjective obj = new NoOptimizationObjective(self(), timeLimit, verbose);
        return obj.findOptimalSolution(nbSolutions, 0);
    }

    default List<RestoptSolution> maximizeMESH(int nbSolutions, int precision, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        EffectiveMeshSizeObjective obj = new EffectiveMeshSizeObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }

    default List<RestoptSolution> maximizeIIC(int nbSolutions, int precision, int distanceThreshold, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        IntegralIndexOfConnectivityObjective obj = new IntegralIndexOfConnectivityObjective(self(), timeLimit, verbose, true, precision, distanceThreshold);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }

    default List<RestoptSolution> maximizeMinRestore(int nbSolutions, int timeLimit, double optimalityGap, boolean verbose) throws Exception {
        MinRestoreObjective obj = new MinRestoreObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }

    default List<RestoptSolution> minimizeMinRestore(int nbSolutions, int timeLimit, double optimalityGap, boolean verbose) throws Exception {
        MinRestoreObjective obj = new MinRestoreObjective(self(), timeLimit, verbose, false);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }

    default List<RestoptSolution> maximizeNbPUS(int nbSolutions, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        NbPlanningUnitsObjective obj = new NbPlanningUnitsObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }

    default List<RestoptSolution> minimizeNbPUS(int nbSolutions, int timeLimit, double optimalityGap, boolean verbose) throws RestoptException {
        NbPlanningUnitsObjective obj = new NbPlanningUnitsObjective(self(), timeLimit, verbose, false);
        return obj.findOptimalSolution(nbSolutions, optimalityGap);
    }
}
