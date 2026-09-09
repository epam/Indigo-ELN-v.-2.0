import { useForm } from '@tanstack/react-form';
import { useNavigate } from '@tanstack/react-router';

import { FormDialog } from '@/components/common/form-dialog';
import { Field } from '@/components/ui/field';
import { Select } from '@/components/ui/select';
import { useCreateExperiment } from '@/lib/api/experiments';
import { useTemplates } from '@/lib/api/templates';

import type { Template } from '@/lib/types/templates.ts';

interface AddExperimentFormValues {
  template: Template | null;
}

const EMPTY_FORM: AddExperimentFormValues = { template: null };

const required = ({ value }: { value: Template | null }) => (value ? undefined : 'Select Template is required');

/**
 * The Add Experiment modal — create only. There is no edit counterpart: an experiment is edited
 * on its own page, where every field saves as it is left.
 *
 * One field, because everything else about a new experiment is either assigned by the server (the
 * name, `<notebook name>-0001`) or edited afterwards. The template is required and decides the
 * experiment's whole layout, so it cannot be chosen later.
 *
 * Loading the templates is `FormDialog`'s initializing phase, the same one Add Notebook waits out
 * for its number.
 */
function AddExperimentDialog({
  open,
  onOpenChange,
  notebookId,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  notebookId: string;
}) {
  const navigate = useNavigate();
  const templates = useTemplates(open);
  const createExperiment = useCreateExperiment(notebookId);

  const form = useForm({
    defaultValues: EMPTY_FORM,
    onSubmit: async ({ value }) => {
      // The validator below is what guarantees this; `canSubmit` gates the button on it.
      const created = await createExperiment.mutateAsync({ templateID: value.template!.id });
      onOpenChange(false);
      form.reset();
      await navigate({ to: '/experiments/$id', params: { id: created.id } });
    },
  });

  return (
    // Save stays disabled until a template is picked, so a click can never land on a form that
    // is not ready to be submitted.
    <form.Subscribe selector={(state) => !state.canSubmit || state.isValidating}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            // Reopening starts from nothing chosen, not the abandoned pick.
            if (!nextOpen) form.reset();
            onOpenChange(nextOpen);
          }}
          title="Add Experiment"
          submitDisabled={submitDisabled}
          // `isPending` alone, not `isFetching` too: a background refetch of a list already on
          // screen must not freeze a dialog someone is part-way through — see `useTemplates`.
          initializing={templates.isPending}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field
            name="template"
            /*
              `onMount` as well as `onChange`, which the other dialogs do not need. TanStack Form
              leaves `canSubmit` true until a validator has run, so without it Save would be
              clickable on a form whose only field is empty — the project and notebook dialogs get
              away with that because their name field is seeded with something valid.
            */
            validators={{ onMount: required, onChange: required }}
          >
            {(field) => (
              <Field
                id={field.name}
                label="Select Template"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                {/*
                  No `emptyLabel`: the field is required, so there must be no row that puts it
                  back to null. `placeholder` is what names the control until something is picked.

                  A failed load needs nothing beyond `error` — the popup then says why it is
                  empty, `apiFetch` has already toasted, and Save stays disabled because there is
                  nothing to choose.
                */}
                <Select<Template>
                  id={field.name}
                  value={field.state.value}
                  onValueChange={field.handleChange}
                  items={templates.data?.items ?? []}
                  itemToKey={(template) => template.id}
                  itemToLabel={(template) => template.name}
                  placeholder="Select Template"
                  loading={templates.isPending}
                  error={templates.isError}
                />
              </Field>
            )}
          </form.Field>
        </FormDialog>
      )}
    </form.Subscribe>
  );
}

export { AddExperimentDialog };
