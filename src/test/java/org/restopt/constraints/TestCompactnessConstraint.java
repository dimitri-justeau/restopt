package org.restopt.constraints;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.exception.RestoptException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestCompactnessConstraint {

    @Test
    public void testMaxCompactness() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postCompactnessConstraint(6);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int[] pus = sol.getRestorationPlanningUnits();
        double[][] coords = new double[pus.length][];
        //double[][] compCoords = restoptProblem.getGrid().getCartesianCoordinates();
        for (int i = 0; i < pus.length; i++) {
            coords[i] = restoptProblem.getGrid().getCartesianCoordinates(pus[i]);
            //coords[i] = compCoords[restoptProblem.getGrid().getUngroupedPartialIndex(pus[i])];
        }
        assertDiameter(coords, 0, 6);
    }

    @Test
    public void testMinMaxCompactness() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postCompactnessConstraint(2, 4);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int[] pus = sol.getRestorationPlanningUnits();
        double[][] coords = new double[pus.length][];
        //double[][] compCoords = restoptProblem.getGrid().getCartesianCoordinates();
        for (int i = 0; i < pus.length; i++) {
            coords[i] = restoptProblem.getGrid().getCartesianCoordinates(pus[i]);
            //coords[i] = compCoords[restoptProblem.getGrid().getUngroupedPartialIndex(pus[i])];
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
