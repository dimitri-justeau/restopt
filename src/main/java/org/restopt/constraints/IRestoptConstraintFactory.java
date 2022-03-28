package org.restopt.constraints;

import org.restopt.BaseProblem;

import java.io.IOException;

public interface IRestoptConstraintFactory {

    BaseProblem self();

    default void postNbComponentsConstraint(int minNbCC, int maxNbCC) {
        new NbComponentsConstraint(self(), minNbCC, maxNbCC).post();
    }

    default void postCompactnessConstraint(double maxDiameter) {
        new CompactnessConstraint(self(), maxDiameter).post();
    }

    default void postCompactnessConstraint(double minDiameter, double maxDiameter) {
        new CompactnessConstraint(self(), minDiameter, maxDiameter).post();
    }

    default void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, int cellArea, double minProportion) throws IOException {
        new RestorableAreaConstraint(self(), minAreaToRestore, maxAreaToRestore, cellArea, minProportion).post();
    }

    default void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, int[] cellArea, double minProportion) throws IOException {
        new RestorableAreaConstraint(self(), minAreaToRestore, maxAreaToRestore, cellArea, minProportion).post();
    }

}