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
import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorPar.ParScaleType;
import de.uni_potsdam.hpi.asg.common.stg.GFile;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGBlueprintLibraryBuilder {
    private static final Logger  logger             = LogManager.getLogger();

    private static final String  parScaledPlaceName = "scaled";
    private static final String  parUniquePlaceName = "unique";
    private static final Pattern parSignalPattern   = Pattern.compile("([rac])([A-Z])");

    //private static final String  specialLastStr  = "_L_";
    //(" + specialLastStr + ")?

    private STGBlueprintLibraryBuilder() {
    }

    public static STGBlueprintLibrary create(Breeze2STGComponents components, Protocol protocol) {
        Map<String, STGGenerator> componentBlueprints = new HashMap<>();
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

            boolean scaled = false;
            for(Channel chan : comp.getChannels().getAllChannels()) {
                if(chan.getScale() != null) {
                    scaled = true;
                    break;
                }
            }

            if(!scaled) {
                componentBlueprints.put(compName, new STGGeneratorUnscaled(compName, stg));
                continue;
            }

            boolean seqMode = false;
            for(Place p : stg.getPlaces().values()) {
                if(p.getId().startsWith("seq")) {
                    seqMode = true;
                }
            }

            if(seqMode) {
                STGGeneratorSeq seqGen = getGeneratorSeq(comp, stg);
                if(seqGen == null) {
                    continue;
                }
                componentBlueprints.put(compName, seqGen);
            } else {
                STGGeneratorPar parGen = getGeneratorPar(comp, stg);
                if(parGen == null) {
                    continue;
                }
                componentBlueprints.put(compName, parGen);
            }
        }

        if(componentBlueprints.isEmpty()) {
            return null;
        }
        return new STGBlueprintLibrary(componentBlueprints);
    }

    private static STGGeneratorSeq getGeneratorSeq(Breeze2STGComponent comp, STG stg) {
        return null;
    }

    private static STGGeneratorPar getGeneratorPar(Breeze2STGComponent comp, STG stg) {
        Map<Signal, ParScaleType> scaledSignals = findScaledSignalsPar(stg, comp);
        if(scaledSignals == null) {
            return null;
        }

        Map<Transition, ParScaleType> scaledTransitions = findScaledTransitionsPar(stg, scaledSignals);
        if(scaledTransitions == null) {
            return null;
        }

        Map<Place, ParScaleType> scaledPlaces = findScaledPlacesPar(stg, scaledTransitions);
        if(scaledPlaces == null) {
            return null;
        }

        return new STGGeneratorPar(comp.getBreezename(), stg, scaledSignals, scaledTransitions, scaledPlaces);
    }

    private static Map<Place, ParScaleType> findScaledPlacesPar(STG stg, Map<Transition, ParScaleType> scaledTransitions) {
        Map<Place, ParScaleType> scaledPlaces = new HashMap<>();
        for(Entry<String, Place> placeEntry : stg.getPlaces().entrySet()) {
            String id = placeEntry.getKey();
            Place place = placeEntry.getValue();
            // check name
            if(id.startsWith(parScaledPlaceName)) {
                scaledPlaces.put(place, ParScaleType.scaled);
                continue;
            } else if(id.startsWith(parUniquePlaceName)) {
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

    private static Map<Transition, ParScaleType> findScaledTransitionsPar(STG stg, Map<Signal, ParScaleType> scaledSignals) {
        Map<Transition, ParScaleType> scaledTransitions = new HashMap<>();
        for(Transition t : stg.getTransitions()) {
            scaledTransitions.put(t, scaledSignals.get(t.getSignal()));
        }
        return scaledTransitions;
    }

    private static Map<Signal, ParScaleType> findScaledSignalsPar(STG stg, Breeze2STGComponent comp) {
        Map<Signal, ParScaleType> retVal = new HashMap<>();
        Matcher m = null;
        for(Signal sig : stg.getSignals()) {
            m = parSignalPattern.matcher(sig.getName());
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

    /*
     * //_L_
            if(m.group(3) == null) {
                //signal is normal
                if(chan.getScale() != null) {
                    //scaled
                    if(retVal.containsKey(sig) && retVal.get(sig) != TransitionScaleType.scaled) {
                        logger.error("Contradiction");
                    }
                    retVal.put(sig, TransitionScaleType.scaled);
                } else {
                    if(retVal.containsKey(sig) && retVal.get(sig) != TransitionScaleType.unique) {
                        logger.error("Contradiction");
                    }
                    retVal.put(sig, TransitionScaleType.unique);
                }
            } else {
                //has _L_
                Signal parent = stg.getSignal(m.group(1) + m.group(2));
                if(retVal.containsKey(parent) && retVal.get(parent) == TransitionScaleType.unique) {
                    logger.error("Signal with special last should be scaled");
                    return null;
                }
                retVal.put(parent, TransitionScaleType.scaledWSL);
                retVal.put(sig, TransitionScaleType.iSL);
            }
     */
}
