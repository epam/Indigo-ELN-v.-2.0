import {useForm} from '@tanstack/react-form';

import {FormDialog} from '@/components/common/form-dialog';
import {
  NOTEBOOK_NAME_LENGTH,
  notebookNameSchema,
  toNotebookEditRequest,
  toNotebookFormValues,
} from '@/components/notebooks/notebook-form';
import {Field} from '@/components/ui/field';
import {Input} from '@/components/ui/input';
import {RichTextEditor} from '@/components/ui/rich-text-editor';
import {checkNotebookNameExists, useEditNotebook} from '@/lib/api/notebooks';

import type {NotebookDetails} from '@/lib/types/notebooks.ts';

const NAME_CHECK_DEBOUNCE_MS = 300;

/**
 * The Edit Notebook modal. Unlike `ProjectFormDialog` this has no create mode: a new notebook
 * is made from the project page, which is a separate task.
 */
function NotebookFormDialog({
  open,
  onOpenChange,
  notebook,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  notebook: NotebookDetails;
}) {
  const editNotebook = useEditNotebook(notebook.id);
  const initialValues = toNotebookFormValues(notebook);

  const form = useForm({
    defaultValues: initialValues,
    onSubmit: async ({ value }) => {
      await editNotebook.mutateAsync(toNotebookEditRequest(value, initialValues));
      onOpenChange(false);
    },
  });

  return (
    // Save stays disabled while the name is invalid or its uniqueness check is still in
    // flight, so a click can never land on a form that is not ready to be submitted.
    <form.Subscribe selector={(state) => !state.canSubmit || state.isValidating}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            // Reopening starts from the notebook as stored, not the abandoned draft.
            if (!nextOpen) form.reset(initialValues);
            onOpenChange(nextOpen);
          }}
          title="Edit Notebook"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field
            name="name"
            validators={{
              onChange: ({ value }) => notebookNameSchema.safeParse(value).error?.issues[0]?.message,
              // The unique constraint has no friendly server-side message, so the duplicate
              // has to be caught here. Skipped while the sync rules already fail.
              onChangeAsyncDebounceMs: NAME_CHECK_DEBOUNCE_MS,
              onChangeAsync: async ({ value }) => {
                const name = value.trim();
                if (!notebookNameSchema.safeParse(value).success) return undefined;
                // The notebook's own name is not a duplicate of itself.
                if (name === notebook.name) return undefined;
                return (await checkNotebookNameExists(name)) ? `Notebook '${name}' already exists` : undefined;
              },
            }}
          >
            {(field) => (
              <Field
                id={field.name}
                label="Notebook Name"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                <Input
                  id={field.name}
                  name={field.name}
                  value={field.state.value}
                  placeholder="Notebook Name"
                  inputMode="numeric"
                  maxLength={NOTEBOOK_NAME_LENGTH}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                />
              </Field>
            )}
          </form.Field>

          <form.Field name="description">
            {(field) => (
              <Field id={field.name} label="Notebook Description">
                <RichTextEditor
                  id={field.name}
                  aria-labelledby={`${field.name}-label`}
                  value={field.state.value}
                  onChange={field.handleChange}
                  placeholder="Text"
                />
              </Field>
            )}
          </form.Field>
        </FormDialog>
      )}
    </form.Subscribe>
  );
}

export { NotebookFormDialog };
