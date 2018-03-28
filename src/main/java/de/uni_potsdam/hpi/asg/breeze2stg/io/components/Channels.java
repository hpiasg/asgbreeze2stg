package de.uni_potsdam.hpi.asg.breeze2stg.io.components;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Channels {
    //@formatter:off
    @XmlElement(name = "balsachannel")
    private List<BalsaChannel> balsaChannels;
    @XmlElement(name = "internalchannel")
    private List<InternalChannel> internalChannels;
    //@formatter:on

    private Set<Channel>          allChannels;

    public List<BalsaChannel> getBalsaChannels() {
        return balsaChannels;
    }

    public List<InternalChannel> getInternalChannels() {
        return internalChannels;
    }

    public Set<Channel> getAllChannels() {
        if(allChannels == null) {
            allChannels = new HashSet<>();
            allChannels.addAll(balsaChannels);
            if(internalChannels != null) {
                allChannels.addAll(internalChannels);
            }
        }
        return allChannels;
    }
}
