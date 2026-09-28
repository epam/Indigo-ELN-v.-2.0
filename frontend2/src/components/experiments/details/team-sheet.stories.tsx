import { useState } from 'react';
import { expect, screen, userEvent } from 'storybook/test';

import { TeamSheet } from '@/components/experiments/details/team-sheet';
import { Button } from '@/components/ui/button';
import { PROJECT_ACL } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ACLEntry } from '@/lib/types/common.ts';

/**
 * The sheet is driven by its opener, as it is on the experiment screen — so the story ships the
 * opener too rather than pinning `open` and losing the transition.
 */
function TeamSheetWithTrigger({ acl, canManage }: { acl: ACLEntry[]; canManage: boolean }) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setOpen(true)}>Team</Button>
      <TeamSheet open={open} onOpenChange={setOpen} acl={acl} canManage={canManage} />
    </>
  );
}

const meta = {
  title: 'Experiments/Details/TeamSheet',
  component: TeamSheetWithTrigger,
  args: { acl: PROJECT_ACL, canManage: false },
} satisfies Meta<typeof TeamSheetWithTrigger>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Read-only: no add row, and every level reads as text rather than a menu. */
export const ReadOnly: Story = {
  play: async () => {
    await userEvent.click(await screen.findByRole('button', { name: 'Team' }));

    // Portalled, so `screen` rather than `within(canvasElement)`.
    await expect(await screen.findByRole('heading', { name: 'Team' })).toBeInTheDocument();
    await expect(screen.getByText(PROJECT_ACL[0].displayName)).toBeInTheDocument();
    await expect(screen.queryByLabelText('Add team members')).not.toBeInTheDocument();
  },
};

/** With the mutations left out, `canManage` still shows the add row — it just cannot fire. */
export const Manageable: Story = {
  args: { canManage: true },
  play: async () => {
    await userEvent.click(await screen.findByRole('button', { name: 'Team' }));
    await expect(await screen.findByLabelText('Add team members')).toBeInTheDocument();
  },
};
