import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { StoichiometryTable } from '@/components/experiments/stoichiometry/stoichiometry-table';
import { useExperiment } from '@/lib/api/experiments';
import { makeExperimentDetails, makeReaction } from '@/mocks/fixtures';
import { failingMutateHandlers, handlers, recalculatingMutateHandlers, slowMutateHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];

/** Records the mutations that actually reached the wire, so a story can assert the payload. */
const sent: unknown[] = [];

/** Slow enough to outlast `SavingOverlay`'s 300 ms delay, so the region really does go inert. */
const freezingHandlers = [
  http.post('/api/eln/experiments/:id/mutate', async () => {
    await new Promise((resolve) => setTimeout(resolve, 900));
    return HttpResponse.json({ patch: {} });
  }),
  ...handlers,
];

const spyHandlers = [
  http.post('/api/eln/experiments/:id/mutate', async ({ request }) => {
    sent.push(await request.json());
    return HttpResponse.json({ patch: {} });
  }),
  ...handlers,
];

const meta = {
  title: 'Experiments/Stoichiometry/StoichiometryTable',
  component: StoichiometryTable,
  args: { experiment: EXPERIMENT, reaction: REACTION },
  parameters: { layout: 'padded' },
} satisfies Meta<typeof StoichiometryTable>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Five compounds, every one of them open — the batches are what the table is for. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('columnheader', { name: 'Chem. Name' })).toBeInTheDocument();
    // Chem. Name is editable, so it is an input carrying a value rather than text in the DOM.
    await expect(canvas.getByRole('textbox', { name: 'Chem. Name, row 1' })).toHaveValue('Salicylic acid');
    // The compound row still summarises its batches, above the nested table listing them.
    await expect(canvas.getByText('2, 3, 4, 6')).toBeInTheDocument();
    // Expanded by default: every compound shows the sample columns' header, no clicking required.
    await expect(canvas.getAllByRole('columnheader', { name: 'Density' })).toHaveLength(5);
    await expect(canvas.getByRole('button', { name: 'Hide batches of row 1' })).toBeInTheDocument();
  },
};

/** A compound's batches carry the sample columns, which are not the compound ones. */
export const NestedRows: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getAllByRole('columnheader', { name: 'Density' })).not.toHaveLength(0);
    await expect(canvas.getByRole('textbox', { name: 'Comments, batch 2' })).toHaveValue(
      'Dried over molecular sieves before use',
    );
  },
};

/** The chevron shuts one compound without touching the others. */
export const Collapsed: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Hide batches of row 2' }));

    await expect(canvas.queryByRole('textbox', { name: 'Comments, batch 2' })).not.toBeInTheDocument();
    await expect(canvas.getAllByRole('columnheader', { name: 'Density' })).toHaveLength(4);
    // Its summary of those batches is still on the compound row.
    await expect(canvas.getByText('2, 3, 4, 6')).toBeInTheDocument();
  },
};

/**
 * **The two alignment constraints**, measured rather than eyeballed.
 *
 * Both levels are rows of one table now, so this is really asserting that the spans in
 * `columns.ts` put the sample cells where they are meant to go: Batch # under Batch #, and the
 * two delete columns ending together. Checked for *every* compound, since a per-compound drift
 * is exactly the failure the old two-table layout had.
 */
export const ColumnsLineUp: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const [outerBatch, ...innerBatches] = canvas.getAllByRole('columnheader', { name: 'Batch #' });
    await expect(innerBatches).toHaveLength(5);
    for (const innerBatch of innerBatches) {
      await expect(Math.round(innerBatch.getBoundingClientRect().left)).toBe(
        Math.round(outerBatch.getBoundingClientRect().left),
      );
    }

    // The delete columns carry no header text, so the trailing cell of each row is the anchor.
    const rows = canvas.getAllByRole('row');
    const rights = rows.map((row) => {
      const cells = row.querySelectorAll('th, td');
      return Math.round(cells[cells.length - 1].getBoundingClientRect().right);
    });
    await expect(new Set(rights).size).toBe(1);
  },
};

