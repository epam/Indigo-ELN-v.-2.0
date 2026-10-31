import { http, HttpResponse } from 'msw';
import { useState } from 'react';
import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { TemplateFormDialog } from '@/components/templates/template-form-dialog';
import { makeTemplateDetails } from '@/mocks/fixtures';
import { handlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { TemplateDetails, TemplateRequest } from '@/lib/types/templates.ts';

function TemplateFormDialogHarness({ onCreated }: { onCreated: (template: TemplateDetails) => void }) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <TemplateFormDialog
        open={open}
        onOpenChange={setOpen}
        existingNames={['Default', 'Analytical']}
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
  args: { onCreated: fn() },
} satisfies Meta<typeof TemplateFormDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** One empty tab to start from; Save waits for a name, a tab name and a component. */
export const Default: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    await expect(within(dialog).getByRole('button', { name: 'Save' })).toBeDisabled();
    await expect(within(dialog).queryByRole('button', { name: /Remove Tab/ })).not.toBeInTheDocument();
  },
};

/**
 * Two tabs, the second with a stoichiometry table trimmed to two blocks. The body is the wire
 * shape the backend validates: names trimmed, components in layout order, and only the
 * stoichiometry component carrying settings.
 */
export const CreatesTemplate: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  play: async ({ args }) => {
    posted.length = 0;
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);

    await userEvent.type(d.getByLabelText(/Template Name/), ' Synthesis ');

    const first = d.getByRole('group', { name: 'Tab 1 components' });
    await userEvent.type(d.getByLabelText(/Tab 1 Name/), 'Summary');
    await userEvent.click(within(first).getByRole('checkbox', { name: 'Product Batch Summary' }));

    await userEvent.click(d.getByRole('button', { name: 'Add Tab' }));
    const second = d.getByRole('group', { name: 'Tab 2 components' });
    await userEvent.type(d.getByLabelText(/Tab 2 Name/), 'Reaction');
    // Ticked out of layout order on purpose.
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Stoichiometric Calculation' }));
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Experiment Details' }));
    await userEvent.click(within(second).getByRole('checkbox', { name: 'Reaction Products' }));

    const save = d.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);

    await waitFor(() =>
      expect(posted).toEqual([
        {
          name: 'Synthesis',
          templateTabs: [
            { name: 'Summary', components: [{ type: 'batches' }] },
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
    await waitFor(() => expect(args.onCreated).toHaveBeenCalledOnce());
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/** A taken name, and two tabs of one name, are refused as they are typed. */
export const RefusesDuplicates: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);

    const name = d.getByLabelText(/Template Name/);
    await userEvent.type(name, 'Default');
    await userEvent.tab();
    await expect(await d.findByText("Template 'Default' already exists")).toBeInTheDocument();

    await userEvent.type(d.getByLabelText(/Tab 1 Name/), 'Reaction');
    await userEvent.click(d.getByRole('button', { name: 'Add Tab' }));
    await userEvent.type(d.getByLabelText(/Tab 2 Name/), 'Reaction');
    await expect((await d.findAllByText("Another tab is already called 'Reaction'")).length).toBe(2);
    await expect(d.getByRole('button', { name: 'Save' })).toBeDisabled();
  },
};

/** A tab with no component keeps Save disabled, however complete the rest is. */
export const RequiresAComponent: Story = {
  play: async () => {
    const dialog = await screen.findByRole('dialog', { name: 'Add Template' });
    const d = within(dialog);
    await userEvent.type(d.getByLabelText(/Template Name/), 'Empty');
    await userEvent.type(d.getByLabelText(/Tab 1 Name/), 'Reaction');
    // Ticking a component makes the tab valid; unticking it empties the tab again.
    const group = d.getByRole('group', { name: 'Tab 1 components' });
    await userEvent.click(within(group).getByRole('checkbox', { name: 'Attachments' }));
    await expect(d.getByRole('button', { name: 'Save' })).toBeEnabled();
    await userEvent.click(within(group).getByRole('checkbox', { name: 'Attachments' }));
    await expect(d.getByRole('button', { name: 'Save' })).toBeDisabled();
  },
};
