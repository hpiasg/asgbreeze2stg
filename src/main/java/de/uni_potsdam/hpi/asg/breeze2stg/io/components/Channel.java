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
import javax.xml.bind.annotation.XmlEnum;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class Channel {
    @XmlEnum
    public enum ScaleType {
        port_count, // 
        input_count, //
        output_count, //
        control_in, // 
        control_out, //
        guard_count
    }

    //@formatter:off
    @XmlAttribute(name = "stgname", required = true)
    protected String stgName;
    @XmlAttribute(name = "scale", required = false)
    protected ScaleType scale;
    //@formatter:on

    public ScaleType getScale() {
        return scale;
    }

    public String getStgName() {
        return stgName;
    }
}
