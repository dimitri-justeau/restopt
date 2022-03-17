package org.restopt.objective;

import org.restopt.BaseProblem;

import java.io.IOException;

public interface IObjectiveFactory {

    BaseProblem self();

    default boolean findSolution(String outputPath, int timeLimit) throws IOException {
        return findSolution(outputPath, timeLimit, true);
    }

    default boolean findSolution(String outputPath, int timeLimit, boolean verbose) throws IOException {
        NoOptimizationObjective obj = new NoOptimizationObjective(self(), timeLimit, verbose);
        return obj.findOptimalSolution(outputPath);
    }

    default boolean maximizeMESH(int precision, String outputPath, int timeLimit) throws IOException {
        return maximizeMESH(precision, outputPath, timeLimit, true);
    }

    default boolean maximizeMESH(int precision, String outputPath, int timeLimit, boolean verbose) throws IOException {
        EffectiveMeshSizeObjective obj = new EffectiveMeshSizeObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution(outputPath);
    }

    default boolean maximizeIIC(int precision, String outputPath, int timeLimit) throws IOException {
        return maximizeIIC(precision, outputPath, timeLimit, true);
    }

    default boolean maximizeIIC(int precision, String outputPath, int timeLimit, boolean verbose) throws IOException {
        IntegralIndexOfConnectivityObjective obj = new IntegralIndexOfConnectivityObjective(self(), timeLimit, verbose, true, precision);
        return obj.findOptimalSolution(outputPath);
    }
}
