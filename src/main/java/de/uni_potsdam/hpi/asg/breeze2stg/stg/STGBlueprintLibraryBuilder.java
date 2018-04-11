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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponents;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.common.stg.GFile;
import de.uni_potsdam.hpi.asg.common.stg.model.Place;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.protocols.io.stgindex.STGIndex;

public class STGBlueprintLibraryBuilder {
    private static final Logger logger = LogManager.getLogger();

    private STGBlueprintLibraryBuilder() {
    }

    public static STGBlueprintLibrary create(Breeze2STGComponents components, STGIndex protocol) {
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
                //TODO uncomment
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
                    break;
                }
            }

            if(seqMode) {
                STGParserSeq seqParser = new STGParserSeq(comp, stg);
                STGGeneratorSeq seqGen = seqParser.getGenerator();
                if(seqGen == null) {
                    continue;
                }
                componentBlueprints.put(compName, seqGen);
            } else {
                // par mode
                STGParserPar parParser = new STGParserPar(comp, stg);
                STGGeneratorPar parGen = parParser.getGenerator();
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
}
