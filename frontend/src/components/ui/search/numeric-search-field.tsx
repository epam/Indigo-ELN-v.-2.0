import type { ChangeEvent } from 'react';
import { useState } from 'react';

import { OperatorNumberInput } from '@/components/ui/operator-number-input';
import type { NumericSearch, NumericSearchOperator } from '@/lib/types/search.ts';
import { NUMERIC_SEARCH_OPERATOR_LABELS, NUMERIC_SEARCH_OPERATORS } from '@/lib/types/search.ts';

/**
 * An operator picker and a number, as one control: "≥ 90".
 *
 * A search with no number is no search at all, so the pair is emitted as `null` until the
 * box holds one — the same contract as indigo-frontend's NumericSearchComponent, which
 * emits null while `value == null`. The operator therefore has to survive locally in the
 * meantime: picked on an empty box it changes nothing upstream, but the button must still
 * show what was picked.
 */
function NumericSearchField({
  value,
  onValueChange,
  id,
  label,
  disabled,
}: {
  value: NumericSearch | null;
  onValueChange: (value: NumericSearch | null) => void;
  id: string;
  /** Names the operator button for screen readers, e.g. "Batch Yield, % operator". */
  label: string;
  /** Renders what is set but accepts no interaction — what a PubChem catalog forces. */
  disabled?: boolean;
}) {
  const [pendingOperator, setPendingOperator] = useState<NumericSearchOperator>('eq');
  // The committed value wins whenever there is one; otherwise the button shows the choice
  // waiting for a number.
  const operator = value?.type ?? pendingOperator;

  function handleOperatorChange(next: NumericSearchOperator) {
    setPendingOperator(next);
    if (value) onValueChange({ ...value, type: next });
  }

  function handleNumberChange(event: ChangeEvent<HTMLInputElement>) {
    // valueAsNumber is NaN for '' and for a half-typed '-' or '1e', all of which mean
    // "no filter yet" rather than a number to send.
    const parsed = event.target.valueAsNumber;
    onValueChange(Number.isNaN(parsed) ? null : { type: operator, value: parsed });
  }

  return (
    <OperatorNumberInput
      id={id}
      label={label}
      operators={NUMERIC_SEARCH_OPERATORS}
      operatorLabels={NUMERIC_SEARCH_OPERATOR_LABELS}
      operator={operator}
      onOperatorChange={handleOperatorChange}
      value={value?.value ?? ''}
      disabled={disabled}
      onChange={handleNumberChange}
    />
  );
}

export { NumericSearchField };
