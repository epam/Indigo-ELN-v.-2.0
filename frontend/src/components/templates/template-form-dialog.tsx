import { useForm } from '@tanstack/react-form';
import { Plus, Trash2 } from 'lucide-react';
import { useEffect } from 'react';

import { FormDialog } from '@/components/common/form-dialog';
import {
  EMPTY_TEMPLATE_TAB,
  startingTemplateForm,
  templateFormErrors,
  toggleComponent,
  toTemplateRequest,
} from '@/components/templates/template-form';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { useCreateTemplate } from '@/lib/api/templates';

import type { TemplateDetails, TemplateTab } from '@/lib/types/templates.ts';
import {
  COMPONENT_LABELS,
  STOICHIOMETRY_PART_LABELS,
  STOICHIOMETRY_PARTS,
  TEMPLATE_COMPONENT_TYPES,
  TEMPLATE_NAME_MAX_LENGTH,
} from '@/lib/types/templates.ts';

/** The message for a field, once it has been changed or Save has been tried. */
function shownError(meta: { isTouched: boolean; errors: unknown[] }, tried: boolean): string | undefined {
  const [error] = meta.errors;
  return (meta.isTouched || tried) && typeof error === 'string' ? error : undefined;
}

/**
 * Add Template: a name, and one or more tabs, each a name and the components it shows.
 *
 * **It opens as a copy of `starter`** — the seeded Default template — so a name and Save are
 * enough, as issue #739's steps expect; the tabs are there to change, not to build from nothing.
 * `initializing` holds the form under a spinner while that copy is still on its way.
 *
 * **The structure is final once saved.** `PATCH /templates/{id}` takes the name and nothing else,
 * so the tabs set here are the ones every experiment built on the template gets.
 *
 * Validation is one form-level validator rather than one per field: the name is checked against
 * `existingNames`, the list the page already holds, and the tabs come and go. It returns its
 * messages keyed by field (`templateFormErrors`), so each still lands on its own field, and Save
 * follows `canSubmit` as in `ProjectFormDialog`. The backend's unique constraint answers too,
 * through the usual toast, should two people race for a name.
 */
