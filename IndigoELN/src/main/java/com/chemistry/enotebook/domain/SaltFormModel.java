package com.chemistry.enotebook.domain;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class SaltFormModel extends GenericCodeModel {

    private static final long serialVersionUID = 8174943220379293210L;

    private static final Log LOGGER = LogFactory.getLog(SaltFormModel.class);

    private static final String PARENT = "00";
    private static final String UNKNOWN = "99";

    private double molWgt = 0.0;

    SaltFormModel(String code) {
        super(code);
        updateValuesBasedOnCode();
    }

    /**
     * Extended this to treat the Parent Salt code and Unknown Salt form the same
     */
    private static boolean isParentCode(String testCode) {
        return StringUtils.isBlank(testCode) || testCode.trim().equalsIgnoreCase(PARENT) || testCode.trim().equalsIgnoreCase(UNKNOWN);
    }

    private void updateValuesBasedOnCode() {
        try {
            //TODO REWRITE
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch salt metadata from SaltCodeCache.", e);
        }
    }

    @Override
    public void setCode(String newCode) {
        String newCodeNonNull = newCode == null ? "" : newCode;

        if (!genericCode.equals(newCodeNonNull)) {
            if (!isParentCode(newCodeNonNull)) {
                genericCode = newCodeNonNull;
                updateValuesBasedOnCode();
            } else {
                //setCode("00");  // vb 11/16 to avoid stack overflow
                genericCode = PARENT;
                setDescription("");

                molWgt = 0.0;
            }
        }
    }

    double getMolWgt() {
        return molWgt;
    }

    @Override
    public boolean equals(Object value) {
        boolean result = false;
        if (value instanceof SaltFormModel) {
            SaltFormModel sf = (SaltFormModel) value;
            result = this.getCode().equals(sf.getCode());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode();
    }
}
