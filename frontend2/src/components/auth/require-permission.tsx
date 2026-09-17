import { Navigate } from '@tanstack/react-router';
import type { ReactNode } from 'react';

import { useHasPermission } from '@/lib/api/user';
import type { ApplicationPermission } from '@/lib/types/user';

/**
 * Gates a route on an application permission. Mirrors the Angular roleGuard
 * (indigo-frontend/src/app/role.guard.ts), which redirects to '/' rather than
 * showing a denial.
 */
export function RequirePermission({
  permission,
  children,
}: {
  permission: ApplicationPermission;
  children: ReactNode;
}) {
  const allowed = useHasPermission(permission);

  // Still resolving — render nothing rather than redirect on an unknown answer.
  if (allowed === undefined) return null;
  // replace: the forbidden URL stays out of history, so Back does not bounce into it.
  if (!allowed) return <Navigate to="/" replace />;
  return children;
}
