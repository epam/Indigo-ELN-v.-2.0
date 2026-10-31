import { TemplateRow } from '@/components/templates/template-row';
import { makeTemplate, TEMPLATES } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Templates/TemplateRow',
  component: TemplateRow,
  args: { template: makeTemplate(), selected: false, onSelect: () => {} },
} satisfies Meta<typeof TemplateRow>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The row whose sheet is open. */
export const Selected: Story = { args: { selected: true } };

/** The name truncates rather than wrapping, keeping every row the same height. */
export const LongName: Story = {
  args: { template: makeTemplate({ name: 'Palladium-Catalysed Cross-Coupling — Full Stoichiometry and Analytics' }) },
};

/** Rows are meant to be read as a stack — this is what the page looks like. */
export const Stacked: Story = {
  render: () => (
    <div className="flex flex-col gap-3">
      {TEMPLATES.map((template, index) => (
        <TemplateRow key={template.id} template={template} selected={index === 0} onSelect={() => {}} />
      ))}
    </div>
  ),
};
