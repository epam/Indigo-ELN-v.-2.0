import {describe, expect, it} from 'vitest';

import {formatBytes} from '@/lib/utils';

describe('formatBytes', () => {
  it('uses the largest unit the value reaches', () => {
    expect(formatBytes(512)).toBe('512B');
    expect(formatBytes(1024)).toBe('1.0KB');
    expect(formatBytes(16_384)).toBe('16.0KB');
    expect(formatBytes(1024 ** 2)).toBe('1.0MB');
    expect(formatBytes(3_400_000)).toBe('3.2MB');
    expect(formatBytes(1024 ** 3)).toBe('1.0GB');
  });

  /** Steps are binary, matching what indigo-frontend has always shown for the same file. */
  it('stays on the smaller unit just below a threshold', () => {
    expect(formatBytes(1023)).toBe('1023B');
    expect(formatBytes(1024 ** 2 - 1)).toBe('1024.0KB');
  });

  it('renders an empty file rather than dividing by a unit', () => {
    expect(formatBytes(0)).toBe('0B');
  });
});
