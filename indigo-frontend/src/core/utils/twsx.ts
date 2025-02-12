import clsx, { ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export const twsx = (...classvalues: ClassValue[]): string =>
  twMerge(clsx(classvalues));
