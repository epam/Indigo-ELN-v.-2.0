import { ChevronDown } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { ApiImage } from '@/components/common/api-image';
import { SavingOverlay } from '@/components/common/saving-overlay';
import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import { isSampleProtected } from '@/components/experiments/stoichiometry/batches/columns';
import {
  externalSupplierLabel,
  meltingPointLabel,
  purityCalculationLabels,
  residualSolventLabels,
  solubilityLabels,
} from '@/components/experiments/stoichiometry/batches/detail';
import { cellId } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { Collapsible, CollapsiblePanel, CollapsibleTrigger } from '@/components/ui/collapsible';
import { MultiCombobox } from '@/components/ui/combobox';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { useDictionary } from '@/lib/api/dictionaries';
import { cn } from '@/lib/utils';

import type { BatchRow } from '@/components/experiments/stoichiometry/batches/columns';
import type { StoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { CompoundRef, EnteredValue, Reaction } from '@/lib/types/reactions.ts';
import { unitLabel } from '@/lib/types/reactions.ts';

/**
 * What one batch of the Product Batch Summary holds, behind its chevron: the compound's structure
 * on the left, the batch's own fields on the right, and the rest under Additional Information.
 *
 * **It saves, which indigo-frontend's `BatchDetailPanelComponent` does not** — that one builds a
 * `FormGroup` and leaves handling its `valueChanges` as a TO DO, so every edit in it is discarded.
 * Each field here commits on its own gesture with no Save button, exactly as the table's cells and
 * `ExperimentDetailsPanel` do, and through the **table's** `useStoichiometryMutations` rather than
 * a hook of its own: one subscription covers every row, and a panel save then queues behind the
 * cell saves in `experimentWrite`'s scope instead of racing them.
 *
 * Three groups of fields, and which group a field is in is not a style choice:
 *
 * - **Editable**, one `SetOutput*` mutation each.
 * - **Derived**, shown read-only because the backend computes them — the batch MW and MF, the
 *   theoretical weight and moles, the precursor ids, the conversational batch number.
 * - **Composite**, read-only for now: melting point, residual solvents, solubility, external
 *   supplier and purity calculations are nested records and discriminated unions, and each needs
 *   an editor dialog that does not exist yet. Their mutations are typed and waiting; see the TODO
 *   on each.
 *
 * Every mutation here is keyed by the **sample** anchor. Salt Code, Salt EQ and Stereoisomer have
 * `SetOutputRow*` siblings keyed by the *output* — sending one of those from here names a product
 * row rather than a batch, and 400s.
 */
export function BatchDetailPanel({
  row,
  reaction,
  canEdit,
  mutations,
}: {
  row: BatchRow;
  /** For `precursorReactantIds`, which is derived on the reaction rather than on the batch. */
  reaction: Reaction;
  canEdit: boolean;
  /** The table's, not one of this panel's own — see the note above. */
  mutations: StoichiometryMutations;
}) {
  const { output, sample } = row;
  const anchor = sample.anchor;

  /**
   * An `UNKNOWN` compound `@JsonIgnore`s salt code, stereoisomer, salt EQ, compound key and
   * calculated MF, so narrowing the union is what makes those fields reachable at all — and their
   * absence is why the controls are disabled rather than merely empty.
   */
  const compound: Extract<CompoundRef, { type: 'STORED' | 'VIRTUAL' }> | undefined =
    output.compound.type === 'UNKNOWN' ? undefined : output.compound;

  /**
   * Compound-level edits freeze once the batch has gone to the registry: `CompoundHandlers`
   * rejects a registered sample's salt code, salt EQ and stereoisomer. The batch's own fields
   * stay live, the same split the table's numeric cells make.
   */
  const compoundEditable = canEdit && compound != null && !isSampleProtected(sample);

  const id = (name: string) => `batch-${anchor}-${name}`;
  const pending = (name: string) => mutations.savingCells.has(cellId(anchor, name));
  const commit = (name: string, mutation: ModelMutation) => mutations.save(cellId(anchor, name), mutation);

  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,320px)_minmax(0,1fr)]">
      <Structure compound={output.compound} batch={sample.shortNbkBatchNumber} />

      <div className="flex min-w-0 flex-col gap-4">
        <h3 className="text-[16px]/6 font-semibold text-neutral-1000">
          Notebook Batch #: <span className="font-normal">{sample.nbkBatchNumber}</span>
        </h3>

        <div className="grid gap-x-6 gap-y-4 md:grid-cols-2">
          <Fact label="Calculated Batch MW" value={compound?.molWeight.value} />
          {/*
            `MolFormula` serialises through `@JsonValue toHTMLString()`, so this arrives as
            `C<sub>9</sub>H<sub>8</sub>O<sub>4</sub>` — the same reason `FormulaCell` exists. The
            only markup is `<sub>` and the string is composed from an element table, not from user
            input, so there is nothing to sanitise.
          */}
          <Fact label="Calculated Batch MF" html={compound?.calculatedBatchMF} />

          <DictionaryField
            id={id('source')}
            label="Source"
            dictionary="SAMPLE_SOURCE"
            value={sample.source}
            editable={canEdit}
            pending={pending('source')}
            onCommit={(next) => commit('source', { type: 'SetOutputSource', anchor, source: next })}
          />
          <TextField
            id={id('structureComment')}
            label="Structure Comments"
            value={sample.structureComment}
            editable={canEdit}
            pending={pending('structureComment')}
            onCommit={(next) =>
              commit('structureComment', { type: 'SetOutputStructureComment', anchor, structureComment: next })
            }
          />

          <DictionaryField
            id={id('saltCode')}
            label="Salt Code & Name"
            dictionary="SALT_CODE"
            value={compound?.saltCode}
            editable={compoundEditable}
            pending={pending('saltCode')}
            onCommit={(next) => commit('saltCode', { type: 'SetOutputSaltCode', anchor, saltCode: next })}
          />
          <DictionaryField
            id={id('stereoisomerCode')}
            label="Stereoisomer Code"
            dictionary="STEREOISOMER_CODE"
            value={compound?.stereoisomerCode}
            editable={compoundEditable}
            pending={pending('stereoisomerCode')}
            onCommit={(next) =>
              commit('stereoisomerCode', { type: 'SetOutputStereoisomerCode', anchor, stereoisomerCode: next })
            }
          />

          <DictionaryField
            id={id('sourceDetails')}
            label="Source Detail"
            dictionary="SAMPLE_SOURCE_DETAILS"
            value={sample.sourceDetails}
            editable={canEdit}
            pending={pending('sourceDetails')}
            onCommit={(next) =>
              commit('sourceDetails', { type: 'SetOutputSourceDetails', anchor, sourceDetails: next })
            }
          />
          {/* Derived on the reaction: the STR code of every reactant sample that has one. */}
          <ReadonlyField
            id={id('precursors')}
            label="Precursor/Reactant IDs"
            value={reaction.precursorReactantIds.join(', ')}
          />

          <NumberField
            id={id('saltEQ')}
            label="Salt Equivalent"
            value={compound?.saltEQ}
            editable={compoundEditable}
            pending={pending('saltEQ')}
            onCommit={(next) => commit('saltEQ', { type: 'SetOutputSaltEQ', anchor, saltEQ: next })}
          />
          {/*
            `STRCodeSample` is a `@JsonValue` string. indigo-frontend models it as an object and
            interpolates it straight into the template, which prints `[object Object]`.
          */}
          <ReadonlyField id={id('strCode')} label="Conversational Batch number" value={sample.strCode} />

          <ReadonlyField id={id('theoWeight')} label="Theo. Weight" value={quantity(output.theoWeight)} />
          <ReadonlyField id={id('theoMol')} label="Theo. Moles" value={quantity(output.theoMol)} />

          <div className="md:col-span-2">
            <TextField
              id={id('batchComment')}
              label="Batch Comment"
              value={sample.batchComment}
              editable={canEdit}
              pending={pending('batchComment')}
              onCommit={(next) => commit('batchComment', { type: 'SetOutputBatchComment', anchor, batchComment: next })}
            />
          </div>
        </div>

        <AdditionalInformation>
          <DictionaryField
            id={id('componentState')}
            label="Compound State"
            dictionary="COMPONENT_STATE"
            value={sample.componentState}
            editable={canEdit}
            pending={pending('componentState')}
            onCommit={(next) =>
              commit('componentState', { type: 'SetOutputComponentState', anchor, componentState: next })
            }
          />
          <ReadonlyField id={id('compoundKey')} label="Virtual Compound ID" value={compound?.compoundKey} />

          <MultiDictionaryField
            id={id('compoundProtection')}
            label="Compound Protection"
            dictionary="COMPOUND_PROTECTION"
            value={sample.compoundProtection ?? []}
            editable={canEdit}
            pending={pending('compoundProtection')}
            onCommit={(next) =>
              commit('compoundProtection', {
                type: 'SetOutputCompoundProtection',
                anchor,
                // `@Size(min = 1)` when present: clearing sends null, never an empty list.
                compoundProtection: next.length === 0 ? null : next,
              })
            }
          />
          {/* TODO(melting-point-editor): `SetOutputMeltingPoint` takes `{lower, upper, comments}`. */}
          <ReadonlyField id={id('meltingPoint')} label="Melting Point" value={meltingPointLabel(sample.meltingPoint)} />

          {/* TODO(residual-solvents-editor): `SetOutputResidualSolvents` takes a row per solvent. */}
          <ChipsField label="Residual Solvents" values={residualSolventLabels(sample.residualSolvents)} />
          <MultiDictionaryField
            id={id('storageInstructions')}
            label="Storage Instructions"
            dictionary="STORAGE_INSTRUCTIONS"
            value={sample.storageInstructions ?? []}
            editable={canEdit}
            pending={pending('storageInstructions')}
            onCommit={(next) =>
              commit('storageInstructions', {
                type: 'SetOutputStorageInstructions',
                anchor,
                storageInstructions: next.length === 0 ? null : next,
              })
            }
          />

          <MultiDictionaryField
            id={id('healthHazards')}
            label="Health Hazards"
            dictionary="HEALTH_HAZARD"
            value={sample.healthHazards}
            editable={canEdit}
            pending={pending('healthHazards')}
            onCommit={(next) =>
              // `@NotNull` on this one, unlike its three neighbours: clearing sends an empty list.
              commit('healthHazards', { type: 'SetOutputHealthHazards', anchor, healthHazards: next })
            }
          />
          {/* TODO(solubility-editor): `SolubidityInSolvent` is a QUANTITATIVE/QUALITATIVE union. */}
          <ChipsField label="Solubility in Solvents" values={solubilityLabels(sample.solubilityInSolvents)} />

          <MultiDictionaryField
            id={id('handlingPrecautions')}
            label="Handling Precautions"
            dictionary="HANDLING_PRECAUTIONS"
            value={sample.handlingPrecautions ?? []}
            editable={canEdit}
            pending={pending('handlingPrecautions')}
            onCommit={(next) =>
              commit('handlingPrecautions', {
                type: 'SetOutputHandlingPrecautions',
                anchor,
                handlingPrecautions: next.length === 0 ? null : next,
              })
            }
          />
          {/* TODO(external-supplier-editor): `SetOutputExternalSupplier` pairs a supplier with a
              registry number, so the dictionary picker alone will not do. */}
          <ReadonlyField
            id={id('externalSupplier')}
            label="External Supplier"
            value={externalSupplierLabel(sample.externalSupplier)}
          />

          {/* TODO(purity-calculations-editor): `PurityCalculation` is method, operator and value. */}
          <ChipsField label="Purity Calculations" values={purityCalculationLabels(sample.purityCalculations)} />
        </AdditionalInformation>
      </div>
    </div>
  );
}