/**
 * **Entering edit mode must not resize anything.** Columns are content-sized, so an editor that
 * sat in the flow would move the column twice over: out, because a bordered input plus a unit
 * menu asks for more than a short label, and back in when the edited cell happened to be the
 * widest one, because its label had left the layout. `NumericCell` lays the editor over an
 * invisible copy of that label instead, so the cell measures the same either way.
 *
 * Checked on the **widest** cell of each column, which is the one that would shrink it.
 */
export const EditingDoesNotResizeTheColumn: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const table = canvas.getByRole('table', { name: 'Reactants, reagents and solvents' });
    const width = (el: Element) => Math.round(el.getBoundingClientRect().width);

    // Text metrics change when the webfont swaps in, which moves the table by ~24px on its own.
    // Measuring before that lands would fail this for a reason that has nothing to do with editing.
    await document.fonts.ready;

    for (const name of ['Weight', 'Volume', 'Mol', 'Density', 'Molarity', 'Purity']) {
      const header = canvas.getAllByRole('columnheader', { name })[0];
      // The widest *display* in the column — the label that holds the column open.
      const inputs = canvas.getAllByLabelText(new RegExp(`^${name}, batch`));
      const widest = inputs.reduce((a, b) => (width(a) >= width(b) ? a : b));

      const before = { column: width(header), table: width(table) };
      await userEvent.click(widest);
      await expect(widest).toHaveFocus();

      await expect({ column: width(header), table: width(table) }).toEqual(before);
      await userEvent.click(document.body);
    }
  },
};

/**
 * "At least the full page width", as an assertion: auto layout leaves a table narrower than its
 * container unless something asks otherwise, and `w-full` is what asks.
 */
export const FillsAvailableWidth: Story = {
  play: async ({ canvasElement }) => {
    const table = within(canvasElement).getByRole('table', { name: 'Reactants, reagents and solvents' });
    const scroller = table.parentElement!;
    await expect(table.getBoundingClientRect().width).toBeGreaterThanOrEqual(scroller.clientWidth);
  },
};

/** Nothing added yet. */
export const Empty: Story = {
  args: { reaction: makeReaction({ inputs: [], limitingAnchor: undefined }) },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByText('No material added')).toBeInTheDocument();
  },
};

/** The search box filters compounds, and says so when nothing matches. */
export const Search: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Search materials'), 'pyridine');
    // Pyridine is the only row left, so it is row 1 — the index column renumbers.
    await expect(canvas.getByRole('textbox', { name: 'Chem. Name, row 1' })).toHaveValue('Pyridine');
    await expect(canvas.queryByDisplayValue('Salicylic acid')).not.toBeInTheDocument();

    await userEvent.clear(canvas.getByLabelText('Search materials'));
    await userEvent.type(canvas.getByLabelText('Search materials'), 'zzz');
    await expect(canvas.getByText('No material matches this search')).toBeInTheDocument();
  },
};

/** Permission alone is one of the two gates. Without EDIT_EXPERIMENTS everything is inert. */
export const ReadOnlyNoPermission: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Delete compound 1' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Add empty row' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Add sample' })).toBeDisabled();
    // The data is all still there — read-only, not hidden.
    await expect(canvas.getByText('Salicylic acid')).toBeInTheDocument();
  },
};

/**
 * Status is the **other** gate, and the one easy to forget: a completed experiment is read-only
 * to a user who holds every permission on it.
 */
export const ReadOnlyCompleted: Story = {
  args: { experiment: makeExperimentDetails({ status: 'COMPLETED' }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Delete compound 1' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Significant figures' })).toBeDisabled();
  },
};

/**
 * Rxn Role is a `Select`, not a `Combobox`: four fixed values and `@NotNull` on the record, so
 * there is nothing to type into and nothing to clear. Picking one sends `SetInputRowRole`.
 */
