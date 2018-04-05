package de.uni_potsdam.hpi.asg.breeze2stg.stg;

import java.util.Map;

import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorSeq.SeqPlaceScaleType;
import de.uni_potsdam.hpi.asg.breeze2stg.stg.STGGeneratorSeq.SeqTransScaleType;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;

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

import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;
import de.uni_potsdam.hpi.asg.common.stg.model.Transition;

public class STGGeneratorSeq extends STGGenerator {

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
        // TODO Auto-generated method stub
        return null;
    }

}
