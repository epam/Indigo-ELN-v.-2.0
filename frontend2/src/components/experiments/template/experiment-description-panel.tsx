import type { FocusEvent } from 'react';
import { useRef, useState } from 'react';

import { RichText } from '@/components/common/rich-text';
import { SavingOverlay } from '@/components/common/saving-overlay';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { useEditExperiment } from '@/lib/api/experiments';
import { richTextEdit } from '@/lib/rich-text';
import { canEditExperiment } from '@/lib/types/experiments.ts';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';

/**
 * The `experimentDescription` template component.
 *
 * **There is no edit mode on this screen**, unlike the project and notebook cards, which open a
 * form dialog: the editor is the field, and leaving it saves. `richTextEdit` decides whether
 * anything actually changed, so tabbing through costs no request.
 *
 * The draft is local because the editor has to keep the user's keystrokes while the PATCH is in
 * flight — and because a save writes the server's own copy back into the query cache, which
 * would otherwise fight the caret.
 */
export function ExperimentDescriptionPanel({
  experiment,
  labelledBy,
}: {
  experiment: ExperimentDetails;
  /** The card heading's id — the editor names itself after the visible title. */
  labelledBy?: string;
}) {
  const edit = useEditExperiment(experiment.id);
  const [draft, setDraft] = useState(experiment.description ?? '');
  /**
   * What the field held when the user last entered it — the thing a save is judged against.
   *
   * **Not `experiment.description`.** Tiptap re-serialises stored HTML into its own canonical
   * form as it loads: plain text becomes `<p>text</p>`, a `<div>` becomes a `<p>`, newlines
   * between tags are dropped. That arrives through `onChange` exactly like a keystroke, so a
   * draft compared against the stored string differs before the user has touched anything, and
   * every blur PATCHed a value nobody edited. Snapshotting at focus takes the normalisation —
   * which always happens before the field can be focused — into the baseline instead.
   */
  const baseline = useRef(draft);

  const canEdit = canEditExperiment(experiment);
  if (!canEdit) return <RichText html={experiment.description} />;

  /** Whether focus crossed the widget's boundary, rather than moving within it. */
  function isExternal(event: FocusEvent<HTMLDivElement>) {
    // `relatedTarget` is null when focus goes nowhere at all — a click on the page background —
    // which counts as crossing.
    return !event.relatedTarget || !event.currentTarget.contains(event.relatedTarget);
  }

  /**
   * Opens an editing session. Guarded the same way as the blur: moving between the toolbar and
   * the text area is not a new session, and re-snapshotting there would swallow the edit in
   * progress.
   */
  function handleFocus(event: FocusEvent<HTMLDivElement>) {
    if (isExternal(event)) baseline.current = draft;
  }

  /**
   * Fires on `focusout`, which bubbles from the toolbar buttons too — so a click on Bold would
   * otherwise read as leaving the field.
   *
   * The baseline is left alone: the next focus takes a fresh snapshot, and a blur cannot happen
   * without one, so a save that failed is retried the next time the field is left.
   */
  function handleBlur(event: FocusEvent<HTMLDivElement>) {
    if (!isExternal(event)) return;

    const description = richTextEdit(draft, baseline.current);
    if (description) edit.mutate({ description: description.value });
  }

  return (
    <div onFocus={handleFocus} onBlur={handleBlur}>
      {/*
        Both, and not by accident: `SavingOverlay` inerts the region, which is what makes one
        primitive work for any control, while `disabled` is Tiptap's own contract and greys the
        toolbar buttons individually rather than dimming the whole frame.
      */}
      {/* `top`, not the default: the editor is tall, and a centred spinner would land on the prose. */}
      <SavingOverlay pending={edit.isPending} spinner="top">
        <RichTextEditor
          value={draft}
          onChange={setDraft}
          disabled={edit.isPending}
          aria-labelledby={labelledBy}
          placeholder="Describe the experiment"
        />
      </SavingOverlay>
    </div>
  );
}
