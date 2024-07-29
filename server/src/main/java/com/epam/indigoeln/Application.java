/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln;

import com.epam.indigo.Indigo;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfConst;
import com.lowagie.text.FontFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public interface Profile {
        String DEV = "dev";
        String CORS = "cors";
    }

    /**
     * Main method, used to run the application.
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        new Indigo();
        FontFactory.register(PdfConst.MAIN_FONT_FAMILY_DIR, PdfConst.MAIN_FONT_FAMILY);
        SpringApplication.run(Application.class, args);
    }

    /**
     * Run application in servlet container.
     *
     * @param builder Builder
     * @return Spring application builder
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }
}
