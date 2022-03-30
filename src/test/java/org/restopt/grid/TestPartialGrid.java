package org.restopt.grid;

import org.restopt.grid.regular.square.PartialRegularSquareGrid;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class TestPartialGrid {

    @Test
    public void testPartialGrid() {
        int width = 10;
        int height = 10;
        int[] data = new int[] { // 1 values are out, 2 are groups
                1, 1, 1, 1, 1, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        int[] out = IntStream.range(0, data.length).filter(i -> data[i] == 1).toArray();
        PartialRegularSquareGrid grid = new PartialRegularSquareGrid(height, width, out);
        Assert.assertEquals(grid.getDiscardSet().size(), 15);
        int[] discard = grid.getDiscardSet().toArray();
        Arrays.sort(discard);
        Assert.assertEquals(discard, new int[] {0, 1, 2, 3, 4, 10, 11, 12, 13, 14, 20, 21, 22, 23, 34});
        Assert.assertEquals(grid.getCompleteIndex(0), 5);
        Assert.assertEquals(grid.getCompleteIndex(15), 5);
        Assert.assertEquals(grid.getCompleteIndex(41), 26);
        Assert.assertTrue(Arrays.equals(grid.getCoordinatesFromIndex(26), new int[] {5, 1}));
    }
}
