package org.restopt.search;

import org.restopt.RestoptProblem;

/**
 * Abstract base class for restopt search strategies
 */
public abstract class AbstractRestoptSearchStrategy {

    protected RestoptProblem problem;

    public AbstractRestoptSearchStrategy(RestoptProblem problem) {
        this.problem = problem;
    }

    public abstract void setSearch();
}
