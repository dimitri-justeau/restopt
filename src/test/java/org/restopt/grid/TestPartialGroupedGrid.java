package org.restopt.grid;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.restopt.RasterConnectivityFinder;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class TestPartialGroupedGrid {

    @Test
    public void testPartialGroupedGrid() {
        int width = 10;
        int height = 10;
        int[] data = new int[] { // 1 values are out, 2 are groups
                1, 1, 1, 2, 2, 0, 0, 0, 0, 0,
                1, 0, 0, 2, 2, 0, 0, 1, 1, 0,
                0, 0, 0, 2, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 0, 0, 2, 0,
                0, 0, 0, 1, 1, 1, 0, 0, 0, 0,
                0, 0, 0, 2, 2, 0, 0, 0, 0, 0,
                0, 0, 0, 2, 2, 0, 0, 2, 2, 0,
                0, 0, 1, 2, 2, 0, 1, 1, 1, 0,
                0, 0, 0, 0, 0, 0, 1, 1, 1, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        int[] out = IntStream.range(0, data.length).filter(i -> data[i] == 1).toArray();
        int[] groups = IntStream.range(0, data.length).filter(i -> data[i] == 2).toArray();
        RegularSquareGrid regGrid = new RegularSquareGrid(height, width);
        RasterConnectivityFinder g = new RasterConnectivityFinder(
                height, width, data, 2, Neighborhoods.FOUR_CONNECTED
        );
        PartialRegularGroupedGrid grid = new PartialRegularGroupedGrid(height, width, out, g);
        Assert.assertEquals(grid.getNbGroups(), 4);
        // 4 groups totalizing 14 cells -> -10 cells from the original grid
        Assert.assertEquals(grid.getNbCells(), height * width - out.length - 10);
        Assert.assertEquals(grid.getPartialIndex(4), 1);
        try {
            grid.getUngroupedCompleteIndex(0);
            Assert.fail();
        } catch (RuntimeException e) {}
        Assert.assertEquals(grid.getUngroupedCompleteIndex(4), 5);
        Assert.assertEquals(grid.getUngroupedPartialIndex(4), 2);
        for (int i = 0; i < grid.getNbGroups(); i++) {
            ISet s = grid.getGroup(i);
            int[] a = s.toArray();
            Arrays.sort(a);
            a = IntStream.of(a).map(v -> grid.getCompleteIndex(v)).toArray();
            switch (s.size()) {
                case 1:
                    Assert.assertEquals(a[0], 38);
                    break;
                case 2:
                    Assert.assertTrue(Arrays.equals(a, new int[] {67, 68}));
                    break;
                case 5:
                    Assert.assertTrue(Arrays.equals(a, new int[] {3, 4, 13, 14, 23}));
                    break;
                case 6:
                    Assert.assertTrue(Arrays.equals(a, new int[] {53, 54, 63, 64, 73, 74}));
                    break;
                default:
                    Assert.fail();
            }
        }
    }
}
