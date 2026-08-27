import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';

import type { StructureEditorResult } from '@/components/chemistry/structure-editor-dialog';
import type { Meta, StoryObj } from '@storybook/react-vite';

/**
 * Ketcher itself is mocked in Storybook (.storybook/mocks/), so the sketcher is a
 * placeholder and every structure renders as the same canned benzene.
 */
function SchemeEditorHarness({ initial }: { initial: string | null }) {
  const [value, setValue] = useState(initial);
  return (
    <div className="w-[640px]">
      <SchemeEditor
        value={value}
        onChange={(next: StructureEditorResult | null) => setValue(next?.structure ?? null)}
      />
    </div>
  );
}

const meta = {
  title: 'Chemistry/SchemeEditor',
  component: SchemeEditorHarness,
  args: { initial: null },
} satisfies Meta<typeof SchemeEditorHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Empty: Story = {};

export const WithStructure: Story = {
  args: { initial: 'a-molfile' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByRole('img', { name: 'Chemical structure' })).toBeInTheDocument();
  },
};

/** Drawing something fills the frame; the sketcher is portalled, so reach it via `screen`. */
export const DrawsAStructure: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Draw Structure' }));
    // Save stays disabled until the sketcher reports itself ready.
    const save = await screen.findByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);
    await expect(await canvas.findByRole('img', { name: 'Chemical structure' })).toBeInTheDocument();
  },
};
