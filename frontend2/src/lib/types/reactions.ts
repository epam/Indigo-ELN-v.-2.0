import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * `src/lib/types/reactions.ts` mirrors the backend `reaction/model` package — the JSON blob
 * stored in `experiment.model` and diffed by every model mutation.
 *
 * Ported from the Java, deliberately **not** from indigo-frontend's `experiment.i.ts`: those
 * interfaces were generated from an older spec and have drifted from the wire in five places
 * (a `rxnVersion` field that does not exist, `STRCode*`/`NbkBatchNumber` modelled as objects
 * when `@JsonValue` makes them strings, `SolubidityInSolvent` flattened when it is a
 * discriminated union, and an `EnteredValueSource` enum whose members are not what is sent).
 * Each one is noted again at the type it affects.
 *
 * Every `Anchor` subclass (`ReactionAnchor`, `InputAnchor`, …) wraps a single UUID and
 * serialises as a bare string, so they are all `UUID` here.
 */

/* ── Units ─────────────────────────────────────────────────────────────────────────────── */

export type MolUnit = 'UMOL' | 'MMOL' | 'MOL';
export type WeightUnit = 'MG' | 'G' | 'KG';
export type VolumeUnit = 'ML' | 'L';
export type DensityUnit = 'G_ML';
export type MolarityUnit = 'MM' | 'M';
export type MolWeightUnit = 'G_PER_MOL';
export type NoUnit = 'NO_UNIT';

/**
 * Who set a value, and how strongly it holds. `fixed` > a user edit > `calculated` >
 * `default`, and a user edit carries the revision it was made in — which is why the
 * user-entered case is a bare positive integer rather than a name.
 */
export type EnteredValueSource = number | 'fixed' | 'default' | 'calculated';

/**
 * One numeric cell of the stoichiometry model. `EnteredValue.Serializer` writes `value`,
 * `unit` and `source` only when a value is present, `exactValue` only when the value is
 * exact, and `overwritten` only when true — so every field is optional and `{}` is a real
 * value the wire produces (empty, but overwritten).
 */
export interface EnteredValue<U> {
  value?: string;
  exactValue?: number;
  unit?: U;
  source?: EnteredValueSource;
  overwritten?: boolean;
}

/* ── Compounds ─────────────────────────────────────────────────────────────────────────── */

/**
 * `MolFormula` serialises through `@JsonValue toHTMLString()`, so a formula arrives as
 * **HTML** — `C<sub>6</sub>H<sub>6</sub>`, not `C6H6`. Rendering it as plain text shows the
 * tags; it needs `dangerouslySetInnerHTML` or equivalent.
 */
export type MolFormula = string;

/** `STRCodeCompound`/`STRCodeSample` are `@JsonValue` strings: `STR-00000001-01[-003]`. */
export type STRCode = string;

/** `NbkBatchNumber` is a `@JsonValue` string: `20240101-0001-003`. */
export type NbkBatchNumber = string;

interface CompoundRefBase {
  formula?: MolFormula;
  molWeight?: EnteredValue<MolWeightUnit>;
}

/** What `Stored` and `Virtual` share; `Unknown` `@JsonIgnore`s all of it. */
interface IdentifiedCompoundRef extends CompoundRefBase {
  compoundID: UUID;
  formula: MolFormula;
  molWeight: EnteredValue<MolWeightUnit>;
  exactMass: EnteredValue<NoUnit>;
  calculatedBatchMF: string;
  compoundKey?: string;
  casNumber?: string;
  stereoisomerCode?: DictionaryItemRef;
  saltCode?: DictionaryItemRef;
  saltEQ?: number;
}

/**
 * Polymorphic on `type` (`@JsonTypeInfo(Id.NAME)`). An `UNKNOWN` compound carries only
 * `formula` and `molWeight` — every other getter on it is `@JsonIgnore`d — which is why the
 * three cases cannot share one flat interface.
 */
export type CompoundRef =
  | ({ type: 'STORED' } & IdentifiedCompoundRef)
  | ({ type: 'VIRTUAL' } & IdentifiedCompoundRef)
  | ({ type: 'UNKNOWN' } & CompoundRefBase);

/* ── Enums ─────────────────────────────────────────────────────────────────────────────── */

export type ReactionRole = 'REACTANT' | 'REAGENT' | 'CATALYST' | 'SOLVENT' | 'OUTPUT';
export type ReactionOutputType = 'FINAL' | 'BY_PRODUCT' | 'INTERMEDIATE';
export type SampleRegistrationStatus = 'IN_PROGRESS' | 'FAILED' | 'REGISTERED';
export type ComparisonOperator = 'GREATER_THAN' | 'LESS_THAN' | 'EQUALS' | 'APPROXIMATELY';
export type PurityCalculationType = 'NMR' | 'HPLC' | 'LCMS' | 'CHN' | 'MS';
export type SolubidityQualitativeType = 'SOLUBLE' | 'UNSOLUBLE' | 'PRECIPITATE';

