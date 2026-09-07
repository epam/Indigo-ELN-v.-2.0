import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { collectionQueryString, getNextPageParam, useSettledSearch } from '@/lib/api/collections';

import type { Page } from '@/lib/types/common.ts';

describe('collectionQueryString', () => {
  it('omits search and createdByMe when unset', () => {
    expect(collectionQueryString({ search: '', sort: 'EARLIEST', createdByMe: false }, 0, 10)).toBe(
      'sort=EARLIEST&pageNo=0&pageSize=10',
    );
  });

  it('includes both once set', () => {
    expect(collectionQueryString({ search: 'acid test', sort: 'LATEST', createdByMe: true }, 2, 10)).toBe(
      'sort=LATEST&pageNo=2&pageSize=10&search=acid+test&createdByMe=true',
    );
  });

  it('carries the page size it is given, so the two lists stay independent', () => {
    expect(collectionQueryString({ search: '', sort: 'LATEST', createdByMe: false }, 0, 25)).toContain('pageSize=25');
  });
});

describe('getNextPageParam', () => {
  function page(pageNo: number, totalPages: number): Page<unknown> {
    return { pageNo, pageSize: 10, totalItems: totalPages * 10, totalPages, items: [] };
  }

  it('returns the next page number while pages remain', () => {
    expect(getNextPageParam(page(0, 3))).toBe(1);
  });

  it('returns undefined on the last page', () => {
    expect(getNextPageParam(page(2, 3))).toBeUndefined();
  });

  it('returns undefined when there are no results at all', () => {
    expect(getNextPageParam(page(0, 0))).toBeUndefined();
  });
});

describe('useSettledSearch', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  function setup(initial: string) {
    return renderHook(({ search }: { search: string }) => useSettledSearch(search), {
      initialProps: { search: initial },
    });
  }

  it('holds a typed term back until it stops changing', () => {
    const view = setup('');

    view.rerender({ search: 'k' });
    expect(view.result.current).toBe(false);

    act(() => void vi.advanceTimersByTime(300));
    expect(view.result.current).toBe(true);
  });

  it('lets a cleared term through at once, since clearing is a gesture rather than typing', () => {
    const view = setup('kin');

    view.rerender({ search: '' });
    expect(view.result.current).toBe(true);
  });
});
