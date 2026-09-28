import { createFileRoute, useRouter } from '@tanstack/react-router';

import { LoginCard } from '@/components/auth/login-card';
import { z } from '@/lib/zod';

export const Route = createFileRoute('/login')({
  validateSearch: z.object({
    redirect: z.string().optional(),
  }),
  head: () => ({ meta: [{ title: 'Indigo ELN - Log in' }] }),
  component: LoginPage,
});

function LoginPage() {
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
