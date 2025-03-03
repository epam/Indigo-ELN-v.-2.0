import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

export const dropdownAnimation = trigger('dropdownAnimation', [
  state(
    'void',
    style({
      transform: 'translateY(-10px)',
      opacity: 0,
    }),
  ),
  state(
    '*',
    style({
      transform: 'translateY(0)',
      opacity: 1,
    }),
  ),
  transition('void => *', animate('200ms ease-out')),
  transition('* => void', animate('150ms ease-in')),
]);
