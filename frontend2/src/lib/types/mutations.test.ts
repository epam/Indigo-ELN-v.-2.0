import { describe, expect, expectTypeOf, it } from 'vitest';

import type { ExperimentMutation, ModelMutation, Mutation } from '@/lib/types/mutations.ts';

/**
 * The mirror is only worth having if it stays one. These are type-level assertions — they
 * compile to nothing and assert nothing at runtime, which is exactly right: the claim is about
 * the shape of the union, and `tsc` is what checks it.
 */

/**
 * Every `type` tag the backend's `@JsonSubTypes` list declares, in its order. Regenerate with:
 *
 * ```
 * grep -o 'Type(\w*\.\(\w*\)\.class)' \
 *   backend/eln/eln-api/src/main/java/com/epam/indigoeln/reaction/model/mutation/Mutation.java
 * ```
 *
 * A member added there and not here fails `MISSING` below; one invented here and absent from
 * the union fails `EXTRA`. Either way the diff names the mutation.
 */
const BACKEND_MUTATION_TYPES = [
  'SetScheme',
  'ResolveInputs',
  'AddEmptyInput',
  'AddInput',
  'AddNoProductSample',
  'ImportSDF',

  'SetInputRowRole',
  'SetInputRowMol',
  'SetInputRowChemicalName',
  'SetInputRowLimiting',
  'SetInputRowSaltCode',
  'SetInputRowSaltEQ',
  'SetInputRowEQ',
  'SetInputCompoundStereoisomerCode',
  'SetInputCompoundMolWeight',
  'RemoveInputRow',

  'SetInputDensity',
  'SetInputMolarity',
  'SetInputVolume',
  'SetInputPurity',
  'SetInputHealthHazards',
  'SetInputMol',
  'SetInputWeight',
  'SetInputComment',
  'RemoveInput',

  'AddProductSample',
  'SetOutputRowType',
  'SetOutputRowSaltCode',
  'SetOutputRowSaltEQ',
  'SetOutputRowEQ',
  'SetOutputRowName',
  'SetOutputRowChemicalName',
  'SetOutputRowIntended',
  'SetOutputCompoundStereoisomerCode',
  'SetOutputCompoundMolWeight',

  'SetOutputDensity',
  'SetOutputMolarity',
  'SetOutputVolume',
  'SetOutputPurity',
  'SetOutputHealthHazards',
  'SetOutputActualMol',
  'SetOutputActualWeight',
  'RegisterSample',
  'SetOutputHandlingPrecautions',
  'SetOutputStorageInstructions',
  'SetOutputCompoundProtection',
  'SetOutputSolubilityInSolvents',
  'SetOutputResidualSolvents',
  'SetOutputMeltingPoint',
  'SetOutputPurityCalculations',
  'SetOutputExternalSupplier',
  'SetOutputSource',
  'SetOutputSourceDetails',
  'SetOutputComponentState',
  'SetOutputBatchComment',
  'SetOutputStructureComment',
  'RemoveProductSample',
  'SetOutputSaltCode',
  'SetOutputSaltEQ',
  'SetOutputStereoisomerCode',
  'SetOutputMolfile',

  'CreateExperiment',
  'SetExperimentSignificantFigures',
  'EditExperimentAttributes',
  'EditExperimentAccess',
  'SetBatchCreator',
  'CreateExperimentAttachment',
  'DeleteExperimentAttachment',
  'CancelExperiment',
  'ReopenExperiment',
  'CompleteExperiment',
  'SubmitExperiment',
  'SignatureUpdated',
  'MakeVersion',
  'ExperimentAccessUpdated',
  'ExperimentNameUpdated',
  'Undo',
  'Redo',

  'CreateProject',
  'EditProjectAttributes',
  'EditProjectAccess',
  'CreateProjectAttachment',
  'DeleteProjectAttachment',
  'ProjectAccessUpdated',
  'ProjectUndo',
  'ProjectRedo',

  'CreateNotebook',
  'EditNotebookAttributes',
  'EditNotebookAccess',
  'CreateNotebookAttachment',
  'DeleteNotebookAttachment',
  'NotebookAccessUpdated',
  'NotebookUndo',
  'NotebookRedo',
] as const;

type BackendType = (typeof BACKEND_MUTATION_TYPES)[number];

/** The `type` tags the union here actually declares. */
type MirroredType = Mutation['type'];

type MISSING = Exclude<BackendType, MirroredType>;
type EXTRA = Exclude<MirroredType, BackendType>;

describe('the mutation mirror', () => {
  it('declares every member of the backend @JsonSubTypes list, and no others', () => {
    expectTypeOf<MISSING>().toEqualTypeOf<never>();
    expectTypeOf<EXTRA>().toEqualTypeOf<never>();
  });

  /**
   * The two `Exclude`s above compare sets, so a member listed twice — the likely slip when
   * transcribing ninety-four names — would pass them both. The count and the de-duplicated
   * count are what catch that, and they are runtime assertions because a length is a value.
   *
   * ```
   * grep -c '@JsonSubTypes.Type(' \
   *   backend/eln/eln-api/src/main/java/com/epam/indigoeln/reaction/model/mutation/Mutation.java
   * ```
   */
  it("lists each of the backend's 94 members exactly once", () => {
    expect(BACKEND_MUTATION_TYPES).toHaveLength(94);
    expect(new Set(BACKEND_MUTATION_TYPES).size).toBe(94);
  });

  /**
   * `ModelMutation` is what `POST /experiments/{id}/mutate` accepts, and the endpoint is the
   * only reason the distinction exists: everything else answers through its own REST call.
   */
  it('keeps the mutate endpoint to the members it accepts', () => {
    expectTypeOf<ModelMutation>().toExtend<ExperimentMutation>();
    // `ImportSDF` needs the multipart upload; project and notebook scopes are not experiments.
    expectTypeOf<
      Extract<ModelMutation['type'], 'ImportSDF' | 'CreateProject' | 'CreateNotebook'>
    >().toEqualTypeOf<never>();
    // ...and the ones with their own endpoints stay out too.
    expectTypeOf<
      Extract<ModelMutation['type'], 'CompleteExperiment' | 'EditExperimentAttributes'>
    >().toEqualTypeOf<never>();
  });
});
