package org.restopt.cli;

import org.chocosolver.solver.exception.ContradictionException;
import org.restopt.BaseProblem;
import org.restopt.DataLoader;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "restopt",
        description = "Find connected and compact areas maximizing the effective mesh size (MESH) or the integral index of connectivity (IIC).",
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class Main {

    private enum Objective {
        MESH,
        IIC;
    }

    @CommandLine.Option(
            names = "-objective",
            description = "Optimization objective, will be maximized (MESH or IIC).",
            required = true
    )
    Objective objective;

    @CommandLine.Option(
            names = "-habitat",
            description = "Path to habitat binary raster.",
            required = true
    )
    String habitatBinaryRasterPath;

    @CommandLine.Option(
            names = "-accessible",
            description = "Path to accessible areas raster (accessible cells are identified with accessibleValue," +
                    " which is by default set to 1. If all degraded are accessible, set accessibleValue to 0 and use" +
                    " the habitat raster as argument).",
            required = true
    )
    String accessibleBinaryRasterPath;

    @CommandLine.Option(
            names = "-restorable",
            description = "Path to restorable areas raster (values must be between 0 and cellArea).",
            required = true
    )
    String restorableBinaryRasterPath;

    @CommandLine.Option(
            names = "-o",
            description = "Output path for generated files. Two files are generated: one raster (.tif) representing" +
                    "the solution, and a csv file containing its characteristics. File extensions are" +
                    "automatically added to the end of the output path.",
            required = true
    )
    String outputPath;

    @CommandLine.Option(
            names = "-cellArea",
            description = "Total area of a cell.",
            required = true
    )
    int cellArea;

    @CommandLine.Option(
            names = "-maxNbCC",
            description = "Maximum number of connected components (default is 1).",
            defaultValue = "1"
    )
    int maxNbCC;

    @CommandLine.Option(
            names = "-maxDiam",
            description = "Maximum diameter, in cell width.",
            required = true
    )
    int maxDiam;

    @CommandLine.Option(
            names = "-minRestore",
            description = "Minimum area to restore (between 0 and maxRestore).",
            required = true
    )
    int minRestore;

    @CommandLine.Option(
            names = "-minProportion",
            description = "Minimum habitat proportion need to restore a cell (between 0 and 1).",
            required = true
    )
    double minProportion;

    @CommandLine.Option(
            names = "-maxRestore",
            description = "Maximum area to restore (greater than minRestore).",
            required = true
    )
    int maxRestore;

    @CommandLine.Option(
            names = "-precision",
            description = "The solver optimizes integer variables, so MESH or IIC is multiplied by 10^<precision>" +
                    "and restored as a real rounded to <precision> numbers after the decimal point.",
            defaultValue = "4"
    )
    int precision;

    @CommandLine.Option(
            names = "-timeLimit",
            description = "Time limit for optimization (in seconds).",
            defaultValue = "0"
    )
    int timeLimit;

//    @CommandLine.Option(
//            names = "-lns",
//            description = "Use Large Neighbourhood Search (LNS). " +
//            "Note that the solver cannot provide optimality guarantee when LNS is used. LNS must thus be used with a time limit."
//    )
//    boolean lns;

    @CommandLine.Option(
            names = "-accessibleValue",
            description = "Value of accessible cells in accessible areas raster.",
            defaultValue = "1"
    )
    int accessibleVal;

    public static void main(String[] args) throws IOException, ContradictionException {

        if (args.length == 0) {
            new CommandLine(new Main()).usage(System.out);
            return;
        }

        Main main = new Main();
        CommandLine cli = new CommandLine(main);
        cli.parseArgs(args);
        if (cli.isUsageHelpRequested()) {
            cli.usage(System.out);
            return;
        }

        assert main.maxNbCC > 0;
        assert main.maxDiam > 0;
        assert main.maxRestore > 0;
        assert main.minProportion > 0 && main.minProportion <= 1;

        DataLoader data = new DataLoader(
                main.habitatBinaryRasterPath,
                main.accessibleBinaryRasterPath,
                main.restorableBinaryRasterPath
        );

        BaseProblem baseProblem;
        baseProblem = new BaseProblem(data, main.accessibleVal);
        baseProblem.postNbComponentsConstraint(1, main.maxNbCC);
        baseProblem.postCompactnessConstraint(main.maxDiam);
        baseProblem.postRestorableConstraint(main.minRestore, main.maxRestore, main.cellArea, main.minProportion);
        if (main.objective == Objective.MESH) {
            baseProblem.maximizeMESH(main.precision, main.outputPath, main.timeLimit, false);
        } else {
            if (main.objective == Objective.IIC) {
                baseProblem.maximizeIIC(main.precision, main.outputPath, main.timeLimit, false);
            }
        }
    }
}
