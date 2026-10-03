import { KeyRound } from 'lucide-react';
import { useRef, useState } from 'react';

import { FormDialog } from '@/components/common/form-dialog';
import { Button } from '@/components/ui/button';
import { Field } from '@/components/ui/field';
import { PasswordInput } from '@/components/ui/password-input';

/**
 * Asks the signer for their own PKCS12 keystore and its password — what
 * `POST /documents/{id}/sign` signs the PDF with. The signature service holds no key of its own,
 * so the signature in the document is the certificate chosen here.
 *
 * Like `SignDialog`, it resolves the choice to the caller rather than posting anything itself, and
 * `FormDialog` keeps it open on a rejection — a wrong password, say, which `apiFetch` has already
 * toasted — with the file still chosen.
 */
export function ApproveDialog({
  open,
  onOpenChange,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (keystore: File, password: string) => Promise<void>;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [keystore, setKeystore] = useState<File | null>(null);
  const [password, setPassword] = useState('');

  return (
    <FormDialog
      open={open}
      onOpenChange={(next) => {
        // Neither the key nor its password outlives the dialog.
        if (!next) {
          setKeystore(null);
          setPassword('');
        }
        onOpenChange(next);
      }}
      title="Sign Document"
      submitLabel="Approve"
      // The password is not required: a keystore may be protected by an empty one.
      submitDisabled={keystore === null}
      onSubmit={() => onSubmit(keystore!, password)}
    >
      <Field id="keystore-file" label="Keystore File" required>
        <input
          ref={inputRef}
          id="keystore-file"
          type="file"
          accept=".p12,.pfx"
          className="hidden"
          onChange={(event) => {
            const file = event.target.files?.[0];
            // Cleared first, so picking the same file twice in a row still fires onChange.
            event.target.value = '';
            if (file) setKeystore(file);
          }}
        />
        <Button variant="outline" onClick={() => inputRef.current?.click()} className="w-full border-dashed">
          <KeyRound />
          <span className="truncate">{keystore ? keystore.name : 'Select Keystore (.p12, .pfx)'}</span>
        </Button>
      </Field>
      <Field id="keystore-password" label="Keystore Password">
        <PasswordInput
          id="keystore-password"
          label="Keystore Password"
          value={password}
          onChange={setPassword}
          autoComplete="off"
        />
      </Field>
    </FormDialog>
  );
}
