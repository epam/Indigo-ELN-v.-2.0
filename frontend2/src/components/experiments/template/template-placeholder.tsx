import type { ReactNode } from 'react';

/**
 * Stands in for a template component that is not built yet. Every one of the six uses it, so the
 * screen is reviewable as a whole while each body is filled in as its own task.
 *
 * Deliberately plain: a placeholder that mimics the real controls invites review comments about
 * a thing that does not exist.
 */
export function TemplatePlaceholder({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-24 items-center justify-center rounded-6 border border-dashed border-neutral-300 px-4 py-6 text-center text-[14px]/6 text-neutral-700">
      {children}
    </div>
  );
}
