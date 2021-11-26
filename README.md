# restopt - Ecological restoration planning to reduce habitat fragmentation and increase habitat connectivity

[![Java CI with Maven](https://github.com/dimitri-justeau/restopt/actions/workflows/maven.yml/badge.svg)](https://github.com/dimitri-justeau/restopt/actions/workflows/maven.yml) [![codecov](https://codecov.io/gh/dimitri-justeau/restopt/branch/master/graph/badge.svg?token=O0TFEUGPGF)](https://codecov.io/gh/dimitri-justeau/restopt) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/84acbf275a6a4abeb65b712993004652)](https://www.codacy.com/gh/dimitri-justeau/restopt/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dimitri-justeau/restopt&amp;utm_campaign=Badge_Grade) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

`restopt` is an ecological restoration planning software which is specifically designed to identify connected,
accessible, and compact areas for ecological restoration, with an emphasis on reducing habitat fragmentation and
increasing habitat connectivity.

For a detailed description of the problem solved by `restopt`, please refer to the following research article:

"*Constrained optimization of landscape indices in conservation planning to support ecological restoration in New
Caledonia*", by Dimitri Justeau-Allaire, Ghislain Vieilledent, Nicolas Rinck, Philippe Vismara, Xavier Lorca, and
Philippe Birnbaum (https://besjournals.onlinelibrary.wiley.com/doi/full/10.1111/1365-2664.13803).

`restopt` is currently distributed as .jar command-line interface. To execute this jar, the Java Runtime Environment (
JRE) is needed (version 8 minimum), download and installation instructions for the JRE are available
here: https://www.oracle.com/java/technologies/javase-downloads.html, or here: https://openjdk.java.net/install/.

Last release: https://github.com/dimitri-justeau/restopt/releases/tag/1.0-beta

An R interface is under development: stay tuned !

To execute restopt, you need three rasters that must have the same extents and resolutions:

- `-habitat`: a binary habitat raster, with value 1 for habitat, 0 for non-habitat, and -1 for cells that must not be
  considered in the problem. It is important to discard these cells as they can alter the value of the indices, slow
  down the solving time, and increase the amount of memory needed.

- `-restorable`: a quantitative raster corresponding to the amount of restorable area in each cell. The values must be
  integer between 0 and `cellArea`, a parameter which corresponds to the total area of a raster cell.

- `accessible`: a raster corresponding to the accessible cells (those that can be considered for restoration). These
  cells are identified by the value of the parameter `accessibleValue`. If every cell are accessible, use the binary
  habitat raster with `accessibleValue = 1`.

The following parameters are also required:

- `-objective`: the index to maximize, `MESH` or `IIC`.

- `-cellArea`: the total area of a raster cell.

- `-o`: the output path for generated files. Use a path without extension (e.g. `/home/user/restoptResults/result`), the
  program will generate a tif raster file of the solution (e.g. `/home/user/restoptResults/result.tif`), and a csv file
  containing the characteristics of the solution (e.g. `/home/user/restoptResults/result.csv`).

- `-maxDiam`: the maximum allowed diameter of the restored area, in cell edge length.

- `-minRestore`: the minimum area to restore (between 0 and `maxRestore`), in surface units (must be the same as the
  restorable area raster).

- `-maxRestore`: the maximum area to restore (greater than `minRestore`).

- `-minProportion`: the minimum habitat proportion needed to consider a cell as restored (between 0 and 1).

The following parameters are optional :

- `-maxNbCC`: the maximum number of allowed connected components in the restored region. Default is 1, i.e. fully
  connected.

- `-precision`: the solver optimizes integer variables, thus the maximized index is multiplied by 10e<precision> and
  after optimization restored as a real rounded to <precision> numbers after the decimal point. Default is 4.

- `-accessibleValue`: the value of accessible cells in the corresponding raster. Default is 1.

- `-timeLimit`: time limit for optimization, in seconds. Default is no time limit (terminate when a solution is proved
  optimal).

Input data corresponding to the article case study is provided in the restopt download archive. In the accessible areas
raster, value 1 corresponds to accessible cells in Borendy, and value 2 corresponds to accessible cells in Unia. Below
is a usage example for finding the optimal connected and compact (maximum diameter ~1500 m) area to maximize MESH in
Unia:

    java -jar restopt.jar -objective MESH -habitat /home/user/habitat.tif -accessible /home/user/accessible.tif -restorable /home/user/restorable.tif -cellArea 23 -accessibleValue 2 -maxDiam 6 -minRestore 90 -maxRestore 110 -minProportion 0.7 -precision 3 -o /home/user/result

It is possible to get usage help using the `-h` flag:

```shell
user@wonderful-computer:~$ java -jar restopt.jar -h
Usage: restopt [-hV] [-lns] -accessible=<accessibleBinaryRasterPath>
               [-accessibleValue=<accessibleVal>] -cellArea=<cellArea>
               -habitat=<habitatBinaryRasterPath> -maxDiam=<maxDiam>
               [-maxNbCC=<maxNbCC>] -maxRestore=<maxRestore>
               -minProportion=<minProportion> -minRestore=<minRestore>
               -o=<outputPath> -objective=<objective> [-precision=<precision>]
               -restorable=<restorableBinaryRasterPath> [-timeLimit=<timeLimit>]
Find connected and compact areas maximizing the effective mesh size (MESH) or
the integral index of connectivity (IIC).
      -accessible=<accessibleBinaryRasterPath>
                             Path to accessible areas raster (accessible cells
                               are identified with accessibleValue, which is by
                               default set to 1. If all degraded are
                               accessible, set accessibleValue to 0 and use the
                               habitat raster as argument).
      -accessibleValue=<accessibleVal>
                             Value of accessible cells in accessible areas
                               raster.
      -cellArea=<cellArea>   Total area of a cell.
  -h, --help                 Show this help message and exit.
      -habitat=<habitatBinaryRasterPath>
                             Path to habitat binary raster.
      -lns                   Use Large Neighbourhood Search (LNS). Note that
                               the solver cannot provide optimality guarantee
                               when LNS is used. LNS must thus be used with a
                               time limit.
      -maxDiam=<maxDiam>     Maximum diameter, in cell width.
      -maxNbCC=<maxNbCC>     Maximum number of connected components (default is
                               1).
      -maxRestore=<maxRestore>
                             Maximum area to restore (greater than minRestore).
      -minProportion=<minProportion>
                             Minimum habitat proportion need to restore a cell
                               (between 0 and 1).
      -minRestore=<minRestore>
                             Minimum area to restore (between 0 and maxRestore).
  -o=<outputPath>            Output path for generated files. Two files are
                               generated: one raster (.tif) representingthe
                               solution, and a csv file containing its
                               characteristics. File extensions
                               areautomatically added to the end of the output
                               path.
      -objective=<objective> Optimization objective, will be maximized (MESH or
                               IIC).
      -precision=<precision> The solver optimizes integer variables, so MESH or
                               IIC is multiplied by 10^<precision>and restored
                               as a real rounded to <precision> numbers after
                               the decimal point.
      -restorable=<restorableBinaryRasterPath>
                             Path to restorable areas raster (values must be
                               between 0 and cellArea).
      -timeLimit=<timeLimit> Time limit for optimization (in seconds).
  -V, --version              Print version information and exit.
```
