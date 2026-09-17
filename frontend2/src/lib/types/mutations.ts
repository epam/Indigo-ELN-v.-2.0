import type { AccessForm, DocumentStatus, UserRef, UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentRef } from '@/lib/types/experiments.ts';
import type {
  DensityUnit,
  ExternalSupplier,
  MeltingPoint,
  MolarityUnit,
  MolUnit,
  PurityCalculation,
  ReactionOutputType,
  ReactionRole,
  ResidualSolvent,
  SolubidityInSolvent,
  VolumeUnit,
  WeightUnit,
} from '@/lib/types/reactions.ts';

/**
 * The experiment model-mutation protocol: `POST /experiments/{id}/mutate` takes one
 * `Mutation` and answers with a `MutationResponse` carrying a **JSON diff**, not a new
 * experiment. `src/lib/json-patcher.ts` applies that diff.
 */

/**
 * Mirrors `Mutation` (eln-api, reaction/model/mutation) — an interface Jackson serialises with
 * `@JsonTypeInfo(Id.SIMPLE_NAME, property = "type")`, so the wire shape is a discriminated
 * union whose tag is the record's own class name.
 *
 * **This is the complete set** — all 94 members of the backend's `@JsonSubTypes` list, in its
 * order — rather than only the ones a screen sends today. The unions below mirror the Java
 * interface hierarchy one for one, so a member that exists there and not here is a diff of two
 * files rather than a search through the app.
 *
 * Two things follow from mirroring everything, and both matter:
 *
 * - **Most of these never travel over `/mutate`.** The backend gates that with
 *   `isMutateMethodAllowed()`, and the ones it refuses have their own REST endpoints — a
 *   project is created by `POST /projects`, an attachment by an upload, an experiment
 *   completed by `POST /experiments/{id}/complete`. `ModelMutation` below is the subset that
 *   endpoint accepts, and it is what the mutate hook takes; nothing outside this file should
 *   be typed on the full `Mutation`.
 * - **Several carry `created*Anchor` members that a client must not send.** They are filled in
 *   server-side so an undo/redo replay regenerates the same anchors. They are omitted from the
 *   interfaces here rather than marked optional, so there is no way to send one by accident.
 */
export type Mutation = ExperimentMutation | ProjectMutation | NotebookMutation;

/**
 * Everything scoped to one experiment: the experiment's own fields, its reaction steps, and the
 * four levels of row beneath them. Mirrors the Java `ExperimentMutation` and its sub-interfaces.
 */
export type ExperimentMutation =
  | ExperimentLevelMutation
  | ReactionMutation
  | ReactionInputMutation
  | ReactionInputSampleMutation
  | ReactionOutputMutation
  | ReactionOutputSampleMutation;

/**
 * What `POST /experiments/{id}/mutate` actually accepts — the members whose
 * `isMutateMethodAllowed()` is true. Everything else answers through its own endpoint, and
 * sending one here is rejected before a handler ever sees it.
 *
 * `ImportSDF` is the one reaction-level exception: it needs the multipart upload at
 * `POST /experiments/{id}/datamodel/reactions/{anchor}/importSDF`.
 */
export type ModelMutation =
  | SetExperimentSignificantFigures
  | SetBatchCreator
  | Undo
  | Redo
  | Exclude<ReactionMutation, ImportSDF>
  | ReactionInputMutation
  | ReactionInputSampleMutation
  | ReactionOutputMutation
  | ReactionOutputSampleMutation;

/** The seventeen mutations declared on `ExperimentMutation` itself — no row anchor involved. */
export type ExperimentLevelMutation =
  | CreateExperiment
  | SetExperimentSignificantFigures
  | EditExperimentAttributes
  | EditExperimentAccess
  | SetBatchCreator
  | CreateExperimentAttachment
  | DeleteExperimentAttachment
  | CancelExperiment
  | ReopenExperiment
  | CompleteExperiment
  | SubmitExperiment
  | SignatureUpdated
  | MakeVersion
  | ExperimentAccessUpdated
  | ExperimentNameUpdated
  | Undo
  | Redo;

/** The six mutations keyed by a `Reaction`'s anchor — one step. */
export type ReactionMutation = SetScheme | ResolveInputs | AddEmptyInput | AddInput | AddNoProductSample | ImportSDF;

