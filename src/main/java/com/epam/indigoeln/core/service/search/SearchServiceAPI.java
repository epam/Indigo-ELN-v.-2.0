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
package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;

import java.util.Collection;

/**
 * Provides functionality for search product batch details.
 */
public interface SearchServiceAPI {

    Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest);

    /**
     * Return information.
     *
     * @return information
     */
    Info getInfo();

    /**
     * Describes Info object.
     */
    class Info {

        private int key;

        private String value;

        private boolean isChecked;

        public Info() {
        }

        public Info(int key, String value, boolean isChecked) {
            this.key = key;
            this.value = value;
            this.isChecked = isChecked;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isIsChecked() {
            return isChecked;
        }

        public void setIsIsChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
    }
}
