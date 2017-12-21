package com.epam.indigoeln.core.model;

/**
 * Name of entity where user has been added to
 */
public enum FirstEntityName {

    PROJECT("Project"),
    NOTEBOOK("Notebook"),
    EXPERIMENT("Experiment");

    private final String value;

    FirstEntityName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Checking of name (If false, user can be added to another entity)
     * @param firstEntityName
     * @param entity
     * @return
     */
    public static boolean firstEntityIsInner(FirstEntityName firstEntityName, FirstEntityName entity){
        if(firstEntityName.equals(FirstEntityName.PROJECT)){
            return false;
        }
        if(firstEntityName.equals(FirstEntityName.NOTEBOOK) && entity.equals(FirstEntityName.PROJECT)){
            return true;
        }
        if (firstEntityName.equals(FirstEntityName.EXPERIMENT)) {
            return true;
        }
        return true;
    }
}
