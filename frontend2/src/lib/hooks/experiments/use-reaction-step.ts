/**
 * Which reaction step the experiment screen is showing, as an index into
 * `ExperimentModel.reactions`.
 *
 * Always the first one for now. The step strip that used to pick it is gated off in
 * `stoichiometry-panel.tsx` — adding, renaming and deleting a step all need mutations the
 * backend does not have yet (there is no `AddReaction`), so a selector would only ever have
 * moved between steps someone else created. This exists so that when those arrive, the
 * selection becomes real in one place instead of in every panel that reads it.
 */
export function useReactionStep(): number {
  return 0;
}
