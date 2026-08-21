import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_auth/users')({
  component: () => <h1 className="text-[16px]/6 font-semibold">Users — coming soon</h1>,
});
