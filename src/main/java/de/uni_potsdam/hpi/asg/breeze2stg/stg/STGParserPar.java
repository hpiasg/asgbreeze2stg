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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorPar.ParScaleType;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGParserPar {
    private static final Logger  logger          = LogManager.getLogger();

    private static final String  scaledPlaceName = "scaled";
    private static final String  uniquePlaceName = "unique";
    private static final Pattern signalPattern   = Pattern.compile("([rac])([A-Z])");

    private Breeze2STGComponent  comp;
    private STG                  stg;

    public STGParserPar(Breeze2STGComponent comp, STG stg) {
        this.comp = comp;
        this.stg = stg;
    }

    public STGGeneratorPar getGenerator() {
        Map<Signal, ParScaleType> scaledSignals = findScaledSignals();
        if(scaledSignals == null) {
            return null;
        }

        Map<Transition, ParScaleType> scaledTransitions = findScaledTransitions(scaledSignals);
        if(scaledTransitions == null) {
            return null;
        }

        Map<Place, ParScaleType> scaledPlaces = findScaledPlaces(scaledTransitions);
        if(scaledPlaces == null) {
            return null;
        }

        return new STGGeneratorPar(comp.getBreezename(), stg, scaledSignals, scaledTransitions, scaledPlaces);
    }

    private Map<Place, ParScaleType> findScaledPlaces(Map<Transition, ParScaleType> scaledTransitions) {
        Map<Place, ParScaleType> scaledPlaces = new HashMap<>();
        for(Entry<String, Place> placeEntry : stg.getPlaces().entrySet()) {
            String id = placeEntry.getKey();
            Place place = placeEntry.getValue();
            // check name
            if(id.startsWith(scaledPlaceName)) {
                scaledPlaces.put(place, ParScaleType.scaled);
                continue;
            } else if(id.startsWith(uniquePlaceName)) {
                scaledPlaces.put(place, ParScaleType.unique);
                continue;
            }
            // check surroundings
//            if(!stg.getInitMarking().contains(place)) {
            if(place.isMarkedGraphPlace()) {
                Transition preT = place.getPreset().get(0);
                Transition postT = place.getPostset().get(0);
                if(scaledTransitions.get(preT).equals(scaledTransitions.get(postT))) {
                    scaledPlaces.put(place, scaledTransitions.get(preT));
                    continue;
                }
            }
//            }
            logger.warn("Not clear if place '" + place.toString() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' is unique or scaled. Please specify");
            return null;
        }
        return scaledPlaces;
    }

    private Map<Transition, ParScaleType> findScaledTransitions(Map<Signal, ParScaleType> scaledSignals) {
        Map<Transition, ParScaleType> scaledTransitions = new HashMap<>();
        for(Transition t : stg.getTransitions()) {
            scaledTransitions.put(t, scaledSignals.get(t.getSignal()));
        }
        return scaledTransitions;
    }

    private Map<Signal, ParScaleType> findScaledSignals() {
        Map<Signal, ParScaleType> retVal = new HashMap<>();
        Matcher m = null;
        for(Signal sig : stg.getSignals()) {
            m = signalPattern.matcher(sig.getName());
            if(!m.matches()) {
                logger.warn("Signal '" + sig.getName() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' does not follow naming scheme");
                return null;
            }
            //matches
            Channel chan = comp.getChannelByStgName(m.group(2));
            if(chan == null) {
                logger.warn("Channel for signal '" + sig.getName() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' does not follow naming scheme");
                return null;
            }
            if(chan.getScale() != null) {
                //scaled
                retVal.put(sig, ParScaleType.scaled);
            } else {
                retVal.put(sig, ParScaleType.unique);
            }
        }
        return retVal;
    }
}
