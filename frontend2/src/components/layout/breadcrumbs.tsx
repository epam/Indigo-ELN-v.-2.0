import { Link, type LinkProps } from '@tanstack/react-router';
import { Fragment } from 'react';

export type BreadcrumbItem = { label: string; link?: LinkProps };

/**
 * The last item is the current page — always plain text, never a link.
 *
 * The trail gives way from the left when the row is tight. `All Projects` is short and fixed so
 * it never shrinks; the ancestors carry an outsized `shrink` factor so they absorb nearly all of
 * the squeeze before the current page starts to clip — flex distributes shrinkage in proportion
 * to factor × basis, so an even factor would clip the page's own name alongside its ancestors'.
 *
 * The experiment header is what needs this: four levels plus a status, a team and three buttons
 * in one row, and the label that has to stay readable is the experiment's own name at the end.
 */
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
              <li className={index === 0 ? 'shrink-0' : isCurrent ? 'min-w-0' : 'min-w-0 shrink-[100]'}>
                {isCurrent ? (
                  <span aria-current="page" title={label} className="block truncate text-neutral-1000">
                    {label}
                  </span>
                ) : link ? (
                  // title, because a squeezed trail clips these first and the name is the point.
                  <Link {...link} title={label} className="block truncate text-blue-400">
                    {label}
                  </Link>
                ) : (
                  <span className="block truncate">{label}</span>
                )}
              </li>
            </Fragment>
          );
        })}
      </ol>
    </nav>
  );
}
