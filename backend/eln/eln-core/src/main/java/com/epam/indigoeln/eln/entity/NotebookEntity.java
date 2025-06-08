package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.config.hibernate.ExperimentCountArrayType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"}, includeFieldNames = false)
@Entity(name = "Notebook")
@Table(name = "Notebook_View")
@NamedEntityGraph(
        name = "Notebook.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("aclShort"),
                @NamedAttributeNode("aclCount"),
        }
)
@NamedEntityGraph(
        name = "Notebook.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("aclEntities"),
        }
)
@NamedEntityGraph(
        name = "Notebook.withACL",
        attributeNodes = {
                @NamedAttributeNode("aclEntities")
        }
)
public class NotebookEntity extends BaseEntity implements WithAttachments, WithACL<NotebookACLEntity> {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id", updatable = false)
    private ProjectEntity project;

    @NotEmpty
    private String name;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector", insertable = false, updatable = false)
    private String searchVector;

    @Basic
    @Nullable
    @Column(name = "current_access", insertable = false, updatable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccessLevel currentAccess;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "experiment_count", insertable = false, updatable = false)
    @Type(ExperimentCountArrayType.class)
    private Map<ExperimentStatus, Integer> experimentCount;

    @NotNull
    @OneToMany(mappedBy = "notebook")
    private Set<ExperimentEntity> experiments = new HashSet<>(0);

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "acl_short", insertable = false, updatable = false)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] aclShort;

    @NotNull
    @Basic(fetch =  FetchType.LAZY)
    @Column(name = "acl_count", insertable = false, updatable = false)
    private Integer aclCount;

    @NotNull
    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, NotebookACLEntity> aclEntities;

    @NotNull
    @ManyToMany()
    @JoinTable(name = "notebook_attachment", joinColumns = @JoinColumn(name = "notebook_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @Override
    public void insertACL(UserEntity user, AccessLevel access, Boolean inherited) {
        getAclEntities().put(user, new NotebookACLEntity(this, user, access, inherited));
    }
}
