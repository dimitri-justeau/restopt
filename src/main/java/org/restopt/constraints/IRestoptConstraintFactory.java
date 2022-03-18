package org.restopt.constraints;

import org.restopt.BaseProblem;
import org.restopt.objectives.EffectiveMeshSizeObjective;
import org.restopt.objectives.IntegralIndexOfConnectivityObjective;
import org.restopt.objectives.NoOptimizationObjective;

import java.io.IOException;

public interface IRestoptConstraintFactory {

    BaseProblem self();

    default void postNbComponentsConstraint(int minNbCC, int maxNbCC) {
        new NbComponentsConstraint(self(), minNbCC, maxNbCC).post();
    }

    default void postCompactnessConstraint(double maxDiameter) {
        new CompactnessConstraint(self(), maxDiameter).post();
    }

    default void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, int cellArea, double minProportion) {
        new RestorableAreaConstraint(self(), minAreaToRestore, maxAreaToRestore, cellArea, minProportion).post();
    }

}