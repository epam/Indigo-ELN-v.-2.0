import type { KeyboardEvent, ReactNode, SubmitEvent } from 'react';
import { useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';
import { Button, type ButtonVariant } from '@/components/ui/button';

interface FormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel?: string;
  cancelLabel?: string;
  /**
   * The submit button's look. `destructive` is what a confirmation of something irreversible
   * wants — Cancel Experiment, say — where the default blue would read as the safe choice.
   */
  submitVariant?: ButtonVariant;
  submitDisabled?: boolean;
  /**
   * The mirror of the submitting phase, for a dialog that has to load something before its form
   * is usable — Add Notebook waits on `/notebooks/next-number` for the name it seeds. The body
   * goes inert under a spinner and Save is disabled, but Cancel, Escape and the backdrop stay
   * live: nothing has been typed yet, so there is nothing to protect.
   */
  initializing?: boolean;
  /**
   * Resolves once the work is done — the dialog is then the caller's to close (so it can
   * navigate first). A rejection leaves the dialog open with its fields intact; the error
   * itself has already been reported by `apiFetch`.
   */
  onSubmit: () => Promise<void>;
  children: ReactNode;
}

/**
 * A modal wrapping a form, owning the submitting state so no caller has to repeat it.
 * Port of indigo-frontend's FormDialogComponent, minus Formly.
 */
function FormDialog({
  open,
  onOpenChange,
  title,
  submitLabel = 'Save',
  cancelLabel = 'Cancel',
  submitVariant,
  submitDisabled,
  initializing,
  onSubmit,
  children,
}: FormDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submit() {
    if (isSubmitting || submitDisabled || initializing) return;
    setIsSubmitting(true);
    try {
      await onSubmit();
    } catch {
      // Stay open so the user can retry or correct the input.
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    void submit();
  }

  /**
   * Ctrl/Cmd+Enter presses the default button from anywhere in the dialog — the usual
   * shortcut, and the only way to submit from a rich-text field, where a plain Enter
   * inserts a paragraph instead.
   */
  function handleKeyDown(event: KeyboardEvent) {
    if (event.key !== 'Enter' || !(event.ctrlKey || event.metaKey)) return;
    // Something more specific already claimed the chord (leaving a code block, say).
    if (event.defaultPrevented) return;
    event.preventDefault();
    void submit();
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(nextOpen) => {
        // A half-finished submit should not be abandoned by an Escape or backdrop click.
        if (!isSubmitting) onOpenChange(nextOpen);
      }}
    >
      <DialogContent
        title={title}
        // The form wraps the footer too, so Enter in a field submits and the Save button
        // can stay a plain submit button.
        render={<form onSubmit={handleSubmit} onKeyDown={handleKeyDown} noValidate />}
        footer={
          <>
            <DialogClose
              render={
                <Button type="button" variant="secondary" size="lg" disabled={isSubmitting}>
                  {cancelLabel}
                </Button>
              }
            />
            <Button
              type="submit"
              variant={submitVariant}
              size="lg"
              loading={isSubmitting}
              disabled={submitDisabled || initializing}
            >
              {submitLabel}
            </Button>
          </>
        }
      >
        {/*
          `w-full` overrides the `w-fit` that `spinner="center"` hugs its content with — twMerge
          takes the last word. The inner `gap-4` is the one `DialogContent`'s scroll area would
          apply itself if the fields were still its direct children.
        */}
        <SavingOverlay
          pending={initializing ?? false}
          spinner="center"
          showDelayMs={0}
          label="Loading…"
          className="w-full"
        >
          <div className="flex flex-col gap-4">{children}</div>
        </SavingOverlay>
      </DialogContent>
    </Dialog>
  );
}

export { FormDialog };
