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
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"}, includeFieldNames = false)
@Entity(name = "Project")
@Table(name = "Project_View")
@NamedEntityGraph(
        name = "Project.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("notebookCount"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("aclShort"),
                @NamedAttributeNode("aclCount"),
        }
)
@NamedEntityGraph(
        name = "Project.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("keywords"),
                @NamedAttributeNode("notebookCount"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("aclEntities"),
        }
)
public class ProjectEntity extends BaseEntity implements WithAttachments, WithACL<ProjectACLEntity> {

    @NotEmpty
    private String name;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String literature;

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
    @Column(name = "notebook_count", insertable = false, updatable = false)
    private Integer notebookCount;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "experiment_count", insertable = false, updatable = false)
    @Type(ExperimentCountArrayType.class)
    private Map<ExperimentStatus, Integer> experimentCount;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "acl_short", insertable = false, updatable = false)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] aclShort;

    @NotNull
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, ProjectACLEntity> aclEntities;

    @NotNull
    @Basic(fetch =  FetchType.LAZY)
    @Column(name = "acl_count", insertable = false, updatable = false)
    private Integer aclCount;

    @NotNull
    @ManyToMany
    @OrderColumn(name = "ordinal")
    @JoinTable(name = "project_keyword", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    private List<DictionaryItemEntity> keywords = new ArrayList<>(0);

    @NotNull
    @OneToMany(mappedBy = "project")
    private Set<NotebookEntity> notebooks = new HashSet<>(0);

    @NotNull
    @OneToMany(mappedBy = "project")
    private Set<ExperimentEntity> experiments = new HashSet<>(0);

    @NotNull
    @ManyToMany
    @JoinTable(name = "project_attachment", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @Override
    public void insertACL(UserEntity user, AccessLevel access, Boolean inherited) {
        getAclEntities().put(user, new ProjectACLEntity(this, user, access));
    }
}
