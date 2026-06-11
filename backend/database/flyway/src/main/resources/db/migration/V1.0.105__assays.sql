-- Assay data registration (microtitre plate) feature.
--
-- Adds a first-class, cross-experiment relational model for plate-based assay data:
--   * Assay            - reusable assay/protocol definitions (snapshot-on-apply)
--   * Calculation      - library or custom-formula derivation definitions
--   * Plate / Well     - microtitre plates (SBS or custom/sparse) and their wells
--   * Well_Content     - 0..many contents per well (registered Sample, control, or free-text)
--   * Assay_Value      - unified value node (raw reading, concentration, or derived result)
--   * Assay_Value_Scope- what a value represents (well / well-group / plate / plate-group / cross)
--   * Value_Dependency - DAG edges between values (drives propagation + cycle detection)
--   * Value_Conflict   - raised when an upstream change would alter a value in a signed experiment

-------------------------------------------------------------------------------
-- Enumerated types
-------------------------------------------------------------------------------
CREATE TYPE Plate_Format AS ENUM ('SBS_6', 'SBS_12', 'SBS_24', 'SBS_48', 'SBS_96', 'SBS_384', 'SBS_1536', 'CUSTOM');
CREATE TYPE Well_Type AS ENUM ('SAMPLE', 'CONTROL', 'BLANK', 'EMPTY');
CREATE TYPE Well_Content_Kind AS ENUM ('SAMPLE', 'CONTROL', 'FREE_TEXT');
CREATE TYPE Assay_Value_Kind AS ENUM ('RAW', 'CONCENTRATION', 'DERIVED');
CREATE TYPE Assay_Value_Type AS ENUM ('NUMERIC', 'QUALITATIVE', 'CURVE_FIT', 'TEXT', 'BOOLEAN');
CREATE TYPE Value_Qualifier AS ENUM ('EQ', 'GT', 'LT', 'GE', 'LE', 'APPROX');
CREATE TYPE Calculation_Kind AS ENUM ('LIBRARY', 'FORMULA');
CREATE TYPE Value_Scope_Type AS ENUM ('WELL', 'WELL_GROUP', 'PLATE', 'PLATE_GROUP', 'CROSS');
CREATE TYPE Assay_Status AS ENUM ('DRAFT', 'ACTIVE', 'ARCHIVED');

-------------------------------------------------------------------------------
-- Assay definition (reusable, mutable per application via snapshot-on-apply)
-------------------------------------------------------------------------------
CREATE TABLE Assay (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    status Assay_Status NOT NULL DEFAULT 'DRAFT',
    -- Default layout, readout definitions, control definitions and calculation
    -- definitions. Copied onto each Plate as definition_snapshot when applied.
    definition JSONB NOT NULL DEFAULT '{}',
    search_vector TSVECTOR,
    CONSTRAINT assay_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT assay_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT assay_name_uq UNIQUE (name)
);
CREATE INDEX ix_assay_search_vector ON Assay USING GIN (search_vector);
CREATE INDEX ix_assay_name ON Assay USING GIN (name gin_trgm_ops);

-------------------------------------------------------------------------------
-- Calculation definition (library calc or custom formula)
-------------------------------------------------------------------------------
CREATE TABLE Calculation (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    assay_id UUID,
    name VARCHAR(256) NOT NULL,
    calc_kind Calculation_Kind NOT NULL,
    library_id VARCHAR(128),          -- e.g. PERCENT_INHIBITION, NORMALIZE_TO_CONTROLS, Z_PRIME, MEAN, SD, CV, IC50_4PL (when LIBRARY)
    expression TEXT,                  -- custom formula source (when FORMULA)
    params JSONB NOT NULL DEFAULT '{}',
    input_selectors JSONB NOT NULL DEFAULT '{}',  -- named input roles -> well/plate/value selectors
    output_value_type Assay_Value_Type NOT NULL DEFAULT 'NUMERIC',
    output_unit VARCHAR(64),
    CONSTRAINT calculation_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT calculation_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT calculation_assay_id_fk FOREIGN KEY (assay_id) REFERENCES Assay (id) ON DELETE CASCADE,
    CONSTRAINT calculation_kind_chk CHECK (
        (calc_kind = 'LIBRARY' AND library_id IS NOT NULL) OR
        (calc_kind = 'FORMULA' AND expression IS NOT NULL)
    )
);
CREATE INDEX ix_calculation_assay_id ON Calculation (assay_id);

