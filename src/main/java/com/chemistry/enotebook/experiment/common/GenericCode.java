package com.chemistry.enotebook.experiment.common;

import com.chemistry.enotebook.experiment.common.interfaces.DeepClone;
import com.chemistry.enotebook.experiment.common.interfaces.DeepCopy;

public class GenericCode extends ObservableObject implements DeepCopy,
        DeepClone {
    private static final long serialVersionUID = -6038505473833465183L;

    private static final int HASH_PRIME = 37967;

    protected String code = "";
    protected String description = "";

    public GenericCode() {
    }

    private GenericCode(String code, String descr) {
        super();
        this.code = code;
        if (descr != null)
            description = descr;
    }

    public String getCode() {
        return code;
    }


    public void deepCopy(Object resource) {
        if (resource instanceof GenericCode) {
            GenericCode genCode = (GenericCode) resource;
            code = genCode.code;
            description = genCode.description;
            if (!genCode.isLoading()) {
                setModified(true);
            }
        }
    }

    public Object deepClone() {
        return new GenericCode(code, description);
    }

    public boolean equals(Object value) {
        boolean result = false;
        if (value instanceof GenericCode) {
            GenericCode gCode = (GenericCode) value;
            result = this.hashCode() == gCode.hashCode();
        }
        return result;
    }

    public int hashCode() {
        return HASH_PRIME + HASH_PRIME
                * (code.hashCode() + description.hashCode());
    }

}
