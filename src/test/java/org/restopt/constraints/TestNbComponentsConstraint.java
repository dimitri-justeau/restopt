package org.restopt.constraints;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.restopt.RestoptProblem;
import org.restopt.DataLoader;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.objectives.RestoptSolution;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestNbComponentsConstraint {

    @Test
    public void testConnected() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        UndirectedGraph g = sol.getRestorationGraph();
        ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(g);
        cf.findAllCC();
        Assert.assertEquals(cf.getNBCC(), 1);
    }

    @Test
    public void testSeveralComponent() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(2, 2);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        UndirectedGraph g = sol.getRestorationGraph();
        ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(g);
        cf.findAllCC();
        Assert.assertEquals(cf.getNBCC(), 2);
    }

    @Test
    public void testInterval() throws IOException, ContradictionException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(2, 10);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        UndirectedGraph g = sol.getRestorationGraph();
        ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(g);
        cf.findAllCC();
        Assert.assertTrue(cf.getNBCC() >= 2);
        Assert.assertTrue(cf.getNBCC() <= 10);
    }
}