/** The ten mutations keyed by a `ReactionInput`'s anchor — one compound row. */
export type ReactionInputMutation =
  | SetInputRowRole
  | SetInputRowMol
  | SetInputRowChemicalName
  | SetInputRowLimiting
  | SetInputRowSaltCode
  | SetInputRowSaltEQ
  | SetInputRowEQ
  | SetInputCompoundStereoisomerCode
  | SetInputCompoundMolWeight
  | RemoveInputRow;

/** The nine mutations keyed by a `ReactionInputSample`'s anchor — one batch of one compound. */
export type ReactionInputSampleMutation =
  | SetInputDensity
  | SetInputMolarity
  | SetInputVolume
  | SetInputPurity
  | SetInputHealthHazards
  | SetInputMol
  | SetInputWeight
  | SetInputComment
  | RemoveInput;

/** The ten mutations keyed by a `ReactionOutput`'s anchor — one product row. */
export type ReactionOutputMutation =
  | AddProductSample
  | SetOutputRowType
  | SetOutputRowSaltCode
  | SetOutputRowSaltEQ
  | SetOutputRowEQ
  | SetOutputRowName
  | SetOutputRowChemicalName
  | SetOutputRowIntended
  | SetOutputCompoundStereoisomerCode
  | SetOutputCompoundMolWeight;

/** The twenty-six mutations keyed by a `ReactionOutputSample`'s anchor — one product batch. */
export type ReactionOutputSampleMutation =
  | SetOutputDensity
  | SetOutputMolarity
  | SetOutputVolume
  | SetOutputPurity
  | SetOutputHealthHazards
  | SetOutputActualMol
  | SetOutputActualWeight
  | RegisterSample
  | SetOutputHandlingPrecautions
  | SetOutputStorageInstructions
  | SetOutputCompoundProtection
  | SetOutputSolubilityInSolvents
  | SetOutputResidualSolvents
  | SetOutputMeltingPoint
  | SetOutputPurityCalculations
  | SetOutputExternalSupplier
  | SetOutputSource
  | SetOutputSourceDetails
  | SetOutputComponentState
  | SetOutputBatchComment
  | SetOutputStructureComment
  | RemoveProductSample
  | SetOutputSaltCode
  | SetOutputSaltEQ
  | SetOutputStereoisomerCode
  | SetOutputMolfile;

/** The eight mutations scoped to a project. None of them travels over `/mutate`. */
export type ProjectMutation =
  | CreateProject
  | EditProjectAttributes
  | EditProjectAccess
  | CreateProjectAttachment
  | DeleteProjectAttachment
  | ProjectAccessUpdated
  | ProjectUndo
  | ProjectRedo;

/** The eight mutations scoped to a notebook, shaped exactly like the project ones. */
export type NotebookMutation =
  | CreateNotebook
  | EditNotebookAttributes
  | EditNotebookAccess
  | CreateNotebookAttachment
  | DeleteNotebookAttachment
  | NotebookAccessUpdated
  | NotebookUndo
  | NotebookRedo;

/* ── Experiment-level ──────────────────────────────────────────────────────────────────── */

/**
 * Every dictionary reference below is a `DictionaryItemRef`. The Java side has a class per
 * dictionary — `SaltCodeRef`, `TherapeuticAreaRef`, `HealthHazardRef` and a dozen more — but
 * they all serialise to the same `{ id, name }`, so one type covers them. Which dictionary a
 * field takes is named in its comment where it is not obvious from the field name.
 *
 * A `JsonNullable<T>` member is written `field?: T | null`: the record is `NON_ABSENT`, so an
 * absent value is an omitted key ("leave this alone") and an explicit `null` is a cleared one.
 * That distinction is the whole point of the type — do not collapse it to `T | undefined`.
 */

/** `POST /experiments`, not `/mutate`. */
export interface CreateExperiment {
  type: 'CreateExperiment';
  templateID: UUID;
  description?: string | null;
  therapeuticArea?: DictionaryItemRef | null;
  projectCode?: DictionaryItemRef | null;
}

/**
 * How many significant figures every calculated value is formatted to. Backend-only: the patch
 * comes back with every affected `EnteredValue.value` already re-rendered as a string, so there
 * is no client-side rounding anywhere. `@Min(1) @Max(5)` on the record.
 */
export interface SetExperimentSignificantFigures {
  type: 'SetExperimentSignificantFigures';
  significantFigures: number;
}

