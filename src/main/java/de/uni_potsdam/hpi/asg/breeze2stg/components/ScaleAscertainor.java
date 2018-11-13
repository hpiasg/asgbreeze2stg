package de.uni_potsdam.hpi.asg.breeze2stg.components;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Breeze2STGComponent;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel;
import de.uni_potsdam.hpi.asg.breeze2stg.io.components.Channel.ScaleType;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentInst;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Channel.ChannelType;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Parameter.ParameterType;

public class ScaleAscertainor {
    private static final Logger logger = LogManager.getLogger();

    public static int getScale(Breeze2STGComponent comp, HSComponentInst inst) {
        int scaleFactor = 0;
        for(Channel chan : comp.getChannels().getAllChannels()) {
            if(chan.getScale() == null) {
                // unscaled
                continue;
            }
            Integer chanScaleFactor = determineScale(chan.getScale(), inst);
            if(chanScaleFactor == null) {
                logger.error("Scale type undefined: " + chan.getScale());
                return -1;
            }
            if(scaleFactor != 0 && scaleFactor != chanScaleFactor) {
                logger.warn("Unequal scale factors for different channels: " + scaleFactor + ", " + chanScaleFactor + ". Using larger one");
                scaleFactor = (scaleFactor > chanScaleFactor) ? scaleFactor : chanScaleFactor;
                continue;
            }
            scaleFactor = chanScaleFactor;
        }
        return scaleFactor;
    }

    private static Integer determineScale(ScaleType type, HSComponentInst inst) {
        switch(type) {
            case control_in: {
                int chan_id = inst.getComp().getComp().getChannels().getChannel(ChannelType.control_in).getId();
                return inst.getChan(chan_id).size();
            }
            case control_out: {
                int chan_id = inst.getComp().getComp().getChannels().getChannel(ChannelType.control_out).getId();
                return inst.getChan(chan_id).size();
            }
            case input_count:
                return paramToInteger(inst, ParameterType.input_count);
            case output_count:
                return paramToInteger(inst, ParameterType.output_count);
            case port_count:
                return paramToInteger(inst, ParameterType.port_count);
        }
        return null;
    }

    private static Integer paramToInteger(HSComponentInst inst, ParameterType param) {
        Object paramObj = inst.getType().getParamValue(param);
        Integer paramInt = null;
        if(paramObj instanceof Integer) {
            paramInt = (Integer)paramObj;
        }
        return paramInt;
    }
}
