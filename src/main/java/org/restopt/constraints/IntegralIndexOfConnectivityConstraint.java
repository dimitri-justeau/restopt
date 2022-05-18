package org.restopt.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.restopt.RestoptProblem;
import org.restopt.choco.PropIIC;
import org.restopt.exception.RestoptException;

public class IntegralIndexOfConnectivityConstraint extends AbstractRestoptConstraint {

    public static final String KEY_IIC = "iic";

    protected int minIIC;
    protected int maxIIC;

    protected int distanceThreshold;

    protected int precision;

    protected IntVar iic;

    public IntegralIndexOfConnectivityConstraint(RestoptProblem restoptProblem, double minIIC, double maxIIC,
                                                 int distanceThreshold, int precision) throws RestoptException {
        super(restoptProblem);
        if (minIIC < 0 || maxIIC > 1) {
            throw new RestoptException("min and max IIC must be comprised between 0 and 1");
        }
        if (distanceThreshold <= 0) {
            throw new RestoptException("Distance threshold for IIC must be at least 1");
        }
        this.minIIC = (int) (minIIC * Math.pow(10, precision));
        this.maxIIC = (int) (maxIIC * Math.pow(10, precision));
        this.distanceThreshold = distanceThreshold;
        this.precision = precision;
        problem.setIICConstraint(this);
    }

    @Override
    public void post() {
        if (problem.getAdditionalVariables().containsKey(KEY_IIC)) {
            this.iic = problem.getAdditionalVariables().get(KEY_IIC);
            this.getModel().arithm(iic, ">=", minIIC).post();
            this.getModel().arithm(iic, "<=", maxIIC).post();
        } else {
            this.iic = problem.getModel().intVar(minIIC, maxIIC);
            Constraint consIIC = new Constraint(
                    "IIC_constraint",
                    new PropIIC(
                            problem.getHabitatGraphVar(),
                            iic,
                            problem.getGrid(),
                            problem.getLandscapeArea(),
                            distanceThreshold + 1,
                            precision,
                            false
                    )
            );
            problem.getModel().post(consIIC);
        }
    }

    public int getPrecision() {
        return precision;
    }

    public IntVar getIic() {
        return iic;
    }
}
