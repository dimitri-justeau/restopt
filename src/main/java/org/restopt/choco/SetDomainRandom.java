/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.restopt.choco;

import org.chocosolver.solver.search.strategy.selectors.values.SetValueSelector;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

import java.util.Random;

/**
 * Selects a random value in the set domain variable
 *
 * @author Dimitri Justeau-Allaire
 * @since 05/01/2022
 */
public class SetDomainRandom implements SetValueSelector {

    private final Random rand;

    public SetDomainRandom(long seed) {
        this.rand = new Random(seed);
    }

    @Override
    public int selectValue(SetVar setVar) {
        int i = rand.nextInt(setVar.getUB().size() - setVar.getLB().size()) + 1;
        ISetIterator iter = setVar.getUB().iterator();
        int val = -1;
        while (i > 0) {
            val = iter.nextInt();
            if (!setVar.getLB().contains(val)) {
                i--;
            }
        }
        return val;
    }

}
