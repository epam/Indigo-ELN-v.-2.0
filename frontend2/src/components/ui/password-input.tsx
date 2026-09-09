import { Eye, EyeOff, X } from 'lucide-react';
import { useState } from 'react';

import { GroupInput, InputAction, InputGroup } from '@/components/ui/input';

/**
 * A password box with the clear and reveal buttons inside it.
 *
 * Base UI ships no password input — `input`, `field` and `otp-field`, none of them with a reveal —
 * so the toggle is ours. It is the only state here: everything else is controlled, and `visible`
 * deliberately is not, since nothing outside has any business knowing whether the characters are
 * on screen.
 *
 * `label` is not rendered — the visible one belongs to `Field` — it only names the two buttons,
 * which are icon-only and would otherwise be announced as nothing.
 */
function PasswordInput({
  id,
  label,
  value,
  onChange,
  onBlur,
  placeholder,
  autoComplete,
  clearable = true,
  className,
}: {
  id: string;
  /** Names the buttons: "Clear Password", "Show Password". */
  label: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  placeholder?: string;
  autoComplete?: string;
  /** The X. Off for a field a user is expected to correct rather than retype. */
  clearable?: boolean;
  className?: string;
}) {
  const [visible, setVisible] = useState(false);

  return (
    <InputGroup className={className}>
      <GroupInput
        id={id}
        name={id}
        type={visible ? 'text' : 'password'}
        autoComplete={autoComplete}
        placeholder={placeholder}
        value={value}
        onBlur={onBlur}
        onChange={(event) => onChange(event.target.value)}
      />
      {clearable && value && (
        <InputAction label={`Clear ${label}`} onClick={() => onChange('')}>
          <X className="size-4" />
        </InputAction>
      )}
      <InputAction label={visible ? `Hide ${label}` : `Show ${label}`} onClick={() => setVisible((shown) => !shown)}>
        {visible ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
      </InputAction>
    </InputGroup>
  );
}

export { PasswordInput };