/* ── Output-sample value objects ───────────────────────────────────────────────────────── */

export interface MeltingPoint {
  lower?: number;
  upper?: number;
  comments?: string;
}

export interface ExternalSupplier {
  supplier: DictionaryItemRef;
  registryNumber: string;
}

export interface PurityCalculation {
  type: PurityCalculationType;
  operator: ComparisonOperator;
  purity: number;
  comment?: string;
}

export interface ResidualSolvent {
  solvent: DictionaryItemRef;
  eq: number;
  comment?: string;
}

/**
 * Polymorphic on `type`, like `CompoundRef`. indigo-frontend models this flat with a
 * `solubidityType` field; the discriminator on the wire is `type`, and the two cases carry
 * different fields.
 */
export type SolubidityInSolvent = { solvent: DictionaryItemRef; comment?: string } & (
  | { type: 'QUANTITATIVE'; operator?: ComparisonOperator; value?: number; unit?: DensityUnit }
  | { type: 'QUALITATIVE'; qualitativeType?: SolubidityQualitativeType }
);

/* ── Rows and samples ──────────────────────────────────────────────────────────────────── */

/** `ReactionRow` — what an input row and an output row share. */
interface ReactionRow {
  compound: CompoundRef;
  eq: EnteredValue<NoUnit>;
  /** Position in the rxnfile; present only for roles that appear in the drawn scheme. */
  rxnPosition?: number;
}

/** `ReactionSample` — what an input sample and an output sample share. */
interface ReactionSample {
  density?: EnteredValue<DensityUnit>;
  molarity?: EnteredValue<MolarityUnit>;
  volume?: EnteredValue<VolumeUnit>;
  purity: EnteredValue<NoUnit>;
  strCode?: STRCode;
  healthHazards: DictionaryItemRef[];
}

export interface ReactionInputSample extends ReactionSample {
  anchor: UUID;
  sampleId?: UUID;
  nbkBatchNumber?: NbkBatchNumber;
  mol?: EnteredValue<MolUnit>;
  weight?: EnteredValue<WeightUnit>;
  comment?: string;
}

export interface ReactionOutputSample extends ReactionSample {
  anchor: UUID;
  nbkBatchNumber: NbkBatchNumber;
  /** Read-only, derived: the ordinal of `nbkBatchNumber` alone, zero-padded to three. */
  shortNbkBatchNumber: string;
  actualMol?: EnteredValue<MolUnit>;
  actualWeight?: EnteredValue<WeightUnit>;
  /** Java field `yieldValue`, remapped to `yield` by `@JsonProperty`. */
  yield?: EnteredValue<NoUnit>;
  registrationStatus?: SampleRegistrationStatus;
  registrationStatusMessage?: string;
  sampleId?: UUID;
  handlingPrecautions?: DictionaryItemRef[];
  storageInstructions?: DictionaryItemRef[];
  compoundProtection?: DictionaryItemRef[];
  solubilityInSolvents?: SolubidityInSolvent[];
  residualSolvents?: ResidualSolvent[];
  meltingPoint?: MeltingPoint;
  purityCalculations?: PurityCalculation[];
  externalSupplier?: ExternalSupplier;
  source?: DictionaryItemRef;
  sourceDetails?: DictionaryItemRef;
  componentState?: DictionaryItemRef;
  batchComment?: string;
  structureComment?: string;
}

export interface ReactionInput extends ReactionRow {
  anchor: UUID;
  role: ReactionRole;
  mol?: EnteredValue<MolUnit>;
  chemicalName?: string;
  samples: ReactionInputSample[];
  /**
   * Read-only, derived from the reaction's `limitingAnchor`, and `NON_DEFAULT` — so it is
   * absent rather than `false` on every row but the limiting one.
   */
  limiting?: boolean;
}

export interface ReactionOutput extends ReactionRow {
  anchor: UUID;
  outputName: string;
  chemicalName?: string;
  type: ReactionOutputType;
  intended: boolean;
  theoMol?: EnteredValue<MolUnit>;
  theoWeight?: EnteredValue<WeightUnit>;
  samples: ReactionOutputSample[];
}

/**
 * One step of an experiment. The `model`/`reaction`/`row` parents are `@JsonBackReference`
 * on the backend and are not on the wire, so the tree here is strictly downward.
 */
export interface Reaction {
  anchor: UUID;
  /** Lowercase `f` — the `SetScheme` mutation spells the same thing `rxnFile`. */
  rxnfile?: string;
  inputs: ReactionInput[];
  /** Which input row is limiting; `ReactionInput.limiting` is derived from it. */
  limitingAnchor?: UUID;
  outputs: ReactionOutput[];
  /** Read-only, derived: the STR code of every reactant sample that has one. */
  precursorReactantIds: STRCode[];
}

/**
 * Mirrors `ExperimentModel`, the JSON blob in `experiment.model`. `reactions` are the steps,
 * and it is `@NotEmpty` — an experiment always has at least one.
 */
export interface ExperimentModel {
  reactions: Reaction[];
  significantFigures: number;
}
