import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@/lib/types/reactions.ts';

/**
 * The experiment model-mutation protocol: `POST /experiments/{id}/mutate` takes one
 * `Mutation` and answers with a `MutationResponse` carrying a **JSON diff**, not a new
 * experiment. `src/lib/json-patcher.ts` applies that diff.
 */

/**
 * Mirrors `Mutation` (eln-api, reaction/model/mutation) — a sealed interface Jackson
 * serialises with `@JsonTypeInfo(Id.SIMPLE_NAME, property = "type")`, so the wire shape is a
 * discriminated union whose tag is the record's own class name.
 *
 * The backend declares some eighty of them. Only the ones a screen actually sends are
 * declared here, exactly as `ExperimentEditRequest` declares only the fields it edits —
 * adding a member is how the next mutation gets wired up.
 */
export type Mutation =
  SetScheme | AddEmptyInput | SetExperimentSignificantFigures | ReactionInputMutation | ReactionInputSampleMutation;

/** The nine mutations keyed by a `ReactionInput`'s anchor — one compound row. */
export type ReactionInputMutation =
  | SetInputRowRole
  | SetInputRowChemicalName
  | SetInputRowLimiting
  | SetInputRowSaltCode
  | SetInputRowSaltEQ
  | SetInputRowEQ
  | SetInputRowMol
  | SetInputCompoundMolWeight
  | RemoveInputRow;

/** The nine mutations keyed by a `ReactionInputSample`'s anchor — one batch of one compound. */
export type ReactionInputSampleMutation =
  | SetInputWeight
  | SetInputVolume
  | SetInputMol
  | SetInputDensity
  | SetInputMolarity
  | SetInputPurity
  | SetInputHealthHazards
  | SetInputComment
  | RemoveInput;

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

/* ── Reaction-level ────────────────────────────────────────────────────────────────────── */

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

/* ── Experiment-level ──────────────────────────────────────────────────────────────────── */

/**
 * How many significant figures every calculated value is formatted to. Backend-only: the
 * patch comes back with every affected `EnteredValue.value` already re-rendered as a string,
 * so there is no client-side rounding anywhere. `@Min(1) @Max(5)` on the record.
 */
export interface SetExperimentSignificantFigures {
  type: 'SetExperimentSignificantFigures';
  significantFigures: number;
}

/* ── Input rows ────────────────────────────────────────────────────────────────────────── */

/**
 * Every mutation below is keyed by `anchor`, and **which** anchor is the whole distinction
 * between the two groups: a `ReactionInputMutation` names a `ReactionInput` (the compound
 * row), a `ReactionInputSampleMutation` names one of that row's `ReactionInputSample`s.
 * Sending a sample anchor to a row mutation resolves to nothing and the call 400s.
 *
 * **Numbers are sent as strings** — `EnteredValue.value` is a string on the wire, and these
 * mirror it. `saltEQ` is the one member the Java record declares as a `Double`; Jackson
 * coerces a JSON string into it, and the record is due to be changed to match. Do not
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

/** Note `molUnit`, not `unit` — the sample-level `SetInputMol` spells the same thing `unit`. */
export interface SetInputRowMol {
  type: 'SetInputRowMol';
  anchor: UUID;
  mol: string | null;
  molUnit: MolUnit | null;
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

export interface SetInputWeight {
  type: 'SetInputWeight';
  anchor: UUID;
  weight: string | null;
  unit: WeightUnit | null;
}

export interface SetInputVolume {
  type: 'SetInputVolume';
  anchor: UUID;
  volume: string | null;
  unit: VolumeUnit | null;
}

export interface SetInputMol {
  type: 'SetInputMol';
  anchor: UUID;
  mol: string | null;
  unit: MolUnit | null;
}

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

/** Mirrors `MutationResponse` (eln-api, eln/model). */
export interface MutationResponse {
  /**
   * The diff, in the dialect `JSONPatcher` reads — never a standard RFC 6902 patch.
   * `@NotNull`, so always present, though it is `{}` when nothing changed.
   */
  patch: unknown;
  /**
   * Input anchor → the molfile the backend could not resolve to a compound. `SetScheme`
   * populates this on essentially every scheme edit.
   */
  unresolvedInputs?: Record<UUID, string>;
  /** Things the user should be told about what the mutation did. `NON_EMPTY`. */
  messages?: string[];
  debugMessages?: string[];
  /** Reaction anchor → a server-rendered SVG. Unused here: schemes are drawn client-side. */
  reactionImages?: Record<UUID, string>;
}
