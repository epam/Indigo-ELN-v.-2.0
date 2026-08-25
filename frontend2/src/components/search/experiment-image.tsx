import { ApiImage } from '@/components/common/api-image';
import { experimentPicturePath } from '@/lib/api/experiments';

/**
 * An experiment's reaction scheme, server-rendered and loaded lazily — a result list is
 * mostly rows nobody scrolls to. Everything but the URL and the frame size is ApiImage's.
 */
function ExperimentImage({ experimentId, revision }: { experimentId: string; revision: number | null }) {
  return (
    <ApiImage
      path={experimentPicturePath(experimentId, revision)}
      alt="Reaction scheme"
      className="h-[88px] w-[140px]"
    />
  );
}

export { ExperimentImage };
