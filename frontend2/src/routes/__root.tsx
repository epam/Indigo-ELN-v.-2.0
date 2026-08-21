import { createRootRoute, HeadContent, Link, Outlet } from '@tanstack/react-router';
import { lazy, Suspense } from 'react';

import { Button } from '@/components/ui/button';

const Devtools = import.meta.env.DEV
  ? lazy(async () => {
      const [{ TanStackRouterDevtools }, { ReactQueryDevtools }] = await Promise.all([
        import('@tanstack/react-router-devtools'),
        import('@tanstack/react-query-devtools'),
      ]);
      return {
        default: () => (
          <>
            <TanStackRouterDevtools position="bottom-right" />
            <ReactQueryDevtools buttonPosition="bottom-left" />
          </>
        ),
      };
    })
  : null;

export const Route = createRootRoute({
  component: RootComponent,
  notFoundComponent: NotFound,
});

function RootComponent() {
  return (
    <>
      <HeadContent />
      <Outlet />
      {Devtools && (
        <Suspense>
          <Devtools />
        </Suspense>
      )}
    </>
  );
}

function NotFound() {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4">
      <h1 className="text-2xl font-semibold">Page not found</h1>
      {/* Renders an <a>, so Base UI must not apply native <button> semantics. */}
      <Button nativeButton={false} render={<Link to="/" />}>
        Back to start
      </Button>
    </div>
  );
}
