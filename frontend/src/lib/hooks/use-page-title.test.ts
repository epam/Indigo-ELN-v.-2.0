import { renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { usePageTitle } from './use-page-title';

describe('usePageTitle', () => {
  it('joins the parts after the app name', () => {
    renderHook(() => usePageTitle('Project Foo', 'Notebooks'));

    expect(document.title).toBe('Indigo ELN - Project Foo - Notebooks');
  });

  it('drops falsy parts, leaving the bare app name while nothing has loaded', () => {
    renderHook(() => usePageTitle(undefined, false));

    expect(document.title).toBe('Indigo ELN');
  });

  it('follows the parts as they change', () => {
    const { rerender } = renderHook(({ name }: { name?: string }) => usePageTitle(name && `Experiment ${name}`), {
      initialProps: {},
    });
    expect(document.title).toBe('Indigo ELN');

    rerender({ name: '00000001-0002' });

    expect(document.title).toBe('Indigo ELN - Experiment 00000001-0002');
  });
});