/** `0.0137 mg` — a derived quantity and its unit, as one read-only string. */
function quantity(value: EnteredValue<string> | undefined): string | undefined {
  if (value?.value == null) return undefined;
  const unit = unitLabel(value.unit);
  return unit ? `${value.value} ${unit}` : value.value;
}

/**
 * The compound's structure, from the ELN's own renderer.
 *
 * **Read-only, and deliberately without the design's pencil.** `CompoundRef` carries no molfile —
 * only a `compoundID` — so a sketcher opened from here would start empty and a Save would replace
 * the structure with whatever had been drawn from scratch. `SetOutputMolfile` is typed and waiting
 * for a molfile to seed it with.
 */
function Structure({ compound, batch }: { compound: CompoundRef; batch: string }) {
  const compoundID = compound.type === 'UNKNOWN' ? undefined : compound.compoundID;

  const frame = 'min-h-[260px] rounded-md border border-dashed border-neutral-300';

  if (compoundID == null) {
    return (
      <div className={cn(frame, 'flex items-center justify-center p-4 text-center text-[14px]/6 text-neutral-700')}>
        No structure available
      </div>
    );
  }

  // `border-dashed` overrides ApiImage's own solid frame; the rest of its box is what we want.
  return (
    <ApiImage path={`/api/eln/compounds/${compoundID}/picture`} alt={`Structure of batch ${batch}`} className={frame} />
  );
}

