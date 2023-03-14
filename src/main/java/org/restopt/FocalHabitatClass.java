package org.restopt;

import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.util.stream.IntStream;

public class FocalHabitatClass {

    int classValue;
    RasterConnectivityFinder habGraph;
    int nonHabNonAcc;
    PartialRegularGroupedGrid grid;
    int[] availablePlanningUnits;

    public FocalHabitatClass(DataLoader data, int classValue, int accessibleVal, int nonHabitatValue) {

        this.classValue = classValue;

        int[] outPixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] < nonHabitatValue || data.getHabitatData()[i] == data.noDataHabitat)
                .toArray();

        int[] nonHabitatNonAccessiblePixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == nonHabitatValue && data.getAccessibleData()[i] != accessibleVal)
                .toArray();

        int[] habitatPixelsComp = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == classValue)
                .toArray();

        habGraph = new RasterConnectivityFinder(
                data.getHeight(), data.getWidth(),
                data.getHabitatData(), 1,
                Neighborhoods.FOUR_CONNECTED
        );
        nonHabNonAcc = nonHabitatNonAccessiblePixels.length;

        this.grid = new PartialRegularGroupedGrid(data.getHeight(), data.getWidth(), ArrayUtils.concat(outPixels, nonHabitatNonAccessiblePixels), habGraph);

        int[] nonHabitatPixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == nonHabitatValue)
                .toArray();

        availablePlanningUnits = IntStream.range(0, data.getAccessibleData().length)
                .filter(i -> data.getAccessibleData()[i] == accessibleVal && data.getHabitatData()[i] == nonHabitatValue)
                .map(i -> grid.getGroupIndexFromCompleteIndex(i))
                .toArray();

        System.out.println("Current landscape state loaded");
        System.out.println("    Habitat cells = " + habitatPixelsComp.length + " ");
        System.out.println("    Non habitat cells = " + nonHabitatPixels.length + " ");
        System.out.println("    Accessible non habitat cells = " + availablePlanningUnits.length + " ");
        System.out.println("    Out cells = " + outPixels.length);

    }
}
