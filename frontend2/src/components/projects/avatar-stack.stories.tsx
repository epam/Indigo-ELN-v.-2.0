import { AvatarStack } from '@/components/projects/avatar-stack';
import { makeAclEntry } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const ACL = [makeAclEntry('Administrator'), makeAclEntry('Mark Liu'), makeAclEntry('Sofia Rossi')];

const meta = {
  title: 'Projects/AvatarStack',
  component: AvatarStack,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof AvatarStack>;

export default meta;
type Story = StoryObj<typeof meta>;

export const One: Story = { args: { acl: ACL.slice(0, 1), aclCount: 1 } };

export const Several: Story = { args: { acl: ACL, aclCount: 3 } };

/** aclCount exceeds the returned acl, so the overflow badge appears. */
export const Overflow: Story = { args: { acl: ACL, aclCount: 12 } };

export const Empty: Story = { args: { acl: [], aclCount: 0 } };
