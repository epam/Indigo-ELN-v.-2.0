import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { useDebouncedDraft } from './use-debounced-draft';

const DELAY = 300;

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

function setup(initial = '') {
  const onCommit = vi.fn();
  const view = renderHook(({ value }: { value: string }) => useDebouncedDraft(value, onCommit, DELAY), {
    initialProps: { value: initial },
  });
  return { onCommit, view };
}

describe('useDebouncedDraft', () => {
  it('commits the draft once the user stops typing', () => {
    const { onCommit, view } = setup();

    act(() => view.result.current[1]('ab'));
    expect(view.result.current[0]).toBe('ab');
    expect(onCommit).not.toHaveBeenCalled();

    act(() => void vi.advanceTimersByTime(DELAY));
    expect(onCommit).toHaveBeenCalledExactlyOnceWith('ab');
  });

  it('coalesces successive keystrokes into a single commit', () => {
    const { onCommit, view } = setup();

    act(() => view.result.current[1]('a'));
    act(() => void vi.advanceTimersByTime(DELAY - 1));
    act(() => view.result.current[1]('ab'));
    act(() => void vi.advanceTimersByTime(DELAY));

    expect(onCommit).toHaveBeenCalledExactlyOnceWith('ab');
  });

  it('does not re-commit when the committed value echoes back as the prop', () => {
    const { onCommit, view } = setup();

    act(() => view.result.current[1]('ab'));
    act(() => void vi.advanceTimersByTime(DELAY));
    view.rerender({ value: 'ab' });
    act(() => void vi.advanceTimersByTime(DELAY));

    expect(onCommit).toHaveBeenCalledTimes(1);
  });

  it('lets an external change win over a pending draft', () => {
    const { onCommit, view } = setup('old');

    act(() => view.result.current[1]('typed'));
    view.rerender({ value: 'external' });

    expect(view.result.current[0]).toBe('external');

    act(() => void vi.advanceTimersByTime(DELAY));
    expect(onCommit).not.toHaveBeenCalled();
  });
});
