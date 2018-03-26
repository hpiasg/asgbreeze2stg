package de.uni_potsdam.hpi.asg.breeze2stg.io.protocol;

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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlRootElement(name = "protocol")
@XmlAccessorType(XmlAccessType.NONE)
public class Protocol {
    protected static final Logger   logger = LogManager.getLogger();

    //@formatter:off
    @XmlElement(name = "component")
    private List<ProtocolComponent> components;
    //@formatter:on

    private File                    protocolDir;

    protected Protocol() {
    }

    public List<ProtocolComponent> getComponents() {
        return components;
    }

    public File getSTGFileForComponent(String name) {
        for(ProtocolComponent comp : components) {
            if(comp.getBreezename().equals(name)) {
                return new File(protocolDir, comp.getStgFileName());
            }
        }
        return null;
    }

    protected void setProtocolDir(File protocolDir) {
        this.protocolDir = protocolDir;
    }
}
