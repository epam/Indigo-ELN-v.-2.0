import { Pencil, PenLine } from 'lucide-react';
import { useEffect, useState } from 'react';

import { StructureEditorDialog, type StructureEditorResult } from '@/components/chemistry/structure-editor-dialog';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { renderStructure } from '@/lib/ketcher';
import { notifyError } from '@/lib/toast';
import { cn } from '@/lib/utils';

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
 * Shows a chemical structure — molecule or reaction — and opens the Ketcher sketcher to
 * change it. Empty, it offers a single Draw Structure button; filled, it renders the
 * structure as an SVG with an edit button in the corner.
 */
function SchemeEditor({ value, onChange, className, disabled }: SchemeEditorProps) {
  const [editorOpen, setEditorOpen] = useState(false);
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
    <>
      <div
        className={cn(
          'relative flex h-30 items-center justify-center rounded-md border border-dashed border-neutral-300',
          className,
        )}
      >
        {imageUrl ? (
          <>
            <button
              type="button"
              disabled={disabled}
              onClick={() => setEditorOpen(true)}
              aria-label="Edit structure"
              className="flex size-full cursor-pointer items-center justify-center p-2 outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
            >
              <img src={imageUrl} alt="Chemical structure" className="max-h-full max-w-full object-contain" />
            </button>
            <Button
              variant="secondary"
              size="icon-lg"
              aria-hidden
              tabIndex={-1}
              disabled={disabled}
              onClick={() => setEditorOpen(true)}
              className="absolute top-2 right-2 text-blue-400"
            >
              <Pencil />
            </Button>
          </>
        ) : isRendering ? (
          <Skeleton className="size-full" />
        ) : (
          <Button variant="secondary" size="lg" disabled={disabled} onClick={() => setEditorOpen(true)}>
            <PenLine className="text-blue-400" />
            Draw Structure
          </Button>
        )}
      </div>

      <StructureEditorDialog open={editorOpen} onOpenChange={setEditorOpen} value={value} onSave={onChange} />
    </>
  );
}

export { SchemeEditor };