/** `PUT /experiments/{id}`, not `/mutate`. Every member is a `JsonNullable` — see above. */
export interface EditExperimentAttributes {
  type: 'EditExperimentAttributes';
  title?: string | null;
  therapeuticArea?: DictionaryItemRef | null;
  projectCode?: DictionaryItemRef | null;
  description?: string | null;
  literature?: string | null;
  linkedExperiments?: ExperimentRef[] | null;
  continuedFrom?: ExperimentRef[] | null;
  continuedTo?: ExperimentRef[] | null;
}

/** `POST /experiments/{id}/access`, not `/mutate`. `@NotEmpty`. */
export interface EditExperimentAccess {
  type: 'EditExperimentAccess';
  edits: AccessForm[];
}

/** Who registers this experiment's product batches. */
export interface SetBatchCreator {
  type: 'SetBatchCreator';
  batchCreator: UserRef;
}

/** Recorded after the upload endpoint has stored the file; not sent to `/mutate`. */
export interface CreateExperimentAttachment {
  type: 'CreateExperimentAttachment';
  attachmentID: UUID;
}

export interface DeleteExperimentAttachment {
  type: 'DeleteExperimentAttachment';
  attachmentID: UUID;
}

/** The four status transitions, each behind its own endpoint rather than `/mutate`. */
export interface CancelExperiment {
  type: 'CancelExperiment';
}

export interface ReopenExperiment {
  type: 'ReopenExperiment';
}

export interface CompleteExperiment {
  type: 'CompleteExperiment';
}

export interface SubmitExperiment {
  type: 'SubmitExperiment';
  signatureTemplateID: UUID;
}

/** Raised by the signature service, never by a browser. */
export interface SignatureUpdated {
  type: 'SignatureUpdated';
  message: string;
  /** Mirrors `DocumentStatus` — the signing workflow's own states, not `ExperimentStatus`. */
  documentStatus: DocumentStatus;
  attachmentID: UUID;
}

/** Snapshots the experiment into its version history. */
export interface MakeVersion {
  type: 'MakeVersion';
}

/**
 * Bookkeeping the backend writes onto an experiment when an ancestor is renamed or its access
 * changes. Both arrive in the undo history; neither is something a client sends.
 */
export interface ExperimentAccessUpdated {
  type: 'ExperimentAccessUpdated';
  projectName?: string | null;
  notebookName?: string | null;
}

export interface ExperimentNameUpdated {
  type: 'ExperimentNameUpdated';
  notebookName: string;
}

/**
 * Undo and redo replay the recorded mutation history, which is why so many records carry
 * server-filled anchors: a replay has to regenerate exactly the ids the original run created.
 */
export interface Undo {
  type: 'Undo';
}

export interface Redo {
  type: 'Redo';
}

/* ── Reaction-level ────────────────────────────────────────────────────────────────────── */

/**
 * Replaces a reaction's drawn scheme. The backend diffs the new rxnfile against the old one
 * by canonical SMILES, then creates, deletes and repositions the input and output rows to
 * match — which is why a scheme edit comes back as a patch touching most of the model.
 *
 * The record has five more members (`createdReactantAnchors` and friends). They are filled
 * in server-side so an undo/redo replay generates the same anchors, and a client must not
 * send them.
 */
export interface SetScheme {
  type: 'SetScheme';
  anchor: UUID;
  /** Note the capital `F`; the model field this writes is `Reaction.rxnfile`. Null clears it. */
  rxnFile: string | null;
}

/**
 * Attaches registered samples to input rows the scheme could not match to a compound — the
 * ones `MutationResponse.unresolvedInputs` names. `@NotEmpty` on the map.
 */
export interface ResolveInputs {
  type: 'ResolveInputs';
  anchor: UUID;
  /** Input row anchor → the sample id to bind to it. */
  inputSamples: Record<UUID, UUID>;
}

/**
 * Appends an input row carrying an `UNKNOWN` compound and one empty sample. The two
 * `created*Anchor` members the record also declares are filled in server-side so an undo/redo
 * replay regenerates the same anchors; a client must not send them.
 */
export interface AddEmptyInput {
  type: 'AddEmptyInput';
  /** The reaction's own anchor, not a row's. */
  anchor: UUID;
}

/** The same, for a compound that is already registered. */
export interface AddInput {
  type: 'AddInput';
  anchor: UUID;
  sampleId: UUID;
}

