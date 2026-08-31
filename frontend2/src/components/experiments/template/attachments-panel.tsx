import { AttachmentList } from '@/components/common/attachment-list';
import { useExperimentAttachments } from '@/lib/api/experiments';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';

/**
 * The `attachments` template component — the same list the project and notebook cards show,
 * bound to the experiment's own endpoints.
 *
 * `heading={null}` because the card around it is already titled *Attachments*; on those other
 * two screens the list is one labelled section among several, so it names itself there.
 */
export function AttachmentsPanel({ experiment }: { experiment: ExperimentDetails }) {
  const actions = useExperimentAttachments(experiment.id);

  return (
    <AttachmentList
      attachments={experiment.attachments}
      canEdit={experiment.currentPermissions.includes('EDIT_EXPERIMENTS')}
      heading={null}
      actions={actions}
    />
  );
}
