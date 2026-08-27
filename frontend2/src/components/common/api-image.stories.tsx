import { http, HttpResponse } from 'msw';
import { expect, waitFor, within } from 'storybook/test';

import { ApiImage } from '@/components/common/api-image';
import { REACTION_SCHEME_SVG } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const PATH = '/api/eln/experiments/11111111-1111-4111-8111-111111111111/picture?revision=3';

/**
 * Counts requests, so a story can assert one was *not* made. The repo's other "prove it
 * wasn't called" techniques (vi.mock of apiFetch, vi.stubGlobal on fetch) sit below MSW and
 * only work in jsdom; in browser mode the count has to come from the handler itself.
 */
let requests = 0;

const countingHandlers = [
  http.get('/api/eln/experiments/:id/picture', () => {
    requests += 1;
    return HttpResponse.text(REACTION_SCHEME_SVG, { headers: { 'Content-Type': 'image/svg+xml' } });
  }),
];

function ApiImageHarness({ spacer = false }: { spacer?: boolean }) {
  return (
    <div className="p-4">
      {/* Taller than the viewport plus the 200px root margin, so the image starts unseen. */}
      {spacer && <div style={{ height: '200vh' }} className="bg-neutral-100" />}
      <ApiImage path={PATH} alt="Reaction scheme" className="h-[88px] w-[140px]" />
    </div>
  );
}

const meta = {
  title: 'Common/ApiImage',
  component: ApiImageHarness,
  parameters: { layout: 'fullscreen', msw: { handlers: countingHandlers } },
  // Not in the harness: resetting a module variable while rendering is a render side
  // effect, which the React Compiler lint rules reject.
  beforeEach: () => {
    requests = 0;
  },
  args: {},
} satisfies Meta<typeof ApiImageHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** In view on mount, so it loads straight away. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByAltText('Reaction scheme')).toBeInTheDocument();
    await expect(requests).toBe(1);
  },
};

/** The point of the component: nothing is fetched until it is scrolled to. */
export const LoadsOnlyWhenScrolledTo: Story = {
  args: { spacer: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    // Give a request time to happen if it were going to — asserting immediately would pass
    // even against an eager component.
    await new Promise((resolve) => setTimeout(resolve, 500));
    await expect(canvas.queryByAltText('Reaction scheme')).not.toBeInTheDocument();
    await expect(requests).toBe(0);

    // Re-scrolled on every attempt: a single scroll can land before the frame it needs to
    // reveal is laid out, and scrolling again is a no-op once already at the bottom.
    await waitFor(
      () => {
        canvasElement.ownerDocument.documentElement.scrollTo({ top: 999_999 });
        expect(canvas.getByAltText('Reaction scheme')).toBeInTheDocument();
      },
      { timeout: 10_000 },
    );
    await expect(requests).toBe(1);
  },
};

/** A failed request marks the frame; `apiFetch` has already toasted the reason. */
export const FetchFailed: Story = {
  parameters: {
    msw: { handlers: [http.get('/api/eln/experiments/:id/picture', () => new HttpResponse(null, { status: 404 }))] },
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await expect(await canvas.findByLabelText('Reaction scheme could not be loaded')).toBeInTheDocument();
    await expect(canvas.queryByAltText('Reaction scheme')).not.toBeInTheDocument();
  },
};
