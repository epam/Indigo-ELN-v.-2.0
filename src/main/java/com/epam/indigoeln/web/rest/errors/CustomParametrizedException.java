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
package com.epam.indigoeln.web.rest.errors;

/**
 * Custom, parametrized exception, which can be translated on the client side.
 * For example:
 * <p>
 * <pre>
 * throw new CustomParametrizedException(&quot;myCustomError&quot;, &quot;hello&quot;, &quot;world&quot;);
 * </pre>
 * <p>
 * Can be translated with:
 * <p>
 * <pre>
 * "error.myCustomError" :  "The server says {{params[0]}} to {{params[1]}}"
 * </pre>
 */
public class CustomParametrizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final String[] params;

    public CustomParametrizedException(Exception cause) {
        this(cause.getMessage(), cause);
    }

    public CustomParametrizedException(String message, String... params) {
        this(message, null, params);
    }

    public CustomParametrizedException(String message, Exception cause, String... params) {
        super(message, cause);
        this.message = message;
        this.params = params;
    }

    public ParametrizedErrorDTO getErrorDTO() {
        return new ParametrizedErrorDTO(message, params);
    }

}
