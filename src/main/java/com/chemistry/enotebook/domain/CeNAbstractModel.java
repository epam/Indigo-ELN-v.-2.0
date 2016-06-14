package com.chemistry.enotebook.domain;

public abstract class CeNAbstractModel implements java.io.Serializable {

    // If you dont explicitly define a serialVersionUID, the language requires that the VM generate one,
    // using some function of all field and method names in the class.Some compilers add synthetic methods
    // that can cause different values for same class compiled with different compilers.
    // So every class should have explicit definition of serialVersionUID
    public static final long serialVersionUID = 7526472295622776147L;
    // Every POJO should implement this method to release memory
    /*
	 * public void dispose() throws Throwable{ finalize(); }
	 */
    // GUID generated key ( Also Key in Database if highlevel object)
    // This key value is important for an object that represents a CeN table record
    // Ex: NotebookPageModel,ReactionStepModel,ReactionSchemeModel,BatchModel,ParentCompoundModel
    // Classes used as it is as they are pure POJOs in 1.1
    // BatchNumber
    // BatchType
    // UnitType
    // NotebookRef
    // ReactionType
    //Boolean flag to identify if a model has changed.Model itself is responsible
    //for changing the flag when data of interest has changed and that needs to be persisited in DB
    boolean modelChanged = false;
    //Flag to indicate if the object is loaded from DB or new obj in memory

    /**
     * Set the Boolean flag to identify if a model has changed.  Model itself is responsible
     * for changing the flag when data of interest has changed and that needs to be persisited in DB
     */
    protected void setModelChanged(boolean hasChanged) {
        this.modelChanged = hasChanged;
    }

    //This was added for 1.1 compatabiity
    public void setModified(boolean bol) {
        setModelChanged(bol);
    }


    boolean isBeingCloned() {
        return false;
    }

}

