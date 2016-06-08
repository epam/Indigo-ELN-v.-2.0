package com.chemistry.enotebook.domain;


class GenericCodeModel extends CeNAbstractModel {

    private static final long serialVersionUID = -3999094756405895275L;

    String genericCode = "";
    private String genericDescription = "";

    private GenericCodeModel(String code, String desc) {
        this.genericCode = code;
        this.genericDescription = desc;
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
        if (description == null)
            this.genericDescription = "";
        else
            this.genericDescription = description;
    }

}
