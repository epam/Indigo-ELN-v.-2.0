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
package com.chemistry.enotebook.signature.exception;

import com.chemistry.enotebook.signature.IndigoSignatureServiceException;

public class MissingDocumentException extends IndigoSignatureServiceException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8767842142486882247L;

	public MissingDocumentException(Exception e) {
		super(e);
	}

	public MissingDocumentException(String s) {
		super(s);
	}
}
