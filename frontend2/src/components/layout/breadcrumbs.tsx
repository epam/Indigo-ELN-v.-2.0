import { Link, type LinkProps } from '@tanstack/react-router';
import { Fragment } from 'react';

export type BreadcrumbItem = { label: string; link?: LinkProps };

/** The last item is the current page — always plain text, never a link. */
export function Breadcrumbs({ items, className }: { items: BreadcrumbItem[]; className?: string }) {
  return (
    <nav aria-label="Breadcrumb" className={className}>
      {/* min-h-9 matches the `lg` Button next to it on the projects page, so the row keeps
          the same height on pages that have no button and breadcrumbs don't jump between them. */}
      <ol className="flex min-h-9 items-center gap-2 text-[16px]/6 font-semibold">
        {items.map(({ label, link }, index) => {
          const isCurrent = index === items.length - 1;

          return (
            <Fragment key={label}>
              {index > 0 && (
                <li aria-hidden="true" className="shrink-0 text-neutral-700">
                  /
                </li>
              )}
              <li className={isCurrent ? 'min-w-0' : 'shrink-0'}>
                {isCurrent ? (
                  <span aria-current="page" className="block truncate text-neutral-1000">
                    {label}
                  </span>
                ) : link ? (
                  <Link {...link} className="text-blue-400">
                    {label}
                  </Link>
                ) : (
                  label
                )}
              </li>
            </Fragment>
          );
        })}
      </ol>
    </nav>
  );
}
