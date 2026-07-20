package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.config.hibernate.ExperimentModelType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.ExperimentModel;
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
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"}, includeFieldNames = false)
@Entity(name = "Experiment")
@SecondaryTable(name = "Experiment_Access_View",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "experiment_id", referencedColumnName = "id")
)
@SecondaryTable(name = "Experiment_Marked_View",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "experiment_id", referencedColumnName = "id")
)
@NamedEntityGraph(
        name = "Experiment.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("shortACL"),
                @NamedAttributeNode("aclCount"),
                @NamedAttributeNode("markedOrNull"),
        }
)
@NamedEntityGraph(
        name = "Experiment.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("title"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode("literature"),
                @NamedAttributeNode("therapeuticArea"),
                @NamedAttributeNode("projectCode"),
                @NamedAttributeNode("aclEntities"),
                @NamedAttributeNode("model"),
                @NamedAttributeNode("batchCreator"),
                @NamedAttributeNode("linkedExperiments"),
                @NamedAttributeNode("continuedFrom"),
                @NamedAttributeNode("continuedTo"),
                @NamedAttributeNode("currentAccessOrNull"),
                @NamedAttributeNode("markedOrNull"),
                @NamedAttributeNode("attachments")
        }
)
@NamedEntityGraph(
        name = "Experiment.withACL",
        attributeNodes = {
                @NamedAttributeNode("aclEntities"),
        }
)
@NamedEntityGraph(
        name = "Experiment.forRef",
        attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("name"),
        }
)
@DynamicUpdate
public class ExperimentEntity extends BaseEntity implements WithAttachments, WithACL<ExperimentACLEntity>, WithRevision {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private ProjectEntity project;

    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private NotebookEntity notebook;

    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private TemplateEntity template;

    @NotEmpty
    @Pattern(regexp = "^\\d{8}-\\d{4}$")
    private String name;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ExperimentStatus status;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private DictionaryItemEntity therapeuticArea;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private DictionaryItemEntity projectCode;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String literature;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity batchCreator;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] linkedExperiments = new UUID[0];

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] continuedFrom = new UUID[0];

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] continuedTo = new UUID[0];

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(ExperimentModelType.class)
    private ExperimentModel model;

    @Basic(fetch = FetchType.LAZY)
    private byte @Nullable [] picture;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] shortACL;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] fullACL;

    @NotNull
    private Boolean deleted;

    @NotNull
    private Integer revision;

    @Nullable
    private Integer version;

    @Nullable
    private String signatureNumber;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_attachment_id")
    private AttachmentEntity signatureAttachment;

    @NotNull
    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, ExperimentACLEntity> aclEntities = new HashMap<>(0);

    @NotNull
    @ManyToMany
    @JoinTable(name = "experiment_attachment", joinColumns = @JoinColumn(name = "experiment_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @NotNull
    @ElementCollection
    @CollectionTable(name = "Experiment_Referenced_Compound", joinColumns = @JoinColumn(name = "experiment_id"))
    private Set<ExperimentReferencedCompound> referencedCompounds = new HashSet<>(0);

    @NotNull
    @ElementCollection
    @CollectionTable(name = "Experiment_Rxnfile", joinColumns = @JoinColumn(name = "experiment_id"))
    @Column(name = "rxnfile")
    @OrderColumn(name = "ordinal")
    private List<String> rxnfiles = new ArrayList<>(0);

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Experiment_Access_View", insertable = false, updatable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("access_view")
    private AccessLevel currentAccessOrNull;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Experiment_Access_View", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("access_view")
    private Integer aclCount;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Experiment_Marked_View", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("marked_view")
    private Boolean markedOrNull;

    @Override
    public void insertACL(UserEntity user, AccessLevel access) {
        getAclEntities().put(user, new ExperimentACLEntity(this, user, access));
    }

    @Override
    @Transient
    public NotebookEntity getACLParent() {
        return notebook;
    }

    @Transient
    public AccessLevel getCurrentAccess() {
        return currentAccessOrNull != null ? currentAccessOrNull : AccessLevel.NONE;
    }

    @Transient
    public boolean isMarked() {
        return getMarkedOrNull() == Boolean.TRUE;
    }
}
