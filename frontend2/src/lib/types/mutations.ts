import type { UUID } from '@/lib/types/common.ts';

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
export type Mutation = SetScheme;

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
