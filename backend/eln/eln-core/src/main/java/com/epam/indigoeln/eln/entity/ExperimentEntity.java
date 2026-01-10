package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.*;
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
@NamedEntityGraph(
        name = "Experiment.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("shortACL"),
                @NamedAttributeNode(value = "calculatedInfo", subgraph = "Experiment.calculatedInfo.list"),
        },
        subgraphs = @NamedSubgraph(
                name = "Notebook.calculatedInfo.list",
                attributeNodes = {
                        @NamedAttributeNode("aclCount"),
                        @NamedAttributeNode("marked"),
                }
        )
)
@NamedEntityGraph(
        name = "Experiment.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode("therapeuticArea"),
                @NamedAttributeNode("projectCode"),
                @NamedAttributeNode("aclEntities"),
                @NamedAttributeNode("signatures"),
                @NamedAttributeNode("model"),
                @NamedAttributeNode(value = "calculatedInfo", subgraph = "Experiment.calculatedInfo.details"),
        },
        subgraphs = @NamedSubgraph(
                name = "Experiment.calculatedInfo.details",
                attributeNodes = {
                        @NamedAttributeNode("currentAccess"),
                        @NamedAttributeNode("marked"),
                }
        )
)
@NamedEntityGraph(
        name = "Experiment.forSignature",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("signatures"),
        }
)
@NamedEntityGraph(
        name = "Experiment.withACL",
        attributeNodes = {
                @NamedAttributeNode("aclEntities"),
        }
)
@DynamicUpdate
public class ExperimentEntity extends BaseEntity implements WithAttachments, WithACL<ExperimentACLEntity> {

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
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    private AttachmentEntity reportForSignature;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    private String model;

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

    @NotNull
    private Integer lastUsedAnchor;

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
    @OneToMany(mappedBy = "experiment")
    @OrderBy("revision")
    private List<ExperimentRevisionEntity> revisions = new ArrayList<>(0);

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private CalculatedInfo calculatedInfo;

    @NotNull
    @OrderColumn(name = "ordinal")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "experiment")
    private List<ExperimentSignatureEntity> signatures = new ArrayList<>(0);

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

    @Override
    public void insertACL(UserEntity user, AccessLevel access) {
        getAclEntities().put(user, new ExperimentACLEntity(this, user, access));
    }

    @Override
    @Transient
    public NotebookEntity getACLParent() {
        return notebook;
    }

    @SuppressWarnings("unchecked")
    public <A extends Anchor> A generateNextAnchor(Class<A> klass) {
        int number = ++lastUsedAnchor;
        if (klass.equals(ReactionAnchor.class)) {
            return (A) new ReactionAnchor(number);
        } else if (klass.equals(InputAnchor.class)) {
            return (A) new InputAnchor(number);
        } else if (klass.equals(InputSampleAnchor.class)) {
            return (A) new InputSampleAnchor(number);
        } else if (klass.equals(OutputAnchor.class)) {
            return (A) new OutputAnchor(number);
        } else if (klass.equals(OutputSampleAnchor.class)) {
            return (A) new OutputSampleAnchor(number);
        } else {
            throw new IllegalArgumentException(klass.getName());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity(name = "ExperimentCalculatedInfo")
    @Table(name = "Experiment_View_2")
    public static class CalculatedInfo extends IdentifiableEntity {

        @Basic
        @Nullable
        @Column(insertable = false, updatable = false)
        @JdbcType(PostgreSQLEnumJdbcType.class)
        private AccessLevel currentAccess;

        @NotNull
        @Basic(fetch = FetchType.LAZY)
        @Column(insertable = false, updatable = false)
        private Integer aclCount;

        @NotNull
        @Basic(fetch = FetchType.LAZY)
        private Boolean marked;
    }
}
