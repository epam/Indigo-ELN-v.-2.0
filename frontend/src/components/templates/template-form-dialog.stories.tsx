import { http, HttpResponse } from 'msw';
import { useState } from 'react';
import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { TemplateFormDialog } from '@/components/templates/template-form-dialog';
import { makeTemplateDetails } from '@/mocks/fixtures';
import { handlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { TemplateDetails, TemplateRequest, TemplateTab } from '@/lib/types/templates.ts';

const DEFAULT_TABS = makeTemplateDetails().templateTabs;

function TemplateFormDialogHarness({
  starter,
  onCreated,
}: {
  starter: readonly TemplateTab[] | undefined;
  onCreated: (template: TemplateDetails) => void;
}) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <TemplateFormDialog
        open={open}
        onOpenChange={setOpen}
        existingNames={['Default', 'Analytical']}
        starter={starter}
        onCreated={onCreated}
      />
    </>
  );
}

/** Every `POST /templates` body, so a story can check exactly what was built. */
const posted: TemplateRequest[] = [];

const spyHandlers = [
  http.post('/api/eln/templates', async ({ request }) => {
    const body = (await request.json()) as TemplateRequest;
    posted.push(body);
    return HttpResponse.json(
      makeTemplateDetails({ id: 'new-template', name: body.name, templateTabs: body.templateTabs }),
    );
  }),
  ...handlers,
];

const meta = {
  title: 'Templates/TemplateFormDialog',
  component: TemplateFormDialogHarness,
  args: { starter: DEFAULT_TABS, onCreated: fn() },
} satisfies Meta<typeof TemplateFormDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Opens as a copy of Default's tabs, unnamed. */
export const Default: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);
    await expect(d.getByLabelText(/Template Name/)).toHaveValue('');
    await expect(d.getByLabelText(/Tab 1 Name/)).toHaveValue(DEFAULT_TABS[0].name);
    await expect(d.getByLabelText(new RegExp(`Tab ${DEFAULT_TABS.length} Name`))).toHaveValue(
      DEFAULT_TABS.at(-1)!.name,
    );
  },
};

/**
 * Issue #739's own steps: enter a valid name, Save. The template created is Default's structure
 * under the new name, exactly as the backend will store it.
 */
export const NameAndSaveIsEnough: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  play: async ({ args }) => {
    posted.length = 0;
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);

    await userEvent.type(d.getByLabelText(/Template Name/), ' Synthesis ');
    const save = d.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);

    await waitFor(() => expect(posted).toEqual([{ name: 'Synthesis', templateTabs: DEFAULT_TABS }]));
    await waitFor(() => expect(args.onCreated).toHaveBeenCalledOnce());
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/**
 * Built from scratch: without a starter there is one "Tab 1". Components go out in layout order
 * whatever order they were ticked in, and only the stoichiometry component carries settings.
 */
export const BuildsFromScratch: Story = {
  args: { starter: undefined },
  parameters: { msw: { handlers: spyHandlers } },
  play: async () => {
    posted.length = 0;
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);

    await userEvent.type(d.getByLabelText(/Template Name/), 'Synthesis');
    await expect(d.getByLabelText(/Tab 1 Name/)).toHaveValue('Tab 1');
    const first = d.getByRole('group', { name: 'Tab 1 components' });
    await userEvent.click(within(first).getByRole('checkbox', { name: 'Product Batch Summary' }));

    await userEvent.click(d.getByRole('button', { name: 'Add Tab' }));
    const second = d.getByRole('group', { name: 'Tab 2 components' });
    await userEvent.clear(d.getByLabelText(/Tab 2 Name/));
    await userEvent.type(d.getByLabelText(/Tab 2 Name/), 'Reaction');
    // Ticked out of layout order on purpose.
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Stoichiometric Calculation' }));
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Experiment Details' }));
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Reaction Products' }));

    await userEvent.click(d.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(posted).toEqual([
        {
          name: 'Synthesis',
          templateTabs: [
            { name: 'Tab 1', components: [{ type: 'batches' }] },
            {
              name: 'Reaction',
              components: [
                { type: 'experimentDetails' },
                {
                  type: 'stoichiometryTable',
                  reactionScheme: true,
                  reactantsReagentsSolvents: true,
                  intendedProducts: false,
                },
              ],
            },
          ],
        },
      ]),
    );
  },
};

/** A taken name is refused as it is typed, and Save waits for a free one. */
export const RefusesTakenName: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);
    await userEvent.type(d.getByLabelText(/Template Name/), 'Default');
    await expect(await d.findByText("Template 'Default' already exists")).toBeInTheDocument();
    await waitFor(() => expect(d.getByRole('button', { name: 'Save' })).toBeDisabled());
  },
};

/**
 * Emptying a tab says why at once, beside the tab — not just by greying Save out — and Save
 * comes back as soon as the tab has a component again.
 */
export const SaysWhyATabCannotBeSaved: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);
    await userEvent.type(d.getByLabelText(/Template Name/), 'Synthesis');

    // Default's last tab holds Version History alone.
    const last = d.getByRole('group', { name: `Tab ${DEFAULT_TABS.length} components` });
    const versionHistory = within(last).getByRole('checkbox', { name: 'Version History' });
    await userEvent.click(versionHistory);

    await expect(await within(last).findByRole('alert')).toHaveTextContent('Pick at least one component');
    await waitFor(() => expect(d.getByRole('button', { name: 'Save' })).toBeDisabled());

    await userEvent.click(versionHistory);
    await waitFor(() => expect(within(last).queryByRole('alert')).not.toBeInTheDocument());
    await waitFor(() => expect(d.getByRole('button', { name: 'Save' })).toBeEnabled());
  },
};

/** Pressing Save on an empty name says what is missing rather than doing nothing. */
export const SaveWithoutAName: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  play: async () => {
    posted.length = 0;
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);
    await userEvent.click(d.getByRole('button', { name: 'Save' }));
    await expect(await d.findByText('Template Name is required')).toBeInTheDocument();
    await expect(posted).toEqual([]);
  },
};