export const PicksRxnRole: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const role = (await canvas.findAllByLabelText('Reaction role'))[0];
    await expect(role).toHaveTextContent('Reactant');
    // A button holding a label, not an input holding a value — so there is nothing to type into
    // and no ✕ to clear. Scoped to this control: Salt Code next to it is a Combobox and keeps
    // both, because a salt code is optional and its list is worth filtering.
    await expect(role.tagName).toBe('BUTTON');
    await expect(role.querySelector('input')).toBeNull();

    await userEvent.click(role);
    await userEvent.click(await screen.findByRole('option', { name: 'Catalyst' }));

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputRowRole', anchor: 'd0000000-0000-4000-8000-000000000001', role: 'CATALYST' },
      ]),
    );
  },
};

/**
 * **Provenance colouring**, the thing the table exists to show: where a number came from decides
 * how it looks. `EnteredValue.source` is `'fixed' | 'default' | 'calculated'` **or a number** —
 * the revision a user entered it in — so "typed by a person" is `typeof source === 'number'`,
 * not a named member.
 *
 * `calculated` is deliberately the *unstyled* case: it is the overwhelming majority of a
 * recalculated table, and colouring it would leave nothing standing out.
 *
 * The last assertion is the one that earns its keep. Class names surviving is not the same as
 * the styles reaching the page — a token renamed in `styles.css`, or a utility Tailwind never
 * generated, would leave every cell the same colour with the markup still looking correct.
 */
export const ValueSourcesAreColoured: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // The provenance classes live on the display span. It is `aria-hidden` — the input beside it
    // carries the label — so it is reached through the cell rather than by its accessible name.
    const display = (label: string) =>
      canvas
        .getByLabelText(label)
        .closest('[data-slot="numeric-cell"]')!
        .querySelector('[data-slot="numeric-cell-value"]')!;

    const userEntered = display('Weight, batch 1');
    const calculated = display('Mol, batch 1');
    const fixed = display('Density, batch 2');
    const byDefault = display('Purity, batch 3');

    await expect(userEntered).toHaveClass('text-blue-400', 'font-semibold');
    await expect(fixed).toHaveClass('text-violet-400');
    await expect(byDefault).toHaveClass('text-neutral-700', 'italic');
    // Calculated carries no colour of its own and inherits the table's.
    await expect(calculated.className).not.toMatch(/text-(blue|violet|neutral)-/);

    const colours = [userEntered, calculated, fixed, byDefault].map((el) => getComputedStyle(el).color);
    await expect(new Set(colours).size).toBe(4);
  },
};

/**
 * **Every control in a cell reads at the table's own size.** The shared `Combobox` and `Select`
 * default to the 14px a form wants; in a 13px table that is visibly a size larger than the text
 * beside it, and swapping a cell into edit mode used to change the type size under the cursor.
 * The stoichiometry cells opt into `size="sm"`.
 *
 * Measured rather than asserted on class names — the point is what the browser renders, and a
 * control could pick up a size from anywhere.
 */
export const ControlsMatchTheTableTextSize: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const fontOf = (el: Element) => getComputedStyle(el).fontSize;

    // The reference: a plain read-only cell, which is what everything should match.
    const reference = fontOf(canvas.getAllByText('STR-00000000-89')[0]);

    for (const el of [
      canvas.getByLabelText('Weight, batch 1'), // number input
      canvas.getByLabelText('Weight, batch 1 unit'), // native unit select
      canvas.getByLabelText('Comments, batch 2'), // free-text cell
      canvas.getAllByLabelText('Reaction role')[0], // Select
      canvas.getByLabelText('Salt Code, row 3'), // Combobox
      canvas.getByLabelText('Hazard Comments, batch 1'), // MultiCombobox
    ]) {
      await expect(fontOf(el)).toBe(reference);
    }

    // ...and the display a numeric cell swaps with, so the size does not shift on focus.
    const display = canvas
      .getByLabelText('Weight, batch 1')
      .closest('[data-slot="numeric-cell"]')!
      .querySelector('[data-slot="numeric-cell-value"]')!;
    await expect(fontOf(display)).toBe(reference);
  },
};

/**
 * Salt Code is a `Select` too — short closed list, nothing to type. It differs from Rxn Role in
 * being **optional**, so clearing is a row in the list rather than a ✕: `SetInputRowSaltCode`
 * takes a null, and a select has no other way out of a value.
 */
