import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { PENDING_SHOW_DELAY_MS } from '@/lib/hooks/use-delayed-flag';

beforeEach(() => vi.useFakeTimers({ shouldAdvanceTime: false }));
afterEach(() => vi.useRealTimers());

function advance(ms: number) {
  act(() => void vi.advanceTimersByTime(ms));
}

function renderOverlay(pending: boolean) {
  return render(
    <SavingOverlay pending={pending}>
      <input aria-label="Title" />
    </SavingOverlay>,
  );
}

describe('SavingOverlay', () => {
  it('freezes at once, and shows the spinner only past the delay', () => {
    renderOverlay(true);
    // No waiting: a control that is being saved must not take a second change.
    expect(screen.getByLabelText('Title').closest('[inert]')).not.toBeNull();
    expect(screen.queryByRole('status')).toBeNull();

    advance(PENDING_SHOW_DELAY_MS - 1);
    expect(screen.queryByRole('status')).toBeNull();

    advance(1);
    expect(screen.getByRole('status').textContent).toBe('Saving…');
  });

  it('unfreezes a save that beat the delay without ever showing anything', () => {
    const view = renderOverlay(true);
    advance(PENDING_SHOW_DELAY_MS - 1);

    view.rerender(
      <SavingOverlay pending={false}>
        <input aria-label="Title" />
      </SavingOverlay>,
    );
    expect(screen.getByLabelText('Title').closest('[inert]')).toBeNull();

    advance(PENDING_SHOW_DELAY_MS);
    expect(screen.queryByRole('status')).toBeNull();
  });
});
