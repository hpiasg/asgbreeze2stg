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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Breeze2STGComponent {

    //@formatter:off
    @XmlAttribute(name = "breezename", required = true)
    private String breezename;
    @XmlElement(name = "channels")
    private Channels channels;
    //@formatter:on

    public Channel getChannelByStgName(String stgname) {
        for(Channel chan : channels.getBalsaChannels()) {
            if(chan.getStgName().equals(stgname)) {
                return chan;
            }
        }
        for(Channel chan : channels.getInternalChannels()) {
            if(chan.getStgName().equals(stgname)) {
                return chan;
            }
        }
        return null;
    }

    public String getBreezename() {
        return breezename;
    }

    public Channels getChannels() {
        return channels;
    }
}
