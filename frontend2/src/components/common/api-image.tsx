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
 * There is no error branch. `apiFetch` has already raised a toast, and the picture endpoint
 * never 404s — a missing picture comes back as a 1x1 placeholder — so a broken-image frame
 * would only add noise. The Angular original went further and re-armed its observer after a
 * failure, which quietly retried the request on every scroll past; this does not.
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
  const { data } = useApiImage(path, seen);

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
      ) : (
        // Only once something is actually on its way: a screenful of pulsing placeholders
        // for images nothing has asked for yet is noise, so an unseen frame stays empty.
        seen && <Skeleton className="size-full rounded-none" />
      )}
    </div>
  );
}

export { ApiImage };