-------------------------------------------------------------------------------
-- Plate (belongs to an experiment; many plates per experiment)
-------------------------------------------------------------------------------
CREATE TABLE Plate (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    experiment_id UUID NOT NULL,
    assay_id UUID,
    name VARCHAR(256) NOT NULL,
    barcode VARCHAR(256),
    plate_format Plate_Format NOT NULL,
    row_count INT NOT NULL,
    col_count INT NOT NULL,
    -- Snapshot of the assay definition taken when the assay was applied to this
    -- plate, so later edits to the shared Assay do not alter existing plates.
    definition_snapshot JSONB NOT NULL DEFAULT '{}',
    CONSTRAINT plate_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT plate_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT plate_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT plate_assay_id_fk FOREIGN KEY (assay_id) REFERENCES Assay (id),
    CONSTRAINT plate_dimensions_chk CHECK (row_count > 0 AND col_count > 0)
);
CREATE INDEX ix_plate_experiment_id ON Plate (experiment_id);
CREATE INDEX ix_plate_barcode ON Plate (barcode);

-------------------------------------------------------------------------------
-- Well (sparse / non-rectangular handled by simply omitting absent wells)
-------------------------------------------------------------------------------
CREATE TABLE Well (
    id UUID PRIMARY KEY,
    plate_id UUID NOT NULL,
    row_index INT NOT NULL,
    col_index INT NOT NULL,
    address VARCHAR(16) NOT NULL,     -- e.g. A1, H12, AB48
    well_type Well_Type NOT NULL DEFAULT 'EMPTY',
    CONSTRAINT well_plate_id_fk FOREIGN KEY (plate_id) REFERENCES Plate (id) ON DELETE CASCADE,
    CONSTRAINT well_plate_position_uq UNIQUE (plate_id, row_index, col_index),
    CONSTRAINT well_position_chk CHECK (row_index >= 0 AND col_index >= 0)
);
CREATE INDEX ix_well_plate_id ON Well (plate_id);

