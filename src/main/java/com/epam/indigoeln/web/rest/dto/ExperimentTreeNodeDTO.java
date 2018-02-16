package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.mongodb.DBObject;
import lombok.val;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A DTO for representing Experiment like a Tree Node with its properties.
 */
public class ExperimentTreeNodeDTO extends TreeNodeDTO {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EE MMM dd HH:mm:ss z yyyy");

    private String status;
    private String authorFullName;
    private String reactionImage;
    private ZonedDateTime creationDate;

    public ExperimentTreeNodeDTO() {
        super();
    }

    public ExperimentTreeNodeDTO(Experiment experiment) {
        super(experiment);
        this.status = experiment.getStatus().toString();
        setName(experiment.getFullName());
    }

    public ExperimentTreeNodeDTO(DBObject obj) {
        super(obj);
        this.status = ExperimentStatus.fromValue(String.valueOf(obj.get("status"))).toString();
        this.creationDate = ZonedDateTime.parse(String.valueOf(obj.get("creationDate")),
                dateTimeFormatter.withZone(ZoneId.systemDefault()));
        val experimentName = String.valueOf(obj.get("name"));
        val experimentVersion = Integer.parseInt(String.valueOf(obj.get("experimentVersion")));
        val lastVersion = Boolean.parseBoolean(String.valueOf(obj.get("lastVersion")));

        if (experimentVersion > 1 || !lastVersion) {
            setName(experimentName + " v" + experimentVersion);
        } else {
            setName(experimentName);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public String getReactionImage() {
        return reactionImage;
    }

    public void setReactionImage(String reactionImage) {
        this.reactionImage = reactionImage;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
