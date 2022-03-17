package org.restopt;

import org.chocosolver.solver.exception.ContradictionException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestSolve {

    @Test
    public void testSolveMESH() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 1);
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_mesh", "");
        baseProblem.maximizeMESH(3, temp.toString(), 0);
        Files.delete(temp);
    }

    @Test
    public void testSolveMESH2() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_mesh2", "");
        baseProblem.maximizeMESH(3, temp.toString(), 0);
        Files.delete(temp);
    }

    @Test
    public void testSolveIIC() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 1);
//        baseProblem.getModel().getSolver().showContradiction();
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_iic", "");
        baseProblem.maximizeIIC(3, temp.toString(), 0);
        Files.delete(temp);
    }

    @Test
    public void testSolveIIC2() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_iic2", "");
        baseProblem.maximizeIIC(3, temp.toString(), 0);
        Files.delete(temp);
    }
}
