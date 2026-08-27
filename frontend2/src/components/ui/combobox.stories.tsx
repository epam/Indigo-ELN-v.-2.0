import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { MultiCombobox } from '@/components/ui/combobox';
import { KEYWORDS } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** Local prefix filtering, standing in for the suggest endpoint the real caller uses. */
function ComboboxHarness({
  initial = [],
  allowCustomValues = false,
  suggestions = KEYWORDS,
  loading = false,
  error = false,
}: {
  initial?: string[];
  allowCustomValues?: boolean;
  /** Overridable so a story can pin the list empty regardless of what is typed. */
  suggestions?: string[];
  loading?: boolean;
  error?: boolean;
}) {
  const [value, setValue] = useState<string[]>(initial);
  const [inputValue, setInputValue] = useState('');
  const items = suggestions
    .filter((keyword) => keyword.toLowerCase().startsWith(inputValue.trim().toLowerCase()) && !value.includes(keyword))
    .sort();

  return (
    <div className="w-[420px]">
      <label id="keywords-label" htmlFor="keywords" className="text-[14px]/6">
        Project Keywords
      </label>
      <MultiCombobox
        id="keywords"
        value={value}
        onValueChange={setValue}
        items={items}
        inputValue={inputValue}
        onInputValueChange={setInputValue}
        placeholder="Add Keyword"
        emptyMessage="No matching keywords"
        allowCustomValues={allowCustomValues}
        loading={loading}
        error={error}
      />
    </div>
  );
}

const meta = {
  title: 'UI/MultiCombobox',
  component: ComboboxHarness,
  args: {},
} satisfies Meta<typeof ComboboxHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithSelection: Story = {
  args: { initial: ['kinase', 'screening'] },
};

/** Typing filters the list, and Enter commits the highlighted suggestion as a chip. */
export const SelectSuggestion: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('sul');
    await waitFor(() => expect(document.body).toHaveTextContent('sulfonamide'));

    await userEvent.keyboard('{ArrowDown}{Enter}');
    await waitFor(() => expect(canvas.getByLabelText('Remove sulfonamide')).toBeInTheDocument());
  },
};

/** With allowCustomValues, text no suggestion matches still becomes a chip on Enter. */
export const FreeFormEntry: Story = {
  args: { allowCustomValues: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('zirconium{Enter}');
    await waitFor(() => expect(canvas.getByLabelText('Remove zirconium')).toBeInTheDocument());
  },
};

/**
 * A comma is an ordinary character, not a commit key. Chemistry keywords are full of them
 * — N,N-dimethylformamide, 1,3-butadiene, 2,4-D — and treating comma as "add this chip"
 * would make every one of those impossible to type.
 */
export const CommaStaysInsideAKeyword: Story = {
  args: { allowCustomValues: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('N,N-dimethylformamide{Enter}');

    await waitFor(() => expect(canvas.getByLabelText('Remove N,N-dimethylformamide')).toBeInTheDocument());
    // Not split at the comma into "N" and "N-dimethylformamide".
    await expect(canvas.queryByLabelText('Remove N')).not.toBeInTheDocument();
  },
};

/** Backspace on an empty input removes the last chip (Base UI's own behaviour). */
export const BackspaceRemovesLastChip: Story = {
  args: { initial: ['kinase', 'screening'] },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('{Backspace}');
    await waitFor(() => expect(canvas.queryByLabelText('Remove screening')).not.toBeInTheDocument());
    await expect(canvas.getByLabelText('Remove kinase')).toBeInTheDocument();
  },
};

/** ArrowLeft highlights a chip, then Backspace deletes that one rather than the last. */
export const ArrowLeftThenBackspace: Story = {
  args: { initial: ['kinase', 'screening', 'stability'] },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('{ArrowLeft}{ArrowLeft}{Backspace}');
    await waitFor(() => expect(canvas.queryByLabelText('Remove screening')).not.toBeInTheDocument());
    await expect(canvas.getByLabelText('Remove kinase')).toBeInTheDocument();
    await expect(canvas.getByLabelText('Remove stability')).toBeInTheDocument();
  },
};

/**
 * PageDown jumps a page down the list where a single ArrowDown moves one row. Base UI
 * ignores the Page keys, so this asserts the handler MultiCombobox adds.
 */
export const PageKeysNavigate: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    // The whole keyword list, so there is more than one page step to travel.
    await userEvent.click(canvas.getByLabelText('Show suggestions'));
    await waitFor(() => expect(highlightableOptions().length).toBeGreaterThan(PAGE_STEP));

    await userEvent.keyboard('{ArrowDown}');
    await waitFor(() => expect(highlightedIndex()).toBe(0));

    await userEvent.keyboard('{PageDown}');
    await waitFor(() => expect(highlightedIndex()).toBe(PAGE_STEP));

    await userEvent.keyboard('{PageUp}');
    await waitFor(() => expect(highlightedIndex()).toBe(0));
  },
};

/**
 * Base UI's own list navigation wraps around at the ends, so the page keys have to clamp:
 * near the bottom PageDown must stop on the last item, not carry on into the first.
 */
