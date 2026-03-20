import { EnteredValue } from '@core/types/entities/values.i';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';

export function determineCellClasses(value?: EnteredValue<unknown>, experiment?: ExperimentDetail): string[] {
  const classes = [];
  if (value != null) {
    if (typeof value.source === 'number') {
      if (value.source > 0) {
        classes.push('value-state-set-manually');
      } else if (value.overwritten) {
        classes.push('animate-[flash-red_500ms_ease-in-out]');
        classes.push('test-animal-red');
      } else if (value.source === -experiment?.revision) {
        // classes.push('value-state-last-updated');
        classes.push('animate-[flash-green_500ms_ease-in-out]');
        classes.push('test-animal-green');
      }
    } else if (value.source === 'default') {
      classes.push('value-state-default');
    } else if (value.source === 'fixed') {
      classes.push('value-state-fixed');
    }
  }
  return classes;
}