/**
 * Appends a product row that was **not** drawn in the scheme — `intended: false`, `BY_PRODUCT`,
 * on an unknown compound — plus one batch. What the Product Batch Summary's "Add New Batch"
 * sends, and why the products table filters on `intended`.
 */
export interface AddNoProductSample {
  type: 'AddNoProductSample';
  anchor: UUID;
}

/**
 * Creates a product row per compound in an uploaded SDF. `isMutateMethodAllowed()` is false:
 * it goes to `POST /experiments/{id}/datamodel/reactions/{anchor}/importSDF` as multipart,
 * never to `/mutate`, which is why `ModelMutation` excludes it.
 */
export interface ImportSDF {
  type: 'ImportSDF';
  anchor: UUID;
  compoundIDs: UUID[];
}

/* ── Input rows ────────────────────────────────────────────────────────────────────────── */

/**
 * Every mutation below is keyed by `anchor`, and **which** anchor is the whole distinction
 * between the two groups: a `ReactionInputMutation` names a `ReactionInput` (the compound
 * row), a `ReactionInputSampleMutation` names one of that row's `ReactionInputSample`s.
 * Sending a sample anchor to a row mutation resolves to nothing and the call 400s.
 *
 * **Numbers are sent as strings** — `EnteredValue.value` is a string on the wire, and these
 * mirror it. `saltEQ` is the one member the Java records declare as a `Double`; Jackson
 * coerces a JSON string into it, and the records are due to be changed to match. Do not
 * "correct" it to a number.
 *
 * Clearing a value sends `null` for the value *and* its unit.
 */
export interface SetInputRowRole {
  type: 'SetInputRowRole';
  anchor: UUID;
  /** `OUTPUT` is a valid enum member but never a valid choice for an input row. */
  role: ReactionRole;
}

/** Note `molUnit`, not `unit` — the sample-level `SetInputMol` spells the same thing `unit`. */
export interface SetInputRowMol {
  type: 'SetInputRowMol';
  anchor: UUID;
  mol: string | null;
  molUnit: MolUnit | null;
}

export interface SetInputRowChemicalName {
  type: 'SetInputRowChemicalName';
  anchor: UUID;
  chemicalName: string | null;
}

/**
 * Makes this row the limiting reagent. It carries no boolean because there is nothing to
 * toggle: naming the row is the mutation, and the backend clears whichever row held it and
 * recalculates the rest of the table off the new one.
 */
export interface SetInputRowLimiting {
  type: 'SetInputRowLimiting';
  anchor: UUID;
}

export interface SetInputRowSaltCode {
  type: 'SetInputRowSaltCode';
  anchor: UUID;
  saltCode: DictionaryItemRef | null;
}

export interface SetInputRowSaltEQ {
  type: 'SetInputRowSaltEQ';
  anchor: UUID;
  /** A string, though the record says `Double` — see the note above. */
  saltEQ: string | null;
}

export interface SetInputRowEQ {
  type: 'SetInputRowEQ';
  anchor: UUID;
  eq: string | null;
}

export interface SetInputCompoundStereoisomerCode {
  type: 'SetInputCompoundStereoisomerCode';
  anchor: UUID;
  stereoisomerCode: DictionaryItemRef | null;
}

/** Only meaningful for an `UNKNOWN` compound; a stored or virtual one carries a registry value. */
export interface SetInputCompoundMolWeight {
  type: 'SetInputCompoundMolWeight';
  anchor: UUID;
  molWeight: string | null;
}

/** Deletes the compound row and every sample under it. */
export interface RemoveInputRow {
  type: 'RemoveInputRow';
  anchor: UUID;
}

/* ── Input samples ─────────────────────────────────────────────────────────────────────── */

export interface SetInputDensity {
  type: 'SetInputDensity';
  anchor: UUID;
  density: string | null;
  unit: DensityUnit | null;
}

export interface SetInputMolarity {
  type: 'SetInputMolarity';
  anchor: UUID;
  molarity: string | null;
  unit: MolarityUnit | null;
}

export interface SetInputVolume {
  type: 'SetInputVolume';
  anchor: UUID;
  volume: string | null;
  unit: VolumeUnit | null;
}

/** Purity is a percentage and carries no unit — the model types it `EnteredValue<NoUnit>`. */
export interface SetInputPurity {
  type: 'SetInputPurity';
  anchor: UUID;
  purity: string | null;
}

/** `@NotNull` on the record: clearing the list means sending `[]`, never `null`. */
export interface SetInputHealthHazards {
  type: 'SetInputHealthHazards';
  anchor: UUID;
  healthHazards: DictionaryItemRef[];
}

