import { expect, screen, userEvent, within } from 'storybook/test';

import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Menu',
  component: Menu,
  render: () => (
    <Menu>
      <MenuTrigger className="cursor-pointer rounded-2 border border-neutral-300 px-3 py-2 text-[14px]/6 outline-none focus-visible:ring-3 focus-visible:ring-ring/50">
        Actions
      </MenuTrigger>
      <MenuContent>
        <MenuItem onClick={() => {}}>Rename</MenuItem>
        <MenuItem onClick={() => {}}>Duplicate</MenuItem>
      </MenuContent>
    </Menu>
  ),
} satisfies Meta<typeof Menu>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Closed: Story = {};

/** The popup is portalled, so it has to be queried through `screen`, not the canvas. */
export const Open: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Actions' }));
    await expect(await screen.findByRole('menuitem', { name: 'Rename' })).toBeInTheDocument();
    await expect(screen.getByRole('menuitem', { name: 'Duplicate' })).toBeInTheDocument();
  },
};
