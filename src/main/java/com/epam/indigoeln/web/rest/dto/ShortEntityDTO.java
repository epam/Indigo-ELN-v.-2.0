package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.util.JSR310DateConverters;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Short data for Model Entities : Experiments, Notebooks and Projects.
 * Could be used in case, when all items should be fetched from database.
 */
public class ShortEntityDTO {

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

    public ShortEntityDTO(DBObject obj, Map<Object, DBObject> users) {
        this.fullId = String.valueOf(obj.get("_id"));
        this.id = SequenceIdUtil.extractShortId(this.fullId);
        this.parentId = SequenceIdUtil.extractParentId(this.fullId);
        this.name = String.valueOf(obj.get("name"));
        this.lastEditDate = JSR310DateConverters.DateToZonedDateTimeConverter.INSTANCE.convert((Date) obj.get("lastEditDate"));
        this.creationDate = JSR310DateConverters.DateToZonedDateTimeConverter.INSTANCE.convert((Date) obj.get("creationDate"));

        val author = users.get(((DBRef) obj.get("author")).getId());
        if (author != null) {
            this.author = new ShortUserDTO(author);
        }

        val lastModifiedBy = users.get(((DBRef) obj.get("lastModifiedBy")).getId());
        if (lastModifiedBy != null) {
            this.lastModifiedBy = new ShortUserDTO(lastModifiedBy);
        }
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
        return "ShortEntityDTO{"
                + ", fullId='" + fullId + '\''
                + ", name='" + name + '\''
                + '}';
    }
}
