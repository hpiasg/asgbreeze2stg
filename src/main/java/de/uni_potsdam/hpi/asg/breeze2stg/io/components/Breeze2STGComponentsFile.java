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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Breeze2STGComponentsFile {
    private static final Logger logger        = LogManager.getLogger();
    private static final String injarfilename = "/breeze2stgComponents.xml";

    public static Breeze2STGComponents readIn(String filename) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Breeze2STGComponents.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            if(filename == null || filename.equals("")) {
                InputStream inputStream = Breeze2STGComponent.class.getResourceAsStream(injarfilename);
                return (Breeze2STGComponents)jaxbUnmarshaller.unmarshal(inputStream);
            } else {
                File file = new File(filename);
                if(file.exists()) {
                    return (Breeze2STGComponents)jaxbUnmarshaller.unmarshal(file);
                } else {
                    logger.error("File " + filename + " not found");
                    return null;
                }
            }
        } catch(JAXBException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public static boolean writeOut(Breeze2STGComponents comps, File file) {
        try {
            Writer fw = new FileWriter(file);
            JAXBContext context = JAXBContext.newInstance(Breeze2STGComponents.class);
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
