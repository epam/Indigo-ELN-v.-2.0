import { Star } from 'lucide-react';
import type { MouseEvent } from 'react';

import { Button } from '@/components/ui/button';
import { useToggleMark } from '@/lib/api/experiments';
import { cn } from '@/lib/utils';

/**
 * Stars or unstars an experiment, from inside the card that links to it.
 *
 * The click has to be stopped twice over: `preventDefault` so the surrounding `<Link>` does
 * not navigate, and `stopPropagation` so nothing else treats it as a click on the card.
 */
export function StarButton({
  experimentId,
  marked,
  disabled,
}: {
  experimentId: string;
  marked: boolean;
  /** For a surface that shows the star but cannot yet write it — see the experiment header. */
  disabled?: boolean;
}) {
  const toggle = useToggleMark();

  function handleClick(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    toggle.mutate({ id: experimentId, marked: !marked });
  }

  return (
    <Button
      variant="ghost"
      size="icon-xs"
      aria-label={marked ? 'Remove from starred' : 'Add to starred'}
      aria-pressed={marked}
      loading={toggle.isPending}
      disabled={disabled}
      onClick={handleClick}
    >
      {/* fill, not just colour: an outline star and a filled one are told apart at a glance. */}
      <Star className={cn('size-4', marked ? 'fill-blue-400 text-blue-400' : 'text-neutral-700')} />
    </Button>
  );
}