-------------------------------------------------------------------------------
-- Well contents: 0..many per well (registered Sample, named control, free-text)
-------------------------------------------------------------------------------
CREATE TABLE Well_Content (
    id UUID PRIMARY KEY,
    well_id UUID NOT NULL,
    content_kind Well_Content_Kind NOT NULL,
    sample_id UUID,                   -- when SAMPLE
    control_type_id UUID,             -- when CONTROL (dictionary item)
    free_text VARCHAR(1000),          -- when FREE_TEXT (label or external id)
    concentration NUMERIC,
    concentration_unit VARCHAR(64),
    ordinal INT NOT NULL DEFAULT 0,
    CONSTRAINT well_content_well_id_fk FOREIGN KEY (well_id) REFERENCES Well (id) ON DELETE CASCADE,
    CONSTRAINT well_content_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample (id),
    CONSTRAINT well_content_control_type_id_fk FOREIGN KEY (control_type_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT well_content_kind_chk CHECK (
        (content_kind = 'SAMPLE' AND sample_id IS NOT NULL) OR
        (content_kind = 'CONTROL' AND control_type_id IS NOT NULL) OR
        (content_kind = 'FREE_TEXT' AND free_text IS NOT NULL)
    )
);
CREATE INDEX ix_well_content_well_id ON Well_Content (well_id);
CREATE INDEX ix_well_content_sample_id ON Well_Content (sample_id);

-------------------------------------------------------------------------------
-- Assay value: the unified graph node. Raw readings, concentrations and derived
-- results are all values so the dependency graph is uniform.
-------------------------------------------------------------------------------
CREATE TABLE Assay_Value (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    value_kind Assay_Value_Kind NOT NULL,
    value_type Assay_Value_Type NOT NULL DEFAULT 'NUMERIC',
    readout VARCHAR(256),             -- readout / parameter name this value represents
    numeric_value NUMERIC,
    unit VARCHAR(64),
    qualifier Value_Qualifier,        -- for qualitative results, e.g. GT 10, LT 0.5
    text_value TEXT,
    bool_value BOOLEAN,
    payload JSONB,                    -- curve-fit parameters, fit statistics, vectors, etc.
    calculation_id UUID,              -- the calculation that produced it (when DERIVED)
    -- Owning experiment, denormalised so ACL and signed/locked status can be
    -- resolved without walking scope rows during graph traversal.
    experiment_id UUID NOT NULL,
    is_stale BOOLEAN NOT NULL DEFAULT FALSE,
    is_frozen BOOLEAN NOT NULL DEFAULT FALSE,  -- true when owning experiment is signed/locked
    computed_at TIMESTAMPTZ,
    CONSTRAINT assay_value_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT assay_value_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT assay_value_calculation_id_fk FOREIGN KEY (calculation_id) REFERENCES Calculation (id) ON DELETE SET NULL,
    CONSTRAINT assay_value_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT assay_value_derived_chk CHECK (value_kind <> 'DERIVED' OR calculation_id IS NOT NULL)
);
CREATE INDEX ix_assay_value_experiment_id ON Assay_Value (experiment_id);
CREATE INDEX ix_assay_value_calculation_id ON Assay_Value (calculation_id);
CREATE INDEX ix_assay_value_stale ON Assay_Value (is_stale) WHERE is_stale;

-------------------------------------------------------------------------------
-- What a value represents. A value may span many wells/plates across experiments.
-------------------------------------------------------------------------------
CREATE TABLE Assay_Value_Scope (
    id UUID PRIMARY KEY,
    value_id UUID NOT NULL,
    scope_type Value_Scope_Type NOT NULL,
    well_id UUID,
    plate_id UUID,
    well_content_id UUID,             -- set for CONCENTRATION values bound to a specific content
    CONSTRAINT assay_value_scope_value_id_fk FOREIGN KEY (value_id) REFERENCES Assay_Value (id) ON DELETE CASCADE,
    CONSTRAINT assay_value_scope_well_id_fk FOREIGN KEY (well_id) REFERENCES Well (id) ON DELETE CASCADE,
    CONSTRAINT assay_value_scope_plate_id_fk FOREIGN KEY (plate_id) REFERENCES Plate (id) ON DELETE CASCADE,
    CONSTRAINT assay_value_scope_well_content_id_fk FOREIGN KEY (well_content_id) REFERENCES Well_Content (id) ON DELETE CASCADE,
    CONSTRAINT assay_value_scope_target_chk CHECK (well_id IS NOT NULL OR plate_id IS NOT NULL)
);
CREATE INDEX ix_assay_value_scope_value_id ON Assay_Value_Scope (value_id);
CREATE INDEX ix_assay_value_scope_well_id ON Assay_Value_Scope (well_id);
CREATE INDEX ix_assay_value_scope_plate_id ON Assay_Value_Scope (plate_id);

-------------------------------------------------------------------------------
-- Dependency graph edges (value depends on depends_on_value). Drives both
-- change propagation and circular-dependency detection.
-------------------------------------------------------------------------------
CREATE TABLE Value_Dependency (
    value_id UUID NOT NULL,           -- downstream / derived value
    depends_on_value_id UUID NOT NULL, -- upstream input value
    CONSTRAINT value_dependency_pk PRIMARY KEY (value_id, depends_on_value_id),
    CONSTRAINT value_dependency_value_id_fk FOREIGN KEY (value_id) REFERENCES Assay_Value (id) ON DELETE CASCADE,
    CONSTRAINT value_dependency_depends_on_value_id_fk FOREIGN KEY (depends_on_value_id) REFERENCES Assay_Value (id) ON DELETE CASCADE,
    CONSTRAINT value_dependency_no_self_chk CHECK (value_id <> depends_on_value_id)
);
CREATE INDEX ix_value_dependency_depends_on ON Value_Dependency (depends_on_value_id);

-------------------------------------------------------------------------------
-- Conflicts raised when propagation would change a frozen (signed) value.
-------------------------------------------------------------------------------
CREATE TABLE Value_Conflict (
    id UUID PRIMARY KEY,
    frozen_value_id UUID NOT NULL,
    triggering_value_id UUID,
    detected_at TIMESTAMPTZ NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMPTZ,
    resolved_by_id UUID,
    detail TEXT,
    proposed_numeric_value NUMERIC,   -- what the value would have become
    proposed_payload JSONB,
    CONSTRAINT value_conflict_frozen_value_id_fk FOREIGN KEY (frozen_value_id) REFERENCES Assay_Value (id) ON DELETE CASCADE,
    CONSTRAINT value_conflict_triggering_value_id_fk FOREIGN KEY (triggering_value_id) REFERENCES Assay_Value (id) ON DELETE SET NULL,
    CONSTRAINT value_conflict_resolved_by_id_fk FOREIGN KEY (resolved_by_id) REFERENCES User_Account (id)
);
CREATE INDEX ix_value_conflict_frozen_value_id ON Value_Conflict (frozen_value_id);
CREATE INDEX ix_value_conflict_unresolved ON Value_Conflict (resolved) WHERE NOT resolved;
