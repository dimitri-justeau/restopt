package org.restopt.constraints;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.restopt.DataLoader;
import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.exception.RestoptException;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class TestNoNewPatchConstraint {

    @Test
    public void test1() throws IOException, RestoptException {
        int[] habitat = new int[] {
                0, 0, 0, -1, 0, 1,
                -1, -1, -1, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                1, 0, 0, 0, 1, 1,
                0, 0, 0, 0, 1, 1
        };
        DataLoader data = new DataLoader(habitat, 0, -1.0, 6, 5);
        RestoptProblem restoptProblem = new RestoptProblem(data, 0, 1);
        restoptProblem.postNoNewPatchConstraint();
        // Get initial number of patches
        int np = restoptProblem.grid.getNbGroups();
        // Solve
        List<RestoptSolution> sols = restoptProblem.findSolutions(-1, 0, true);
        for (RestoptSolution sol : sols) {
            Assert.assertTrue(sol.getNbPatches() <= np);
            ISet nodes = sol.getRestorationGraph().getNodes();
            PartialRegularGroupedGrid grid = (PartialRegularGroupedGrid) restoptProblem.grid;
            Assert.assertFalse(nodes.contains(grid.getGroupIndexFromCompleteIndex(0)));
            Assert.assertFalse(nodes.contains(grid.getGroupIndexFromCompleteIndex(1)));
            Assert.assertFalse(nodes.contains(grid.getGroupIndexFromCompleteIndex(2)));
        }
    }

    @Test
    public void test2() throws IOException, RestoptException {
        int[] habitat = new int[] {
                0, 1,
                0, 0,
        };
        DataLoader data = new DataLoader(habitat, 0, -1.0, 2, 2);
        RestoptProblem restoptProblem = new RestoptProblem(data, 0, 1);
        restoptProblem.postNoNewPatchConstraint();
        // Get initial number of patches
        int np = restoptProblem.grid.getNbGroups();
        // Solve
        List<RestoptSolution> sols = restoptProblem.findSolutions(-1, 0, true);
        for (RestoptSolution sol : sols) {
            Assert.assertTrue(sol.getNbPatches() <= np);
        }
        Assert.assertEquals(sols.size(), 7);
    }
}
