package de.uni_potsdam.hpi.asg.breeze2stg.io.config;

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "breeze2stgconfig")
@XmlAccessorType(XmlAccessType.NONE)
public class Config {

    //@formatter:off
    
    @XmlElement(name = "generalComponents", required = true)
    public String generalComponentConfig;
    @XmlElement(name = "breeze2stgComponents", required = true)
    public String breeze2stgComponentConfig;
    @XmlElement(name = "workdir", required = false)
    public String workdir;
    
    //@formatter:on
}
