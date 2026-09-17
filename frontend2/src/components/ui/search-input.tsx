import { Search } from 'lucide-react';

import { GroupInput, InputGroup } from '@/components/ui/input';
import { cn } from '@/lib/utils';

/**
 * A search box: a magnifier on the leading edge and nothing on the trailing one.
 *
 * `type="search"` is part of the contract rather than decoration — it is what makes the box a
 * `searchbox` to a screen reader, and what the toolbar stories query it by.
 *
 * The `pr-3` restores `Input`'s own right padding: `InputGroup` defaults to `pr-1` for a
 * trailing button's sake, and there is none here.
 */
function SearchInput({
  id,
  'aria-label': ariaLabel,
  value,
  onChange,
  placeholder = 'Search',
  className,
}: {
  id?: string;
  /** Required: the box carries no visible label of its own. */
  'aria-label': string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
}) {
  return (
    <InputGroup startIcon={<Search className="size-4" />} className={cn('pr-3', className)}>
      <GroupInput
        id={id}
        type="search"
        aria-label={ariaLabel}
        placeholder={placeholder}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </InputGroup>
  );
}

export { SearchInput };
