import { http, HttpResponse } from 'msw';
import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ProjectFormDialog } from '@/components/projects/project-form-dialog';
import { makeProjectDetails } from '@/mocks/fixtures';
import {
  createProjectErrorHandlers,
  handlers,
  keywordErrorHandlers,
  slowKeywordHandlers,
  submittingProjectHandlers,
  TAKEN_PROJECT_NAME,
} from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ProjectDetails } from '@/lib/types/projects.ts';

function ProjectFormDialogHarness({ project }: { project?: ProjectDetails }) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <ProjectFormDialog open={open} onOpenChange={setOpen} project={project} />
    </>
  );
}

/** Records what actually reached the wire, so a story can assert an untouched Save sends {}. */
const patchBodies: unknown[] = [];

const patchSpyHandlers = [
  http.patch('/api/eln/projects/:id', async ({ request, params }) => {
    patchBodies.push(await request.json());
    return HttpResponse.json(makeProjectDetails({ id: String(params.id) }));
  }),
  ...handlers,
];

const meta = {
  title: 'Projects/ProjectFormDialog',
  component: ProjectFormDialogHarness,
} satisfies Meta<typeof ProjectFormDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The name is checked against /projects/existence as it is typed. */
export const DuplicateName: Story = {
  play: async () => {
    await userEvent.type(screen.getByLabelText(/Project Name/), TAKEN_PROJECT_NAME);
    await waitFor(
      () =>
        expect(screen.getByRole('alert')).toHaveTextContent(`Project with name '${TAKEN_PROJECT_NAME}' already exists`),
      { timeout: 3000 },
    );
  },
};

/** Blanking a required field reports it rather than silently failing on submit. */
export const RequiredName: Story = {
  play: async () => {
    const input = screen.getByLabelText(/Project Name/);
    await userEvent.type(input, 'A');
    await userEvent.clear(input);
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Project Name is required'));
  },
};

/** Keywords come from /projects/keywords/suggest and render as removable chips. */
export const KeywordSuggestions: Story = {
  play: async () => {
    await userEvent.click(screen.getByLabelText('Project Keywords'));
    await userEvent.keyboard('sul');
    await waitFor(() => expect(screen.getByRole('option', { name: 'sulfonamide' })).toBeInTheDocument(), {
      timeout: 3000,
    });

    await userEvent.keyboard('{ArrowDown}{Enter}');
    await waitFor(() => expect(screen.getByLabelText('Remove sulfonamide')).toBeInTheDocument());
  },
};

/** A failed POST leaves the dialog open with the typed values still there. */
export const ServerError: Story = {
  parameters: { msw: { handlers: createProjectErrorHandlers } },
  play: async () => {
    await userEvent.type(screen.getByLabelText(/Project Name/), 'Novel Route Scouting');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    // apiFetch reports the failure, and nothing the user typed is lost.
    await waitFor(() => expect(screen.getByText('name: Project could not be created')).toBeInTheDocument());
    await expect(screen.getByRole('heading', { name: 'Add Project' })).toBeInTheDocument();
    await expect(screen.getByLabelText(/Project Name/)).toHaveValue('Novel Route Scouting');
  },
};

/** Save shows its spinner for as long as the request is in flight. */
export const Submitting: Story = {
  parameters: { msw: { handlers: submittingProjectHandlers } },
  play: async () => {
    await userEvent.type(screen.getByLabelText(/Project Name/), 'Novel Route Scouting');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toHaveAttribute('aria-busy', 'true'));
  },
};

/**
 * Ctrl+Enter submits from inside the description editor, where a plain Enter would only
 * start a new paragraph.
 */
export const CtrlEnterFromRichText: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.type(screen.getByLabelText(/Project Name/), 'Novel Route Scouting');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());

    const description = screen.getByRole('textbox', { name: 'Project Description' });
    await userEvent.click(description);
    await userEvent.keyboard('Screening cascade');
    // A plain Enter belongs to the editor, so the dialog stays open.
    await userEvent.keyboard('{Enter}');
    await expect(screen.getByRole('heading', { name: 'Add Project' })).toBeInTheDocument();

    await userEvent.keyboard('{Control>}{Enter}{/Control}');
    await waitFor(() => expect(within(canvasElement).getByText('Dialog closed.')).toBeInTheDocument());
  },
};

/** Focusing Keywords with an empty box must not claim there are "no matching keywords". */
export const NoKeywordMessageBeforeTyping: Story = {
  play: async () => {
    await userEvent.click(screen.getByLabelText('Project Keywords'));
    await userEvent.click(screen.getByLabelText('Show suggestions'));

    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
    await expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  },
};

/**
 * The reported bug: while suggestions load, the popup used to claim there was nothing to
 * find — it offered "Press Enter to add" before a request had even been sent.
 */
export const KeywordsShowLoading: Story = {
  parameters: { msw: { handlers: slowKeywordHandlers } },
  play: async () => {
    await userEvent.click(screen.getByLabelText('Project Keywords'));
    await userEvent.keyboard('sul');

    await waitFor(() => expect(screen.getByText('Searching…')).toBeInTheDocument());
    await expect(screen.queryByText(/Press Enter to add/)).not.toBeInTheDocument();
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();

    // …and it resolves into real suggestions once the request lands.
    await waitFor(() => expect(screen.getByRole('option', { name: 'sulfonamide' })).toBeInTheDocument(), {
      timeout: 3000,
    });
    await expect(screen.queryByText('Searching…')).not.toBeInTheDocument();
  },
};

/** A failed keyword lookup explains itself instead of looking like an empty result. */
export const KeywordsFailToLoad: Story = {
  parameters: { msw: { handlers: keywordErrorHandlers } },
  play: async () => {
    await userEvent.click(screen.getByLabelText('Project Keywords'));
    await userEvent.keyboard('sul');

    await waitFor(() => expect(screen.getByText('Could not load suggestions')).toBeInTheDocument(), {
      timeout: 3000,
    });
    await expect(screen.queryByText('No matching keywords')).not.toBeInTheDocument();
    // The keyword can still be added by hand.
    await expect(screen.getByText('Press Enter to add “sul”')).toBeInTheDocument();
  },
};

/**
 * **Regression guard**, the mirror of the notebook dialog's. Both rich-text fields are seeded
 * with values the editor has to normalise, and Save without an edit must send an empty body —
 * it used to send normalised copies of both, which can clobber someone else's concurrent edit.
 */
export const UntouchedSaveSendsNothing: Story = {
  parameters: { msw: { handlers: patchSpyHandlers } },
  render: () => (
    <ProjectFormDialogHarness
      project={makeProjectDetails({ literature: 'Smith 2025.', description: '<div>Wrapped.</div>' })}
    />
  ),
  play: async () => {
    patchBodies.length = 0;
    await userEvent.click(await screen.findByRole('button', { name: 'Save' }));
    await waitFor(() => expect(patchBodies).toEqual([{}]));
  },
};
