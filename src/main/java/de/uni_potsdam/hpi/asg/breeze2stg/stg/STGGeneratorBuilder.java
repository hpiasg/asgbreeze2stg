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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponents;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.breeze2stg.io.protocol.Protocol;
import de.uni_potsdam.hpi.asg.common.stg.GFile;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGGeneratorBuilder {
    private static final Logger  logger          = LogManager.getLogger();

    private static final Pattern signalPattern   = Pattern.compile("([rac])([A-Z])");
    private static final String  scaledPlaceName = "scaled";
    private static final String  uniquePlaceName = "unique";

    private STGGeneratorBuilder() {
    }

    public static STGGenerator create(Breeze2STGComponents components, Protocol protocol) {
        Map<String, STGGeneratorData> componentBlueprints = new HashMap<>();
        for(Breeze2STGComponent comp : components.getComponents()) {
            String compName = comp.getBreezename();
            File stgFile = protocol.getSTGFileForComponent(compName);
            if(stgFile == null) {
                logger.warn("No specification for component '" + compName + "' in given protocol");
                continue;
            }
            if(!stgFile.exists()) {
                //logger.warn("Specification file for component '" + compName + "' does not exist");
                continue;
            }
            STG stg = GFile.importFromFile(stgFile);
            if(stg == null) {
                logger.warn("Could not parse STG file '" + stgFile.getAbsolutePath() + "'");
                continue;
            }

            Map<Signal, Boolean> scaledSignals = findScaledSignals(stg, comp);
            if(scaledSignals == null) {
                continue;
            }

            Map<Transition, Boolean> scaledTransitions = findScaledTransitions(stg, scaledSignals);
            if(scaledTransitions == null) {
                continue;
            }

            Map<Place, Boolean> scaledPlaces = findScaledPlaces(stg, scaledTransitions);
            if(scaledPlaces == null) {
                continue;
            }

            componentBlueprints.put(compName, new STGGeneratorData(compName, stg, scaledSignals, scaledTransitions, scaledPlaces));
        }

        if(componentBlueprints.isEmpty()) {
            return null;
        }
        return new STGGenerator(componentBlueprints);
    }

    private static Map<Place, Boolean> findScaledPlaces(STG stg, Map<Transition, Boolean> scaledTransitions) {
        Map<Place, Boolean> scaledPlaces = new HashMap<>();
        for(Entry<String, Place> placeEntry : stg.getPlaces().entrySet()) {
            String id = placeEntry.getKey();
            Place place = placeEntry.getValue();
            // check name
            if(id.startsWith(scaledPlaceName)) {
                scaledPlaces.put(place, true);
                continue;
            } else if(id.startsWith(uniquePlaceName)) {
                scaledPlaces.put(place, false);
                continue;
            }
            // check surroundings
            if(!stg.getInitMarking().contains(place)) {
                if(place.isMarkedGraphPlace()) {
                    Transition preT = place.getPreset().get(0);
                    Transition postT = place.getPostset().get(0);
                    if(scaledTransitions.get(preT).equals(scaledTransitions.get(postT))) {
                        scaledPlaces.put(place, scaledTransitions.get(preT));
                        continue;
                    }
                }
            }
            logger.warn("Not clear if place '" + place.toString() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' is unique or scaled. Please specify");
            return null;
        }
        return scaledPlaces;
    }

    private static Map<Transition, Boolean> findScaledTransitions(STG stg, Map<Signal, Boolean> scaledSignals) {
        Map<Transition, Boolean> scaledTransitions = new HashMap<>();
        for(Transition t : stg.getTransitions()) {
            scaledTransitions.put(t, scaledSignals.get(t.getSignal()));
        }
        return scaledTransitions;
    }

    private static Map<Signal, Boolean> findScaledSignals(STG stg, Breeze2STGComponent comp) {
        Map<Signal, Boolean> retVal = new HashMap<>();
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

            retVal.put(sig, chan.getScale() != null);
        }
        return retVal;
    }
}
