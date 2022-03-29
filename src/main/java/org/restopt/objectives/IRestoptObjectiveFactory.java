package org.restopt.objectives;

import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;

import java.io.IOException;

public interface IRestoptObjectiveFactory {

    RestoptProblem self();

    default RestoptSolution findSolution(int timeLimit, boolean verbose) throws IOException {
        NoOptimizationObjective obj = new NoOptimizationObjective(self(), timeLimit, verbose);
        return obj.findOptimalSolution();
    }

    default RestoptSolution maximizeMESH(int precision, int timeLimit, boolean verbose) {
        EffectiveMeshSizeObjective obj = new EffectiveMeshSizeObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution();
    }

    default RestoptSolution maximizeIIC(int precision, int timeLimit, boolean verbose) {
        IntegralIndexOfConnectivityObjective obj = new IntegralIndexOfConnectivityObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution();
    }

    default RestoptSolution maximizeMinRestore(int timeLimit, boolean verbose) throws Exception {
        MinRestoreObjective obj = new MinRestoreObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution();
    }

    default RestoptSolution maximizeNbPUS(int timeLimit, boolean verbose) {
        NbPlanningUnitsObjective obj = new NbPlanningUnitsObjective(self(), timeLimit, verbose, true);
        return obj.findOptimalSolution();
    }
}
