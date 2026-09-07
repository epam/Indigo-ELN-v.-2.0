import { ApiImage } from '@/components/common/api-image';
import { experimentPicturePath } from '@/lib/api/experiments';

/**
 * An experiment's reaction scheme, server-rendered and loaded lazily — a result list is
 * mostly rows nobody scrolls to. Everything but the URL is ApiImage's.
 */
function ExperimentImage({
  experimentId,
  revision,
  className,
}: {
  experimentId: string;
  revision: number | null;
  /** Sizes the frame, as ApiImage requires — each surface picks its own. */
  className?: string;
}) {
  return <ApiImage path={experimentPicturePath(experimentId, revision)} alt="Reaction scheme" className={className} />;
}

export { ExperimentImage };
