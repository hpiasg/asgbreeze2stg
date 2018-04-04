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

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.stg.model.STG;

public class STGBlueprintLibrary {
    private static final Logger       logger = LogManager.getLogger();

    private Map<String, STGGenerator> componentBlueprints;

    protected STGBlueprintLibrary(Map<String, STGGenerator> componentBlueprints) {
        this.componentBlueprints = componentBlueprints;
    }

    public STG getSTGforComponent(String compName, int scale) {
        STGGenerator gen = componentBlueprints.get(compName);
        if(gen == null) {
            logger.error("No blueprint for component '" + compName + "'");
            return null;
        }
        return gen.generate(scale);
    }
}
