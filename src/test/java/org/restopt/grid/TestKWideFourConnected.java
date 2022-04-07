package org.restopt.grid;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.restopt.grid.neighborhood.regular.square.KWideFourConnected;
import org.restopt.grid.regular.square.RegularSquareGrid;
import org.testng.annotations.Test;

public class TestKWideFourConnected {

    @Test
    public void testMidPointCircle() {
        int width = 10;
        int height = 10;
        RegularSquareGrid grid = new RegularSquareGrid(height, width);
        ISet circle = KWideFourConnected.discreteDisk(3, 3, 1, grid);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (circle.contains(grid.getIndexFromCoordinates(i, j))) {
                    System.out.print(" 1 ");
                } else {
                    System.out.print(" 0 ");
                }
            }
            System.out.println();
        }
    }
}
