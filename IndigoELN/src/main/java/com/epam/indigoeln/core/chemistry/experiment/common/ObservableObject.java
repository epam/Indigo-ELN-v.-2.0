package com.epam.indigoeln.core.chemistry.experiment.common;

import java.util.Observable;
import java.util.Observer;

class ObservableObject extends Observable implements Observer, java.io.Serializable {

    private static final long serialVersionUID = 4121854823514769532L;

    /**
     * Flag set to let others know some value in this object has changed. Should use a combination of subObjectModified and
     * isModified to indicate what has been modified. If isChanging = true. No observers of this object will be notified, but the
     * modified flag can be set. If isLoading = true then the modified flag will not be set, nor will any observers be notified.
     *
     * @param modifiedFlag -
     *                     The object that has it's isModified() flag set.
     */
    public void setModified(boolean modifiedFlag) {
        if (!isLoading() && modifiedFlag) {
            setChanged();
            notifyObservers(this);
        }
    }


    /**
     * Method to determine if this object is currently being loaded.
     */
    boolean isLoading() {
        return false;
    }

    /**
     * Override this method if you are trying to modify behavior. This update sets subObjectModified to true and passes the sub
     * object along to the next level indicating that it is the modified object if it is an instance of PersistableObject. Otherwise
     * it sets the modified flag of this object which will pass this object back through notifyObservers(obj)
     *
     * @param observed -
     *                 The object performing the notification
     * @param obj      -
     *                 if present, the object that has been modified: i.e. isModified() returns true;
     */
    public void update(Observable observed, Object obj) {
        // determine if we are passing a modified PersistableObject
        // up the chain.
        // We are changing and need to reflect that upwards
        this.setModified(true);
    }
}
