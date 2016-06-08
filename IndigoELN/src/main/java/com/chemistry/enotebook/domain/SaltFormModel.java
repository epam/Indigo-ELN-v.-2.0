package com.chemistry.enotebook.domain;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class SaltFormModel extends GenericCodeModel {

    private static final long serialVersionUID = 8174943220379293210L;

    private static final Log log = LogFactory.getLog(SaltFormModel.class);

    private static final String parent = "00";
    private static final String unknown = "99";
    private String formula = "";
    private double molWgt = 0.0;

    SaltFormModel(String code) {
        super(code);
        updateValuesBasedOnCode();
    }

    /**
     * Extended this to treat the Parent Salt code and Unknown Salt form the same
     */
    private static boolean isParentCode(String testCode) {
        return (StringUtils.isBlank(testCode) || testCode.trim().equalsIgnoreCase(parent) || testCode.trim().equalsIgnoreCase(unknown));
    }

    private void updateValuesBasedOnCode() {
        try {
            //TODO REWRITE
//			SaltCodeCache scc = SaltCodeCache.getCache();
//			setDescription(scc.getDescriptionGivenCode(getCode()));
//			setFormula(scc.getMolFormulaGivenCode(getCode()));
//			setMolWgt(scc.getMolWtGivenCode(getCode()));
        } catch (Throwable e) {
            log.warn("Failed to fetch salt metadata from SaltCodeCache.", e);
        }
    }

    public void setCode(String newCode) {
        if (newCode == null)
            newCode = "";
        if (!genericCode.equals(newCode)) {
            if (!isParentCode(newCode)) {
                //setCode(newCode);
                genericCode = newCode;
                updateValuesBasedOnCode();
            } else {
                //setCode("00");  // vb 11/16 to avoid stack overflow
                genericCode = parent;
                setDescription("");
                formula = "";
                molWgt = 0.0;
            }
        }
    }

    double getMolWgt() {
        return molWgt;
    }

    public boolean equals(Object value) {
        boolean result = false;
        if (value instanceof SaltFormModel) {
            SaltFormModel sf = (SaltFormModel) value;
            result = this.getCode().equals(sf.getCode());
        }
        return result;
    }
}
