package org.restopt.constraints;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.exception.RestoptException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestNbPatchesConstraint {

    @Test
    public void test() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbPatchesConstraint(70, 80);
        RestoptSolution sol = restoptProblem.findSolution(10, true);
        sol.printSolutionInfos();
        UndirectedGraph g = sol.getHabitatGraph();
        ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(g);
        cf.findAllCC();
        Assert.assertTrue(cf.getNBCC() >= 70 && cf.getNBCC() <= 80);
    }
}
