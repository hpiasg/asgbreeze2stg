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

import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGGeneratorData {
    private String                   componentName;
    private STG                      stg;
    private Map<Signal, Boolean>     scaledSignals;
    private Map<Transition, Boolean> scaledTransitions;
    private Map<Place, Boolean>      scaledPlaces;

    protected STGGeneratorData(String componentName, STG stg, Map<Signal, Boolean> scaledSignals, Map<Transition, Boolean> scaledTransitions, Map<Place, Boolean> scaledPlaces) {
        this.componentName = componentName;
        this.stg = stg;
        this.scaledSignals = scaledSignals;
        this.scaledTransitions = scaledTransitions;
        this.scaledPlaces = scaledPlaces;
    }

    public String getComponentName() {
        return componentName;
    }

    public Map<Place, Boolean> getScaledPlaces() {
        return scaledPlaces;
    }

    public Map<Signal, Boolean> getScaledSignals() {
        return scaledSignals;
    }

    public Map<Transition, Boolean> getScaledTransitions() {
        return scaledTransitions;
    }

    public STG getStg() {
        return stg;
    }
}