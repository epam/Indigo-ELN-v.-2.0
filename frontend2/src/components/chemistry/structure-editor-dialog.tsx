import { lazy, Suspense, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';
import { notifyError } from '@/lib/toast';

import type { Ketcher } from 'ketcher-core';

// ~28 MB of sketcher and Indigo WASM: fetched on the first Draw Structure click, never before.
const KetcherEditor = lazy(() => import('@/components/chemistry/ketcher-editor'));

export interface StructureEditorResult {
  /** A molfile, or a rxnfile when `isReaction`. */
  structure: string;
  isReaction: boolean;
}

interface StructureEditorDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** molfile or rxnfile to open with; null or empty starts the sketcher blank. */
  value: string | null;
  onSave: (result: StructureEditorResult) => void;
}

/**
 * Wraps the Ketcher sketcher in the app's modal, seeded with the current structure.
 * Port of indigo-frontend's StructureEditorModalComponent.
 */
function StructureEditorDialog({ open, onOpenChange, value, onSave }: StructureEditorDialogProps) {
  const [ketcher, setKetcher] = useState<Ketcher | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  async function handleReady(instance: Ketcher) {
    if (value) {
      try {
        await instance.setMolecule(value);
      } catch (error) {
        notifyError(error);
      }
    }
    // Only now can Save read anything back, so it stays disabled until this point.
    setKetcher(instance);
  }

  async function handleSave() {
    if (!ketcher) return;
    setIsSaving(true);
    try {
      // Ketcher itself decides which one it is — the same structure then routes into a
      // molecule or a reaction search.
      const isReaction = ketcher.containsReaction();
      const structure = isReaction ? await ketcher.getRxn() : await ketcher.getMolfile();
      // No preview is generated here: SchemeEditor renders the structure itself, off the
      // same warm Indigo worker, so doing it twice only slowed Save down.
      onSave({ structure, isReaction });
      onOpenChange(false);
    } catch (error) {
      // Leave the dialog open with the drawing intact so the work is not lost.
      notifyError(error);
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(nextOpen) => {
        if (!nextOpen) setKetcher(null);
        onOpenChange(nextOpen);
      }}
    >
      <DialogContent
        title="Structure Editor"
        className="h-[80vh] w-[1200px] max-w-[calc(100vw-2rem)]"
        footer={
          <>
            <DialogClose render={<Button type="button" variant="secondary" size="lg" />}>Cancel</DialogClose>
            <Button type="button" size="lg" loading={isSaving} disabled={!ketcher} onClick={() => void handleSave()}>
              Save
            </Button>
          </>
        }
      >
        <div className="min-h-0 flex-1">
          <Suspense fallback={<Skeleton className="size-full" />}>
            <KetcherEditor onReady={(instance) => void handleReady(instance)} />
          </Suspense>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export { StructureEditorDialog };
