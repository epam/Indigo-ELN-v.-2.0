import { expect, screen, within } from 'storybook/test';

import { TemplateSheet } from '@/components/templates/template-sheet';
import { makeTemplate } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Templates/TemplateSheet',
  component: TemplateSheet,
  args: { template: makeTemplate(), onClose: () => {} },
} satisfies Meta<typeof TemplateSheet>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The Default fixture: four tabs, the first with a stoichiometry table and all three of its blocks. */
export const Default: Story = {
  play: async () => {
    const structure = await screen.findByRole('region', { name: 'Structure' });
    const s = within(structure);
    await expect(await s.findByRole('heading', { name: 'Experiment Info' })).toBeInTheDocument();
    await expect(s.getByRole('heading', { name: 'Previous Versions' })).toBeInTheDocument();
    await expect(s.getByText(/Reaction Scheme, Reactants, Reagents, Solvents, Reaction Products/)).toBeInTheDocument();
    await expect(s.getByText('Product Batch Summary')).toBeInTheDocument();
  },
};

/** Nothing selected: the sheet is shut. */
export const Closed: Story = {
  args: { template: undefined },
  play: async () => {
    await expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  },
};
