package de.uni_potsdam.hpi.asg.breeze2stg;

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
import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponents;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponentsFile;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Scale;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Scale.ScaleType;
import de.uni_potsdam.hpi.asg.breeze2stg.io.config.Config;
import de.uni_potsdam.hpi.asg.breeze2stg.io.config.ConfigFile;
import de.uni_potsdam.hpi.asg.breeze2stg.io.protocol.Protocol;
import de.uni_potsdam.hpi.asg.breeze2stg.io.protocol.ProtocolFile;
import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractBreezeNetlist;
import de.uni_potsdam.hpi.asg.common.breeze.model.BreezeProject;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentInst;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Channel.ChannelType;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Parameter.ParameterType;
import de.uni_potsdam.hpi.asg.common.invoker.ExternalToolsInvoker;
import de.uni_potsdam.hpi.asg.common.invoker.local.ShutdownThread;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper.Mode;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.iohelper.Zipper;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;

public class Breeze2STGMain {

    public static final String                  DEF_CONFIG_FILE_NAME      = "breeze2stg_config.xml";
    public static final File                    DEF_CONFIG_FILE           = new File(CommonConstants.DEF_CONFIG_DIR_FILE, DEF_CONFIG_FILE_NAME);
    public static final String                  DEF_TOOL_CONFIG_FILE_NAME = "breeze2stg_toolconfig.xml";
    public static final File                    DEF_TOOL_CONFIG_FILE      = new File(CommonConstants.DEF_CONFIG_DIR_FILE, DEF_TOOL_CONFIG_FILE_NAME);

    private static Logger                       logger;
    private static Breeze2STGCommandlineOptions options;

    public static Config                        config;
    public static boolean                       tooldebug;

    public static void main(String[] args) {
        int status = main2(args);
        System.exit(status);
    }

    public static int main2(String[] args) {
        try {
            long start = System.currentTimeMillis();
            int status = -1;
            options = new Breeze2STGCommandlineOptions();
            if(options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(), options.getLogfile(), options.isDebug(), Mode.cmdline);
                logger.debug("Args: " + Arrays.asList(args).toString());
                logger.debug("Using config file " + options.getConfigFile());
                config = ConfigFile.readIn(options.getConfigFile());
                if(config == null) {
                    logger.error("Could not read config");
                    return 1;
                }
                Runtime.getRuntime().addShutdownHook(new ShutdownThread());
                WorkingdirGenerator.getInstance().create(options.getWorkingdir(), config.workdir, "resynwork");
                tooldebug = options.isTooldebug();
                logger.debug("Using tool config file " + options.getToolConfigFile());
                if(!ExternalToolsInvoker.init(options.getToolConfigFile(), tooldebug)) {
                    return 1;
                }
                status = execute();
                zipWorkfile();
                if(!options.isDebug()) {
                    WorkingdirGenerator.getInstance().delete();
                }
            }
            long end = System.currentTimeMillis();
            if(logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return status;
        } catch(Exception | Error e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private static int execute() {
        // Read components config
        Breeze2STGComponents compConfig = Breeze2STGComponentsFile.readIn(config.breeze2stgComponentConfig);
        if(compConfig == null) {
            logger.error("Could not read breeze2stg component configurarion");
            return -1;
        }

        // Read protocol file
        Protocol protocol = ProtocolFile.readIn(options.getProtocolFile());
        if(protocol == null) {
            logger.error("Could not read protocol file " + options.getProtocolFile());
            return -1;
        }

        // Read breeze
        File actualBreezeFile;
        try {
            actualBreezeFile = options.getBreezeFile().getCanonicalFile();
        } catch(IOException e) {
            logger.error(e.getLocalizedMessage());
            return -1;
        }
        BreezeProject proj = BreezeProject.create(actualBreezeFile, config.generalComponentConfig, false, true); //TODO: skips
        if(proj == null) {
            logger.error("Could not create Breeze project");
            return -1;
        }

        // Choose last
        AbstractBreezeNetlist breezeNetlist = null;
        for(AbstractBreezeNetlist n : proj.getSortedNetlists()) {
            breezeNetlist = n;
        }
        if(breezeNetlist == null) {
            logger.error("Breeze file did not contain a netlist");
            return -1;
        }

        for(HSComponentInst inst : breezeNetlist.getAllHSInstances()) {
            String compName = inst.getComp().getComp().getBreezename();

            Breeze2STGComponent comp = compConfig.getComponentByName(compName);
            if(comp == null) {
                logger.error("Breeze2STG component configurarion for component " + compName + " not found");
                return -1;
            }

            int scaleFactor = -1;
            if(comp.getScales() != null) {
                Scale scale = comp.getScales().getScaleById(0);
                if(scale == null) {
                    logger.error("No scale with id 0 for component " + compName + " defined");
                    return -1;
                }
                ScaleType type = scale.getType();
                if(type == null) {
                    logger.error("Scale type for id 0 for component " + compName + " invalid");
                    return -1;
                }

                switch(type) {
                    case control_in: {
                        int chan_id = inst.getComp().getComp().getChannels().getChannel(ChannelType.control_in).getId();
                        scaleFactor = inst.getChan(chan_id).size();
                    }
                        break;
                    case control_out: {
                        int chan_id = inst.getComp().getComp().getChannels().getChannel(ChannelType.control_out).getId();
                        scaleFactor = inst.getChan(chan_id).size();
                    }
                        break;
                    case input_count:
                        scaleFactor = paramToInteger(inst, ParameterType.input_count);
                        break;
                    case output_count:
                        scaleFactor = paramToInteger(inst, ParameterType.output_count);
                        break;
                    case port_count:
                        scaleFactor = paramToInteger(inst, ParameterType.port_count);
                        break;
                }

            }

          //@formatter:off
          System.out.println(
              Strings.padEnd(compName, 25, ' ') + 
              Strings.padEnd((scaleFactor == 0) ? "-" : Integer.toString(scaleFactor), 5, ' ')
          );
          //@formatter:on

        }

        return 0;
    }

    private static Integer paramToInteger(HSComponentInst inst, ParameterType param) {
        Object paramObj = inst.getType().getParamValue(param);
        Integer paramInt = null;
        if(paramObj instanceof Integer) {
            paramInt = (Integer)paramObj;
        }
        return paramInt;
    }

    /**
     * Zips all the files in the working directory (for debugging)
     * 
     * @return <code>true</code>: ok, <code>false</code>: something went wrong
     */
    private static boolean zipWorkfile() {
        if(options.getWorkfile() != null) {
            if(!Zipper.getInstance().zip(options.getWorkfile())) {
                logger.warn("Could not zip temp files");
                return false;
            }
        } else {
            logger.warn("No zip outfile");
            return false;
        }
        return true;
    }
}