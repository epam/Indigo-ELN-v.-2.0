import type { RefObject } from 'react';
import { useEffect, useRef, useState } from 'react';

/**
 * A screenful of lead time, so an image is usually already there by the time it is looked
 * at. indigo-frontend's IsInViewportDirective defaulted to '0px', which starts the fetch at
 * the exact moment of entry and so guarantees the reader watches it load.
 */
const DEFAULT_ROOT_MARGIN = '200px';

/**
 * Whether the referenced element has been scrolled near the viewport yet. Latches: once
 * true it never goes back, so a caller can gate work that should happen once and stay done.
 *
 * The observer is disconnected as soon as it fires — the effect re-runs on the state change
 * and returns before constructing a new one — which is the same contract as the Angular
 * directive's `triggerOnce`. Re-observing would cost a fetch every time a row scrolled out
 * and back, for an answer that cannot change.
 *
 * Note `IntersectionObserver` does not exist in jsdom, so anything using this has to be
 * covered by a browser-mode story rather than a unit test.
 */
export function useInViewport(rootMargin = DEFAULT_ROOT_MARGIN): [RefObject<HTMLDivElement | null>, boolean] {
  const ref = useRef<HTMLDivElement>(null);
  const [seen, setSeen] = useState(false);

  useEffect(() => {
    const element = ref.current;
    if (!element || seen) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) setSeen(true);
      },
      { rootMargin },
    );
    observer.observe(element);
    return () => observer.disconnect();
  }, [seen, rootMargin]);

  return [ref, seen];
}
