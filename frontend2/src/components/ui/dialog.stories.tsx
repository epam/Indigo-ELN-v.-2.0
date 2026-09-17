import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';

import type { Meta, StoryObj } from '@storybook/react-vite';

function DialogHarness({ title, body }: { title: string; body: string }) {
  const [open, setOpen] = useState(true);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent
        title={title}
        footer={
          <>
            <DialogClose
              render={
                <Button variant="secondary" size="lg">
                  Cancel
                </Button>
              }
            />
            <Button size="lg">Save</Button>
          </>
        }
      >
        <p className="text-[14px]/6 text-neutral-800">{body}</p>
      </DialogContent>
    </Dialog>
  );
}

const meta = {
  title: 'UI/Dialog',
  component: DialogHarness,
  args: { title: 'Add Project', body: 'The dialog body scrolls; the header and footer stay put.' },
} satisfies Meta<typeof DialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Long content scrolls inside the body rather than pushing the footer off-screen. */
export const Scrolling: Story = {
  args: { body: 'Reaction step. '.repeat(400) },
};
