import { createFileRoute, useRouter } from '@tanstack/react-router';

import { LoginCard } from '@/components/auth/login-card';
import { usePageTitle } from '@/lib/hooks/use-page-title';
import { z } from '@/lib/zod';

export const Route = createFileRoute('/login')({
  validateSearch: z.object({
    redirect: z.string().optional(),
  }),
  component: LoginPage,
});

function LoginPage() {
  usePageTitle('Log in');
  const { redirect } = Route.useSearch();
  const router = useRouter();

  return (
    <LoginCard
      // `redirect` is an arbitrary href captured by the _auth guard, not one of the router's
      // known route paths, so it goes through history rather than navigate().
      onSignedIn={() => router.history.replace(redirect ?? '/projects')}
    />
  );
}
