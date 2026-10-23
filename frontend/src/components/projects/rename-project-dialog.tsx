import { useForm } from '@tanstack/react-form';
import { useEffect } from 'react';

import { FormDialog } from '@/components/common/form-dialog';
import { projectNameValidators } from '@/components/projects/project-form';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { useEditProject } from '@/lib/api/projects';
import { PROJECT_NAME_MAX_LENGTH } from '@/lib/types/projects.ts';

import type { Project } from '@/lib/types/projects.ts';

/**
 * Rename, from the projects list: the name alone, under the same validators as Edit Project.
 *
 * The PATCH carries `name` and nothing else — `ProjectEditRequest` leaves every absent field
 * alone, so a rename never touches keywords or text someone else is editing. `useEditProject`
 * patches the cached detail and invalidates the list, so the card picks up the new name itself.
 */
function RenameProjectDialog({
  open,
  onOpenChange,
  project,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  project: Pick<Project, 'id' | 'name'>;
}) {
  const editProject = useEditProject(project.id);

  const form = useForm({
    defaultValues: { name: project.name },
    onSubmit: async ({ value }) => {
      const name = value.name.trim();
      if (name !== project.name) await editProject.mutateAsync({ name });
      onOpenChange(false);
    },
  });

  /*
    Every opening starts from the name as stored now — not the abandoned draft, and not the name
    this dialog last saved or was mounted with: it stays mounted between openings, and the list
    refetch, or someone else's rename, can change `project.name` under it. Resetting on open
    rather than on close covers all of them, the close that follows a save included.
  */
  useEffect(() => {
    if (open) form.reset({ name: project.name });
  }, [open, project.name, form]);

  return (
    // Save stays disabled while the name is invalid or its uniqueness check is in flight.
    <form.Subscribe selector={(state) => !state.canSubmit || state.isValidating}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={onOpenChange}
          title="Rename Project"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field name="name" validators={projectNameValidators(project.name)}>
            {(field) => (
              <Field
                id={`rename-${project.id}`}
                label="Project Name"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                <Input
                  id={`rename-${project.id}`}
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
        </FormDialog>
      )}
    </form.Subscribe>
  );
}

export { RenameProjectDialog };
