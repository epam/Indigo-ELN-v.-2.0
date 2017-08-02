package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) { //NOSONAR Equals should do equality
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistrationJob that = (RegistrationJob) o;
        return Objects.equals(id, that.id) &&
                registrationStatus == that.registrationStatus &&
                Objects.equals(registrationJobId, that.registrationJobId) &&
                Objects.equals(registrationRepositoryId, that.registrationRepositoryId) &&
                Objects.equals(lastHandledBy, that.lastHandledBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, registrationStatus, registrationJobId, registrationRepositoryId, lastHandledBy);
    }

    @Override
    public String toString() {
        return "RegistrationJob{" +
                "id='" + id + '\'' +
                ", registrationStatus=" + registrationStatus +
                ", registrationJobId='" + registrationJobId + '\'' +
                ", registrationRepositoryId='" + registrationRepositoryId + '\'' +
                ", lastHandledBy='" + lastHandledBy + '\'' +
                '}';
    }
}