export function TemplateFormDialog({
  open,
  onOpenChange,
  existingNames,
  starter,
  initializing,
  onCreated,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  existingNames: readonly string[];
  starter: readonly TemplateTab[] | undefined;
  initializing?: boolean;
  onCreated: (template: TemplateDetails) => void;
}) {
  const createTemplate = useCreateTemplate();

  const form = useForm({
    defaultValues: startingTemplateForm(starter),
    validators: {
      onChange: ({ value }) => {
        const fields = templateFormErrors(value, existingNames);
        return Object.keys(fields).length > 0 ? { fields } : undefined;
      },
    },
    onSubmit: async ({ value }) => {
      const created = await createTemplate.mutateAsync(toTemplateRequest(value));
      onOpenChange(false);
      onCreated(created);
    },
  });

  // Every opening starts from a fresh copy of the starter — and the copy arrives with the starter,
  // which may land after the dialog has opened. The dialog stays mounted between openings.
  useEffect(() => {
    if (open) form.reset(startingTemplateForm(starter));
  }, [open, starter, form]);

  return (
    <form.Subscribe
      selector={(state) => ({
        submitDisabled: !state.canSubmit || state.isValidating,
        tried: state.submissionAttempts > 0,
      })}
    >
      {({ submitDisabled, tried }) => (
        <FormDialog
          open={open}
          onOpenChange={onOpenChange}
          title="Add Template"
          className="w-[640px] max-w-[calc(100vw-2rem)]"
          submitDisabled={submitDisabled}
          initializing={initializing}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field name="name">
            {(field) => (
              <Field id="template-name" label="Template Name" required error={shownError(field.state.meta, tried)}>
                <Input
                  id="template-name"
                  name={field.name}
                  value={field.state.value}
                  placeholder="Template Name"
                  maxLength={TEMPLATE_NAME_MAX_LENGTH}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                />
              </Field>
            )}
          </form.Field>

          <form.Field name="tabs" mode="array">
            {(tabsField) => (
              <section aria-label="Tabs" className="flex flex-col gap-3">
                <p className="text-[14px]/6 text-neutral-800">
                  Starts as a copy of {starter ? 'the Default template' : 'an empty tab'}. Tabs and their components
                  cannot be changed once the template is saved.
                </p>
                {tabsField.state.value.map((_, index) => {
                  const label = `Tab ${index + 1}`;
                  const id = `template-tab-${index}`;
                  return (
                    // By position: tabs may share a name, and an added tab has none yet.
                    <fieldset key={index} className="flex flex-col gap-3 rounded-6 border border-neutral-300 p-4">
                      <legend className="sr-only">{label}</legend>
                      <div className="flex items-start gap-2">
                        <form.Field name={`tabs[${index}].name`}>
                          {(field) => (
                            <div className="min-w-0 flex-1">
                              <Field
                                id={`${id}-name`}
                                label={`${label} Name`}
                                required
                                error={shownError(field.state.meta, tried)}
                              >
                                <Input
                                  id={`${id}-name`}
                                  value={field.state.value}
                                  placeholder="e.g. Reaction"
                                  onBlur={field.handleBlur}
                                  onChange={(event) => field.handleChange(event.target.value)}
                                />
                              </Field>
                            </div>
                          )}
                        </form.Field>
                        {tabsField.state.value.length > 1 && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            aria-label={`Remove ${label}`}
                            title="Remove tab"
                            onClick={() => tabsField.removeValue(index)}
                            className="mt-7 text-neutral-700"
                          >
                            <Trash2 />
                          </Button>
                        )}
                      </div>

                      <form.Field name={`tabs[${index}].components`}>
                        {(field) => {
                          const error = shownError(field.state.meta, tried);
                          const stoichiometryOn = field.state.value.includes('stoichiometryTable');
                          return (
                            <div role="group" aria-label={`${label} components`} className="flex flex-col gap-2">
                              <span className="text-[14px]/6 font-medium text-neutral-800">Components</span>
                              {TEMPLATE_COMPONENT_TYPES.map((type) => (
                                <div key={type} className="flex flex-col gap-2">
                                  <label className="flex w-fit cursor-pointer items-center gap-2 text-[14px]/6 text-neutral-1000">
                                    <Checkbox
                                      checked={field.state.value.includes(type)}
                                      onCheckedChange={(checked) =>
                                        field.handleChange(toggleComponent(field.state.value, type, checked))
                                      }
                                    />
                                    {COMPONENT_LABELS[type]}
                                  </label>
                                  {type === 'stoichiometryTable' && stoichiometryOn && (
                                    // The blocks only mean something while the component is on; their
                                    // values are kept while it is off, so ticking it again restores them.
                                    <form.Field name={`tabs[${index}].stoichiometry`}>
                                      {(parts) => (
                                        <div className="ml-6 flex flex-col gap-2">
                                          {STOICHIOMETRY_PARTS.map((part) => (
                                            <label
                                              key={part}
                                              className="flex w-fit cursor-pointer items-center gap-2 text-[14px]/6 text-neutral-800"
                                            >
                                              <Checkbox
                                                checked={parts.state.value[part]}
                                                onCheckedChange={(checked) =>
                                                  parts.handleChange({ ...parts.state.value, [part]: checked })
                                                }
                                              />
                                              {STOICHIOMETRY_PART_LABELS[part]}
                                            </label>
                                          ))}
                                        </div>
                                      )}
                                    </form.Field>
                                  )}
                                </div>
                              ))}
                              {error && (
                                <p role="alert" className="text-[12px]/5 text-destructive">
                                  {error}
                                </p>
                              )}
                            </div>
                          );
                        }}
                      </form.Field>
                    </fieldset>
                  );
                })}
                <Button
                  type="button"
                  variant="outline"
                  className="w-fit"
                  onClick={() =>
                    tabsField.pushValue({ ...EMPTY_TEMPLATE_TAB, name: `Tab ${tabsField.state.value.length + 1}` })
                  }
                >
                  <Plus />
                  Add Tab
                </Button>
              </section>
            )}
          </form.Field>
        </FormDialog>
      )}
    </form.Subscribe>
  );
}