export const PicksAndClearsSaltCode: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    // Row 3 is the virtual compound, the only one whose salt code is editable.
    const saltCode = await canvas.findByLabelText('Salt Code, row 3');
    await expect(saltCode.tagName).toBe('BUTTON');
    await expect(saltCode.querySelector('input')).toBeNull();

    await userEvent.click(saltCode);
    // Scoped to the open list: the unit `<select>`s in the numeric cells contribute options of
    // their own, including an `—` for a unit not yet chosen.
    await userEvent.click(await within(await screen.findByRole('listbox')).findByRole('option', { name: 'Na' }));
    await waitFor(() => expect(sent).toHaveLength(1));
    await expect(sent[0]).toMatchObject({ type: 'SetInputRowSaltCode', saltCode: { name: 'Na' } });

    // ...and back out again through the list. Clicking the option inside the resolved listbox
    // rather than driving the keyboard: `{Home}{Enter}` raced Base UI settling the highlight
    // after the popup mounts, which made this pass or fail run to run.
    sent.length = 0;
    await userEvent.click(canvas.getByLabelText('Salt Code, row 3'));
    const list = await screen.findByRole('listbox');
    await userEvent.click(await within(list).findByRole('option', { name: '—' }));
    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputRowSaltCode', anchor: 'd0000000-0000-4000-8000-000000000003', saltCode: null },
      ]),
    );
  },
};

/**
 * A molecular formula arrives as HTML — `C<sub>10</sub>H<sub>12</sub>N<sub>2</sub>O<sub>4</sub>` —
 * and its subscripts sit below the line on `align-sub`. The cell also truncates, which brings
 * `overflow: hidden`, so without padding to clip against the digits get shaved off the bottom.
 *
 * Asserted at several zoom levels because that is how it showed up: the overflow was there all
 * along, and subpixel rounding decided which zooms made it visible.
 */
export const FormulaSubscriptsAreNotClipped: Story = {
  play: async ({ canvasElement }) => {
    await document.fonts.ready;
    const formula = canvasElement.querySelector('sub')!.parentElement!;

    try {
      for (const zoom of [1, 1.1, 1.25, 1.33, 1.5, 1.75, 2]) {
        (document.body.style as unknown as Record<string, string>).zoom = String(zoom);
        await new Promise((resolve) => requestAnimationFrame(() => resolve(null)));

        const sub = formula.querySelector('sub')!;
        const overflow = sub.getBoundingClientRect().bottom - formula.getBoundingClientRect().bottom;
        await expect(`${zoom}x overflow ${overflow.toFixed(1)}px`).toBe(
          `${zoom}x overflow ${Math.min(overflow, 0).toFixed(1)}px`,
        );
      }
    } finally {
      // Zoom is set on the shared document, so leaving it raised would follow other stories home.
      (document.body.style as unknown as Record<string, string>).zoom = '1';
    }
  },
};

/**
 * A stored compound's salt code and salt EQ come from the registry, so both stay locked even
 * though the row has a salt code set. indigo-frontend gated Salt EQ on the code alone and let
 * this row be edited.
 */
export const StoredCompoundSaltLocked: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // Row 4 is the stored compound carrying a salt code; row 3 is the virtual one.
    await expect(canvas.getByLabelText('Salt EQ, row 4')).toBeDisabled();
    await expect(canvas.getByLabelText('Salt EQ, row 3')).toBeEnabled();
  },
};

/**
 * A read-only cell shows the arrow, not the text I-beam — whether it holds a value or an
 * em-dash. `cursor: auto`, the initial value, resolves to an I-beam over text content, and an
 * I-beam is the cursor that means "type here": on a cell that cannot be edited it invites a
 * click that does nothing.
 *
 * Asserted on computed style rather than on class names, since what matters is what the browser
 * actually renders — these stories run in real Chromium, so it can be asked.
 */
