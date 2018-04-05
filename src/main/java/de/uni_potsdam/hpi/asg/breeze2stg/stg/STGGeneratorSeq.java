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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import de.uni_potsdam.hpi.asg.common.stg.STGCopy;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGGeneratorSeq extends STGGenerator {
    private static final Logger logger = LogManager.getLogger();

    public enum SeqTransScaleType {
        scaled, unique, // default 
        specialLast // last transition (automatically unique)
    }

    public enum SeqPlaceScaleType {
        scaled, unique, // default 
        seqStart, seqEnd // start/end of a sequence to copy
    }

    private Map<Signal, SeqTransScaleType>      scaledSignals;
    private Map<Transition, SeqTransScaleType>  scaledTransitions;
    private Map<Place, SeqPlaceScaleType>       scaledPlaces;
    private Map<Integer, STGParserSeqPlacePair> seqPairs;

    public STGGeneratorSeq(String componentName, STG stg, Map<Signal, SeqTransScaleType> scaledSignals, Map<Transition, SeqTransScaleType> scaledTransitions, Map<Place, SeqPlaceScaleType> scaledPlaces, Map<Integer, STGParserSeqPlacePair> seqPairs) {
        super(componentName, stg);
        this.scaledSignals = scaledSignals;
        this.scaledTransitions = scaledTransitions;
        this.scaledPlaces = scaledPlaces;
        this.seqPairs = seqPairs;
    }

    @Override
    public STG generate(int scale) {
        STGCopy copyActor = new STGCopy(stg);
        STG copySTG = copyActor.getCopy();

        //new maps
        Map<Signal, SeqTransScaleType> copyScaledSignals = new HashMap<>();
        for(Signal sig : copySTG.getSignals()) {
            copyScaledSignals.put(sig, scaledSignals.get(copyActor.getSignalMap().inverse().get(sig)));
        }
        Map<Transition, SeqTransScaleType> copyScaledTransitions = new HashMap<>();
        for(Transition t : copySTG.getTransitions()) {
            copyScaledTransitions.put(t, scaledTransitions.get(copyActor.getTransitionMap().inverse().get(t)));
        }
        Map<Place, SeqPlaceScaleType> copyScaledPlaces = new HashMap<>();
        for(Place p : copySTG.getPlaces().values()) {
            copyScaledPlaces.put(p, scaledPlaces.get(copyActor.getPlaceMap().inverse().get(p)));
        }
        Map<Integer, STGParserSeqPlacePair> copySeqPairs = new HashMap<>();
        for(Entry<Integer, STGParserSeqPlacePair> entry : seqPairs.entrySet()) {
            STGParserSeqPlacePair pair = entry.getValue();
            STGParserSeqPlacePair copyPair = new STGParserSeqPlacePair();
            copyPair.seqStart = copyActor.getPlaceMap().get(pair.seqStart);
            copyPair.seqEnd = copyActor.getPlaceMap().get(pair.seqEnd);
            copySeqPairs.put(entry.getKey(), copyPair);
        }

        //scale signals
        Set<Signal> oldSignals = new HashSet<>(copySTG.getSignals());
        for(Signal sig : oldSignals) {
            switch(copyScaledSignals.get(sig)) {
                case scaled:
                    if(scale > 2) {
                        for(int i = 1; i < (scale - 1); i++) {
                            copySTG.addSignal(sig.getName() + Integer.toString(i), sig.getType());
                        }
                    }
                    copySTG.changeSignalName(sig, sig.getName() + "0");
                    break;
                case specialLast:
                    copySTG.changeSignalName(sig, sig.getName().replaceAll(STGParserSeq.specialLastStr, Integer.toString(scale - 1)));
                    break;
                case unique:
                    break;
            }
        }

        //scale transitions
        Set<Transition> oldTransitions = new HashSet<>(copySTG.getTransitions());
        Map<Transition, ArrayList<Transition>> transitionScaleMap = new HashMap<>();
        for(Transition t : oldTransitions) {
            switch(copyScaledTransitions.get(t)) {
                case scaled:
                    ArrayList<Transition> scaleArray = new ArrayList<>();
                    transitionScaleMap.put(t, scaleArray);
                    scaleArray.add(t);
                    if(scale > 2) {
                        String sigBaseName = t.getSignal().getName().replace("0", ""); // signal was already changed to xX0
                        for(int i = 1; i < (scale - 1); i++) {
                            Transition t2 = copySTG.getTransitionOrAdd(sigBaseName + Integer.toString(i), t.getEdge(), t.getId());
                            scaleArray.add(t2);
                        }
                    }
                    break;
                case specialLast:
                case unique:
                    break;
            }
        }

        //scale places & arcs
        Set<Place> oldPlaces = new HashSet<>(copySTG.getPlaces().values());
        Map<Place, ArrayList<Place>> placeScaleMap = new HashMap<>();
        for(Place p : oldPlaces) {
            switch(copyScaledPlaces.get(p)) {
                case seqStart:
                case seqEnd:
                case scaled:
                    ArrayList<Place> scaleArray = new ArrayList<>();
                    placeScaleMap.put(p, scaleArray);
                    scaleArray.add(p);
                    if(scale > 2) {
                        for(int i = 1; i < (scale - 1); i++) {
                            Place p2 = copySTG.getPlaceOrAdd(p.getId() + "_" + Integer.toString(i));
                            scaleArray.add(p2);
                            if(copySTG.getInitMarking().contains(p)) {
                                // p is initially marked, so should the copy
                                copySTG.getInitMarking().add(p2);
                            }
                            // copy arcs from p
                            if(copyScaledPlaces.get(p) == SeqPlaceScaleType.scaled || copyScaledPlaces.get(p) == SeqPlaceScaleType.seqEnd) {
                                for(Transition preT : p.getPreset()) {
                                    switch(copyScaledTransitions.get(preT)) {
                                        case scaled:
                                            // pre transition is scaled - add the one with same i
                                            Transition t = transitionScaleMap.get(preT).get(i);
                                            p2.addPreTransition(t);
                                            t.addPostPlace(p2);
                                            break;
                                        case specialLast:
                                        case unique:
                                            // pre transition is unique - add this one
                                            p2.addPreTransition(preT);
                                            preT.addPostPlace(p2);
                                            break;
                                    }
                                }
                            }
                            if(copyScaledPlaces.get(p) == SeqPlaceScaleType.scaled || copyScaledPlaces.get(p) == SeqPlaceScaleType.seqStart) {
                                for(Transition postT : p.getPostset()) {
                                    switch(copyScaledTransitions.get(postT)) {
                                        case scaled:
                                            // post transition is scaled - add the one with same i
                                            Transition t = transitionScaleMap.get(postT).get(i);
                                            p2.addPostTransition(t);
                                            t.addPrePlace(p2);
                                            break;
                                        case specialLast:
                                        case unique:
                                            // post transition is unique - add this one
                                            p2.addPostTransition(postT);
                                            postT.addPrePlace(p2);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case unique:
                    // place is unique (but maybe transitions pre and post are scaled)
                    List<Transition> oldPreset = new ArrayList<>(p.getPreset());
                    for(Transition preT : oldPreset) {
                        switch(copyScaledTransitions.get(preT)) {
                            case scaled:
                                // pre transition is scaled - add all (but the one already known id=0) to preset
                                for(int i = 1; i < scale; i++) {
                                    Transition t = transitionScaleMap.get(preT).get(i);
                                    p.addPreTransition(t);
                                    t.addPostPlace(p);
                                }
                                break;
                            case specialLast:
                            case unique:
                                // pre transition is unique - already linked
                                break;
                        }

                    }
                    List<Transition> oldPostset = new ArrayList<>(p.getPostset());
                    for(Transition postT : oldPostset) {
                        switch(copyScaledTransitions.get(postT)) {
                            case scaled:
                                // post transition is scaled - add all (but the one already known id=0) to postset
                                for(int i = 1; i < scale; i++) {
                                    Transition t = transitionScaleMap.get(postT).get(i);
                                    p.addPostTransition(t);
                                    t.addPrePlace(p);
                                }
                                break;
                            case specialLast:
                            case unique:
                                // post transition is unique - already linked
                                break;
                        }
                    }
                    break;
            }
        }

        // merge seqStart/End places
        if(scale > 2) {
            for(STGParserSeqPlacePair pair : copySeqPairs.values()) {
                List<Transition> endInitalPostset = new ArrayList<>(pair.seqEnd.getPostset());
                pair.seqEnd.getPostset().clear();
                Place lastEnd = null;
                for(int i = 1; i < (scale - 1); i++) {
                    Place prevEnd = placeScaleMap.get(pair.seqEnd).get(i - 1);
                    Place currStart = placeScaleMap.get(pair.seqStart).get(i);
                    if(!currStart.getPreset().isEmpty()) {
                        logger.warn("curr start preset should be empty!");
                    }
                    if(!prevEnd.getPostset().isEmpty()) {
                        logger.warn("prev end postset should be empty!");
                    }
                    for(Transition t : currStart.getPostset()) {
                        prevEnd.getPostset().add(t);
                        t.addPrePlace(prevEnd);
                        t.getPreset().remove(currStart);
                    }
                    copySTG.getPlaces().remove(currStart.getId());
                    Place currEnd = placeScaleMap.get(pair.seqEnd).get(i);
                    lastEnd = currEnd;
                }
                // connect to transitions after sequence
                for(Transition t : endInitalPostset) {
                    lastEnd.getPostset().add(t);
                    t.addPrePlace(lastEnd);
                    t.getPreset().remove(pair.seqEnd);
                }
            }
        }

        return copySTG;
    }

}
