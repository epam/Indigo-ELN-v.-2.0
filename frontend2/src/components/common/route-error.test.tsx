import {
  createMemoryHistory,
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router';
import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { RouteError } from '@/components/common/route-error';

function Boom(): never {
  throw new Error('kaboom');
}

function routerThatThrows(withErrorComponent: boolean) {
  const rootRoute = createRootRoute();
  const indexRoute = createRoute({ getParentRoute: () => rootRoute, path: '/', component: Boom });

  return createRouter({
    routeTree: rootRoute.addChildren([indexRoute]),
    history: createMemoryHistory({ initialEntries: ['/'] }),
    ...(withErrorComponent ? { defaultErrorComponent: RouteError } : {}),
  });
}

describe('RouteError', () => {
  it('stands in for a route that threw, instead of blanking the app', async () => {
    // React logs a caught render error; the boundary working is the point, not the noise.
    vi.spyOn(console, 'error').mockImplementation(() => {});

    render(<RouterProvider router={routerThatThrows(true)} />);

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
    // Worded by describeError, and it offers a way out rather than a dead end.
    expect(screen.getByText('kaboom')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to start' })).toBeInTheDocument();

    vi.mocked(console.error).mockRestore();
  });

  it('is the only thing standing between a render throw and a blank page', () => {
    // Pins the mechanism the fix relies on. TanStack mounts a boundary only for a route
    // that has an error component (`routeErrorComponent ? CatchBoundary : SafeFragment`),
    // so with none configured React unmounts the tree and renders literally nothing —
    // which is what production used to do.
    vi.spyOn(console, 'error').mockImplementation(() => {});

    const { container } = render(<RouterProvider router={routerThatThrows(false)} />);
    expect(container).toBeEmptyDOMElement();

    vi.mocked(console.error).mockRestore();
  });
});
