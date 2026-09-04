import { useForm } from '@tanstack/react-form';
import { useNavigate } from '@tanstack/react-router';
import { useEffect } from 'react';

import { FormDialog } from '@/components/common/form-dialog';
import {
  EMPTY_NOTEBOOK_FORM,
  NOTEBOOK_NAME_LENGTH,
  notebookNameSchema,
  toNotebookEditRequest,
  toNotebookFormValues,
  toNotebookRequest,
} from '@/components/notebooks/notebook-form';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import {
  checkNotebookNameExists,
  useCreateNotebook,
  useEditNotebook,
  useNextNotebookNumber,
} from '@/lib/api/notebooks';

import type { NotebookDetails } from '@/lib/types/notebooks.ts';

const NAME_CHECK_DEBOUNCE_MS = 300;

/**
 * The notebook modal, in both of its modes: `notebook` absent creates one under `projectId`,
 * `notebook` present edits it. The two share every field and validator; they differ in the seed
 * values, the title, which mutation runs, and whether saving navigates.
 *
 * Creating has a step the project dialog does not: the name is a number the backend hands out, so
 * the form cannot be filled in until `/notebooks/next-number` answers. That wait is `FormDialog`'s
 * initializing phase — the body inert under a spinner — which is also what makes the `form.reset`
 * below safe, since nothing can have been typed into a frozen form for it to discard.
 */
function NotebookFormDialog({
  open,
  onOpenChange,
  projectId,
  notebook,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** The parent project. Required in both modes; when editing it is `notebook.projectId`. */
  projectId: string;
  notebook?: NotebookDetails;
}) {
  const navigate = useNavigate();
  const isCreate = notebook === undefined;

  // All three hooks run unconditionally — `notebook` decides which result is used, never whether
  // the hook is called, or the order would change with the mode.
  const nextNumber = useNextNotebookNumber(open && isCreate);
  const createNotebook = useCreateNotebook(projectId);
  const editNotebook = useEditNotebook(notebook?.id ?? '');

  const initialValues = notebook ? toNotebookFormValues(notebook) : EMPTY_NOTEBOOK_FORM;
  // `isFetching` as well as `isPending`: reopening the dialog refetches a number that is already
  // cached, and the stale one must not be offered while its replacement is in flight.
  const initializing = isCreate && (nextNumber.isPending || nextNumber.isFetching);

  const form = useForm({
    defaultValues: initialValues,
    onSubmit: async ({ value }) => {
      if (notebook) {
        await editNotebook.mutateAsync(toNotebookEditRequest(value, initialValues));
        onOpenChange(false);
        return;
      }
      const created = await createNotebook.mutateAsync(toNotebookRequest(value));
      onOpenChange(false);
      form.reset(EMPTY_NOTEBOOK_FORM);
      await navigate({ to: '/notebooks/$id', params: { id: created.id } });
    },
  });

  /**
   * Seeds the name once the number arrives. It cannot be a `defaultValue`: the dialog renders
   * before the request resolves, and `useForm` reads its defaults only on the first render.
   */
  useEffect(() => {
    if (nextNumber.data) form.reset({ ...EMPTY_NOTEBOOK_FORM, name: nextNumber.data });
  }, [nextNumber.data, form]);

  return (
    // Save stays disabled while the name is invalid or its uniqueness check is still in
    // flight, so a click can never land on a form that is not ready to be submitted.
    <form.Subscribe selector={(state) => !state.canSubmit || state.isValidating}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            // Reopening starts from the seed values again, not the abandoned draft — the notebook
            // as stored when editing, and a blank form when creating, since the number this one
            // was offered will have been asked for again by then.
            if (!nextOpen) form.reset(initialValues);
            onOpenChange(nextOpen);
          }}
          title={isCreate ? 'Add Notebook' : 'Edit Notebook'}
          submitDisabled={submitDisabled}
          initializing={initializing}
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
                if (name === notebook?.name) return undefined;
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
