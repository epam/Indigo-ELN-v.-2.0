package com.chemistry.enotebook.experiment.common.interfaces;

public interface DeepCopy {
    /**
     * Performs a clone on the object and all subobjects such that there is no reference to the object being cloned.
     * Does not set Modified flag to true because no one is listening at the time of the clone. Calling object's responsibility to
     * set modified flag.
     */
    void deepCopy(Object resource);
}
