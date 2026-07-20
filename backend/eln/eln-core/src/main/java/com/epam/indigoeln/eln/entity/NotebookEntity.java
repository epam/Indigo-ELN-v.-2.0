package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.config.hibernate.ExperimentCountArrayType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.NamedEntityGraph;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"}, includeFieldNames = false)
@Entity(name = "Notebook")
@SecondaryTable(name = "Notebook_Access_View",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "notebook_id", referencedColumnName = "id")
)
@NamedEntityGraph(
        name = "Notebook.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("shortACL"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("aclCount")
        }
)
@NamedEntityGraph(
        name = "Notebook.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("fullACL"),
                @NamedAttributeNode("experimentCount"),
                @NamedAttributeNode("currentAccessOrNull"),
                @NamedAttributeNode("attachments")
        }
)
@NamedEntityGraph(
        name = "Notebook.withACL",
        attributeNodes = {
                @NamedAttributeNode("aclEntities"),
                @NamedAttributeNode("experiments")
        }
)
@DynamicUpdate
public class NotebookEntity extends BaseEntity implements WithAttachments, WithACL<NotebookACLEntity>, WithRevision {

    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private ProjectEntity project;

    @NotEmpty
    @Pattern(regexp = "^\\d{8}$", message = "Notebook Name is invalid, use 8 digits only")
    private String name;

    @NotNull
    private Integer revision;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;

    @NotNull
    @OneToMany(mappedBy = "notebook")
    private Set<ExperimentEntity> experiments = new HashSet<>(0);

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] shortACL;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] fullACL;

    @NotNull
    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, NotebookACLEntity> aclEntities = new HashMap<>(0);

    @NotNull
    @OneToMany(mappedBy = "notebook")
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    @Type(ExperimentCountArrayType.class)
    private Map<ExperimentStatus, Integer> experimentCount;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Notebook_Access_View", insertable = false, updatable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("access_view")
    private AccessLevel currentAccessOrNull;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Notebook_Access_View", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("access_view")
    private Integer aclCount;

    @Override
    public void insertACL(UserEntity user, AccessLevel access) {
        getAclEntities().put(user, new NotebookACLEntity(this, user, access));
    }

    @Override
    @Transient
    public WithACL<?> getACLParent() {
        return project;
    }

    @Transient
    public AccessLevel getCurrentAccess() {
        return currentAccessOrNull != null ? currentAccessOrNull : AccessLevel.NONE;
    }
}
