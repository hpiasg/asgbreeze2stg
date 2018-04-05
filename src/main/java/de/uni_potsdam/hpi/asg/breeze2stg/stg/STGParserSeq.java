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
import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorSeq.SeqPlaceScaleType;
import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorSeq.SeqTransScaleType;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGParserSeq {
    private static final Logger                 logger               = LogManager.getLogger();

    public static final String                  specialLastStr       = "_L_";
    private static final Pattern                signalPattern        = Pattern.compile("([rac])([A-Z])(" + specialLastStr + ")?");
    private static final Pattern                seqStartPlacePattern = Pattern.compile("seqStart([0-9]+)");
    private static final Pattern                seqEndPlacePattern   = Pattern.compile("seqEnd([0-9]+)");

    private Breeze2STGComponent                 comp;
    private STG                                 stg;

    private Map<Signal, SeqTransScaleType>      scaledSignals;
    private Map<Transition, SeqTransScaleType>  scaledTransitions;
    private Map<Place, SeqPlaceScaleType>       scaledPlaces;
    private Map<Integer, STGParserSeqPlacePair> seqPairs;

    public STGParserSeq(Breeze2STGComponent comp, STG stg) {
        this.comp = comp;
        this.stg = stg;
    }

    public STGGeneratorSeq getGenerator() {
        if(!findScaledSignals()) {
            return null;
        }

        if(!findScaledTransitions()) {
            return null;
        }

        if(!findScaledPlaces()) {
            return null;
        }

        return new STGGeneratorSeq(comp.getBreezename(), stg, scaledSignals, scaledTransitions, scaledPlaces, seqPairs);
    }

    private boolean findScaledSignals() {
        scaledSignals = new HashMap<>();
        Matcher m = null;
        for(Signal sig : stg.getSignals()) {
            m = signalPattern.matcher(sig.getName());
            if(!m.matches()) {
                logger.warn("Signal '" + sig.getName() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' does not follow naming scheme");
                return false;
            }
            //matches
            Channel chan = comp.getChannelByStgName(m.group(2));
            if(chan == null) {
                logger.warn("Channel for signal '" + sig.getName() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' does not follow naming scheme");
                return false;
            }
            if(m.group(3) == null) {
                //"normal" signal
                if(chan.getScale() != null) {
                    scaledSignals.put(sig, SeqTransScaleType.scaled);
                } else {

                    scaledSignals.put(sig, SeqTransScaleType.unique);
                }
            } else {
                //"_L" signal
                scaledSignals.put(sig, SeqTransScaleType.specialLast);
            }
        }
        return true;
    }

    private boolean findScaledTransitions() {
        scaledTransitions = new HashMap<>();
        for(Transition t : stg.getTransitions()) {
            scaledTransitions.put(t, scaledSignals.get(t.getSignal()));
        }
        return true;
    }

    private boolean findScaledPlaces() {
        scaledPlaces = new HashMap<>();
        seqPairs = new HashMap<>();
        Matcher m = null;
        for(Entry<String, Place> placeEntry : stg.getPlaces().entrySet()) {
            String id = placeEntry.getKey();
            Place place = placeEntry.getValue();
            // check name
            m = seqStartPlacePattern.matcher(id);
            if(m.matches()) {
                scaledPlaces.put(place, SeqPlaceScaleType.seqStart);
                int pairId = Integer.parseInt(m.group(1));
                if(!seqPairs.containsKey(pairId)) {
                    seqPairs.put(pairId, new STGParserSeqPlacePair());
                }
                seqPairs.get(pairId).seqStart = place;
                continue;
            }
            m = seqEndPlacePattern.matcher(id);
            if(m.matches()) {
                scaledPlaces.put(place, SeqPlaceScaleType.seqEnd);
                int pairId = Integer.parseInt(m.group(1));
                if(!seqPairs.containsKey(pairId)) {
                    seqPairs.put(pairId, new STGParserSeqPlacePair());
                }
                seqPairs.get(pairId).seqEnd = place;
                continue;
            }

            // check surroundings
//            if(!stg.getInitMarking().contains(place)) {
            if(place.isMarkedGraphPlace()) {
                Transition preT = place.getPreset().get(0);
                Transition postT = place.getPostset().get(0);
                SeqTransScaleType preTscale = scaledTransitions.get(preT);
                SeqTransScaleType postTscale = scaledTransitions.get(postT);
                switch(preTscale) {
                    case scaled:
                        switch(postTscale) {
                            case scaled:
                                scaledPlaces.put(place, SeqPlaceScaleType.scaled);
                                break;
                            case specialLast:
                            case unique:
                                //unclear
                                break;
                        }
                        break;
                    case specialLast:
                    case unique:
                        switch(postTscale) {
                            case scaled:
                                //unclear
                                break;
                            case specialLast:
                            case unique:
                                scaledPlaces.put(place, SeqPlaceScaleType.unique);
                                break;
                        }
                        break;
                }

                if(scaledPlaces.containsKey(place)) {
                    continue;
                }
            }
//            }
            logger.warn("Not clear if place '" + place.toString() + "' of STG file '" + stg.getFile().getAbsolutePath() + "' is unique or scaled. Please specify");
            return false;
        }
        return true;
    }
}
