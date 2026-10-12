import type { RefObject } from 'react';
import { useEffect, useLayoutEffect } from 'react';

/** The custom property a name's measured width is published under, on the container. */
function widthProperty(name: string): string {
  return `--equal-width-${name}`;
}

/**
 * The `min-width` of an element taking part: the widest of its peers, and never under `floor`.
 * The fallback is what leaves it at its natural width while it is being measured.
 */
export function equalWidth(name: string, floor = 0): string {
  return `max(${floor}px, var(${widthProperty(name)}, 0px))`;
}

/**
 * Clears the published widths, reads every marked element at its natural width, and publishes
 * the maximum per name. Clearing first is what lets the set narrow again; without it an element
 * would only ever report the width it was last given.
 */
function measure(container: HTMLElement, names: readonly string[]) {
  for (const name of names) container.style.removeProperty(widthProperty(name));
  const widths = names.map((name) =>
    Math.max(
      0,
      ...[...container.querySelectorAll(`[data-equal-width="${name}"]`)].map(
        (element) => element.getBoundingClientRect().width,
      ),
    ),
  );
  names.forEach((name, index) => {
    container.style.setProperty(widthProperty(name), `${Math.ceil(widths[index])}px`);
  });
}

/**
 * Makes every element marked `data-equal-width="<name>"` under the container as wide as the
 * widest of them — what a table column does for its cells, for boxes that are not in one.
 *
 * Measured after every render, before paint, because the marked elements come and go with the
 * rows that hold them — and again when a webfont lands, which changes every text width without
 * rendering anything.
 *
 * Deliberately not a `ResizeObserver` on the marked elements: a pass resizes the very elements
 * it would be observing, which the browser reports as a loop error on every real change.
 *
 * jsdom lays nothing out, so anything using this has to be covered by a browser-mode story
 * rather than a unit test.
 */
export function useEqualWidths(containerRef: RefObject<HTMLElement | null>, names: readonly string[]): void {
  useLayoutEffect(() => {
    if (containerRef.current) measure(containerRef.current, names);
  });

  useEffect(() => {
    const onFontsLoaded = () => {
      if (containerRef.current) measure(containerRef.current, names);
    };
    // `?.` for jsdom, which has no `document.fonts` — and no layout to measure either.
    document.fonts?.addEventListener('loadingdone', onFontsLoaded);
    return () => document.fonts?.removeEventListener('loadingdone', onFontsLoaded);
  }, [containerRef, names]);
}
