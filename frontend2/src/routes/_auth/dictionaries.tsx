import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_auth/dictionaries')({
  component: () => <h1 className="text-[16px]/6 font-semibold">Dictionaries — coming soon</h1>,
});
