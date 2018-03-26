package de.uni_potsdam.hpi.asg.breeze2stg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponents;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.breeze2stg.io.protocol.Protocol;
import de.uni_potsdam.hpi.asg.common.stg.GFile;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;

public class STGGenerator {
    private static final Logger  logger        = LogManager.getLogger();

    private static final Pattern signalPattern = Pattern.compile("([rac])([A-Z])");

    private STGGenerator() {
    }

    public static STGGenerator create(Breeze2STGComponents components, Protocol protocol) {
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

            Map<Signal, Boolean> scaledSignals = matchSignals(stg, comp);
            if(scaledSignals == null) {
                continue;
            }

        }

        return null;
    }

    private static Map<Signal, Boolean> matchSignals(STG stg, Breeze2STGComponent comp) {
        Map<Signal, Boolean> retVal = new HashMap<>();
        Matcher m = null;
        for(Signal sig : stg.getSignals()) {
            m = signalPattern.matcher(sig.getName());
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

            retVal.put(sig, chan.getScale() != null);
        }
        return retVal;
    }

}
