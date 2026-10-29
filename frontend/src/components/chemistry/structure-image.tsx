import { ImageOff } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Skeleton } from '@/components/ui/skeleton';
import { useApiImage } from '@/lib/api/images';
import { useInViewport } from '@/lib/hooks/use-in-viewport';
import { renderStructure } from '@/lib/ketcher';
import { notifyError } from '@/lib/toast';
import { cn } from '@/lib/utils';

/**
 * A compound's structure, rendered in the browser by Ketcher — from the structure itself (a
 * PubChem hit's InChI) or from an API path serving its molfile. `ApiImage`'s counterpart, with
 * the same frame and the same lazy start: nothing is fetched or rendered until the frame is
 * scrolled near.
 *
 * A failed render marks its own frame and toasts, since no cache sees a render and nothing else
 * reports it. A failed molfile fetch marks the frame too.
 */
/** The toast id every failed render shares, which is what collapses a burst of them into one. */
const RENDER_FAILED = 'structure-render-failed';

type StructureSource =
  /** Anything Ketcher reads: a molfile, SMILES or InChI. */
  | { structure: string; path?: undefined }
  /** Path for `apiFetch` answering with a molfile as text. Must fully identify it: it is cached forever. */
  | { path: string; structure?: undefined };

export function StructureImage({
  structure: given,
  path,
  alt,
  className,
}: StructureSource & {
  alt: string;
  /** Must size the frame, as for `ApiImage`. */
  className?: string;
}) {
  const [ref, seen] = useInViewport();
  // The same text fetch a served SVG goes through; a molfile needs nothing different from it.
  const fetched = useApiImage(path ?? '', seen && path != null);
  const structure = given ?? fetched.data;
  // Keyed by the structure, so a render that lands late for a replaced one is ignored.
  const [rendered, setRendered] = useState<{ structure: string; url: string | null } | null>(null);

  useEffect(() => {
    if (!seen || structure == null) return;
    let current = true;
    renderStructure(structure)
      .then((url) => {
        if (current) setRendered({ structure, url });
      })
      .catch((error: unknown) => {
        if (!current) return;
        setRendered({ structure, url: null });
        // One toast for the lot: a list of structures that all fail to render is one problem.
        notifyError(error, RENDER_FAILED);
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
      ) : result || fetched.isError ? (
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
