import type { FocusEvent, ReactNode } from 'react';
import { useRef, useState } from 'react';

import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import { SavingOverlay } from '@/components/common/saving-overlay';
import { ExperimentRefsCombobox } from '@/components/experiments/template/experiment-refs-combobox';
import {
  dictionaryEdit,
  experimentRefsEdit,
  refKey,
  titleEdit,
} from '@/components/experiments/template/experiment-details';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { useEditExperiment } from '@/lib/api/experiments';
import { useDraft } from '@/lib/hooks/use-draft';
import { richTextEdit } from '@/lib/rich-text';
import { canEditExperiment } from '@/lib/types/experiments.ts';
import { formatDate } from '@/lib/utils';

import type { DateString, UserRef } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentDetails, ExperimentRef } from '@/lib/types/experiments.ts';

/**
 * One row of the read-only column: a label above its value, matching how `AboutNotebookCard`
 * stacks its sections.
 */
function Fact({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <h3 className="text-[14px]/6 font-semibold text-neutral-800">{label}</h3>
      <p className="text-[14px]/6 text-neutral-1000">{children}</p>
    </div>
  );
}

/** `08 Oct 2024 by John Doe` — the format the design uses for both timestamps. */
function stamp(iso: DateString, user: UserRef): string {
  return `${formatDate(iso)} by ${user.displayName}`;
}

/**
 * The `experimentDetails` template component: seven editable fields in two columns, with the
 * experiment's own provenance in a third, read-only one.
 *
 * **No edit mode and no Save button**, like everything else on this screen. Text and rich text
 * save when the field is left; the pickers save the moment something is selected or removed,
 * because picking *is* the commit gesture — and because the combobox popup is portalled, so a
 * click on an option fires `focusout` past the field and a blur-save would fire early.
 *
 * **Each field owns its own mutation instance**, which is what lets `isPending` freeze only the
 * control being saved while its neighbours stay live — the same reason `TeamCard` holds two
 * instances of one mutation rather than sharing one. They all carry the same write scope, so the
 * queue in `experiments.ts` serialises them however fast the user moves.
 *
 * Each field also keeps a local draft seeded from `experiment`: the control has to show the new
 * value while the PATCH is in flight, and a save that fails should leave the user's input alone
 * rather than snapping back to the server's. It is `useDraft`, so the seed is taken again whenever
 * the server's copy actually moves — otherwise a field only ever showed what the experiment held
 * when the panel mounted, and an edit made in another session would never reach it. A failed save
 * moves nothing, so that still leaves the user's input where it is.
 *
 * The two rich-text fields are the exception and snapshot a baseline on focus instead; see
 * `LiteratureField`.
 */
export function ExperimentDetailsPanel({ experiment }: { experiment: ExperimentDetails }) {
  const canEdit = canEditExperiment(experiment);

  return (
    <div className="grid gap-x-8 gap-y-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,18rem)]">
      <div className="flex flex-col gap-4">
        <TitleField experiment={experiment} canEdit={canEdit} />
        <DictionaryField
          experiment={experiment}
          canEdit={canEdit}
          id="experiment-therapeutic-area"
          label="Therapeutic Area"
          dictionary="THERAPEUTIC_AREA"
          saved={experiment.therapeuticArea}
          toRequest={(value) => ({ therapeuticArea: value })}
        />
        <DictionaryField
          experiment={experiment}
          canEdit={canEdit}
          id="experiment-project-code"
          label="Project Code & Name"
          dictionary="PROJECT_CODE"
          saved={experiment.projectCode}
          toRequest={(value) => ({ projectCode: value })}
        />
      </div>

      <div className="flex flex-col gap-4">
        <RefsField
          experiment={experiment}
          canEdit={canEdit}
          id="experiment-linked"
          label="Linked Experiment"
          saved={experiment.linkedExperiments}
          toRequest={(value) => ({ linkedExperiments: value })}
        />
        <RefsField
          experiment={experiment}
          canEdit={canEdit}
          id="experiment-continued-to"
          label="Cont. TO Rxn"
          saved={experiment.continuedTo}
          toRequest={(value) => ({ continuedTo: value })}
        />
        <RefsField
          experiment={experiment}
          canEdit={canEdit}
          id="experiment-continued-from"
          label="Cont. FROM Rxn"
          saved={experiment.continuedFrom}
          toRequest={(value) => ({ continuedFrom: value })}
        />
      </div>

      {/*
        Spans both rows of its own column so the border runs the full height of the form; the
        literature field below lands in row 2 of the first two columns.
      */}
      <div className="flex flex-col gap-4 xl:row-span-2 xl:border-l xl:border-neutral-300 xl:pl-8">
        <Fact label="Experiment Name">{experiment.name}</Fact>
        <Fact label="Batch Creator">{experiment.batchCreator.displayName}</Fact>
        <Fact label="Created">{stamp(experiment.createdAt, experiment.createdBy)}</Fact>
        <Fact label="Last Edited">{stamp(experiment.modifiedAt, experiment.modifiedBy)}</Fact>
      </div>

      <div className="xl:col-span-2">
        <LiteratureField experiment={experiment} canEdit={canEdit} />
      </div>
    </div>
  );
}

