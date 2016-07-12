package com.epam.indigoeln.core.service.search;

import java.util.Collection;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;

public interface SearchServiceAPI {

    Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest);

    Info getInfo();

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