export interface SetInputMol {
  type: 'SetInputMol';
  anchor: UUID;
  mol: string | null;
  unit: MolUnit | null;
}

export interface SetInputWeight {
  type: 'SetInputWeight';
  anchor: UUID;
  weight: string | null;
  unit: WeightUnit | null;
}

export interface SetInputComment {
  type: 'SetInputComment';
  anchor: UUID;
  comment: string | null;
}

/** Deletes one sample. The row survives — `RemoveInputRow` is what deletes the compound. */
export interface RemoveInput {
  type: 'RemoveInput';
  anchor: UUID;
}

/* ── Output rows ───────────────────────────────────────────────────────────────────────── */

/**
 * The product-row mutations, keyed by a `ReactionOutput`'s **output anchor** — never one of its
 * samples'. The same rule as the input side: the wrong level resolves to nothing and the call
 * 400s.
 */

/**
 * Adds one batch to this product. The record also declares `createdSampleAnchor`, filled in
 * server-side so an undo/redo replay regenerates the same anchor; a client must not send it.
 *
 * A new batch starts at 100 % purity (`AddProductSampleHandler`).
 */
export interface AddProductSample {
  type: 'AddProductSample';
  anchor: UUID;
}

/**
 * Whether the product is the wanted one, a by-product or an intermediate the next step consumes.
 *
 * Note `outputType`, not `unit` or `type` — `type` is this union's own discriminator, and the
 * Java record spells the member `outputType` for the same reason.
 *
 * This is **not** `ReactionOutput.intended`, which is a separate boolean saying the product was
 * drawn in the reaction scheme.
 */
export interface SetOutputRowType {
  type: 'SetOutputRowType';
  anchor: UUID;
  outputType: ReactionOutputType;
}

/**
 * Setting a salt code on a **stored** compound turns it into a virtual one and defaults its
 * `saltEQ` to 1, because the whole `CompoundRef` is rebuilt — which is what moves `molWeight`,
 * `formula` and `exactMass` in the same patch.
 */
export interface SetOutputRowSaltCode {
  type: 'SetOutputRowSaltCode';
  anchor: UUID;
  saltCode: DictionaryItemRef | null;
}

export interface SetOutputRowSaltEQ {
  type: 'SetOutputRowSaltEQ';
  anchor: UUID;
  /** A string, though the record says `Double` — see the note above the input rows. */
  saltEQ: string | null;
}

/** The EQ the theoretical yield is computed from: `theoMol = limiting.mol / limiting.eq * eq`. */
export interface SetOutputRowEQ {
  type: 'SetOutputRowEQ';
  anchor: UUID;
  eq: string | null;
}

/**
 * The product's name — `P0`, `P1`, … by default, from `Reaction.generateNextProductName()`.
 *
 * `@NotNull` on the record, so `string` rather than `string | null`. The backend also rejects a
 * name already used by another output of the same reaction; `apiFetch` toasts that and the cell
 * keeps what was typed.
 */
export interface SetOutputRowName {
  type: 'SetOutputRowName';
  anchor: UUID;
  name: string;
}

export interface SetOutputRowChemicalName {
  type: 'SetOutputRowChemicalName';
  anchor: UUID;
  chemicalName: string | null;
}

/**
 * Whether the product was drawn in the reaction scheme. `SetScheme` sets it for every product
 * molecule it finds; the Product Batch Summary's "Sync with Products" icon is what sets it by
 * hand. An unintended row with no batches left is garbage-collected server-side.
 */
export interface SetOutputRowIntended {
  type: 'SetOutputRowIntended';
  anchor: UUID;
  intended: boolean;
}

export interface SetOutputCompoundStereoisomerCode {
  type: 'SetOutputCompoundStereoisomerCode';
  anchor: UUID;
  stereoisomerCode: DictionaryItemRef | null;
}

/** Only meaningful for an `UNKNOWN` compound, exactly as on the input side. */
export interface SetOutputCompoundMolWeight {
  type: 'SetOutputCompoundMolWeight';
  anchor: UUID;
  molWeight: string | null;
}

/* ── Output samples ────────────────────────────────────────────────────────────────────── */

