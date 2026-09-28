import { useState } from 'react';

import { FormDialog } from '@/components/common/form-dialog';
import { Field } from '@/components/ui/field';
import { Select } from '@/components/ui/select';
import { useSignatureTemplates } from '@/lib/api/experiments';
import type { SignatureTemplateRef } from '@/lib/types/experiments.ts';

/**
 * Picks the signature template a submission goes out under — the one thing
 * `POST /workflow/submit` and `/workflow/completeAndSubmit` need beyond the experiment id.
 *
 * Port of indigo-frontend's `SignDialogComponent`, minus Formly. The template list is
 * administered in the signature service and never here, so this is a read and a choice.
 *
 * It resolves the chosen id to the caller rather than posting anything itself: the same dialog
 * serves Submit and Complete and Sign, which are two different endpoints, and `FormDialog` already
 * gives the right behaviour around the caller's promise — the dialog stays open on a rejection
 * with the choice intact, and `apiFetch` has already toasted why.
 */
export function SignDialog({
  open,
  onOpenChange,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (signatureTemplateId: string) => Promise<void>;
}) {
  const [template, setTemplate] = useState<SignatureTemplateRef | null>(null);
  const { data: templates, isPending, isError } = useSignatureTemplates(open);

  return (
    <FormDialog
      open={open}
      onOpenChange={(next) => {
        // A fresh choice each time: the previous submission's template is not a sensible default
        // for the next one, and leaving it set would let Sign be pressed without a decision.
        if (!next) setTemplate(null);
        onOpenChange(next);
      }}
      title="Select Signature Template"
      submitLabel="Sign"
      // Required field, no `emptyLabel` on the Select — so a null here is genuinely "not chosen".
      submitDisabled={template === null}
      onSubmit={() => onSubmit(template!.id)}
    >
      <Field id="signature-template" label="Signature Template" required>
        <Select
          id="signature-template"
          value={template}
          onValueChange={setTemplate}
          items={templates ?? []}
          itemToKey={(item) => item.id}
          itemToLabel={(item) => item.name}
          placeholder="Select a signature template"
          loading={isPending}
          error={isError}
        />
      </Field>
    </FormDialog>
  );
}
