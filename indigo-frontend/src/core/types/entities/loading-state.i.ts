export type LoadingState<T> =
  | { state: 'empty' }
  | { state: 'loading' }
  | { state: 'error' }
  | { state: 'ready'; value: T };
