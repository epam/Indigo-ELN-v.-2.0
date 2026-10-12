import { useForm } from '@tanstack/react-form';

import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import { FormDialog } from '@/components/common/form-dialog';
import {
  areResidualSolventRowsValid,
  EMPTY_RESIDUAL_SOLVENT_ROW,
  isSameValue,
  rowLabel,
  toResidualSolventRows,
  toResidualSolvents,
} from '@/components/experiments/stoichiometry/batches/composite-forms';
import { CompositeRows } from '@/components/experiments/stoichiometry/batches/composite-rows';
import { Input } from '@/components/ui/input';

import type { ResidualSolvent } from '@/lib/types/reactions.ts';

const HEADERS = ['Solvent Name', '# EQ. of Solvent', 'Comment'];

/**
 * The solvents left in the batch, a row each. A row nobody filled in is not sent, and one missing
 * its solvent or its EQ — both required by the backend — holds Save back.
 */
function ResidualSolventsDialog({
  open,
  onOpenChange,
  value,
  onSave,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  value: ResidualSolvent[];
  /** Resolves to whether the save landed; the dialog stays open, values intact, when it did not. */
  onSave: (next: ResidualSolvent[]) => Promise<boolean>;
}) {
  const initialValues = { rows: toResidualSolventRows(value) };

  const form = useForm({
    defaultValues: initialValues,
    onSubmit: async ({ value: values }) => {
      const next = toResidualSolvents(values.rows);
      // Whoever reported the failure has already toasted it; the throw only keeps the dialog open.
      if (!isSameValue(next, toResidualSolvents(initialValues.rows)) && !(await onSave(next))) {
        throw new Error('Not saved');
      }
      onOpenChange(false);
      form.reset({ rows: toResidualSolventRows(next) });
    },
  });

  return (
    <form.Subscribe selector={(state) => !areResidualSolventRowsValid(state.values.rows)}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            if (!nextOpen) form.reset(initialValues);
            onOpenChange(nextOpen);
          }}
          title="Residual Solvents"
          className="w-[720px]"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field name="rows" mode="array">
            {(rows) => (
              <CompositeRows
                headers={HEADERS}
                className="grid-cols-[repeat(3,minmax(0,1fr))_auto]"
                rowCount={rows.state.value.length}
                onRemove={(index) => rows.removeValue(index)}
                addLabel="Add Solvent"
                onAdd={() => rows.pushValue(EMPTY_RESIDUAL_SOLVENT_ROW)}
                renderRow={(index) => (
                  <>
                    <form.Field name={`rows[${index}].solvent`}>
                      {(field) => (
                        <DictionaryCombobox
                          aria-label={rowLabel(HEADERS[0], index)}
                          dictionary="SOLVENT"
                          value={field.state.value}
                          onValueChange={field.handleChange}
                          placeholder="Select"
                          clearable={false}
                        />
                      )}
                    </form.Field>
                    <form.Field name={`rows[${index}].eq`}>
                      {(field) => (
                        <Input
                          aria-label={rowLabel(HEADERS[1], index)}
                          type="number"
                          step="any"
                          value={field.state.value}
                          placeholder="0"
                          onBlur={field.handleBlur}
                          onChange={(event) => field.handleChange(event.target.value)}
                        />
                      )}
                    </form.Field>
                    <form.Field name={`rows[${index}].comment`}>
                      {(field) => (
                        <Input
                          aria-label={rowLabel(HEADERS[2], index)}
                          value={field.state.value}
                          placeholder="Text"
                          onBlur={field.handleBlur}
                          onChange={(event) => field.handleChange(event.target.value)}
                        />
                      )}
                    </form.Field>
                  </>
                )}
              />
            )}
          </form.Field>
        </FormDialog>
      )}
    </form.Subscribe>
  );
}

export { ResidualSolventsDialog };
