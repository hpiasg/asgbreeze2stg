package de.uni_potsdam.hpi.asg.breeze2stg.io.components;

/*
 * Copyright (C) 2018-2022 Norman Kluge
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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "breeze2stgComponents")
@XmlAccessorType(XmlAccessType.NONE)
public class Breeze2STGComponents {
    protected static final Logger     logger = LogManager.getLogger();

    //@formatter:off
    @XmlElement(name = "component")
    private List<Breeze2STGComponent> components;
    //@formatter:on

    protected Breeze2STGComponents() {
    }

    public List<Breeze2STGComponent> getComponents() {
        return components;
    }

    public Breeze2STGComponent getComponentByName(String name) {
        for(Breeze2STGComponent comp : components) {
            if(comp.getBreezename().equals(name)) {
                return comp;
            }
        }
        return null;
    }
}
