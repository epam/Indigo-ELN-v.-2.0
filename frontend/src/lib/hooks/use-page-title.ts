import { useEffect } from 'react';

/**
 * Sets the tab title to "Indigo ELN - a - b"; falsy parts (an entity still loading) are dropped.
 *
 * Writes `document.title` rather than rendering a `<title>` or using the route's `head`: both
 * add a second element after the static one in index.html, and the browser reads the first.
 * One caller per page — effects run child-first, so a layout would overwrite its own tab.
 */
export function usePageTitle(...parts: (string | false | undefined)[]) {
  const title = ['Indigo ELN', ...parts.filter(Boolean)].join(' - ');

  useEffect(() => {
    document.title = title;
  }, [title]);
}