export const ReadOnlyCellsUseArrowCursor: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    // Editability decides the cursor, not emptiness. In a numeric cell the input sits on top of
    // the display, so it is the input that the pointer is over — enabled or not.
    const editableInput = canvas.getByLabelText('Weight, batch 1');
    const lockedInput = canvas.getByLabelText('Salt EQ, row 4');
    await expect(editableInput).toBeEnabled();
    await expect(lockedInput).toBeDisabled();
    await expect(getComputedStyle(editableInput).cursor).toBe('text');
    await expect(getComputedStyle(lockedInput).cursor).toBe('default');

    // Columns with no editor at all — the compound-level Weight and Volume, Compound ID, the
    // batch summary — are plain text, whether they hold a value or an em-dash.
    const plain = canvas.getAllByText('—').filter((cell) => cell.closest('[data-slot="numeric-cell"]') === null);
    await expect(plain.length).toBeGreaterThan(0);
    for (const cell of plain) {
      await expect(getComputedStyle(cell).cursor).toBe('default');
    }
    await expect(getComputedStyle(canvas.getAllByText('STR-00000000-89')[0]).cursor).toBe('default');
  },
};

/** With editing off, even the normally-editable numeric cells drop the I-beam. */
export const ReadOnlyExperimentUsesArrowCursor: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Weight, batch 1')).toBeDisabled();
    await expect(getComputedStyle(canvas.getByLabelText('Weight, batch 1')).cursor).toBe('default');
  },
};

/** Reads the experiment back out of the cache, so a landed patch is visible. */
function TableFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <StoichiometryTable experiment={data} reaction={data.model.reactions[0]} /> : null;
}

/** Editing a batch weight sends `SetInputWeight` with the sample's anchor, value and unit. */
export const EditsSampleWeight: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '750{Enter}');

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputWeight', anchor: 'e0000000-0000-4000-8000-00000000000a', weight: '750', unit: 'MG' },
      ]),
    );
  },
};

/**
 * A cell that already holds a value **and** a unit saves as soon as focus leaves it. Nothing else
 * confirms the edit — there is no Save button on this screen — so a blur that quietly kept the
 * number on screen without sending it would leave no way to tell whether the change took.
 */
export const BlurSavesAChangedValue: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '700');
    // No Enter, no unit change — just leaving.
    await userEvent.click(document.body);

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputWeight', anchor: 'e0000000-0000-4000-8000-00000000000a', weight: '700', unit: 'MG' },
      ]),
    );
  },
};

/** Clearing a value sends null for the value **and** its unit — not an empty string. */
export const ClearsCell: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Weight, batch 1'));
    await userEvent.clear(canvas.getByLabelText('Weight, batch 1'));
    await userEvent.keyboard('{Enter}');

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputWeight', anchor: 'e0000000-0000-4000-8000-00000000000a', weight: null, unit: null },
      ]),
    );
  },
};

/**
 * Value and unit go together or not at all, so a number typed into a cell that has no unit yet
 * sends nothing. Tab is what asks for the missing half — the picker is simply the next control.
 */
export const NoRequestUntilAUnitIsChosen: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    // This batch has no molarity, so its unit is unset.
    const input = await canvas.findByLabelText('Molarity, batch 2');
    await userEvent.click(input);
    await userEvent.type(input, '0.25');
    await userEvent.click(document.body);

    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(sent).toEqual([]);
  },
};

/**
 * Tab out of the number input and the unit picker is simply the next control — no `focus()` call,
 * no popup to open.
 *
 * **Two mutations, not one.** Leaving the input saves the number against the unit already there,
 * because that is a finished edit on its own; choosing a different unit then saves again. The
 * alternative — holding the number until focus left the whole cell — made tabbing to the unit
 * look like it had done nothing at all.
 *
 * A native `<select>` renders its list through the OS rather than into the document, so there is
 * nothing to assert as "open"; what matters is that focus reaches it and that choosing saves.
 */
export const TabbingReachesTheUnitPicker: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '2.5');

    await userEvent.tab();
    const unit = canvas.getByLabelText('Weight, batch 1 unit');
    await expect(unit).toHaveFocus();

    // Saved on the way out of the input, keeping the unit it already had.
    const anchor = 'e0000000-0000-4000-8000-00000000000a';
    await waitFor(() => expect(sent).toEqual([{ type: 'SetInputWeight', anchor, weight: '2.5', unit: 'MG' }]));

    await userEvent.selectOptions(unit, 'G');
    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputWeight', anchor, weight: '2.5', unit: 'MG' },
        { type: 'SetInputWeight', anchor, weight: '2.5', unit: 'G' },
      ]),
    );
  },
};

