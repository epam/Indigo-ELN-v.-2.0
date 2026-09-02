import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const DATE_FORMAT = new Intl.DateTimeFormat('en-GB', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
});

export function formatDate(iso: string): string {
  return DATE_FORMAT.format(new Date(iso));
}

/**
 * Ports indigo-frontend's `bytesConverting` pipe: the largest unit the value reaches, to one
 * decimal. Binary steps (1024), matching what the Angular UI has always shown for the same
 * attachment sizes.
 */
const BYTE_UNITS = [
  ['GB', 1024 ** 3],
  ['MB', 1024 ** 2],
  ['KB', 1024],
] as const;

export function formatBytes(bytes: number): string {
  for (const [unit, threshold] of BYTE_UNITS) {
    if (bytes >= threshold) return `${(bytes / threshold).toFixed(1)}${unit}`;
  }
  return `${bytes}B`;
}
