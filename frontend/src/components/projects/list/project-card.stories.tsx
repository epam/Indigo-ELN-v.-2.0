import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ProjectCard } from '@/components/projects/list/project-card';
import { makeAclEntry, makeProject, makeProjectDetails } from '@/mocks/fixtures';
import { handlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/List/ProjectCard',
  component: ProjectCard,
  args: { item: makeProject() },
  decorators: [
    (Story) => (
      <div className="w-[320px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ProjectCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The title truncates rather than wrapping, keeping every card the same height. */
export const LongName: Story = {
  args: {
    item: makeProject({
      name: 'Palladium-Catalysed Cross-Coupling Route Scouting for Intermediate B-17',
    }),
  },
};

export const NoExperiments: Story = {
  args: {
    item: makeProject({ notebookCount: 0, experimentCount: 0, experimentCountByStatus: {} }),
  },
};

/** acl is capped by the backend, so the stack ends in a +N badge. */
export const ManyCollaborators: Story = {
  args: {
    item: makeProject({
      acl: [makeAclEntry('Administrator'), makeAclEntry('Mark Liu'), makeAclEntry('Sofia Rossi')],
      aclCount: 12,
    }),
  },
};

/** Records the PATCH bodies that reached the wire. */
const patched: unknown[] = [];
/** Counts the detail GETs the menu makes, so a story can wait on the answer rather than a clock. */
let detailsServed = 0;

const renameHandlers = [
  http.patch('/api/eln/projects/:id', async ({ params, request }) => {
    const body = (await request.json()) as { name: string };
    patched.push(body);
    return HttpResponse.json(makeProjectDetails({ id: String(params.id), name: body.name }));
  }),
  ...handlers,
];

/**
 * Rename asks the project itself whether it may — the list carries no permissions — so opening
 * the menu fetches the detail, and Rename is live once it grants `EDIT_PROJECTS`. The dialog
 * PATCHes the name and nothing else.
 */
export const Renames: Story = {
  parameters: { msw: { handlers: renameHandlers } },
  play: async ({ canvasElement }) => {
    patched.length = 0;
    await userEvent.click(within(canvasElement).getByRole('button', { name: /^Actions for / }));

    const rename = await screen.findByRole('menuitem', { name: 'Rename' });
    await waitFor(() => expect(rename).not.toHaveAttribute('aria-disabled', 'true'));
    await userEvent.click(rename);

    const dialog = await screen.findByRole('dialog', { name: 'Rename Project' });
    const input = within(dialog).getByLabelText(/Project Name/);
    await userEvent.clear(input);
    await userEvent.type(input, 'Renamed Project');

    const save = within(dialog).getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);

    await waitFor(() => expect(patched).toEqual([{ name: 'Renamed Project' }]));
    await waitFor(() => expect(screen.queryByRole('dialog', { name: 'Rename Project' })).not.toBeInTheDocument());
  },
};

/**
 * Without `EDIT_PROJECTS` on this project the item is there but cannot be chosen, and says why
 * on hover. Waits on the detail having been served, not on a timer.
 */
export const RenameNotAllowed: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('/api/eln/projects/:id', ({ params }) => {
          detailsServed += 1;
          return HttpResponse.json(
            makeProjectDetails({ id: String(params.id), currentPermissions: ['VIEW_PROJECTS'] }),
          );
        }),
        ...handlers,
      ],
    },
  },
  play: async ({ canvasElement }) => {
    detailsServed = 0;
    await userEvent.click(within(canvasElement).getByRole('button', { name: /^Actions for / }));
    const rename = await screen.findByRole('menuitem', { name: 'Rename' });

    await waitFor(() => expect(detailsServed).toBe(1));
    await waitFor(() => expect(rename).toHaveAttribute('title', 'You do not have permission to rename this project'));
    await expect(rename).toHaveAttribute('aria-disabled', 'true');
  },
};

/** While the detail is on its way, Rename is disabled and its icon spins in place. */
export const RenameWhilePermissionLoads: Story = {
  parameters: {
    msw: {
      handlers: [http.get('/api/eln/projects/:id', () => new Promise(() => {})), ...handlers],
    },
  },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: /^Actions for / }));
    const rename = await screen.findByRole('menuitem', { name: 'Rename' });
    await expect(rename).toHaveAttribute('aria-disabled', 'true');
    await expect(rename.querySelector('.animate-spin')).not.toBeNull();
  },
};
