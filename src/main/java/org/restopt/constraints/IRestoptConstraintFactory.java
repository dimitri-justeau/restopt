package org.restopt.constraints;

import org.restopt.RestoptProblem;
import org.restopt.exception.RestoptException;

import java.io.IOException;

public interface IRestoptConstraintFactory {

    RestoptProblem self();

    default void postNbComponentsConstraint(int minNbCC, int maxNbCC) {
        new NbComponentsConstraint(self(), minNbCC, maxNbCC).post();
    }

    default void postNbPatchesConstraint(int minNP, int maxNP) {
        new NbPatchesConstraint(self(), minNP, maxNP).post();
    }

    default void postCompactnessConstraint(double maxDiameter) {
        new CompactnessConstraint(self(), maxDiameter).post();
    }

    default void postCompactnessConstraint(double minDiameter, double maxDiameter) {
        new CompactnessConstraint(self(), minDiameter, maxDiameter).post();
    }

    default void postRestorableConstraint(int minAreaToRestore, int maxAreaToRestore, double minProportion) throws RestoptException {
        new RestorableAreaConstraint(self(), minAreaToRestore, maxAreaToRestore, minProportion).post();
    }

    default void postMinMeshConstraint(double minMesh, int precision) throws RestoptException {
        new EffectiveMeshSizeConstraint(self(), minMesh, self().getLandscapeArea(), precision).post();
    }

    default void postMinIICConstraint(double minIIC, int distanceThreshold, int precision) throws RestoptException {
        new IntegralIndexOfConnectivityConstraint(self(), minIIC, 1, distanceThreshold, precision).post();
    }

    default void postNoNewPatchConstraint() {
        new NoNewPatchConstraint(self()).post();
    }
}