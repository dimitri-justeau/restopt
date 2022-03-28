package org.restopt;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.restopt.objectives.RestoptSolution;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestSolve {

    @Test
    public void testSolveMESH() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 1);
//        baseProblem.postNbComponentsConstraint(1, 1);
        //baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_mesh", "");
        //baseProblem.maximizeMESH(3, temp.toString(), 720);
        int[] ub = baseProblem.getRestoreSetVar().getUB().toArray();
        List<Integer> a = new ArrayList<>();
        for (int i = 0; i < ub.length; i++) {
            a.add(i);
        }
        a.sort((i, j) -> {
            int ic = baseProblem.getGrid().getUngroupedCompleteIndex(ub[i]);
            int jc = baseProblem.getGrid().getUngroupedCompleteIndex(ub[j]);
            int ir = (int) Math.round(baseProblem.getData().getRestorableData()[ic]);
            int jr = (int) Math.round(baseProblem.getData().getRestorableData()[jc]);
            ir = ir < 0 ? 0 : ir;
            jr = jr < 0 ? 0 : jr;
            return (ir - jr);
        });
        BoolVar[] boolVars = new BoolVar[ub.length];
        for (int i = 0; i < a.size(); i++) {
            boolVars[i] = baseProblem.getModel().setBoolView(baseProblem.getRestoreSetVar(), ub[a.get(i)]);
        }
        baseProblem.getModel().getSolver().setSearch(Search.inputOrderUBSearch(boolVars));
        RestoptSolution sol = baseProblem.maximizeNbPUS(240, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }

    @Test
    public void testSolveMESH2() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_mesh2", "");
        RestoptSolution sol = baseProblem.maximizeMESH(3, 30, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }

    @Test
    public void testSolveIIC() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 1);
        baseProblem.postNbComponentsConstraint(1, 1);
        baseProblem.postCompactnessConstraint(6);
        baseProblem.postRestorableConstraint(90, 110, 23, 0.7);
        Path temp = Files.createTempFile("test_iic", "");
        RestoptSolution sol = baseProblem.maximizeIIC(3, 0, true);
        sol.export(temp.toString(), true);
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
        RestoptSolution sol = baseProblem.maximizeIIC(3, 0, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }
}
