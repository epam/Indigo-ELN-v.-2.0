import { type ClassNameValue, twMerge } from 'tailwind-merge';

export const twsx = (...classvalues: ClassNameValue[]): string =>
  twMerge(classvalues);
