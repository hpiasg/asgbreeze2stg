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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.stg.STGCopy;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGGenerator {
    private static final Logger           logger = LogManager.getLogger();

    private Map<String, STGGeneratorData> componentBlueprints;

    protected STGGenerator(Map<String, STGGeneratorData> componentBlueprints) {
        this.componentBlueprints = componentBlueprints;
    }

    public STG getSTGforComponent(String compName, int scale) {
        STGGeneratorData data = componentBlueprints.get(compName);
        if(data == null) {
            logger.error("No blueprint for component '" + compName + "'");
            return null;
        }

        return expand(data, scale);
    }

    private STG expand(STGGeneratorData data, int scale) {
        STGCopy copyActor = new STGCopy(data.getStg());
        STG copySTG = copyActor.getCopy();

        //new maps
        Map<Signal, Boolean> scaledSignals = new HashMap<>();
        for(Signal sig : copySTG.getSignals()) {
            scaledSignals.put(sig, data.getScaledSignals().get(copyActor.getSignalMap().inverse().get(sig)));
        }
        Map<Transition, Boolean> scaledTransitions = new HashMap<>();
        for(Transition t : copySTG.getTransitions()) {
            scaledTransitions.put(t, data.getScaledTransitions().get(copyActor.getTransitionMap().inverse().get(t)));
        }
        Map<Place, Boolean> scaledPlaces = new HashMap<>();
        for(Entry<String, Place> pE : copySTG.getPlaces().entrySet()) {
            Place p = pE.getValue();
            scaledPlaces.put(p, data.getScaledPlaces().get(copyActor.getPlaceMap().inverse().get(p)));
        }

        //scale signals
        Set<Signal> oldSignals = new HashSet<>(copySTG.getSignals());
        for(Signal sig : oldSignals) {
            if(scaledSignals.get(sig)) {
                // signal is scaled
                if(scale > 1) {
                    for(int i = 1; i < scale; i++) {
                        copySTG.addSignal(sig.getName() + Integer.toString(i), sig.getType());
                    }
                }
                copySTG.changeSignalName(sig, sig.getName() + "0");
            }
        }

        //scale transitions
        Set<Transition> oldTransitions = new HashSet<>(copySTG.getTransitions());
        Map<Transition, ArrayList<Transition>> transitionScaleMap = new HashMap<>();
        for(Transition t : oldTransitions) {
            if(scaledTransitions.get(t)) {
                // transition is scaled
                ArrayList<Transition> scaleArray = new ArrayList<>();
                transitionScaleMap.put(t, scaleArray);
                scaleArray.add(t);
                if(scale > 1) {
                    String sigBaseName = t.getSignal().getName().replace("0", ""); // signal was already changed to xX0
                    for(int i = 1; i < scale; i++) {
                        Transition t2 = copySTG.getTransitionOrAdd(sigBaseName + Integer.toString(i), t.getEdge(), t.getId());
                        scaleArray.add(t2);
                    }
                }
            }
        }

        //scale places & arcs
        Set<Place> oldPlaces = new HashSet<>(copySTG.getPlaces().values());
        for(Place p : oldPlaces) {
            if(scaledPlaces.get(p)) {
                // place is scaled
                if(scale > 1) {
                    for(int i = 1; i < scale; i++) {
                        Place p2 = copySTG.getPlaceOrAdd(p.getId() + "_" + Integer.toString(i));
                        // copy arcs from p
                        for(Transition preT : p.getPreset()) {
                            if(scaledTransitions.get(preT)) {
                                // pre transition is scaled - add the one with same i
                                Transition t = transitionScaleMap.get(preT).get(i);
                                p2.addPreTransition(t);
                                t.addPostPlace(p2);
                            } else {
                                // pre transition is unique - add this one
                                p2.addPreTransition(preT);
                                preT.addPostPlace(p2);
                            }
                        }
                        for(Transition postT : p.getPostset()) {
                            if(scaledTransitions.get(postT)) {
                                // post transition is scaled - add the one with same i
                                Transition t = transitionScaleMap.get(postT).get(i);
                                p2.addPostTransition(t);
                                t.addPrePlace(p2);
                            } else {
                                // post transition is unique - add this one
                                p2.addPostTransition(postT);
                                postT.addPrePlace(p2);
                            }
                        }
                    }
                }
            } else {
                // place is unique (but maybe transitions pre and post are scaled)
                List<Transition> oldPreset = new ArrayList<>(p.getPreset());
                for(Transition preT : oldPreset) {
                    if(scaledTransitions.get(preT)) {
                        // pre transition is scaled - add all (but the one already known id=0) to preset
                        for(int i = 1; i < scale; i++) {
                            Transition t = transitionScaleMap.get(preT).get(i);
                            p.addPreTransition(t);
                            t.addPostPlace(p);
                        }
                    }
                    // else: pre transition is unique - already linked
                }
                List<Transition> oldPostset = new ArrayList<>(p.getPostset());
                for(Transition postT : oldPostset) {
                    if(scaledTransitions.get(postT)) {
                        // post transition is scaled - add all (but the one already known id=0) to postset
                        for(int i = 1; i < scale; i++) {
                            Transition t = transitionScaleMap.get(postT).get(i);
                            p.addPostTransition(t);
                            t.addPrePlace(p);
                        }
                    }
                    // else: post transition is unique - already linked
                }
            }
        }

        return copySTG;
    }
}
