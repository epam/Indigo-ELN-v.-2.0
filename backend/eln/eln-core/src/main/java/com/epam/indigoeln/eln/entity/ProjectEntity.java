package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.config.hibernate.ExperimentCountArrayType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
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
@NamedEntityGraph(
        name = "Project.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("shortACL"),
                @NamedAttributeNode("notebookCount"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode(value = "calculatedInfo", subgraph = "Project.calculatedInfo.list")
        },
        subgraphs = @NamedSubgraph(
                name = "Project.calculatedInfo.list",
                attributeNodes = {
                        @NamedAttributeNode("aclCount")
                }
        )
)
@NamedEntityGraph(
        name = "Project.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("keywords"),
                @NamedAttributeNode("fullACL"),
                @NamedAttributeNode("notebookCount"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode(value = "calculatedInfo", subgraph = "Project.calculatedInfo.details")
        },
        subgraphs = @NamedSubgraph(
                name = "Project.calculatedInfo.details",
                attributeNodes = {
                        @NamedAttributeNode("currentAccess"),
                }
        )
)
@DynamicUpdate
public class ProjectEntity extends BaseEntity implements WithAttachments, WithACL<ProjectACLEntity>, WithRevision {

    @NotEmpty(message = "Project Name is required")
    @Size(max = 256, message = "Project name must be at most 256 characters")
    private String name;

    @NotNull
    private Integer revision;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String literature;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] shortACL;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] fullACL;

    @NotNull
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, ProjectACLEntity> aclEntities = new HashMap<>(0);

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

    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    private Integer notebookCount;

    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    @Type(ExperimentCountArrayType.class)
    private Map<ExperimentStatus, Integer> experimentCount;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private CalculatedInfo calculatedInfo;

    @Override
    public void insertACL(UserEntity user, AccessLevel access) {
        getAclEntities().put(user, new ProjectACLEntity(this, user, access));
    }

    @Override
    @Nullable
    @Transient
    public WithACL<?> getACLParent() {
        return null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity(name = "ProjectCalculatedInfo")
    @Table(name = "Project_View_2")
    public static class CalculatedInfo extends IdentifiableEntity {

        @Basic
        @Nullable
        @Column(insertable = false, updatable = false)
        @JdbcType(PostgreSQLEnumJdbcType.class)
        private AccessLevel currentAccess;

        @NotNull
        @Basic(fetch =  FetchType.LAZY)
        @Column(insertable = false, updatable = false)
        private Integer aclCount;
    }
}
