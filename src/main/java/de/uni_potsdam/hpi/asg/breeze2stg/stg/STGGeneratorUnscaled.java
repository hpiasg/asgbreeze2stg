package de.uni_potsdam.hpi.asg.breeze2stg.stg;

/*
 * Copyright (C) 2018 Norman Kluge
 * 
 * This file is part of ASGbreeze2stg.
 * 
 * ASGbreeze2stg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGbreeze2stg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGbreeze2stg.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentType;

import de.uni_potsdam.hpi.asg.common.stg.STGCopy;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;

public class STGGeneratorUnscaled extends STGGeneratorBlueprint {
    private static final Logger logger = LogManager.getLogger();

    protected STGGeneratorUnscaled(String componentName, STG stg) {
        super(componentName, stg);
    }

    @Override
    public STG generate(int scale, HSComponentType type) {
        return generate(scale); // type not needed
    }

    public STG generate(int scale) {
        if(scale != 0) {
            logger.warn("Component is unscaled, but a scale factor of '" + scale + "' was given. Ignoring");
        }
        STGCopy copy = new STGCopy(stg);
        return copy.getCopy();
    }
}
