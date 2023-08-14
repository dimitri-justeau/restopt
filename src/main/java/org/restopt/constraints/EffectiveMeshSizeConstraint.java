package org.restopt.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.restopt.RestoptProblem;
import org.restopt.choco.PropEffectiveMeshSize;
import org.restopt.exception.RestoptException;

public class EffectiveMeshSizeConstraint extends AbstractRestoptConstraint {

    public static final String KEY_MESH = "mesh";

    protected int minMESH;
    protected int maxMESH;

    protected int precision;

    protected IntVar mesh;

    public EffectiveMeshSizeConstraint(RestoptProblem restoptProblem, double minMESH, double maxMESH, int precision) throws RestoptException {
        super(restoptProblem);
        this.minMESH = (int) (minMESH * Math.pow(10, precision));
        this.maxMESH = (int) (maxMESH * Math.pow(10, precision));
        this.precision = precision;
        problem.setMeshConstraint(this);
    }

    @Override
    public void post() {
        if (problem.getAdditionalVariables().containsKey(KEY_MESH)) {
            this.mesh = problem.getAdditionalVariables().get(KEY_MESH);
            this.getModel().arithm(mesh, ">=", minMESH).post();
            this.getModel().arithm(mesh, "<=", maxMESH).post();
        } else {
            this.mesh = problem.getModel().intVar("mesh", minMESH, maxMESH);
            Constraint meshCons = new Constraint(
                    "MESH_constraint",
                    new PropEffectiveMeshSize(
                            problem.getHabitatGraphVar(),
                            this.mesh,
                            problem.getGrid().getSizeCells(),
                            problem.getLandscapeArea(),
                            precision,
                            true
                    )
            );
            problem.getModel().post(meshCons);
        }
    }

    public int getPrecision() {
        return precision;
    }

    public IntVar getMesh() {
        return mesh;
    }
}
