import { expect, screen, userEvent, waitFor, within } from 'storybook/test';
import { http, HttpResponse } from 'msw';

import { SignatureRow } from '@/components/signatures/signature-row';
import { makeSignatureDocument, makeUserRef, SIGNATURE_DOCUMENTS } from '@/mocks/fixtures';
import { handlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** What the Approves handler recorded, so its play function can assert on the request itself. */
const signed: { id: string; keystore: string; password: string }[] = [];

const meta = {
  title: 'Signatures/SignatureRow',
  component: SignatureRow,
  args: { item: makeSignatureDocument() },
} satisfies Meta<typeof SignatureRow>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The default fixture: a rejected author beside a witness who has not acted. */
export const RejectedAndPending: Story = {};

export const AllApproved: Story = { args: { item: SIGNATURE_DOCUMENTS[1] } };

/** The signed-in user's own block is still open, so it offers Approve and Reject. */
export const CanSign: Story = { args: { item: SIGNATURE_DOCUMENTS[2] } };

export const LongName: Story = {
  args: {
    item: makeSignatureDocument({
      name: 'Palladium-Catalysed Cross-Coupling Route Scouting for Intermediate B-17, version 12',
    }),
  },
};

/** Six signers, to check the column keeps its shape as the list grows. */
export const ManySigners: Story = {
  args: {
    item: makeSignatureDocument({
      signatures: ['Administrator', 'Mark Liu', 'Ana Ruiz', 'Tom Becker', 'Priya Nair', 'Lena Fischer'].map(
        (name, index) => ({
          id: `signer-${index}`,
          user: makeUserRef(name),
          reason: index === 0 ? ('AUTHOR' as const) : ('WITNESS' as const),
          status: index < 2 ? ('APPROVED' as const) : ('WAITING' as const),
          canSignOrReject: false,
        }),
      ),
    }),
  },
};

/** Rows are read as a stack — this is the whole list. */
export const Stacked: Story = {
  render: () => (
    <div className="flex flex-col gap-3">
      {SIGNATURE_DOCUMENTS.map((document) => (
        <SignatureRow key={document.id} item={document} />
      ))}
    </div>
  ),
};

/**
 * Approving posts to the signature service. The assertion is on the request reaching the spy, not
 * on the re-render — a story should not wait on the far end of a chain it does not own.
 */
export const Approves: Story = {
  args: { item: SIGNATURE_DOCUMENTS[2] },
  parameters: {
    msw: {
      handlers: [
        http.post('/api/signature/documents/:id/sign', async ({ params, request }) => {
          const form = await request.formData();
          signed.push({
            id: String(params.id),
            keystore: (form.get('keystore') as File).name,
            password: String(form.get('password')),
          });
          return HttpResponse.json(SIGNATURE_DOCUMENTS[2]);
        }),
        ...handlers,
      ],
    },
  },
  play: async ({ canvasElement }) => {
    signed.length = 0;
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Approve' }));
    // The dialog is portalled out of the canvas.
    const dialog = within(await screen.findByRole('dialog'));
    await expect(dialog.getByRole('button', { name: 'Approve' })).toBeDisabled();
    await userEvent.upload(dialog.getByLabelText(/Keystore File/), new File(['key'], 'john.p12'));
    await userEvent.type(dialog.getByLabelText('Keystore Password'), 'secret');
    await userEvent.click(dialog.getByRole('button', { name: 'Approve' }));
    await waitFor(() =>
      expect(signed).toEqual([{ id: SIGNATURE_DOCUMENTS[2].id, keystore: 'john.p12', password: 'secret' }]),
    );
  },
};
