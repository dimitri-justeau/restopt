package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.objectives.RestoptSolution;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestEffectiveMeshSizeObjective {

    @Test
    public void testEffectiveMeshSizeObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90, 110, 0.7);
        Path temp = Files.createTempFile("test_mesh2", "");
        RestoptSolution sol = restoptProblem.maximizeMESH(3, 30, true);
        sol.export(temp.toString(), true);
        Files.delete(temp);
    }
}
