import { Skeleton } from '@/components/ui/skeleton';
import { useExperimentPicture } from '@/lib/api/experiments';

/**
 * An experiment's reaction scheme, server-rendered.
 *
 * The endpoint answers with an SVG document rather than a URL, so it goes into the `img`
 * as a data URL — the same thing indigo-frontend's ApiImage does with its inline SVG, and
 * it avoids owning an object URL that would have to be revoked. There is no error branch:
 * `apiFetch` has already toasted the failure, the endpoint never 404s (a missing picture
 * is a 1x1 placeholder), and a broken frame would say nothing useful about the result.
 */
function ReactionScheme({ experimentId, revision }: { experimentId: string; revision: number | null }) {
  const { data, isPending } = useExperimentPicture(experimentId, revision);

  return (
    <div className="flex h-[88px] w-[140px] shrink-0 items-center justify-center overflow-hidden rounded-md border border-neutral-300 bg-neutral-000">
      {isPending ? (
        <Skeleton className="size-full rounded-none" />
      ) : data ? (
        <img
          src={`data:image/svg+xml;charset=utf-8,${encodeURIComponent(data)}`}
          alt="Reaction scheme"
          className="max-h-full max-w-full object-contain"
        />
      ) : null}
    </div>
  );
}

export { ReactionScheme };