export const PageKeysStopAtTheEnds: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await userEvent.click(canvas.getByLabelText('Show suggestions'));
    await waitFor(() => expect(highlightableOptions().length).toBeGreaterThan(PAGE_STEP));
    const last = highlightableOptions().length - 1;
    // Fewer than a full page of items remain below this point.
    await expect(last).toBeLessThan(PAGE_STEP * 2);

    await userEvent.keyboard('{ArrowDown}{PageDown}');
    await waitFor(() => expect(highlightedIndex()).toBe(PAGE_STEP));

    // A second PageDown has less than a page left, so it lands on the last item.
    await userEvent.keyboard('{PageDown}');
    await waitFor(() => expect(highlightedIndex()).toBe(last));

    // And once there, it stays rather than wrapping to the top.
    await userEvent.keyboard('{PageDown}');
    await waitFor(() => expect(highlightedIndex()).toBe(last));

    // The same at the other end.
    await userEvent.keyboard('{PageUp}');
    await waitFor(() => expect(highlightedIndex()).toBe(last - PAGE_STEP));
    await userEvent.keyboard('{PageUp}');
    await waitFor(() => expect(highlightedIndex()).toBe(0));
    await userEvent.keyboard('{PageUp}');
    await waitFor(() => expect(highlightedIndex()).toBe(0));
  },
};

/** Mirrors PAGE_STEP in combobox.tsx — the distance the Page keys are meant to travel. */
const PAGE_STEP = 10;

function highlightableOptions(): HTMLElement[] {
  return Array.from(document.querySelectorAll<HTMLElement>('[role="option"]'));
}

function highlightedIndex(): number {
  return highlightableOptions().findIndex((option) => option.hasAttribute('data-highlighted'));
}

/** Without the opt-in, Enter on unmatched text does nothing — no chip, no data loss. */
export const RejectsCustomValue: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Project Keywords');

    await userEvent.click(input);
    await userEvent.keyboard('zirconium{Enter}');

    await expect(canvas.queryByLabelText('Remove zirconium')).not.toBeInTheDocument();
    // Base UI's own reset then clears the unmatched text, as a suggestion-only combobox
    // should — the selection can only ever come from the list.
    await expect(input).toHaveValue('');
  },
};

/** With the opt-in, the empty popup invites Enter and names what it would add. */
export const EnterHintWhenNoMatches: Story = {
  args: { allowCustomValues: true },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium');

    await waitFor(() => expect(screen.getByText('Press Enter to add “zirconium”')).toBeInTheDocument());
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
  },
};

/**
 * Nothing typed and nothing to suggest: no hint, and no "no matches" either — nothing was
 * searched for. The popup stays shut rather than opening an empty box.
 */
export const NothingToShowBeforeTyping: Story = {
  args: { allowCustomValues: true, suggestions: [] },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Show suggestions'));

    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
    await expect(screen.queryByText(/Press Enter to add/)).not.toBeInTheDocument();
    await expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  },
};

/** Typing then clearing the box puts it back to showing nothing at all. */
export const MessageClearsWithTheQuery: Story = {
  args: { allowCustomValues: true, suggestions: [] },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium');
    await waitFor(() => expect(screen.getByText('Press Enter to add “zirconium”')).toBeInTheDocument());

    await userEvent.clear(within(canvasElement).getByLabelText('Project Keywords'));
    await waitFor(() => expect(screen.queryByText(/Press Enter to add/)).not.toBeInTheDocument());
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
  },
};

/**
 * A keyword already held as a chip is filtered out of the suggestions, emptying the list —
 * but Enter would be a no-op there, so it must not be advertised.
 */
export const NoHintForAlreadySelectedValue: Story = {
  // A one-item suggestion set, so filtering out the chosen chip really does empty the list
  // — with the full KEYWORDS list, 'kinase-inhibitor' would still match the prefix.
  args: { allowCustomValues: true, initial: ['kinase'], suggestions: ['kinase'] },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Project Keywords'));
    await userEvent.keyboard('kinase');

    await waitFor(() => expect(screen.getByText('No matching keywords')).toBeInTheDocument());
    await expect(screen.queryByText(/Press Enter to add/)).not.toBeInTheDocument();
  },
};

/** While suggestions are loading the popup says so, instead of claiming there are none. */
export const Loading: Story = {
  args: { allowCustomValues: true, suggestions: [], loading: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium');

    await waitFor(() => expect(screen.getByText('Searching…')).toBeInTheDocument());
    // Neither claim may be made before the search has finished.
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
    await expect(screen.queryByText(/Press Enter to add/)).not.toBeInTheDocument();
    await expect(canvas.getByLabelText('Show suggestions')).toHaveAttribute('aria-busy', 'true');
  },
};

/** Loading with nothing typed still opens nothing — the chevron just spins. */
export const LoadingBeforeTyping: Story = {
  args: { allowCustomValues: true, suggestions: [], loading: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByLabelText('Show suggestions'));

    await waitFor(() => expect(screen.getByText('Searching…')).toBeInTheDocument());
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
  },
};

/** A failed lookup says so, rather than passing itself off as "no matches". */
export const SuggestionsFailed: Story = {
  args: { allowCustomValues: true, suggestions: [], error: true },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium');

    await waitFor(() => expect(screen.getByText('Could not load suggestions')).toBeInTheDocument());
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
    // Adding a custom value never depended on the lookup, so the offer stands.
    await expect(screen.getByText('Press Enter to add “zirconium”')).toBeInTheDocument();
  },
};

/** Enter still works after a failed lookup — the hint is not decoration. */
export const CanStillAddAfterFailure: Story = {
  args: { allowCustomValues: true, suggestions: [], error: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium{Enter}');

    await waitFor(() => expect(canvas.getByLabelText('Remove zirconium')).toBeInTheDocument());
  },
};

/** With no custom values to offer, a failure leaves only the failure. */
export const SuggestionsFailedSuggestionOnly: Story = {
  args: { suggestions: [], error: true },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('Project Keywords'));
    await userEvent.keyboard('zirconium');

    await waitFor(() => expect(screen.getByText('Could not load suggestions')).toBeInTheDocument());
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
  },
};