/**
 * The case that prompted the change: a cell that already holds **both** halves saves the moment
 * the number is changed and focus moves on — even when it moves only as far as the unit picker
 * beside it, which is still inside the same cell.
 */
export const TabbingToTheUnitSavesTheValue: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '812');
    await userEvent.tab();

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetInputWeight', anchor: 'e0000000-0000-4000-8000-00000000000a', weight: '812', unit: 'MG' },
      ]),
    );
  },
};

/**
 * **The property the whole structure exists for.** Both forms are always in the DOM, so tab order
 * is the document's own — a row walks value, unit, value, unit with nothing managing focus. The
 * previous version mounted the editor on demand and had to move focus by hand, which is where the
 * stuck picker and the dead blur came from.
 */
export const TabOrderWalksTheRow: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const weight = canvas.getByLabelText('Weight, batch 1');
    weight.focus();

    await userEvent.tab();
    await expect(canvas.getByLabelText('Weight, batch 1 unit')).toHaveFocus();

    await userEvent.tab();
    await expect(canvas.getByLabelText('Volume, batch 1')).toHaveFocus();

    // ...and back out again, so nothing traps focus inside a cell.
    await userEvent.tab({ shift: true });
    await expect(canvas.getByLabelText('Weight, batch 1 unit')).toHaveFocus();
  },
};

/**
 * Escape abandons the edit outright: it reverts the draft, drops focus, and sends nothing — so
 * the cell is back to its display without having touched the server.
 *
 * The last of those is the one worth a test. Blurring is what returns the cell to view mode, but
 * blur is also what commits, and the commit reads the draft from the render already on screen —
 * the value Escape just discarded. Without the guard in `NumericCell`, Escape would *save* the
 * thing it is supposed to throw away.
 */
export const EscapeRevertsAndLeaves: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '999');
    await userEvent.keyboard('{Escape}');

    // Focus is gone, so `:focus-within` no longer holds the editor up.
    await expect(input).not.toHaveFocus();
    // The draft is back to what the server confirmed...
    await waitFor(() => expect(input).toHaveValue(676.5));
    await expect(canvas.getByText('676.5 mg')).toBeInTheDocument();

    // ...and nothing was sent, which is the part that would silently regress.
    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(sent).toEqual([]);
  },
};

/**
 * **Regression guard**, the same one the other panels carry: opening a cell and leaving it
 * without changing anything must send nothing. Tabbing across a row is a normal thing to do.
 */
export const NoRequestWhenUnchanged: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Weight, batch 1'));
    await userEvent.click(document.body);

    await userEvent.click(canvas.getByRole('textbox', { name: 'Chem. Name, row 1' }));
    await userEvent.click(document.body);

    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(sent).toEqual([]);
  },
};

/**
 * The flash. The server answers a one-cell edit by recalculating a value the user did not
 * touch, and that cell is marked so the change is not silent.
 */
export const Recalculated: Story = {
  parameters: { msw: { handlers: recalculatingMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Purity, batch 2'));
    await userEvent.type(canvas.getByLabelText('Purity, batch 2'), '99{Enter}');

    // The patch rewrites this batch's mol, which nothing on screen asked for.
    // The provenance classes and the flash live on the display span, not on the input.
    await waitFor(() => expect(canvas.getByText('0.0075 mmol')).toBeInTheDocument());
    await expect(canvas.getByText('0.0075 mmol')).toHaveClass('animate-[flash-green_500ms_ease-in-out]');
  },
};

/** One cell freezes while it saves; its neighbours stay live. */
export const Saving: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Weight, batch 1'));
    await userEvent.type(canvas.getByLabelText('Weight, batch 1'), '5{Enter}');

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));
    // Only that cell: the batch's volume cell is untouched.
    await expect(canvas.getByLabelText('Volume, batch 1')).toBeEnabled();
  },
};

