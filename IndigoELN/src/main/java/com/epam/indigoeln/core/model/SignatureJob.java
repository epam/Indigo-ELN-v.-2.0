package com.epam.indigoeln.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document(collection = SignatureJob.COLLECTION_NAME)
public class SignatureJob implements Serializable {

    private static final long serialVersionUID = -1294065072311648742L;

    public static final String COLLECTION_NAME = "signature_job";

    @Id
    private String id;

    private ExperimentStatus experimentStatus;
    private String experimentId;
    private String lastHandledBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExperimentStatus getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(ExperimentStatus experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getLastHandledBy() {
        return lastHandledBy;
    }

    public void setLastHandledBy(String lastHandledBy) {
        this.lastHandledBy = lastHandledBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignatureJob that = (SignatureJob) o;
        return Objects.equals(id, that.id) &&
                experimentStatus == that.experimentStatus &&
                Objects.equals(experimentId, that.experimentId) &&
                Objects.equals(lastHandledBy, that.lastHandledBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, experimentStatus, experimentId, lastHandledBy);
    }

    @Override
    public String toString() {
        return "SignatureJob{" +
                "id='" + id + '\'' +
                ", experimentStatus=" + experimentStatus +
                ", experimentId='" + experimentId + '\'' +
                ", lastHandledBy='" + lastHandledBy + '\'' +
                '}';
    }
}
