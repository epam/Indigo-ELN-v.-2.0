import { useState } from 'react';

import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

interface FormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel?: string;
  cancelLabel?: string;
  submitDisabled?: boolean;
  /**
   * Resolves once the work is done — the dialog is then the caller's to close (so it can
   * navigate first). A rejection leaves the dialog open with its fields intact; the error
   * itself has already been reported by `apiFetch`.
   */
  onSubmit: () => Promise<void>;
  children: React.ReactNode;
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
  submitDisabled,
  onSubmit,
  children,
}: FormDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submit() {
    if (isSubmitting || submitDisabled) return;
    setIsSubmitting(true);
    try {
      await onSubmit();
    } catch {
      // Stay open so the user can retry or correct the input.
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    void submit();
  }

  /**
   * Ctrl/Cmd+Enter presses the default button from anywhere in the dialog — the usual
   * shortcut, and the only way to submit from a rich-text field, where a plain Enter
   * inserts a paragraph instead.
   */
  function handleKeyDown(event: React.KeyboardEvent) {
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
            <Button type="submit" size="lg" loading={isSubmitting} disabled={submitDisabled}>
              {submitLabel}
            </Button>
          </>
        }
      >
        {children}
      </DialogContent>
    </Dialog>
  );
}

export { FormDialog };
