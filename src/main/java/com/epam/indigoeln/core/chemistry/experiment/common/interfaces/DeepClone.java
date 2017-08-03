package com.epam.indigoeln.core.chemistry.experiment.common.interfaces;

public interface DeepClone {
    /**
     * Performs a clone on the object and all subobjects such that there is no reference to the object being cloned.
     * <p>
     * Does not set Modified flag to true because no one is listening at the time of the clone. Calling object's responsibility to
     * set modified flag.
     *
     * @return Object of the same type as that being cloned
     */
    Object deepClone();
}
