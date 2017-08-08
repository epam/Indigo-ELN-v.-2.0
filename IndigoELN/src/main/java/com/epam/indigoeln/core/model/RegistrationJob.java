package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Document(collection = RegistrationJob.COLLECTION_NAME)
public class RegistrationJob implements Serializable {

    private static final long serialVersionUID = 5186246198191004066L;

    public static final String COLLECTION_NAME = "registration_job";

    @Id
    private String id;

    private RegistrationStatus.Status registrationStatus;
    private String registrationJobId;
    private String registrationRepositoryId;
    private String lastHandledBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RegistrationStatus.Status getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus.Status registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getRegistrationJobId() {
        return registrationJobId;
    }

    public void setRegistrationJobId(String registrationJobId) {
        this.registrationJobId = registrationJobId;
    }

    public String getRegistrationRepositoryId() {
        return registrationRepositoryId;
    }

    public void setRegistrationRepositoryId(String registrationRepositoryId) {
        this.registrationRepositoryId = registrationRepositoryId;
    }

    public String getLastHandledBy() {
        return lastHandledBy;
    }

    public void setLastHandledBy(String lastHandledBy) {
        this.lastHandledBy = lastHandledBy;
    }
}
