import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_auth/templates')({
  component: () => <h1 className="text-[16px]/6 font-semibold">Templates — coming soon</h1>,
});