/**
 * The fold the design puts the rest of the batch under. The `Collapsible` primitive directly
 * rather than `CollapsibleCard`: this already sits inside a table row, and a card here would be a
 * second raised surface on top of the one it is in.
 */
function AdditionalInformation({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);

  return (
    <Collapsible open={open} onOpenChange={setOpen}>
      <CollapsibleTrigger className="rounded-2 py-1">
        <ChevronDown
          aria-hidden
          className={cn('size-5 shrink-0 text-neutral-700 transition-transform duration-150', open && 'rotate-180')}
        />
        <h4 className="text-[14px]/6 font-semibold text-neutral-1000">Additional Information</h4>
      </CollapsibleTrigger>
      <CollapsiblePanel>
        <div className="grid gap-x-6 gap-y-4 pt-4 md:grid-cols-2">{children}</div>
      </CollapsiblePanel>
    </Collapsible>
  );
}

/** A derived value with no box around it — the two headline numbers at the top of the panel. */
function Fact({ label, value, html }: { label: string; value?: string; html?: string }) {
  return (
    <div className="flex flex-col gap-1">
      <h4 className="text-[14px]/6 font-semibold text-neutral-800">{label}</h4>
      {html != null && html !== '' ? (
        <p
          className="text-[14px]/6 text-neutral-1000 [&_sub]:align-sub [&_sub]:text-[0.75em] [&_sub]:leading-none"
          dangerouslySetInnerHTML={{ __html: html }}
        />
      ) : (
        <p className="text-[14px]/6 text-neutral-1000">{value || '—'}</p>
      )}
    </div>
  );
}

