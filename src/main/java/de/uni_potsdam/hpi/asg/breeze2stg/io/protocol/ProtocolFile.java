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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtocolFile {
    private static final Logger logger = LogManager.getLogger();

    public static Protocol readIn(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Protocol.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            if(file.exists()) {
                Protocol retVal = (Protocol)jaxbUnmarshaller.unmarshal(file);
                retVal.setProtocolDir(file.getParentFile());
                return retVal;
            } else {
                logger.error("File " + file.getAbsolutePath() + " not found");
                return null;
            }
        } catch(JAXBException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public static boolean writeOut(Protocol comps, File file) {
        try {
            Writer fw = new FileWriter(file);
            JAXBContext context = JAXBContext.newInstance(Protocol.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(comps, fw);
            return true;
        } catch(JAXBException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(IOException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }
}
