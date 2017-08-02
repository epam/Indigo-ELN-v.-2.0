package com.chemistry.enotebook.domain;


class GenericCodeModel extends CeNAbstractModel {

    private static final long serialVersionUID = -3999094756405895275L;

    String genericCode = "";

    GenericCodeModel(String code) {
        this.genericCode = code;
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