/**
 * Adding a row is a write like any other, so the toolbar button reports it the way a cell does —
 * a spinner over the button, and the button inert meanwhile so the row cannot be added twice.
 *
 * Only past `SavingOverlay`'s 300 ms delay: the usual `AddEmptyInput` beats that and shows
 * nothing, which is the point of the delay.
 */
export const AddingARow: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await userEvent.click(canvas.getByRole('button', { name: 'Add empty row' }));

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));

    // The busy region is the button itself, not the toolbar: `SavingOverlay` publishes
    // `data-saving` on the box it froze, and that box holds only this one button.
    const busy = canvasElement.querySelector('[data-saving]');
    await expect(busy).toContainElement(canvas.getByRole('button', { name: 'Add empty row' }));
    await expect(busy).not.toContainElement(canvas.getByRole('button', { name: 'Add sample' }));
    // So the rest of the toolbar stays live while the row is on its way.
    await expect(canvas.getByRole('button', { name: 'Add sample' })).toBeEnabled();
  },
};

/**
 * **A slow save must not cost the user their place.** `SavingOverlay` freezes the region it
 * covers with `inert`, and `inert` blurs whatever is inside it — so committing a number on the
 * way to its unit picker would freeze the cell that picker lives in and drop focus to nowhere.
 * The overlay hands focus back when it unfreezes.
 *
 * Only when focus is still nowhere: a user who has moved on in the meantime keeps their place.
 */
export const SlowSaveRestoresFocus: Story = {
  parameters: { msw: { handlers: freezingHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Weight, batch 1');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '451');
    await userEvent.tab();

    const unit = canvas.getByLabelText('Weight, batch 1 unit');
    await expect(unit).toHaveFocus();

    // The save outlasts the delay, so the cell freezes and `inert` takes focus away.
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));
    await expect(unit).not.toHaveFocus();

    // ...and it comes back once the save lands.
    await waitFor(() => expect(screen.queryByRole('status')).not.toBeInTheDocument(), { timeout: 5_000 });
    await waitFor(() => expect(unit).toHaveFocus());
  },
};

/** A failed save leaves the cell showing what the server last confirmed. */
export const FailedSaveKeepsServerValue: Story = {
  parameters: { msw: { handlers: failingMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Weight, batch 1'));
    await userEvent.clear(canvas.getByLabelText('Weight, batch 1'));
    await userEvent.type(canvas.getByLabelText('Weight, batch 1'), '5{Enter}');

    // Nothing was written to the cache, so the display still shows what the server confirmed.
    await waitFor(() => expect(canvas.getByText('676.5 mg')).toBeInTheDocument());
  },
};

/** Picking a precision sends the experiment-level mutation; the backend reformats every value. */
export const SetsSignificantFigures: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Significant figures' }));
    await userEvent.click(await screen.findByRole('menuitem', { name: '3' }));

    await waitFor(() => expect(sent).toEqual([{ type: 'SetExperimentSignificantFigures', significantFigures: 3 }]));
  },
};

/** The add button sends `AddEmptyInput` against the reaction, not against a row. */
export const AddsEmptyRow: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    await userEvent.click(await within(canvasElement).findByRole('button', { name: 'Add empty row' }));

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'AddEmptyInput', anchor: 'b0000000-0000-4000-8000-000000000001' }]),
    );
  },
};

/**
 * The two deletes are different mutations against different anchors — `RemoveInputRow` takes
 * the compound with all its batches, `RemoveInput` takes one batch.
 */
export const DeletesRowAndBatch: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Delete batch 1' }));
    await waitFor(() =>
      expect(sent).toEqual([{ type: 'RemoveInput', anchor: 'e0000000-0000-4000-8000-00000000000a' }]),
    );

    sent.length = 0;
    await userEvent.click(canvas.getByRole('button', { name: 'Delete compound 1' }));
    await waitFor(() =>
      expect(sent).toEqual([{ type: 'RemoveInputRow', anchor: 'd0000000-0000-4000-8000-000000000001' }]),
    );
  },
};
