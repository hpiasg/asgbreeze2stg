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
import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.Config;
import de.uni_potsdam.hpi.asg.breeze2stg.io.ConfigFile;
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
        return 0;
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