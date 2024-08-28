/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.controllers;

import com.chemistry.enotebook.signature.SignatureUtil;
import com.chemistry.enotebook.signature.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/")
public class EasySignatureController {
    private static Logger log = LoggerFactory.getLogger(EasySignatureController.class);

    @Autowired
    private SignatureUtil signatureUtil;

    @RequestMapping(value = "/signDocument", method = RequestMethod.POST)
    public @ResponseBody
    String signDocument(@RequestParam(value = "id") String id,
                        @RequestParam(value = "file", required = false) MultipartFile file,
                        @RequestParam(value = "password", required=false) String keyStoragePassword,
                        @RequestParam(value = "comment", required = false) String comment, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            return signatureUtil.doSign(id, Util.getUsername(request), comment, file.getBytes(), keyStoragePassword);
        } catch (Exception e) {
            log.error("failed to sign", e);
            return Util.generateErrorJsonString(e.getLocalizedMessage());
        }
    }
}
