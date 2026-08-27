import { ImageOff } from 'lucide-react';

import { Skeleton } from '@/components/ui/skeleton';
import { useApiImage } from '@/lib/api/images';
import { useInViewport } from '@/lib/hooks/use-in-viewport';
import { cn } from '@/lib/utils';

/**
 * An image fetched from the ELN API, and only once it is scrolled near.
 *
 * A page of search results holds twenty rows; loading every scheme on mount meant twenty
 * requests for images most of which are never looked at. This is indigo-frontend's ApiImage
 * ported: the frame is observed, the fetch starts on first entry, and the observer is done
 * after that.
 *
 * Any failed fetch marks its own frame — 401, 403, 404, 500 alike. `apiFetch` has already
 * raised a toast saying why, but a toast in the corner cannot say *which* of twenty rows
 * came back empty, and a frame left blank is indistinguishable from one still loading. The
 * copy stays deliberately status-agnostic: the toast carries the reason, the frame only
 * says this picture is not coming.
 *
 * There is no retry: the Angular original re-armed its observer after a failure, which
 * quietly re-requested on every scroll past.
 */
function ApiImage({
  path,
  alt,
  className,
}: {
  /** Path for `apiFetch`, including any cache-busting query. */
  path: string;
  alt: string;
  /** Must size the frame: the observer needs a box, and a sized box stops the layout jumping. */
  className?: string;
}) {
  const [ref, seen] = useInViewport();
  const { data, isError } = useApiImage(path, seen);

  return (
    <div
      ref={ref}
      className={cn(
        'flex shrink-0 items-center justify-center overflow-hidden rounded-md border border-neutral-300 bg-neutral-000',
        className,
      )}
    >
      {data ? (
        <img
          src={`data:image/svg+xml;charset=utf-8,${encodeURIComponent(data)}`}
          alt={alt}
          className="max-h-full max-w-full object-contain"
        />
      ) : isError ? (
        // One node with role="img" rather than an icon plus text: the frame stands in for
        // the picture, so it should be announced as the picture that is missing.
        <div
          role="img"
          aria-label={`${alt} could not be loaded`}
          className="flex flex-col items-center gap-1 px-2 text-center text-neutral-700"
        >
          <ImageOff className="size-5 shrink-0" />
          <span className="text-[12px]/4">Could not load</span>
        </div>
      ) : (
        // Only once something is actually on its way: a screenful of pulsing placeholders
        // for images nothing has asked for yet is noise, so an unseen frame stays empty.
        seen && <Skeleton className="size-full rounded-none" />
      )}
    </div>
  );
}

export { ApiImage };
