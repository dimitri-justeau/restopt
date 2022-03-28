package org.restopt;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.restopt.exception.RestoptException;
import org.restopt.objectives.RestoptSolution;
import org.restopt.raster.RasterReader;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSolve {

    @Test
    public void testSolveMESH() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterReader r = new RasterReader(cell_area);
        int[] cellArea = r.readAsIntArray();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 1);
//        baseProblem.postNbComponentsConstraint(1, 1);
        //baseProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90, 110, cellArea, 0.7);
        RestoptSolution sol = restoptProblem.maximizeNbPUS(30, true);
        sol.printSolutionInfos();
    }

    @Test
    public void testSolveMESH2() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_mesh2", "");
        RestoptSolution sol = restoptProblem.maximizeMESH(3, 30, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }

    @Test
    public void testSolveIIC() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 1);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_iic", "");
        RestoptSolution sol = restoptProblem.maximizeIIC(3, 0, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }

    @Test
    public void testSolveIIC2() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_iic2", "");
        RestoptSolution sol = restoptProblem.maximizeIIC(3, 0, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }
}
