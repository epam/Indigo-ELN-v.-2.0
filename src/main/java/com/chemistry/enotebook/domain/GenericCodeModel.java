package com.chemistry.enotebook.domain;


class GenericCodeModel extends CeNAbstractModel {

    private static final long serialVersionUID = -3999094756405895275L;

    String genericCode = "";

    private GenericCodeModel(String code, String desc) {
        this.genericCode = code;
    }

    GenericCodeModel(String code) {
        this(code, "");
    }

    public String getCode() {
        return genericCode;
    }

    public void setCode(String code) {
        if (genericCode == null)
            genericCode = "";
        else
            this.genericCode = code;
    }

    public void setDescription(String description) {
        // do nothing
    }

}
