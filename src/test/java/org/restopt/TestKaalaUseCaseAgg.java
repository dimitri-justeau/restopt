package org.restopt;

import org.restopt.choco.LandscapeIndicesUtils;
import org.testng.annotations.Test;

public class TestKaalaUseCaseAgg {

    @Test
    public void test() throws Exception {
        int[] habitat = new int[] {
                -1, 1, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0
        };
        DataLoader data = new DataLoader(habitat, 0, -1.0, 5, 4);
        RestoptProblem restoptProblem = new RestoptProblem(data, 0, 2);
        RestoptSolution sol = restoptProblem.findSolution(10, true);
    }

    @Test
    public void testHiRes() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/kaala_use_case/habitat_hr.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/kaala_use_case/restorable_hr.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/kaala_use_case/available_hr.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/kaala_use_case/cell_area_hr.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 0, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(20);
        double meshLB = LandscapeIndicesUtils.effectiveMeshSize(restoptProblem.getHabitatGraph(), restoptProblem.getLandscapeArea());
        System.out.println(meshLB * Math.pow(10, 4));
        restoptProblem.postMinMeshConstraint(800, 4);
        RestoptSolution sol = restoptProblem.minimizeMinRestore(
                1, 1, 30, 0, true, "ACTIVITY_BASED"
        ).get(0);
        System.out.println(sol.getCharacteristics());
        /*SolutionExporter exp = new SolutionExporter(
                sol,
                "/home/justeau-allaire/GIS/1.csv",
                "/home/justeau-allaire/GIS/1.tif",
                -1
        );
        exp.generateRaster();*/
    }
}