/**
 * A value the user cannot change, in the same box as the ones they can, so the grid reads as one
 * form rather than two.
 *
 * `readOnly` rather than `disabled`: several of these are identifiers people copy out — the STR
 * codes especially — and a disabled input cannot be focused or selected.
 */
function ReadonlyField({ id, label, value }: { id: string; label: string; value: string | undefined }) {
  return (
    <Field id={id} label={label}>
      <Input
        id={id}
        readOnly
        value={value ?? ''}
        placeholder="—"
        className="cursor-default bg-neutral-100 text-neutral-800"
      />
    </Field>
  );
}

/** Read-only list values, as chips. What each chip says is `batch-detail.ts`'s business. */
function ChipsField({ label, values }: { label: string; values: string[] }) {
  return (
    <div className="flex flex-col gap-1.5">
      <h4 className="text-[14px]/6 text-neutral-800">{label}</h4>
      {values.length === 0 ? (
        <p className="flex h-10 items-center text-[14px]/6 text-neutral-700">—</p>
      ) : (
        <ul className="flex min-h-10 flex-wrap items-center gap-1.5">
          {values.map((value) => (
            <li key={value} className="rounded-2 bg-neutral-200 px-2 py-1 text-[13px]/5 text-neutral-1000">
              {value}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/**
 * A local draft of a saved value, reseeded whenever the server's copy moves.
 *
 * **The reseed is the point.** Without it a value the backend normalises — `1.50` stored as the
 * `Double` 1.5, `01` stored as 1 — leaves the draft permanently disagreeing with what was saved,
 * so the "did this change?" test on the next blur is true again and fires an identical mutation,
 * once per focus/blur cycle for as long as the panel is open. The stoichiometry table avoids the
 * same trap by keying `TextCell` on the saved value; these fields are local to this file, so they
 * carry it themselves rather than making every call site remember a `key`.
 *
 * A *failed* save moves nothing, so the draft survives it — which is what the fields promise.
 *
 * Adjusted during render, the pattern React documents for reacting to a changed prop: an effect
 * would leave a frame showing the stale draft.
 */
function useDraft(saved: string): [string, (next: string) => void] {
  const [draft, setDraft] = useState(saved);
  const [seeded, setSeeded] = useState(saved);

  if (saved !== seeded) {
    setSeeded(saved);
    setDraft(saved);
  }

  return [draft, setDraft];
}

/** Free text, committed when the field is left — the contract every text field on this screen has. */
function TextField({
  id,
  label,
  value,
  editable,
  pending,
  onCommit,
}: {
  id: string;
  label: string;
  value: string | undefined;
  editable: boolean;
  pending: boolean;
  onCommit: (next: string | null) => void;
}) {
  const [draft, setDraft] = useDraft(value ?? '');

  if (!editable) return <ReadonlyField id={id} label={label} value={value} />;

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={pending}>
        <Input
          id={id}
          value={draft}
          placeholder="Text"
          disabled={pending}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={() => {
            const next = draft.trim() === '' ? null : draft.trim();
            // Both sides normalised to null, so "" and undefined are not seen as a change.
            if (next !== (value ?? null)) onCommit(next);
          }}
        />
      </SavingOverlay>
    </Field>
  );
}

/**
 * A number committed on blur. Sent as a **string** — `SetOutputSaltEQ` declares a `Double` on the
 * record but takes a string on the wire, the same as every other numeric mutation.
 */
function NumberField({
  id,
  label,
  value,
  editable,
  pending,
  onCommit,
}: {
  id: string;
  label: string;
  value: number | undefined;
  editable: boolean;
  pending: boolean;
  onCommit: (next: string | null) => void;
}) {
  const saved = value == null ? '' : String(value);
  const [draft, setDraft] = useDraft(saved);

  if (!editable) return <ReadonlyField id={id} label={label} value={saved || undefined} />;

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={pending}>
        <Input
          id={id}
          type="number"
          value={draft}
          placeholder="—"
          disabled={pending}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={() => {
            const trimmed = draft.trim();
            if (trimmed !== saved) onCommit(trimmed === '' ? null : trimmed);
          }}
        />
      </SavingOverlay>
    </Field>
  );
}

/**
 * One item from a built-in dictionary, or none. A `Combobox` rather than the table's `Select`:
 * these lists are the long ones — solvents, suppliers, salt codes — and a form field has the room
 * for a filter the dense table cells do not.
 */
function DictionaryField({
  id,
  label,
  dictionary,
  value,
  editable,
  pending,
  onCommit,
}: {
  id: string;
  label: string;
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef | undefined;
  editable: boolean;
  pending: boolean;
  onCommit: (next: DictionaryItemRef | null) => void;
}) {
  if (!editable) return <ReadonlyField id={id} label={label} value={value?.name} />;

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={pending}>
        <DictionaryCombobox
          id={id}
          dictionary={dictionary}
          value={value ?? null}
          disabled={pending}
          // Picking is the commit; there is no separate confirmation step to wait for.
          onValueChange={(next) => {
            if ((next?.id ?? null) !== (value?.id ?? null)) onCommit(next);
          }}
        />
      </SavingOverlay>
    </Field>
  );
}

/**
 * Several items from a built-in dictionary, as chips.
 *
 * `MultiCombobox` filters nothing itself, so the list is narrowed here against the typed input —
 * the same arrangement `MultiDictionaryCell` makes. `allowCustomValues` stays off: every value has
 * to be a dictionary entry.
 */
function MultiDictionaryField({
  id,
  label,
  dictionary,
  value,
  editable,
  pending,
  onCommit,
}: {
  id: string;
  label: string;
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef[];
  editable: boolean;
  pending: boolean;
  onCommit: (next: DictionaryItemRef[]) => void;
}) {
  const [inputValue, setInputValue] = useState('');
  const { data, isPending, isError } = useDictionary(dictionary);

  if (!editable) {
    return <ChipsField label={label} values={value.map((item) => item.name)} />;
  }

  const term = inputValue.trim().toLowerCase();
  const items = (data ?? []).filter((item) => item.name.toLowerCase().includes(term));

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={pending}>
        <MultiCombobox<DictionaryItemRef>
          id={id}
          value={value}
          items={items}
          itemToKey={(item) => item.id}
          itemToLabel={(item) => item.name}
          inputValue={inputValue}
          onInputValueChange={setInputValue}
          loading={isPending}
          // apiFetch has already toasted the failure; this says why the list is empty.
          error={isError}
          disabled={pending}
          onValueChange={onCommit}
        />
      </SavingOverlay>
    </Field>
  );
}
