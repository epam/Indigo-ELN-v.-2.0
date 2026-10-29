import { Pencil, PenLine } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { StructureEditorDialog, type StructureEditorResult } from '@/components/chemistry/structure-editor-dialog';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface StructureFrameProps {
  /** molfile or rxnfile the sketcher opens with; null starts it blank. */
  value: string | null;
  /** The sketcher's Save — see `StructureEditorDialog`'s `onSave` for what a rejection does. */
  onSave: (result: StructureEditorResult) => void | Promise<void>;
  /** The picture. Absent, the frame shows its empty state instead. */
  children?: ReactNode;
  /** Said above Draw Structure in the empty state — or alone, on a surface that cannot edit. */
  emptyLabel?: string;
  /** False leaves only the picture: no pencil, no Draw Structure, no sketcher behind them. */
  editable?: boolean;
  disabled?: boolean;
  /** Spins the pencil — while the picture renders, or while the structure to seed is fetched. */
  editLoading?: boolean;
  /** `sm` sets the empty state in a dense table's 13px, for a frame that sits beside one. */
  size?: 'sm' | 'md';
  /** Sizes the frame; the height is the only thing call sites usually change. */
  className?: string;
}

/**
 * The dashed frame every structure the user can redraw sits in: the picture with a pencil in its
 * corner, or an empty state offering Draw Structure — and the Ketcher sketcher behind both.
 *
 * **The structure is shown, not pressed.** The whole frame used to be a button, so a click
 * anywhere on a reaction scheme — including one meant to read it — opened the sketcher over it.
 * The pencil is the one way in, and it is a real control: it carries the label and the tab stop
 * the frame gives up, so the keyboard path is unchanged.
 *
 * What the picture is stays with the caller — `SchemeEditor` renders the structure it holds,
 * a batch renders its compound's served molfile — so this owns only the way into the sketcher.
 */
function StructureFrame({
  value,
  onSave,
  children,
  emptyLabel,
  editable = true,
  disabled,
  editLoading,
  size = 'md',
  className,
}: StructureFrameProps) {
  const [editorOpen, setEditorOpen] = useState(false);

  return (
    <>
      <div
        className={cn(
          'relative flex h-30 items-center justify-center rounded-md border border-dashed border-neutral-300',
          className,
        )}
      >
        {children ? (
          <>
            {children}
            {editable && (
              <Button
                variant="secondary"
                size="icon-lg"
                aria-label="Edit structure"
                title="Edit structure"
                disabled={disabled}
                loading={editLoading}
                onClick={() => setEditorOpen(true)}
                className="absolute top-2 right-2 text-blue-400"
              >
                <Pencil />
              </Button>
            )}
          </>
        ) : (
          <div
            className={cn(
              'flex flex-col items-center gap-3 p-4 text-center text-neutral-700',
              size === 'sm' ? 'text-[13px]/5' : 'text-[14px]/6',
            )}
          >
            {emptyLabel}
            {editable && (
              <Button variant="secondary" size="lg" disabled={disabled} onClick={() => setEditorOpen(true)}>
                <PenLine className="text-blue-400" />
                Draw Structure
              </Button>
            )}
          </div>
        )}
      </div>

      {editable && (
        <StructureEditorDialog open={editorOpen} onOpenChange={setEditorOpen} value={value} onSave={onSave} />
      )}
    </>
  );
}

export { StructureFrame };
