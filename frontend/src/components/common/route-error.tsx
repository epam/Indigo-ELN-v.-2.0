import type { ErrorComponentProps } from '@tanstack/react-router';
import { Link, useRouter } from '@tanstack/react-router';

import { Button, buttonVariants } from '@/components/ui/button';
import { describeError } from '@/lib/toast';

/**
 * Rendered in place of a route that threw while rendering.
 *
 * Wired as the router's `defaultErrorComponent` (main.tsx) rather than the root route's
 * `errorComponent`, for two reasons. TanStack only mounts a boundary for a route that
 * actually has one — `Match.js`: `routeErrorComponent ? CatchBoundary : SafeFragment` — so
 * with neither option set a render throw escapes React's root and takes the whole app down
 * to a blank page; the library's own `ErrorComponent` is the boundary's internal fallback
 * and never gets the chance to render. And setting the default gives *every* route its own
 * boundary, which keeps the failure local: an error inside /projects renders here, in the
 * Outlet, leaving AppShell's header and sidebar standing so the user can navigate away.
 *
 * React logs the error itself, so this does not log it again.
 */
function RouteError({ error, reset }: ErrorComponentProps) {
  const router = useRouter();
  // The same wording the toasts use, and it copes with a non-Error throw.
  const [message] = describeError(error);

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-4 p-8 text-center">
      <h1 className="text-[18px]/7 font-semibold text-neutral-1000">Something went wrong</h1>
      <p className="max-w-prose text-[14px]/6 text-neutral-700">{message}</p>
      <div className="flex gap-3">
        <Button
          size="lg"
          onClick={() => {
            // Both: `invalidate` re-runs the loaders that may have produced the bad state,
            // `reset` clears the boundary so the route is rendered again.
            void router.invalidate();
            reset();
          }}
        >
          Try again
        </Button>
        {/*
          Styled with `buttonVariants` rather than rendered through <Button>: Base UI stamps
          role="button" on whatever it renders, even with `nativeButton={false}`, which would
          strip this of its link semantics — it navigates, so it should be announced as a link.
        */}
        <Link to="/" className={buttonVariants({ variant: 'secondary', size: 'lg' })}>
          Back to start
        </Link>
      </div>
    </div>
  );
}

export { RouteError };
