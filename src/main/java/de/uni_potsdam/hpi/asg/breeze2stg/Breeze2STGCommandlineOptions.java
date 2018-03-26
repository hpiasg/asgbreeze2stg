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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import de.uni_potsdam.hpi.asg.common.iohelper.CommandlineOptions;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;

public class Breeze2STGCommandlineOptions extends CommandlineOptions {

    public boolean parseCmdLine(String[] args) {
        return super.parseCmdLine(args, "Usage: ASGbreeze2stg [options] <breeze file>\nOptions:");
    }

    //@formatter:off
    
    @Option(name = "-o", metaVar = "<level>", usage = "Outputlevel: 0:nothing\n1:errors\n2:+warnings\n[3:+info]")
    private int outputlevel = 3;
    @Option(name = "-log", metaVar = "<logfile>", usage = "Define output Logfile, default is breeze2stg" + CommonConstants.LOG_FILE_EXTENSION)
    private File logfile = new File(System.getProperty("user.dir"), "breeze2stg" + CommonConstants.LOG_FILE_EXTENSION);
    @Option(name = "-zip", metaVar = "<zipfile>", usage = "Define the zip file with all temp files, default is breeze2stg" + CommonConstants.ZIP_FILE_EXTENSION)
    private File workfile = new File(System.getProperty("user.dir"), "breeze2stg" + CommonConstants.ZIP_FILE_EXTENSION);
    
    @Option(name = "-cfg", metaVar = "<configfile>", usage = "Config file, default is " + Breeze2STGMain.DEF_CONFIG_FILE_NAME)
    private File configFile = Breeze2STGMain.DEF_CONFIG_FILE;
    @Option(name = "-toolcfg", metaVar = "<configfile>", usage = "External tools config file, default is " + Breeze2STGMain.DEF_TOOL_CONFIG_FILE_NAME)
    private File toolConfigFile = Breeze2STGMain.DEF_TOOL_CONFIG_FILE;
    @Option(name = "-w", metaVar = "<workingdir>", usage = "Working directory. If not given, the value in configfile is used. If there is no entry, 'breeze2stgwork*' in the os default tmp dir is used.")
    private File workingdir = null;

    @Option(name = "-p", metaVar = "<protocolfile>", required = true, usage = "The HS-protocol file to use")
    private File protocolFile = null;
    
    @Argument(metaVar = "breeze File", required = true)
    private File breezeFile;

    @Option(name = "-debug")
    private boolean debug = false;
    @Option(name = "-tooldebug")
    private boolean tooldebug = false;
    
    public File getBreezeFile() {
        return breezeFile;
    }
    
    public int getOutputlevel() {
        return outputlevel;
    }

    public File getLogfile() {
        return logfile;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getWorkfile() {
        return workfile;
    }

    public boolean isDebug() {
        return debug;
    }

    public File getWorkingdir() {
        return workingdir;
    }


    public boolean isTooldebug() {
        return tooldebug;
    }

    public File getToolConfigFile() {
        return toolConfigFile;
    }
    
    public File getProtocolFile() {
        return protocolFile;
    }
}
