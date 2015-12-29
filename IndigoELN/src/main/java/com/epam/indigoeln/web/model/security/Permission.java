package com.epam.indigoeln.web.model.security;

public interface Permission {

    String REVIEW_EXPERIMENT = "REVIEW_EXPERIMENT";
    String[] PERMISSIONS = { REVIEW_EXPERIMENT };

    static boolean isPermission(String value) {
        for (String permission : PERMISSIONS) {
            if (permission.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
