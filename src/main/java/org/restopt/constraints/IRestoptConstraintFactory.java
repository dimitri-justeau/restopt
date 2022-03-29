package org.restopt.constraints;

import org.restopt.RestoptProblem;
import org.restopt.exception.RestoptException;

import java.io.IOException;

public interface IRestoptConstraintFactory {

    RestoptProblem self();

    default void postNbComponentsConstraint(int minNbCC, int maxNbCC) {
        new NbComponentsConstraint(self(), minNbCC, maxNbCC).post();
    }

    default void postCompactnessConstraint(double maxDiameter) {
        new CompactnessConstraint(self(), maxDiameter).post();
    }

    default void postCompactnessConstraint(double minDiameter, double maxDiameter) {
        new CompactnessConstraint(self(), minDiameter, maxDiameter).post();
    }

    default void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, double minProportion) throws IOException, RestoptException {
        new RestorableAreaConstraint(self(), minAreaToRestore, maxAreaToRestore, minProportion).post();
    }
}