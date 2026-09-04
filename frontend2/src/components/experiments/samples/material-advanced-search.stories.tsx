import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';

import type { AddMaterialFormValues } from '@/components/experiments/samples/add-material-form';
import { EMPTY_ADD_MATERIAL_FORM } from '@/components/experiments/samples/add-material-form';
import { MaterialAdvancedSearch } from '@/components/experiments/samples/material-advanced-search';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { SampleCatalogFilter } from '@/lib/types/samples.ts';

/** The panel is fully controlled; the dialog owns these values, and here the story does. */
function Harness({ catalog, open: initialOpen }: { catalog: SampleCatalogFilter; open: boolean }) {
  const [values, setValues] = useState<AddMaterialFormValues>({ ...EMPTY_ADD_MATERIAL_FORM, catalog });
  const [open, setOpen] = useState(initialOpen);

  return (
    <div className="w-[680px]">
      <MaterialAdvancedSearch
        values={values}
        onChange={(patch) => setValues((previous) => ({ ...previous, ...patch }))}
        open={open}
        onOpenChange={setOpen}
      />
    </div>
  );
}

const meta = {
  title: 'Experiments/Samples/MaterialAdvancedSearch',
  component: Harness,
  args: { catalog: 'ELN', open: true },
} satisfies Meta<typeof Harness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Ten filters in two columns, every one of them available on an ELN catalog. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Compound ID')).toBeEnabled();
    await expect(canvas.getByLabelText('External ID')).toBeEnabled();
    await expect(canvas.getByText('Health Hazards')).toBeInTheDocument();
  },
};

/** All Catalogs reaches PubChem, which can only honour the formula. */
export const PubchemLimited: Story = {
  args: { catalog: 'ALL' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Compound ID')).toBeDisabled();
    await expect(canvas.getByLabelText('Molecular Formula')).toBeEnabled();
    await expect(canvas.getByText(/PubChem does not support fine-grained search/)).toBeVisible();
  },
};

/** Collapsed, the header carries what is set — and a count, so a long summary still says how much. */
export const CollapsedSummary: Story = {
  args: { open: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Compound ID'), 'ASA');

    await userEvent.click(canvas.getByRole('button', { name: /Advanced search/ }));

    await expect(await canvas.findByText('Advanced search (1)')).toBeInTheDocument();
    await expect(canvas.getByText('ASA')).toBeInTheDocument();
  },
};
