import {
  animate,
  group,
  query,
  style,
  transition,
  trigger,
} from '@angular/animations';

export enum RouteAnimationType {
  Fade = 'fade',
  SlideLeft = 'slideLeft',
  SlideRight = 'slideRight',
  SlideUp = 'slideUp',
  SlideDown = 'slideDown',
}

export const fadeAnimation = trigger('fadeAnimation', [
  transition('* <=> *', [
    group([
      query(
        ':enter',
        [
          style({ opacity: 0, position: 'absolute', width: '100%' }),
          animate('300ms ease-out', style({ opacity: 1 })),
        ],
        { optional: true },
      ),
      query(
        ':leave',
        [
          style({ opacity: 1, position: 'absolute', width: '100%' }),
          animate('300ms ease-out', style({ opacity: 0 })),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);

export const slideLeftAnimation = trigger('slideLeftAnimation', [
  transition('* <=> *', [
    group([
      query(
        ':enter',
        [
          style({
            transform: 'translateX(100%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateX(0%)' })),
        ],
        { optional: true },
      ),
      query(
        ':leave',
        [
          style({
            transform: 'translateX(0%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateX(-100%)' })),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);

export const slideRightAnimation = trigger('slideRightAnimation', [
  transition('* <=> *', [
    group([
      query(
        ':enter',
        [
          style({
            transform: 'translateX(-100%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateX(0%)' })),
        ],
        { optional: true },
      ),
      query(
        ':leave',
        [
          style({
            transform: 'translateX(0%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateX(100%)' })),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);

export const slideUpAnimation = trigger('slideUpAnimation', [
  transition('* <=> *', [
    group([
      query(
        ':enter',
        [
          style({
            transform: 'translateY(100%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateY(0%)' })),
        ],
        { optional: true },
      ),
      query(
        ':leave',
        [
          style({
            transform: 'translateY(0%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateY(-100%)' })),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);

export const slideDownAnimation = trigger('slideDownAnimation', [
  transition('* <=> *', [
    group([
      query(
        ':enter',
        [
          style({
            transform: 'translateY(-100%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateY(0%)' })),
        ],
        { optional: true },
      ),
      query(
        ':leave',
        [
          style({
            transform: 'translateY(0%)',
            position: 'absolute',
            width: '100%',
            height: '100%',
          }),
          animate('300ms ease-out', style({ transform: 'translateY(100%)' })),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);
