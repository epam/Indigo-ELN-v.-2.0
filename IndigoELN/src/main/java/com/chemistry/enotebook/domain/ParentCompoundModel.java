package com.chemistry.enotebook.domain;


import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;


class ParentCompoundModel extends CeNAbstractModel {

    public static final long serialVersionUID = 7526472295622776147L;

    private double molWgt; // Equivalent to parent molecular weight

    private double getMolWgt() {
        return molWgt;
    }

    int compareTo(Object o) {
        int result = 0;
        if (o != null && o instanceof ParentCompoundModel) {
            ParentCompoundModel cp = (ParentCompoundModel) o;
            if (CeNNumberUtils.doubleEquals(getMolWgt(), cp.getMolWgt(), 0.00001))
                result = 0;
            else if (getMolWgt() - cp.getMolWgt() > 0)
                result = 1;
            else
                result = -1;
        }
        return result;
    }

}
