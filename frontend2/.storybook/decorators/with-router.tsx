import {createMemoryHistory, createRootRoute, createRoute, createRouter, RouterProvider,} from '@tanstack/react-router';
import type {ComponentType} from 'react';
import {useState} from 'react';

import type {Decorator} from '@storybook/react-vite';

/**
 * Every `to` the components link to. TanStack Router resolves a Link against the
 * route tree, so a path missing here makes the Link throw rather than render.
 */
const LINK_PATHS = [
  '/',
  '/projects',
  '/projects/$id',
  '/projects/$id/notebooks',
  '/notebooks/$id',
  '/notebooks/$id/experiments',
  '/experiments/$id',
  '/templates',
  '/dictionaries',
  '/signatures',
  '/users',
  '/login',
] as const;

/**
 * Renders the story inside a throwaway memory router. The root route renders the
 * story and never renders an <Outlet />, so the stub children below exist only to
 * make link targets resolvable.
 */
function RouterScope({ Story }: { Story: ComponentType }) {
  const [router] = useState(() => {
    const rootRoute = createRootRoute({ component: () => <Story /> });
    const stubs = LINK_PATHS.map((path) =>
      createRoute({ getParentRoute: () => rootRoute, path, component: () => null }),
    );

    return createRouter({
      routeTree: rootRoute.addChildren(stubs),
      history: createMemoryHistory({ initialEntries: ['/'] }),
    });
  });

  // The stub tree is deliberately not the app's registered route tree.
  return <RouterProvider router={router as never} />;
}

export const withRouter: Decorator = (Story) => <RouterScope Story={Story} />;
