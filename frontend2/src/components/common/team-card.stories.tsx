import {expect, screen, userEvent, waitFor, within} from 'storybook/test';

import {TeamCard} from '@/components/common/team-card';
import {useProject, useUpdateProjectAccess} from '@/lib/api/projects';
import {makeAclEntry, PROJECT_ACL} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';
import type {ACLEntry} from '@/lib/types/common.ts';

const PROJECT_ID = '11111111-1111-1111-1111-111111111111';

/**
 * `TeamCard` takes its two mutations as props, so every story needs a wrapper that makes
 * them. This one binds them to a project, which is what the project route does.
 */
function ProjectTeamCard({ acl, canManage }: { acl: ACLEntry[]; canManage: boolean }) {
  const addMembers = useUpdateProjectAccess(PROJECT_ID);
  const changeLevel = useUpdateProjectAccess(PROJECT_ID);

  return <TeamCard acl={acl} canManage={canManage} addMembers={addMembers} changeLevel={changeLevel} />;
}

/**
 * The access mutation updates the cached project rather than any local state — so a story
 * that passes `acl` as a fixed prop can never show the result of a change. The interactive
 * stories below read the ACL back out of the cache instead, which is also what makes them a
 * real test of the round trip.
 */
function TeamCardFromCache() {
  const { data } = useProject(PROJECT_ID);
  return data ? <ProjectTeamCard acl={data.acl} canManage /> : null;
}

const meta = {
  title: 'Common/TeamCard',
  component: ProjectTeamCard,
  args: { acl: PROJECT_ACL, canManage: true },
} satisfies Meta<typeof ProjectTeamCard>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Mixed levels, plus one inherited entry and the immutable AUTHOR row. */
export const Default: Story = {};

/** Without MANAGE_PROJECT_ACCESS the add field and every level menu are gone. */
export const ReadOnly: Story = {
  args: { canManage: false },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByLabelText('Add team members')).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: /Admin/ })).not.toBeInTheDocument();
    // The members themselves are still listed, with their levels as plain text.
    await expect(canvas.getByText('Mark Liu')).toBeInTheDocument();
    await expect(canvas.getByText('Admin')).toBeInTheDocument();
  },
};

/** AUTHOR is the creator's own entry; the backend rejects any change, so it is never a menu. */
export const AuthorIsNotEditable: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('Author')).toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: /Author/ })).not.toBeInTheDocument();
  },
};

/**
 * The POST answers with the whole recomputed ACL, which replaces `acl` in the cache — so the
 * row reads back at its new level without a refetch. The menu is portalled, so `screen`
 * rather than the canvas finds its items.
 */
export const ChangeLevel: Story = {
  render: () => <TeamCardFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // Sofia Rossi is already EDIT, so counting is the way to see Tom Becker join her.
    await expect(await canvas.findAllByRole('button', { name: /Can Edit/ })).toHaveLength(1);

    await userEvent.click(canvas.getByRole('button', { name: /Can View/ }));
    await userEvent.click(await screen.findByRole('menuitem', { name: 'Can Edit' }));

    await waitFor(() => expect(canvas.queryByRole('button', { name: /Can View/ })).not.toBeInTheDocument());
    await expect(canvas.getAllByRole('button', { name: /Can Edit/ })).toHaveLength(2);
  },
};

/** Suggestions come from /users/suggest and drop anyone already on the team. */
export const AddMember: Story = {
  render: () => <TeamCardFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = await canvas.findByLabelText('Add team members');
    await expect(canvas.getByRole('button', { name: 'Add Member' })).toBeDisabled();

    // Sofia Rossi is already a member, so she is filtered out of her own match.
    await userEvent.type(input, 'Sofia');
    await expect(await screen.findByText('No matching users')).toBeInTheDocument();

    await userEvent.clear(input);
    await userEvent.type(input, 'Nils');
    await userEvent.click(await screen.findByRole('option', { name: 'Nils Berg' }));

    await expect(canvas.getByRole('button', { name: 'Add Member' })).toBeEnabled();
    await userEvent.click(canvas.getByRole('button', { name: 'Add Member' }));

    // The chip clearing is the one signal that the POST resolved — the name alone is not,
    // since the chip carries it too, and would match before the request had even been sent.
    await waitFor(() => expect(canvas.queryByRole('button', { name: 'Remove Nils Berg' })).not.toBeInTheDocument());
    // Left standing: the new entry, off the recomputed ACL the POST answered with.
    await expect(canvas.getByText('Nils Berg')).toBeInTheDocument();
  },
};

/**
 * The columns hold their positions whatever the values are: long text clips with an ellipsis
 * rather than shoving the row out of alignment, and `(Inherited)` sits outside the truncating
 * span so a long name never costs you the marker.
 */
export const LongValues: Story = {
  args: {
    acl: [
      makeAclEntry('Administrator', { level: 'AUTHOR' }),
      makeAclEntry('Bartholomew Fitzgerald-Wetherington III', {
        level: 'EDIT',
        inherited: true,
        username: 'bartholomew.fitzgerald.wetherington@research.example.com',
      }),
      // The widest level label, against the shortest name.
      makeAclEntry('Ana Li', { level: 'IMPLICIT_VIEW' }),
      makeAclEntry('Mark Liu', { level: 'ADMIN' }),
    ],
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // The full address has to stay reachable once the column clips it.
    await expect(canvas.getByTitle('bartholomew.fitzgerald.wetherington@research.example.com')).toBeInTheDocument();
    // Clipped, not dropped: the marker is a sibling of the truncating name, not inside it.
    await expect(canvas.getByText('(Inherited)')).toBeVisible();
  },
};
