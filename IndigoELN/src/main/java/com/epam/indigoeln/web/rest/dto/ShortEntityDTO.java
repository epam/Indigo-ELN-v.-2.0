package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Short data for Model Entities : Experiments, Notebooks and Projects
 * Could be used in case, when all items should be fetched from database
 */
public class ShortEntityDTO {

    private static final long serialVersionUID = 6066467687623776425L;

    private String id;
    private String parentId;
    private String fullId;
    private String name;

    private ZonedDateTime lastEditDate;
    private ZonedDateTime creationDate;

    private ShortUserDTO author;
    private ShortUserDTO lastModifiedBy;

    public ShortEntityDTO(BasicModelObject obj) {

        this.fullId = obj.getId();
        this.id = SequenceIdUtil.extractShortId(obj);
        this.parentId = SequenceIdUtil.extractParentId(obj);
        this.name = obj.getName();
        this.lastEditDate = obj.getLastEditDate();
        this.creationDate = obj.getCreationDate();
        this.author = obj.getAuthor() != null ? new ShortUserDTO(obj.getAuthor()) : null;
        this.lastModifiedBy = obj.getLastModifiedBy() != null ? new ShortUserDTO(obj.getLastModifiedBy()) : null;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getFullId() {
        return fullId;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getLastEditDate() {
        return lastEditDate;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public ShortUserDTO getAuthor() {
        return author;
    }

    public ShortUserDTO getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public String toString() {
        return "ShortEntityDTO{" +
                ", fullId='" + fullId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
