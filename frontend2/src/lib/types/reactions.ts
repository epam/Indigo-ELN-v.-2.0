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

/** Every measurement unit, matching the backend's `MeasurementUnit.ALL_UNITS`. */
export type MeasurementUnit = MolUnit | WeightUnit | VolumeUnit | DensityUnit | MolarityUnit | MolWeightUnit | NoUnit;

/**
 * How each unit is written. Mirrors the `displayName` on the backend's `MeasurementUnit`
 * enums — the wire carries the enum name (`MMOL`) and never the label (`mmol`), so this is
 * the only place the two are tied together.
 *
 * One flat map over every unit type, matching `MeasurementUnit.ALL_UNITS`: the names are
 * unique across all seven enums, and a per-type map would make `unitLabel` generic for no
 * gain. `NO_UNIT` is the empty string, so a unitless value renders as a bare number.
 */
const UNIT_LABELS: Record<MeasurementUnit, string> = {
  UMOL: 'μmol',
  MMOL: 'mmol',
  MOL: 'mol',
  MG: 'mg',
  G: 'g',
  KG: 'kg',
  ML: 'mL',
  L: 'L',
  MM: 'mM',
  M: 'M',
  G_ML: 'g/mL',
  G_PER_MOL: 'g/mol',
  NO_UNIT: '',
};

/**
 * Takes a bare `string` rather than `MeasurementUnit` because that is what `EnteredValue.unit`
 * is once it has been off the wire, and falls back to the raw enum name so an unmapped member
 * shows as itself rather than as a blank.
 */
export function unitLabel(unit: string | undefined): string {
  return unit === undefined ? '' : (UNIT_LABELS[unit as MeasurementUnit] ?? unit);
}

/**
 * The options a unit picker offers, in the order the backend enum declares them (ascending
 * magnitude). `as const` on each so a column can state which list it takes and have the
 * element type flow through to the mutation it builds.
 */
export const MOL_UNITS = ['UMOL', 'MMOL', 'MOL'] as const satisfies readonly MolUnit[];
export const WEIGHT_UNITS = ['MG', 'G', 'KG'] as const satisfies readonly WeightUnit[];
export const VOLUME_UNITS = ['ML', 'L'] as const satisfies readonly VolumeUnit[];
export const MOLARITY_UNITS = ['MM', 'M'] as const satisfies readonly MolarityUnit[];
export const DENSITY_UNITS = ['G_ML'] as const satisfies readonly DensityUnit[];
export const MOL_WEIGHT_UNITS = ['G_PER_MOL'] as const satisfies readonly MolWeightUnit[];

/**
 * A unitless quantity — EQ, purity, salt EQ. The model still types these `EnteredValue<NoUnit>`
 * and the backend still expects `NO_UNIT` back, so they are a one-option list rather than a
 * separate cell kind: the picker hides itself when there is nothing to choose between.
 */
export const NO_UNITS = ['NO_UNIT'] as const satisfies readonly NoUnit[];

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

/**
 * The formula as text: `C<sub>6</sub>H<sub>6</sub>` → `C6H6`.
 *
 * Needed wherever a formula is used as something other than markup, and both of those are easy
 * to get wrong in the same silent way. **Searching** the raw string means the value can never be
 * found as it is displayed — typing `C6H6` matches nothing, while `sub` matches every row that
 * has a formula at all. **Announcing** it means a screen reader spells the tags out character by
 * character, which is why an `aria-label` or an `alt` naming a compound by its formula needs this
 * too. Only `FormulaCell` and the two `[&_sub]` spans want the markup itself.
 */
export function plainFormula(formula: MolFormula): string {
  return formula.replace(/<[^>]+>/g, '');
}

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

/* ── Enum companion tables ─────────────────────────────────────────────────────────────── */

export const REACTION_ROLES: readonly ReactionRole[] = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT', 'OUTPUT'];

export const REACTION_ROLE_LABELS: Record<ReactionRole, string> = {
  REACTANT: 'Reactant',
  REAGENT: 'Reagent',
  CATALYST: 'Catalyst',
  SOLVENT: 'Solvent',
  OUTPUT: 'Output',
};

/** The reaction roles an *input* row may take. `OUTPUT` is in the enum but never a choice here. */
export const INPUT_ROLES: readonly ReactionRole[] = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT'];

/** The three `ReactionOutputType` members, in the order the picker offers them. */
export const OUTPUT_TYPES: readonly ReactionOutputType[] = ['FINAL', 'BY_PRODUCT', 'INTERMEDIATE'];

/**
 * How each product type reads: the product the chemist was after, something the reaction threw
 * off along the way, or an intermediate the next step consumes. Shared, because the products
 * table offers the three in a picker and the batches table shows the owning product's type as a
 * read-only badge.
 *
 * **`FINAL` reads "Final", deliberately not "Intended"** — even though the design says the
 * latter. `ReactionOutput.intended` is a separate boolean saying the product was drawn in the
 * reaction scheme, and it is what decides membership of this table at all; every row here is
 * `intended` and any of the three types is reachable on it. Two adjacent concepts sharing one
 * word made the column unreadable. "Final" is also what indigo-frontend's batch summary writes
 * for this enum member.
 */
export const OUTPUT_TYPE_LABELS: Record<ReactionOutputType, string> = {
  FINAL: 'Final',
  BY_PRODUCT: 'Side',
  INTERMEDIATE: 'Intermediate',
};

/**
 * The colour each type carries. Read by `OutputTypeCell`'s select trigger in the products table
 * and by `OutputTypeBadge` in the batches one.
 */
export const OUTPUT_TYPE_TRIGGER_CLASS: Record<ReactionOutputType, string> = {
  FINAL: 'border-green-200 bg-green-10',
  BY_PRODUCT: 'border-orange-200 bg-orange-10',
  INTERMEDIATE: 'border-violet-200 bg-violet-10',
};

/**
 * How each registration state reads. A batch that has never been sent reads "None" rather than
 * being blank: an empty cell in this column would be indistinguishable from a missing value.
 */
export const REGISTRATION_STATUS_LABELS: Record<SampleRegistrationStatus, string> = {
  IN_PROGRESS: 'In Progress',
  FAILED: 'Failed',
  REGISTERED: 'Registered',
};

/** `>`, `<`, `=`, `≈` — how each comparison reads in front of a number. */
export const OPERATOR_SYMBOLS: Record<ComparisonOperator, string> = {
  GREATER_THAN: '>',
  LESS_THAN: '<',
  EQUALS: '=',
  APPROXIMATELY: '≈',
};

export const QUALITATIVE_LABELS: Record<SolubidityQualitativeType, string> = {
  SOLUBLE: 'Soluble',
  UNSOLUBLE: 'Insoluble',
  PRECIPITATE: 'Precipitate',
};

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
