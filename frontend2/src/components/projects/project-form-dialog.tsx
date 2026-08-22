import { useForm } from '@tanstack/react-form';
import { useNavigate } from '@tanstack/react-router';

import { FormDialog } from '@/components/common/form-dialog';
import { KeywordCombobox } from '@/components/projects/keyword-combobox';
import {
  EMPTY_PROJECT_FORM,
  PROJECT_NAME_MAX_LENGTH,
  projectNameSchema,
  toProjectRequest,
} from '@/components/projects/project-form';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { checkProjectNameExists, useCreateProject } from '@/lib/api/projects';

const NAME_CHECK_DEBOUNCE_MS = 300;

/**
 * The Add Project modal. Named for the shape rather than the action so the edit flow can
 * reuse it later; only the create path exists today.
 */
function ProjectFormDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (open: boolean) => void }) {
  const navigate = useNavigate();
  const { mutateAsync } = useCreateProject();

  const form = useForm({
    defaultValues: EMPTY_PROJECT_FORM,
    onSubmit: async ({ value }) => {
      const project = await mutateAsync(toProjectRequest(value));
      onOpenChange(false);
      form.reset();
      await navigate({ to: '/projects/$id', params: { id: project.id } });
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
            // Reopening should be a blank slate, not the abandoned draft.
            if (!nextOpen) form.reset();
            onOpenChange(nextOpen);
          }}
          title="Add Project"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field
            name="name"
            validators={{
              onChange: ({ value }) => projectNameSchema.safeParse(value).error?.issues[0]?.message,
              // The unique constraint has no friendly server-side message, so the duplicate
              // has to be caught here. Skipped while the sync rules already fail.
              onChangeAsyncDebounceMs: NAME_CHECK_DEBOUNCE_MS,
              onChangeAsync: async ({ value }) => {
                const name = value.trim();
                if (!projectNameSchema.safeParse(value).success) return undefined;
                return (await checkProjectNameExists(name)) ? `Project with name '${name}' already exists` : undefined;
              },
            }}
          >
            {(field) => (
              <Field
                id={field.name}
                label="Project Name"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                <Input
                  id={field.name}
                  name={field.name}
                  value={field.state.value}
                  placeholder="Project Name"
                  maxLength={PROJECT_NAME_MAX_LENGTH}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                />
              </Field>
            )}
          </form.Field>

          <form.Field name="keywords">
            {(field) => (
              <Field id={field.name} label="Project Keywords">
                <KeywordCombobox id={field.name} value={field.state.value} onValueChange={field.handleChange} />
              </Field>
            )}
          </form.Field>

          <form.Field name="description">
            {(field) => (
              <Field id={field.name} label="Project Description">
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

          <form.Field name="literature">
            {(field) => (
              <Field id={field.name} label="Literature">
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

export { ProjectFormDialog };
