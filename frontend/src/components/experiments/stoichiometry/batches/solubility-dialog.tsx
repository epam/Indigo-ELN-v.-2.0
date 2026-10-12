import { useForm } from '@tanstack/react-form';

import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import { FormDialog } from '@/components/common/form-dialog';
import {
  areSolubilityRowsValid,
  EMPTY_SOLUBILITY_ROW,
  isSameValue,
  rowLabel,
  toSolubilityInSolvents,
  toSolubilityRows,
} from '@/components/experiments/stoichiometry/batches/composite-forms';
import { CompositeRows } from '@/components/experiments/stoichiometry/batches/composite-rows';
import { Input } from '@/components/ui/input';
import { OperatorNumberInput } from '@/components/ui/operator-number-input';
import { Select } from '@/components/ui/select';
import {
  COMPARISON_OPERATORS,
  OPERATOR_SYMBOLS,
  QUALITATIVE_LABELS,
  SOLUBIDITY_QUALITATIVE_TYPES,
  SOLUBIDITY_TYPE_LABELS,
  SOLUBIDITY_TYPES,
} from '@/lib/types/reactions.ts';

import type { SolubidityInSolvent, SolubidityQualitativeType, SolubidityType } from '@/lib/types/reactions.ts';

const HEADERS = ['Solvent Name', 'Solubility Type', 'Solubility Value, g/ml', 'Comment'];

/**
 * How the batch dissolves, a row per solvent — as a number with a comparison, or as a verdict.
 * The type decides which of the two the value column offers. A row nobody filled in is not sent,
 * and one with no solvent, the only member the backend requires, holds Save back.
 */
function SolubilityDialog({
  open,
  onOpenChange,
  value,
  onSave,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  value: SolubidityInSolvent[];
  /** Resolves to whether the save landed; the dialog stays open, values intact, when it did not. */
  onSave: (next: SolubidityInSolvent[]) => Promise<boolean>;
}) {
  const initialValues = { rows: toSolubilityRows(value) };

  const form = useForm({
    defaultValues: initialValues,
    onSubmit: async ({ value: values }) => {
      const next = toSolubilityInSolvents(values.rows);
      // Whoever reported the failure has already toasted it; the throw only keeps the dialog open.
      if (!isSameValue(next, toSolubilityInSolvents(initialValues.rows)) && !(await onSave(next))) {
        throw new Error('Not saved');
      }
      onOpenChange(false);
      form.reset({ rows: toSolubilityRows(next) });
    },
  });

  return (
    <form.Subscribe selector={(state) => !areSolubilityRowsValid(state.values.rows)}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            if (!nextOpen) form.reset(initialValues);
            onOpenChange(nextOpen);
          }}
          title="Solubility in Solvents"
          className="w-[800px]"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
        >
          <form.Field name="rows" mode="array">
            {(rows) => (
              <CompositeRows
                headers={HEADERS}
                className="grid-cols-[repeat(4,minmax(0,1fr))_auto]"
                rowCount={rows.state.value.length}
                onRemove={(index) => rows.removeValue(index)}
                addLabel="Add Solubility"
                onAdd={() => rows.pushValue(EMPTY_SOLUBILITY_ROW)}
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
                    <form.Field name={`rows[${index}].type`}>
                      {(field) => (
                        <Select<SolubidityType>
                          aria-label={rowLabel(HEADERS[1], index)}
                          items={[...SOLUBIDITY_TYPES]}
                          itemToKey={(item) => item}
                          itemToLabel={(item) => SOLUBIDITY_TYPE_LABELS[item]}
                          value={field.state.value}
                          // No blank row in the list, so there is always a type to switch to.
                          onValueChange={(next) => next && field.handleChange(next)}
                        />
                      )}
                    </form.Field>
                    {/* The array field re-renders on a change of length only, so the type is watched here. */}
                    <form.Subscribe selector={(state) => state.values.rows[index]?.type}>
                      {(type) =>
                        type === 'QUANTITATIVE' ? (
                          <form.Field name={`rows[${index}].operator`}>
                            {(operator) => (
                              <form.Field name={`rows[${index}].value`}>
                                {(field) => (
                                  <OperatorNumberInput
                                    label={rowLabel(HEADERS[2], index)}
                                    aria-label={rowLabel(HEADERS[2], index)}
                                    operators={COMPARISON_OPERATORS}
                                    operatorLabels={OPERATOR_SYMBOLS}
                                    operator={operator.state.value}
                                    onOperatorChange={operator.handleChange}
                                    value={field.state.value}
                                    placeholder="0"
                                    onChange={(event) => field.handleChange(event.target.value)}
                                  />
                                )}
                              </form.Field>
                            )}
                          </form.Field>
                        ) : (
                          <form.Field name={`rows[${index}].qualitativeType`}>
                            {(field) => (
                              <Select<SolubidityQualitativeType>
                                aria-label={rowLabel(HEADERS[2], index)}
                                items={[...SOLUBIDITY_QUALITATIVE_TYPES]}
                                itemToKey={(item) => item}
                                itemToLabel={(item) => QUALITATIVE_LABELS[item]}
                                value={field.state.value}
                                placeholder="Qualitative"
                                onValueChange={field.handleChange}
                              />
                            )}
                          </form.Field>
                        )
                      }
                    </form.Subscribe>
                    <form.Field name={`rows[${index}].comment`}>
                      {(field) => (
                        <Input
                          aria-label={rowLabel(HEADERS[3], index)}
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

export { SolubilityDialog };
