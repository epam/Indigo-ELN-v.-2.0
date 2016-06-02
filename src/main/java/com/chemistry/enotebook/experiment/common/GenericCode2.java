package com.chemistry.enotebook.experiment.common;

import com.chemistry.enotebook.domain.CeNAbstractModel;
import com.chemistry.enotebook.experiment.common.interfaces.DeepClone;
import com.chemistry.enotebook.experiment.common.interfaces.DeepCopy;

public class GenericCode2 extends CeNAbstractModel implements DeepCopy, DeepClone {
    public static final long serialVersionUID = 7526472295622776147L;
    protected String code = "";
    protected String description = "";

    public GenericCode2() {
    }

    private GenericCode2(String code, String descr) {
        super();
        this.code = code;
        if (descr != null)
            description = descr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code != null) {
            if (!this.code.equals(code)) {
                if (!code.equals("") || code.equals("00"))
                    this.code = code;
                else {
                    this.code = "";
                    description = "";
                }
                setModelChanged(true);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void deepCopy(Object resource) {
        if (resource instanceof GenericCode2) {
            GenericCode2 genCode = (GenericCode2) resource;
            code = genCode.code;
            description = genCode.description;
            setModelChanged(true);
        }
    }

    public Object deepClone() {
        return new GenericCode2(code, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericCode2 that = (GenericCode2) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
