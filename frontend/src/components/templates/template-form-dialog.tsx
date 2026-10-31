import { useForm } from '@tanstack/react-form';
import { Plus, Trash2 } from 'lucide-react';

import { FormDialog } from '@/components/common/form-dialog';
import {
  EMPTY_TEMPLATE_FORM,
  EMPTY_TEMPLATE_TAB,
  isTemplateFormValid,
  tabComponentsError,
  tabNameError,
  templateNameError,
  toggleComponent,
  toTemplateRequest,
} from '@/components/templates/template-form';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { useCreateTemplate } from '@/lib/api/templates';

import type { TemplateTabValues } from '@/components/templates/template-form';
import type { StoichiometryPart, TemplateDetails } from '@/lib/types/templates.ts';
import {
  COMPONENT_LABELS,
  STOICHIOMETRY_PART_LABELS,
  TEMPLATE_COMPONENT_TYPES,
  TEMPLATE_NAME_MAX_LENGTH,
} from '@/lib/types/templates.ts';

const STOICHIOMETRY_PARTS = Object.keys(STOICHIOMETRY_PART_LABELS) as StoichiometryPart[];

/**
 * Add Template: a name, and one or more tabs, each a name and the components it shows.
 *
 * **The structure is final once saved.** `PATCH /templates/{id}` takes the name and nothing else,
 * so the tabs set here are the ones every experiment built on the template gets — which is why
 * the dialog checks everything it can before Save is even enabled.
 *
 * Errors are worked out from the values on every render by the pure checks in `template-form.ts`
 * rather than by per-field validators: a tab name's uniqueness depends on every other tab, which
 * per-field validation would only see when that field itself changed. A message shows once its
 * field has been left, or once Save has been tried.
 *
 * `existingNames` is the list the page already holds, so a duplicate is caught as it is typed;
 * the backend's unique constraint answers too, through the usual toast, should two people race.
 */
export function TemplateFormDialog({
  open,
  onOpenChange,
  existingNames,
  onCreated,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  existingNames: readonly string[];
  onCreated: (template: TemplateDetails) => void;
}) {
  const createTemplate = useCreateTemplate();

  const form = useForm({
    defaultValues: EMPTY_TEMPLATE_FORM,
    onSubmit: async ({ value }) => {
      if (!isTemplateFormValid(value, existingNames)) return;
      const created = await createTemplate.mutateAsync(toTemplateRequest(value));
      onOpenChange(false);
      form.reset();
      onCreated(created);
    },
  });

  return (
    <form.Subscribe selector={(state) => ({ values: state.values, tried: state.submissionAttempts > 0 })}>
      {({ values, tried }) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            // Reopening starts from a blank template, not the abandoned draft.
            if (!nextOpen) form.reset();
            onOpenChange(nextOpen);
          }}
          title="Add Template"
          className="w-[640px] max-w-[calc(100vw-2rem)]"
          submitDisabled={!isTemplateFormValid(values, existingNames)}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field name="name">
            {(field) => (
              <Field
                id="template-name"
                label="Template Name"
                required
                error={
                  field.state.meta.isTouched || tried ? templateNameError(field.state.value, existingNames) : undefined
                }
              >
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
                  Tabs and their components cannot be changed once the template is saved.
                </p>
                {tabsField.state.value.map((tab, index) => (
                  <TabEditor
                    key={index}
                    index={index}
                    tab={tab}
                    tabs={values.tabs}
                    tried={tried}
                    removable={values.tabs.length > 1}
                    onChange={(next) => tabsField.replaceValue(index, next)}
                    onRemove={() => tabsField.removeValue(index)}
                  />
                ))}
                <Button
                  type="button"
                  variant="outline"
                  className="w-fit"
                  onClick={() => tabsField.pushValue(EMPTY_TEMPLATE_TAB)}
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

/**
 * One tab of the template being built. Controlled by the dialog's array field rather than holding
 * fields of its own, so its errors can be computed against every tab at once — see the dialog.
 * The name's message shows once something is typed or Save is tried; the component check only
 * once Save is tried, since an untouched tab is not yet wrong.
 */
function TabEditor({
  index,
  tab,
  tabs,
  tried,
  removable,
  onChange,
  onRemove,
}: {
  index: number;
  tab: TemplateTabValues;
  tabs: readonly TemplateTabValues[];
  tried: boolean;
  removable: boolean;
  onChange: (tab: TemplateTabValues) => void;
  onRemove: () => void;
}) {
  const id = `template-tab-${index}`;
  const label = `Tab ${index + 1}`;
  const nameError = tried || tab.name !== '' ? tabNameError(tabs, index) : undefined;
  const componentsError = tried ? tabComponentsError(tab) : undefined;
  const stoichiometryOn = tab.components.includes('stoichiometryTable');

  return (
    <fieldset className="flex flex-col gap-3 rounded-6 border border-neutral-300 p-4">
      <legend className="sr-only">{label}</legend>
      <div className="flex items-start gap-2">
        <div className="min-w-0 flex-1">
          <Field id={`${id}-name`} label={`${label} Name`} required error={nameError}>
            <Input
              id={`${id}-name`}
              value={tab.name}
              placeholder="e.g. Reaction"
              onChange={(event) => onChange({ ...tab, name: event.target.value })}
            />
          </Field>
        </div>
        {removable && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label={`Remove ${label}`}
            title="Remove tab"
            onClick={onRemove}
            className="mt-7 text-neutral-700"
          >
            <Trash2 />
          </Button>
        )}
      </div>

      <div role="group" aria-label={`${label} components`} className="flex flex-col gap-2">
        <span className="text-[14px]/6 font-medium text-neutral-800">Components</span>
        {TEMPLATE_COMPONENT_TYPES.map((type) => (
          <div key={type} className="flex flex-col gap-2">
            <label className="flex w-fit cursor-pointer items-center gap-2 text-[14px]/6 text-neutral-1000">
              <Checkbox
                checked={tab.components.includes(type)}
                onCheckedChange={(checked) =>
                  onChange({ ...tab, components: toggleComponent(tab.components, type, checked) })
                }
              />
              {COMPONENT_LABELS[type]}
            </label>
            {type === 'stoichiometryTable' && stoichiometryOn && (
              // The blocks only mean something while the component is on; their values are kept
              // while it is off, so ticking it again restores the last choice.
              <div className="ml-6 flex flex-col gap-2">
                {STOICHIOMETRY_PARTS.map((part) => (
                  <label
                    key={part}
                    className="flex w-fit cursor-pointer items-center gap-2 text-[14px]/6 text-neutral-800"
                  >
                    <Checkbox
                      checked={tab.stoichiometry[part]}
                      onCheckedChange={(checked) =>
                        onChange({ ...tab, stoichiometry: { ...tab.stoichiometry, [part]: checked } })
                      }
                    />
                    {STOICHIOMETRY_PART_LABELS[part]}
                  </label>
                ))}
              </div>
            )}
          </div>
        ))}
        {componentsError && <p className="text-[12px]/5 text-destructive">{componentsError}</p>}
      </div>
    </fieldset>
  );
}
