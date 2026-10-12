import { useForm } from '@tanstack/react-form';

import { FormDialog } from '@/components/common/form-dialog';
import {
  isSameValue,
  toMeltingPoint,
  toMeltingPointForm,
} from '@/components/experiments/stoichiometry/batches/composite-forms';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';

import type { MeltingPoint } from '@/lib/types/reactions.ts';

/**
 * The batch's melting point: a range and a remark, every part of it optional. Saving it empty
 * clears the value.
 */
function MeltingPointDialog({
  open,
  onOpenChange,
  value,
  onSave,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  value: MeltingPoint | undefined;
  /** Resolves to whether the save landed; the dialog stays open, values intact, when it did not. */
  onSave: (next: MeltingPoint | null) => Promise<boolean>;
}) {
  const initialValues = toMeltingPointForm(value);

  const form = useForm({
    defaultValues: initialValues,
    onSubmit: async ({ value: values }) => {
      const next = toMeltingPoint(values);
      // Whoever reported the failure has already toasted it; the throw only keeps the dialog open.
      if (!isSameValue(next, toMeltingPoint(initialValues)) && !(await onSave(next))) throw new Error('Not saved');
      onOpenChange(false);
      form.reset(toMeltingPointForm(next ?? undefined));
    },
  });

  return (
    <FormDialog
      open={open}
      onOpenChange={(nextOpen) => {
        if (!nextOpen) form.reset(initialValues);
        onOpenChange(nextOpen);
      }}
      title="Melting Point"
      onSubmit={() => form.handleSubmit()}
    >
      <div className="grid grid-cols-2 gap-4">
        <form.Field name="lower">
          {(field) => (
            <Field id="melting-point-lower" label="Lower, °C">
              <Input
                id="melting-point-lower"
                type="number"
                step="any"
                value={field.state.value}
                placeholder="0"
                onBlur={field.handleBlur}
                onChange={(event) => field.handleChange(event.target.value)}
              />
            </Field>
          )}
        </form.Field>
        <form.Field name="upper">
          {(field) => (
            <Field id="melting-point-upper" label="Upper, °C">
              <Input
                id="melting-point-upper"
                type="number"
                step="any"
                value={field.state.value}
                placeholder="0"
                onBlur={field.handleBlur}
                onChange={(event) => field.handleChange(event.target.value)}
              />
            </Field>
          )}
        </form.Field>
      </div>
      <form.Field name="comments">
        {(field) => (
          <Field id="melting-point-comments" label="Comments">
            <Input
              id="melting-point-comments"
              value={field.state.value}
              placeholder="Text"
              onBlur={field.handleBlur}
              onChange={(event) => field.handleChange(event.target.value)}
            />
          </Field>
        )}
      </form.Field>
    </FormDialog>
  );
}

export { MeltingPointDialog };
