package org.restopt;

import org.testng.annotations.Test;


public class TestKaalaUseCase {

    @Test
    public void testKaala() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/kaala_use_case/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/kaala_use_case/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/kaala_use_case/available.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/kaala_use_case/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 0);
        //restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(11);
        restoptProblem.postMinMeshConstraint(275.2538, 3);
        RestoptSolution sol = restoptProblem.minimizeMinRestore(0.75, 1, 120, 0, true, "DEFAULT").get(0);
        System.out.println(sol.getDiameter());
    }

}
