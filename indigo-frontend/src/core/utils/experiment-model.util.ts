import { EnteredValue } from '@core/types/entities/values.i';

export function determineCellClasses(
  value: EnteredValue<unknown> | null,
  updatedNodes: Map<unknown, unknown>,
): string[] {
  const classes = [];
  if (value != null) {
    const hasAnyUpdates = updatedNodes.size !== 0;
    const previous = updatedNodes.get(value) as EnteredValue<unknown> | null;
    if (isUserEntered(value)) {
      classes.push('value-state-set-manually');
    } else if (hasAnyUpdates && previous != null && isUserEntered(previous) && !isUserEntered(value)) {
      // overwritten
      classes.push('animate-[flash-red_500ms_ease-in-out]');
      classes.push('test-animal-red');
      console.log('determineCellClasses: overwritten', previous, value);
    } else if (value.source === 'default') {
      classes.push('value-state-default');
    } else if (value.source === 'fixed') {
      classes.push('value-state-fixed');
    } else if (hasAnyUpdates && previous != null && value.source !== previous.source) {
      // recalculated
      classes.push('animate-[flash-green_500ms_ease-in-out]');
      classes.push('test-animal-green');
      console.log('determineCellClasses: recalculated', previous, value);
    }
  }
  return classes;
}

function isUserEntered(value: EnteredValue<unknown>): boolean {
  return typeof value.source === 'number' && value.source > 0;
}