function TitleField({ experiment, canEdit }: { experiment: ExperimentDetails; canEdit: boolean }) {
  const edit = useEditExperiment(experiment.id);
  const [draft, setDraft] = useDraft(experiment.title ?? '');

  function handleBlur() {
    const title = titleEdit(draft, experiment.title);
    if (title) edit.mutate({ title: title.value });
  }

  return (
    <Field id="experiment-title" label="Experiment Title">
      <SavingOverlay pending={edit.isPending}>
        <Input
          id="experiment-title"
          value={draft}
          placeholder="Text"
          disabled={edit.isPending || !canEdit}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={handleBlur}
        />
      </SavingOverlay>
    </Field>
  );
}

function DictionaryField({
  experiment,
  canEdit,
  id,
  label,
  dictionary,
  saved,
  toRequest,
}: {
  experiment: ExperimentDetails;
  canEdit: boolean;
  id: string;
  label: string;
  dictionary: 'THERAPEUTIC_AREA' | 'PROJECT_CODE';
  saved: DictionaryItemRef | undefined;
  toRequest: (value: DictionaryItemRef | null) => Parameters<ReturnType<typeof useEditExperiment>['mutate']>[0];
}) {
  const edit = useEditExperiment(experiment.id);
  // By `id`: the picker's items come from a separate request, so the ref naming the current value
  // is never the object the experiment payload carried and `Object.is` would reseed every render.
  const [value, setValue] = useDraft<DictionaryItemRef | null>(saved ?? null, (item) => item?.id ?? null);

  function handleChange(next: DictionaryItemRef | null) {
    setValue(next);
    // Picking is the commit — there is no separate confirmation step to wait for.
    const change = dictionaryEdit(next, saved);
    if (change) edit.mutate(toRequest(change.value));
  }

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={edit.isPending}>
        <DictionaryCombobox
          id={id}
          dictionary={dictionary}
          value={value}
          onValueChange={handleChange}
          disabled={edit.isPending || !canEdit}
        />
      </SavingOverlay>
    </Field>
  );
}

function RefsField({
  experiment,
  canEdit,
  id,
  label,
  saved,
  toRequest,
}: {
  experiment: ExperimentDetails;
  canEdit: boolean;
  id: string;
  label: string;
  saved: ExperimentRef[];
  toRequest: (value: ExperimentRef[]) => Parameters<ReturnType<typeof useEditExperiment>['mutate']>[0];
}) {
  const edit = useEditExperiment(experiment.id);
  // By its sorted ids, for the reason `DictionaryField` gives — and because the backend field is a
  // `Set`, so a reorder is not a different list.
  const [value, setValue] = useDraft(saved, refKey);

  function handleChange(next: ExperimentRef[]) {
    setValue(next);
    const change = experimentRefsEdit(next, saved);
    if (change) edit.mutate(toRequest(change.value));
  }

  return (
    <Field id={id} label={label}>
      <SavingOverlay pending={edit.isPending}>
        <ExperimentRefsCombobox
          id={id}
          experimentId={experiment.id}
          value={value}
          onValueChange={handleChange}
          disabled={edit.isPending || !canEdit}
        />
      </SavingOverlay>
    </Field>
  );
}

function LiteratureField({ experiment, canEdit }: { experiment: ExperimentDetails; canEdit: boolean }) {
  const edit = useEditExperiment(experiment.id);
  const [draft, setDraft] = useState(experiment.literature ?? '');
  // Snapshotted on focus, not read from the experiment — see `ExperimentDescriptionPanel` for why
  // a rich-text field cannot be compared against the string the server sent.
  const baseline = useRef(draft);

  function isExternal(event: FocusEvent<HTMLDivElement>) {
    return !event.relatedTarget || !event.currentTarget.contains(event.relatedTarget);
  }

  return (
    <Field id="experiment-literature" label="Literature Reference">
      <div
        onFocus={(event) => {
          if (isExternal(event)) baseline.current = draft;
        }}
        onBlur={(event) => {
          if (!isExternal(event)) return;
          const literature = richTextEdit(draft, baseline.current);
          if (literature) edit.mutate({ literature: literature.value });
        }}
      >
        {/* `top`, not the default: the editor is tall, and a centred spinner would land on the prose. */}
        <SavingOverlay pending={edit.isPending} spinner="top">
          <RichTextEditor
            value={draft}
            onChange={setDraft}
            disabled={edit.isPending || !canEdit}
            aria-labelledby="experiment-literature-label"
            placeholder="Text"
          />
        </SavingOverlay>
      </div>
    </Field>
  );
}
