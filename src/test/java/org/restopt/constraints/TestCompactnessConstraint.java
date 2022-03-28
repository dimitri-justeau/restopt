package org.restopt.constraints;

import org.restopt.BaseProblem;
import org.restopt.DataLoader;
import org.restopt.objectives.RestoptSolution;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestCompactnessConstraint {

    @Test
    public void testMaxCompactness() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postCompactnessConstraint(6);
        RestoptSolution sol = baseProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int[] pus = sol.getRestorationPlanningUnits();
        double[][] coords = new double[pus.length][];
        double[][] compCoords = baseProblem.getGrid().getCartesianCoordinates();
        for (int i = 0; i < pus.length; i++) {
            coords[i] = compCoords[baseProblem.getGrid().getUngroupedPartialIndex(pus[i])];
        }
        assertDiameter(coords, 0, 6);
    }

    @Test
    public void testMinMaxCompactness() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postCompactnessConstraint(2, 4);
        RestoptSolution sol = baseProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int[] pus = sol.getRestorationPlanningUnits();
        double[][] coords = new double[pus.length][];
        double[][] compCoords = baseProblem.getGrid().getCartesianCoordinates();
        for (int i = 0; i < pus.length; i++) {
            coords[i] = compCoords[baseProblem.getGrid().getUngroupedPartialIndex(pus[i])];
        }
        assertDiameter(coords, 2, 4);
    }

    private void assertDiameter(double[][] coords, double minDiameter, double maxDiameter) {
        boolean minDiamOk = false;
        for (int i = 0; i < coords.length; i++) {
            for (int j = i + 1; j < coords.length; j++) {
                double[] ci = coords[i];
                double[] cj = coords[j];
                double dist = Math.sqrt(Math.pow(cj[0] - ci[0], 2) + Math.pow(cj[1] - ci[1], 2));
                Assert.assertTrue(dist <= maxDiameter);
                if (dist >= minDiameter) {
                    minDiamOk = true;
                }
            }
        }
        Assert.assertTrue(minDiamOk);
    }
}
