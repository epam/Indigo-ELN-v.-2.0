import { ImageOff } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Skeleton } from '@/components/ui/skeleton';
import { useInViewport } from '@/lib/hooks/use-in-viewport';
import { renderStructure } from '@/lib/ketcher';
import { notifyError } from '@/lib/toast';
import { cn } from '@/lib/utils';

/**
 * A structure the backend has no picture for, rendered in the browser by Ketcher — a PubChem
 * hit's InChI. `ApiImage`'s counterpart, with the same frame and the same lazy start: nothing is
 * rendered until the frame is scrolled near.
 *
 * A failed render marks its own frame and toasts, since unlike `apiFetch` nothing else reports it.
 */
/** The toast id every failed render shares, which is what collapses a burst of them into one. */
const RENDER_FAILED = 'structure-render-failed';

export function StructureImage({
  structure,
  alt,
  className,
}: {
  /** Anything Ketcher reads: a molfile, SMILES or InChI. */
  structure: string;
  alt: string;
  /** Must size the frame, as for `ApiImage`. */
  className?: string;
}) {
  const [ref, seen] = useInViewport();
  // Keyed by the structure, so a render that lands late for a replaced one is ignored.
  const [rendered, setRendered] = useState<{ structure: string; url: string | null } | null>(null);

  useEffect(() => {
    if (!seen) return;
    let current = true;
    renderStructure(structure)
      .then((url) => {
        if (current) setRendered({ structure, url });
      })
      .catch((error: unknown) => {
        if (!current) return;
        setRendered({ structure, url: null });
        // One toast for the lot: a list of structures that all fail to render is one problem.
        notifyError(error, undefined, RENDER_FAILED);
      });
    return () => {
      current = false;
    };
  }, [seen, structure]);

  const result = rendered?.structure === structure ? rendered : null;

  return (
    <div
      ref={ref}
      className={cn(
        'flex shrink-0 items-center justify-center overflow-hidden rounded-md border border-neutral-300 bg-neutral-000',
        className,
      )}
    >
      {result?.url ? (
        <img src={result.url} alt={alt} className="max-h-full max-w-full object-contain" />
      ) : result ? (
        <div
          role="img"
          aria-label={`${alt} could not be rendered`}
          className="flex flex-col items-center gap-1 px-2 text-center text-neutral-700"
        >
          <ImageOff className="size-5 shrink-0" />
          <span className="text-[12px]/4">Could not render</span>
        </div>
      ) : (
        seen && <Skeleton className="size-full rounded-none" />
      )}
    </div>
  );
}
