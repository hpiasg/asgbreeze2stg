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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponents;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.common.stg.GFile;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.protocols.io.stgindex.STGIndex;

public class STGBlueprintLibraryBuilder {
    private static final Logger logger = LogManager.getLogger();

    private STGBlueprintLibraryBuilder() {
    }

    public static STGBlueprintLibrary create(Breeze2STGComponents components, STGIndex protocol, String protocolName) {

        Map<String, STGGenerator> componentBlueprints = new HashMap<>();
        for(Breeze2STGComponent comp : components.getComponents()) {
            String compName = comp.getBreezename();

            File stgFile = protocol.getSTGFileForComponent(compName);
            if(stgFile != null) {
                STGGenerator gen = useBlueprintMode(comp, stgFile);
                if(gen == null) {
                    return null;
                }
                componentBlueprints.put(compName, gen);
                continue;
            }

            String className = protocol.getClassNameForComponent(compName);
            if(className != null) {
                STGGenerator gen = useClassMode(protocolName, comp, className);
                if(gen == null) {
                    return null;
                }
                componentBlueprints.put(compName, gen);
                continue;
            }

            if(stgFile == null) {
                logger.warn("No specification for component '" + compName + "' in given protocol");
                continue;
            }
        }

        if(componentBlueprints.isEmpty()) {
            return null;
        }
        return new STGBlueprintLibrary(componentBlueprints);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "resource"})
    private static STGGenerator useClassMode(String protocolName, Breeze2STGComponent comp, String className) {
        File protocolDir = new File(CommonConstants.DEF_PROTOCOL_DIR_FILE, protocolName);
        if(!protocolDir.exists() || !protocolDir.isDirectory()) {
            logger.error("Could not find protocol dir '" + protocolDir.getAbsolutePath() + "'");
            return null;
        }
        File classDir = new File(protocolDir, "java");
        if(!classDir.exists() || !classDir.isDirectory()) {
            logger.error("Could not find java-protocol dir '" + classDir.getAbsolutePath() + "'");
            return null;
        }

        try {
            URL url = classDir.toURI().toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            Class cls = cl.loadClass(className);
            return (STGGenerator)cls.getDeclaredConstructor(Breeze2STGComponent.class).newInstance(comp);
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public static STGGenerator useBlueprintMode(Breeze2STGComponent comp, File stgFile) {
        String compName = comp.getBreezename();
        if(!stgFile.exists()) {
            logger.warn("Specification file for component '" + compName + "' does not exist");
            return null;
        }
        STG stg = GFile.importFromFile(stgFile);
        if(stg == null) {
            logger.warn("Could not parse STG file '" + stgFile.getAbsolutePath() + "'");
            return null;
        }

        boolean scaled = false;
        for(Channel chan : comp.getChannels().getAllChannels()) {
            if(chan.getScale() != null) {
                scaled = true;
                break;
            }
        }

        if(!scaled) {
            return new STGGeneratorUnscaled(compName, stg);
        }

        // par mode
        STGParserPar parParser = new STGParserPar(comp, stg);
        STGGeneratorPar parGen = parParser.getGenerator();
        if(parGen == null) {
            return null;
        }
        return parGen;
    }
}
