package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ACLEntryArrayType;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        }
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
    @JoinColumn(name = "project_id", updatable = false)
    private ProjectEntity project;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "notebook_id", updatable = false)
    private NotebookEntity notebook;

    @NotEmpty
    @Pattern(regexp = "^\\d{8}-\\d{4}$")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ExperimentStatus status;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeutic_area_id")
    private DictionaryItemEntity therapeuticArea;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_code_id")
    private DictionaryItemEntity projectCode;

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
    private Boolean marked;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
//    private ExperimentModel model;
    private String model;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private byte[] picture;

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
    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "user_id")
    private Map<UserEntity, ExperimentACLEntity> aclEntities;

    @NotNull
    @ManyToMany()
    @JoinTable(name = "experiment_attachment", joinColumns = @JoinColumn(name = "experiment_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    @OrderBy("createdAt")
    private List<AttachmentEntity> attachments = new ArrayList<>(0);

    @Override
    public void insertACL(UserEntity user, AccessLevel access, Boolean inherited) {
        getAclEntities().put(user, new ExperimentACLEntity(this, user, access, inherited));
    }
}
