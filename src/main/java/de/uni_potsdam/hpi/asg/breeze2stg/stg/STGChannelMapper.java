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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.BalsaChannel;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.InternalChannel;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSChannel;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentInst;
import de.uni_potsdam.hpi.asg.common.stg.model.STG;
import de.uni_potsdam.hpi.asg.common.stg.model.Signal;

public class STGChannelMapper {
    private static final Logger  logger        = LogManager.getLogger();
    private static final Pattern signalPattern = Pattern.compile("([rac])([A-Z])([0-9]*)");

    public static boolean replaceInSTG(Breeze2STGComponent comp, HSComponentInst inst, STG stg) {
        Set<Signal> signals = new HashSet<>(stg.getSignals());
        Matcher m = null;
        for(Signal sig : signals) {
            m = signalPattern.matcher(sig.getName());
            if(!m.matches()) {
                logger.error("Signal '" + sig.getName() + "' does not match");
                return false;
            }
            String signalType = m.group(1);
            String channelName = m.group(2);

            int scaleId = (m.group(3).equals("")) ? 0 : Integer.parseInt(m.group(3));
            Channel chan = comp.getChannelByStgName(channelName);
            if(chan == null) {
                logger.error("Channel for signal '" + sig + "' not found");
                return false;
            }

            String newChannelName = null;
            if(chan instanceof BalsaChannel) {
                BalsaChannel bchan = (BalsaChannel)chan;
                int balsaid = bchan.getChanid();
                List<HSChannel> chans = inst.getChan(balsaid);
                if(signalType.equals("c")) {
                    // csc signal => component specific
                    newChannelName = Integer.toString(chans.get(scaleId).getId()) + "_" + inst.getId();
                } else {
                    // req or ack => parallel composition
                    newChannelName = Integer.toString(chans.get(scaleId).getId());
                }
            } else if(chan instanceof InternalChannel) {
                newChannelName = channelName + m.group(3) + "_" + inst.getId();
            } else {
                logger.error("Unkwnon channel type");
                return false;
            }
            stg.changeSignalName(sig, signalType + newChannelName);
        }
        return true;
    }
}