/**
 * One product batch. Rejected outright when the sample has been sent for registration —
 * `registrationStatus != null` freezes its compound, and most of these its values too.
 *
 * The last four carry a `createdOutputAnchor` the backend fills in: changing a batch's salt,
 * stereoisomer or structure can move it onto a different product row, creating one if no row
 * matches. As everywhere else, a client must not send it.
 */
export interface SetOutputDensity {
  type: 'SetOutputDensity';
  anchor: UUID;
  density: string | null;
  unit: DensityUnit | null;
}

export interface SetOutputMolarity {
  type: 'SetOutputMolarity';
  anchor: UUID;
  molarity: string | null;
  unit: MolarityUnit | null;
}

export interface SetOutputVolume {
  type: 'SetOutputVolume';
  anchor: UUID;
  volume: string | null;
  unit: VolumeUnit | null;
}

export interface SetOutputPurity {
  type: 'SetOutputPurity';
  anchor: UUID;
  purity: string | null;
}

/** `@NotNull` on the record: clearing the list means sending `[]`, never `null`. */
export interface SetOutputHealthHazards {
  type: 'SetOutputHealthHazards';
  anchor: UUID;
  healthHazards: DictionaryItemRef[];
}

export interface SetOutputActualMol {
  type: 'SetOutputActualMol';
  anchor: UUID;
  actualMol: string | null;
  unit: MolUnit | null;
}

export interface SetOutputActualWeight {
  type: 'SetOutputActualWeight';
  anchor: UUID;
  actualWeight: string | null;
  unit: WeightUnit | null;
}

/** Sends the batch to the compound registry. Its `registrationStatus` then drives the UI. */
export interface RegisterSample {
  type: 'RegisterSample';
  anchor: UUID;
}

/** `@Size(min = 1)` when present: send `null` to clear it, never `[]`. */
export interface SetOutputHandlingPrecautions {
  type: 'SetOutputHandlingPrecautions';
  anchor: UUID;
  handlingPrecautions: DictionaryItemRef[] | null;
}

export interface SetOutputStorageInstructions {
  type: 'SetOutputStorageInstructions';
  anchor: UUID;
  storageInstructions: DictionaryItemRef[] | null;
}

export interface SetOutputCompoundProtection {
  type: 'SetOutputCompoundProtection';
  anchor: UUID;
  compoundProtection: DictionaryItemRef[] | null;
}

export interface SetOutputSolubilityInSolvents {
  type: 'SetOutputSolubilityInSolvents';
  anchor: UUID;
  solubilityInSolvents: SolubidityInSolvent[] | null;
}

export interface SetOutputResidualSolvents {
  type: 'SetOutputResidualSolvents';
  anchor: UUID;
  residualSolvents: ResidualSolvent[] | null;
}

export interface SetOutputMeltingPoint {
  type: 'SetOutputMeltingPoint';
  anchor: UUID;
  meltingPoint: MeltingPoint | null;
}

export interface SetOutputPurityCalculations {
  type: 'SetOutputPurityCalculations';
  anchor: UUID;
  purityCalculations: PurityCalculation[] | null;
}

export interface SetOutputExternalSupplier {
  type: 'SetOutputExternalSupplier';
  anchor: UUID;
  externalSupplier: ExternalSupplier | null;
}

/** `SAMPLE_SOURCE`, and `SAMPLE_SOURCE_DETAILS` for the one below it. */
export interface SetOutputSource {
  type: 'SetOutputSource';
  anchor: UUID;
  source: DictionaryItemRef | null;
}

export interface SetOutputSourceDetails {
  type: 'SetOutputSourceDetails';
  anchor: UUID;
  sourceDetails: DictionaryItemRef | null;
}

/** `COMPONENT_STATE` — solid, oil, solution and so on. */
export interface SetOutputComponentState {
  type: 'SetOutputComponentState';
  anchor: UUID;
  componentState: DictionaryItemRef | null;
}

export interface SetOutputBatchComment {
  type: 'SetOutputBatchComment';
  anchor: UUID;
  batchComment: string | null;
}

export interface SetOutputStructureComment {
  type: 'SetOutputStructureComment';
  anchor: UUID;
  structureComment: string | null;
}

/**
 * Deletes one batch. The product row survives — and there is **no** mutation that deletes a
 * product row: one is created and destroyed by editing the reaction scheme, or garbage-collected
 * once it is unintended and has no batches left.
 */
export interface RemoveProductSample {
  type: 'RemoveProductSample';
  anchor: UUID;
}

