import { useEffect, useState } from 'react';

import type { StructureEditorResult } from '@/components/chemistry/structure-editor-dialog';
import { StructureFrame } from '@/components/chemistry/structure-frame';
import { Skeleton } from '@/components/ui/skeleton';
import { renderStructure } from '@/lib/ketcher';
import { notifyError } from '@/lib/toast';

interface SchemeEditorProps {
  /** molfile or rxnfile; null or empty renders the empty state. */
  value: string | null;
  /**
   * Passed straight to the sketcher's Save. Returning a promise holds the dialog open until
   * it settles — see `StructureEditorDialog`'s `onSave`.
   */
  onChange: (next: StructureEditorResult | null) => void | Promise<void>;
  /** Sizes the frame; the height is the only thing call sites usually change. */
  className?: string;
  disabled?: boolean;
}

/**
 * Shows a chemical structure — molecule or reaction — that the caller holds as text, in a
 * `StructureFrame`: empty, a single Draw Structure button; filled, the structure rendered as an
 * SVG with an edit button in the corner.
 */
function SchemeEditor({ value, onChange, className, disabled }: SchemeEditorProps) {
  // Keyed by the structure it belongs to, so a render that lands late for a structure
  // the user has already replaced is ignored rather than shown. A null url means that
  // structure failed to render.
  const [rendered, setRendered] = useState<{ structure: string; url: string | null } | null>(null);

  useEffect(() => {
    if (!value) return;

    // A structure the sketcher just drew is already cached, so this usually resolves in
    // a microtask and the skeleton never paints.
    let current = true;
    renderStructure(value)
      .then((url) => {
        if (current) setRendered({ structure: value, url });
      })
      .catch((error: unknown) => {
        if (!current) return;
        // Fall back to the empty state; the toast says why nothing is shown.
        setRendered({ structure: value, url: null });
        notifyError(error);
      });

    return () => {
      current = false;
    };
  }, [value]);

  const imageUrl = value && rendered?.structure === value ? rendered.url : null;
  const isRendering = Boolean(value) && rendered?.structure !== value;

  return (
    <StructureFrame value={value} onSave={onChange} disabled={disabled} editLoading={isRendering} className={className}>
      {imageUrl ? (
        <div className="flex size-full items-center justify-center p-2">
          <img src={imageUrl} alt="Chemical structure" className="max-h-full max-w-full object-contain" />
        </div>
      ) : (
        isRendering && <Skeleton className="size-full" />
      )}
    </StructureFrame>
  );
}

export { SchemeEditor };
