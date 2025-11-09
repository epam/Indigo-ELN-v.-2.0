package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.config.hibernate.ExperimentModelConverter;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
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
@Table(name = "Experiment_View")
@NamedEntityGraph(
        name = "Experiment.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("marked"),
                @NamedAttributeNode("aclShort"),
                @NamedAttributeNode("aclCount"),
        }
)
@NamedEntityGraph(
        name = "Experiment.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode("therapeuticArea"),
                @NamedAttributeNode("projectCode"),
                @NamedAttributeNode("marked"),
                @NamedAttributeNode("aclEntities"),
                @NamedAttributeNode("signatures")
        }
)
@NamedEntityGraph(
        name = "Experiment.forSignature",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("signatures")
        }
)
@NamedEntityGraph(
        name = "Experiment.forReport",
        includeAllAttributes = true
)
@NamedEntityGraph(
        name = "Experiment.withACL",
        attributeNodes = {
                @NamedAttributeNode("aclEntities")
        }
)
//@DynamicUpdate // TODO cannot use until update is done via trigger
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

    @Basic
    @Nullable
    @Column(insertable = false, updatable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccessLevel currentAccess;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private Boolean marked;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = ExperimentModelConverter.class)
    private ExperimentModel model;

    @Basic(fetch = FetchType.LAZY)
    private byte @Nullable[] picture;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    @Type(ACLEntryArrayType.class)
    private ACLEntry[] aclShort;

    @NotNull
    @Basic(fetch =  FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    private Integer aclCount;

    @NotNull
    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, ExperimentACLEntity> aclEntities;

    @NotNull
    @ManyToMany
    @JoinTable(name = "experiment_attachment", joinColumns = @JoinColumn(name = "experiment_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @NotNull
    @OrderColumn(name = "ordinal")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "experiment")
    private List<ExperimentSignatureEntity> signatures = new ArrayList<>();

    @NotNull
    @ElementCollection
    @CollectionTable(name = "Experiment_Referenced_Compound", joinColumns = @JoinColumn(name = "experiment_id"))
    @Column(name = "compound_id")
    private Set<UUID> referencedCompounds = new HashSet<>(0);

    @NotNull
    @ElementCollection
    @CollectionTable(name = "Experiment_Referenced_Dictionary_Item", joinColumns = @JoinColumn(name = "experiment_id"))
    @Column(name = "dictionary_item_id")
    private Set<UUID> referencedDictionaryItemIDs = new HashSet<>(0);

    @Override
    public void insertACL(UserEntity user, AccessLevel access, Boolean inherited) {
        getAclEntities().put(user, new ExperimentACLEntity(this, user, access, inherited));
    }
}