export interface SetOutputSaltCode {
  type: 'SetOutputSaltCode';
  anchor: UUID;
  saltCode: DictionaryItemRef | null;
}

export interface SetOutputSaltEQ {
  type: 'SetOutputSaltEQ';
  anchor: UUID;
  /** A string, though the record says `Double` — see the note above the input rows. */
  saltEQ: string | null;
}

export interface SetOutputStereoisomerCode {
  type: 'SetOutputStereoisomerCode';
  anchor: UUID;
  stereoisomerCode: DictionaryItemRef | null;
}

/** Redraws one batch's structure. `@NotNull` — there is no way to clear it. */
export interface SetOutputMolfile {
  type: 'SetOutputMolfile';
  anchor: UUID;
  molfile: string;
}

/* ── Projects ──────────────────────────────────────────────────────────────────────────── */

/**
 * None of these travels over `/mutate` — a project is not an experiment, and `mutateModel` only
 * resolves an `ExperimentMutation`. They are here because they share the wire's `type` tag and
 * turn up in the undo history and the audit trail, which is where the app will read them.
 */

/** `POST /projects`. */
export interface CreateProject {
  type: 'CreateProject';
  name: string;
  /** `@Size(min = 1)` when present: send `null` to clear it, never `[]`. */
  keywords?: string[] | null;
  literature?: string | null;
  description?: string | null;
}

/** `PUT /projects/{id}`. Every member is a `JsonNullable` — see the note on the dictionaries. */
export interface EditProjectAttributes {
  type: 'EditProjectAttributes';
  name?: string | null;
  keywords?: string[] | null;
  literature?: string | null;
  description?: string | null;
}

/** `@NotEmpty`. */
export interface EditProjectAccess {
  type: 'EditProjectAccess';
  edits: AccessForm[];
}

export interface CreateProjectAttachment {
  type: 'CreateProjectAttachment';
  attachmentID: UUID;
}

export interface DeleteProjectAttachment {
  type: 'DeleteProjectAttachment';
  attachmentID: UUID;
}

/** Written by the backend when a descendant's access changes; not something a client sends. */
export interface ProjectAccessUpdated {
  type: 'ProjectAccessUpdated';
  notebookName?: string | null;
  experimentName?: string | null;
}

/** Undo and redo are per scope, hence the prefixed names — `Undo` alone is the experiment's. */
export interface ProjectUndo {
  type: 'ProjectUndo';
}

export interface ProjectRedo {
  type: 'ProjectRedo';
}

/* ── Notebooks ─────────────────────────────────────────────────────────────────────────── */

/** The project set again, one level down. A notebook is numbered rather than named by a user. */
export interface CreateNotebook {
  type: 'CreateNotebook';
  name: string;
  description?: string | null;
}

export interface EditNotebookAttributes {
  type: 'EditNotebookAttributes';
  name?: string | null;
  description?: string | null;
}

/** `@NotEmpty`. */
export interface EditNotebookAccess {
  type: 'EditNotebookAccess';
  edits: AccessForm[];
}

export interface CreateNotebookAttachment {
  type: 'CreateNotebookAttachment';
  attachmentID: UUID;
}

export interface DeleteNotebookAttachment {
  type: 'DeleteNotebookAttachment';
  attachmentID: UUID;
}

export interface NotebookAccessUpdated {
  type: 'NotebookAccessUpdated';
  projectName?: string | null;
  experimentName?: string | null;
}

export interface NotebookUndo {
  type: 'NotebookUndo';
}

export interface NotebookRedo {
  type: 'NotebookRedo';
}

/* ── The response ──────────────────────────────────────────────────────────────────────── */

/** Mirrors `MutationResponse` (eln-api, eln/model). */
export interface MutationResponse {
  /**
   * The diff, in the dialect `JSONPatcher` reads — never a standard RFC 6902 patch.
   * `@NotNull`, so always present, though it is `{}` when nothing changed.
   */
  patch: unknown;
  /**
   * Input anchor → the molfile the backend could not resolve to a compound. `SetScheme`
   * populates this on essentially every scheme edit, and `ResolveInputs` is the answer to it.
   */
  unresolvedInputs?: Record<UUID, string>;
  /** Things the user should be told about what the mutation did. `NON_EMPTY`. */
  messages?: string[];
  debugMessages?: string[];
  /** Reaction anchor → a server-rendered SVG. Unused here: schemes are drawn client-side. */
  reactionImages?: Record<UUID, string>;
}
