import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

import type { DateString } from '@/lib/types/common.ts';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const DATE_FORMAT = new Intl.DateTimeFormat('en-GB', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
});

export function formatDate(iso: DateString): string {
  return DATE_FORMAT.format(new Date(iso));
}

/**
 * The same date with the time of day. Used where minutes are what distinguish two entries —
 * the revision log, where a single edit session can hold a dozen revisions inside one hour.
 */
const DATE_TIME_FORMAT = new Intl.DateTimeFormat('en-GB', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

export function formatDateTime(iso: DateString): string {
  return DATE_TIME_FORMAT.format(new Date(iso));
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

/**
 * An anchor for a row or sample the client is about to create: 48 random bits as 8 base64url
 * characters, the same form `Anchor.generate()` produces on the backend. An anchor only has to
 * be unique within its experiment, and the backend answers a clash with a 400.
 */
export function newAnchor(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(6));
  return btoa(String.fromCharCode(...bytes))
    .replaceAll('+', '-')
    .replaceAll('/', '_');
}
