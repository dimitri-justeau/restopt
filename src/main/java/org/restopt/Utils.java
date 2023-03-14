package org.restopt;

import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedAggGrid;

import java.util.stream.IntStream;

public class Utils {

    public static int[] getAggregatedPUs(DataLoader data, int accessibleVal, int aggregationFactor) {

        int[] outPixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] <= -1 || data.getHabitatData()[i] == data.noDataHabitat)
                .toArray();

        int[] nonHabitatNonAccessiblePixels = IntStream.range(0, data.getHabitatData().length)
                .filter(i -> data.getHabitatData()[i] == 0 && data.getAccessibleData()[i] != accessibleVal)
                .toArray();

        RasterConnectivityFinder rConn = new RasterConnectivityFinder(
                data.getHeight(), data.getWidth(),
                data.getHabitatData(), 1,
                Neighborhoods.FOUR_CONNECTED
        );

        PartialRegularGroupedAggGrid grid = new PartialRegularGroupedAggGrid(
                data.getHeight(), data.getWidth(),
                ArrayUtils.concat(outPixels, nonHabitatNonAccessiblePixels),
                rConn, aggregationFactor
        );

        int[] pus = new int[data.getHabitatData().length];
        for (int i = 0; i < pus.length; i++) {
            pus[i] = -1;
        }

        for (int i = 0; i < grid.getNbAggregates(); i++) {
            int[] agg = grid.getAggregateCompleteIndices(i + grid.getNbGroups());
            for (int j : agg) {
                pus[j] = i;
            }
        }

        return pus;
    }
}
