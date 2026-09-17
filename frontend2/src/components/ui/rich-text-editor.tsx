import { HardBreak } from '@tiptap/extension-hard-break';
import { Image } from '@tiptap/extension-image';
import { Subscript } from '@tiptap/extension-subscript';
import { Superscript } from '@tiptap/extension-superscript';
import { BackgroundColor, Color, TextStyle } from '@tiptap/extension-text-style';
import { Placeholder } from '@tiptap/extensions';
import { type Editor, EditorContent, useEditor } from '@tiptap/react';
import { StarterKit } from '@tiptap/starter-kit';
import {
  Bold,
  Code,
  Highlighter,
  Italic,
  Palette,
  Quote,
  Strikethrough,
  Subscript as SubscriptIcon,
  Superscript as SuperscriptIcon,
  Underline,
} from 'lucide-react';
import type * as React from 'react';
import { useEffect, useRef } from 'react';

import { cn } from '@/lib/utils';

/**
 * Tiptap binds hard break to both Mod-Enter and Shift-Enter. Mod-Enter is given back to
 * the surrounding dialog, where Ctrl/Cmd+Enter presses the default button — Shift-Enter
 * still inserts a hard break, so no editing capability is lost.
 */
const HardBreakWithoutModEnter = HardBreak.extend({
  addKeyboardShortcuts() {
    return { 'Shift-Enter': () => this.editor.commands.setHardBreak() };
  },
});

/** Uploads a pasted or dropped image and resolves to the URL to render it from. */
export type UploadImage = (file: File) => Promise<string>;

interface RichTextEditorProps {
  /** HTML, matching the backend's unbounded TEXT columns. */
  value: string;
  onChange: (html: string) => void;
  placeholder?: string;
  id?: string;
  'aria-labelledby'?: string;
  /**
   * Enables image paste and drop. Left undefined until a project-less upload endpoint
   * exists — see the "Deferred" note in the plan — and images are ignored meanwhile
   * rather than being inlined as multi-megabyte base64 into the description.
   */
  onUploadImage?: UploadImage;
  /**
   * Freezes the editor while a save is in flight. ProseMirror keeps the caret and the content;
   * it just stops accepting input, so the user does not type into a value about to be replaced
   * by the server's own copy.
   */
  disabled?: boolean;
  className?: string;
}

function RichTextEditor({
  value,
  onChange,
  placeholder,
  id,
  'aria-labelledby': ariaLabelledBy,
  onUploadImage,
  disabled = false,
  className,
}: RichTextEditorProps) {
  // Read inside ProseMirror callbacks, which are registered once and would otherwise
  // close over the first render's prop.
  const uploadRef = useRef(onUploadImage);
  // The editor cannot reference itself in its own config, so the handlers reach it
  // through this ref instead.
  const editorRef = useRef<Editor | null>(null);

  useEffect(() => {
    uploadRef.current = onUploadImage;
  }, [onUploadImage]);

  const editor = useEditor({
    extensions: [
      // StarterKit v3 already bundles bold, italic, underline, strike, code and blockquote.
      StarterKit.configure({ hardBreak: false }),
      HardBreakWithoutModEnter,
      TextStyle,
      Color,
      BackgroundColor,
      Subscript,
      Superscript,
      Image,
      Placeholder.configure({ placeholder: placeholder ?? '' }),
    ],
    content: value,
    editable: !disabled,
    onUpdate: ({ editor: instance }) => onChange(instance.getHTML()),
    editorProps: {
      attributes: {
        ...(id ? { id } : {}),
        ...(ariaLabelledBy ? { 'aria-labelledby': ariaLabelledBy } : {}),
        role: 'textbox',
        'aria-multiline': 'true',
        class: 'min-h-[120px] px-3 py-2 text-[14px]/6 text-neutral-1000 outline-none',
      },
      handlePaste: (_view, event) => insertImageFiles(editorRef, event.clipboardData?.files, uploadRef.current),
      handleDrop: (_view, event) =>
        insertImageFiles(editorRef, (event as DragEvent).dataTransfer?.files, uploadRef.current),
    },
  });

  useEffect(() => {
    editorRef.current = editor;
  }, [editor]);

  /**
   * `editable` in the config above only applies on creation, so a change after that has to be
   * pushed in — the editor outlives every toggle of it.
   *
   * **The `false` is load-bearing.** `setEditable(editable, emitUpdate = true)` emits an `update`
   * unconditionally, even when the editable state did not actually change — so this effect firing
   * on mount reported a change the user never made, carrying `getHTML()`, which is the *canonical*
   * form of whatever was passed in. Every consumer then believed the user had rewritten the field:
   * plain text gained a `<p>`, a `<div>` became a `<p>`, newlines between tags vanished. That made
   * the experiment description PATCH on a blur that changed nothing, and the project and notebook
   * dialogs send a normalised copy of untouched text on Save.
   */
  useEffect(() => {
    if (editor && !editor.isDestroyed) editor.setEditable(!disabled, false);
  }, [editor, disabled]);

  // Keep an externally-reset value (e.g. reopening the dialog) in sync, without
  // clobbering the caret while the user is the one doing the typing.
  useEffect(() => {
    if (editor && !editor.isDestroyed && value !== editor.getHTML()) {
      editor.commands.setContent(value, { emitUpdate: false });
    }
  }, [editor, value]);

  if (!editor) return null;

  return (
    <div
      className={cn(
        'flex flex-col rounded-md border border-neutral-300 bg-background transition-colors',
        'focus-within:border-blue-400 focus-within:ring-3 focus-within:ring-ring/20',
        // Muted rather than hidden: the text stays readable, which is the point of freezing it
        // in place rather than swapping it for a spinner.
        disabled && 'opacity-60',
        className,
      )}
    >
      <Toolbar editor={editor} disabled={disabled} />
      <EditorContent
        editor={editor}
        className={cn('tiptap-content min-h-[120px] overflow-y-auto', disabled && 'cursor-not-allowed')}
      />
    </div>
  );
}

/**
 * Uploads any image files in a paste/drop and inserts them at the caret. Returns true
 * when it took ownership of the event, so ProseMirror skips its own handling.
 */
function insertImageFiles(
  editorRef: React.RefObject<Editor | null>,
  files: FileList | null | undefined,
  upload: UploadImage | undefined,
): boolean {
  const images = Array.from(files ?? []).filter((file) => file.type.startsWith('image/'));
  if (images.length === 0) return false;
  // No upload target yet, but still swallow the paste — the alternative is ProseMirror
  // inlining a multi-megabyte base64 blob into the description HTML.
  if (!upload) return true;

  void Promise.all(images.map((file) => upload(file))).then((urls) => {
    urls.forEach((src) => editorRef.current?.chain().focus().setImage({ src }).run());
  });
  return true;
}

const TOOLBAR_BUTTONS = [
  { name: 'bold', label: 'Bold', icon: Bold },
  { name: 'italic', label: 'Italic', icon: Italic },
  { name: 'underline', label: 'Underline', icon: Underline },
  { name: 'strike', label: 'Strikethrough', icon: Strikethrough },
  { name: 'code', label: 'Code', icon: Code },
  { name: 'blockquote', label: 'Blockquote', icon: Quote },
  { name: 'superscript', label: 'Superscript', icon: SuperscriptIcon },
  { name: 'subscript', label: 'Subscript', icon: SubscriptIcon },
] as const;

function Toolbar({ editor, disabled }: { editor: Editor; disabled: boolean }) {
  return (
    <div
      role="toolbar"
      aria-label="Formatting"
      className="flex flex-wrap items-center gap-1 border-b border-neutral-300 bg-neutral-100 px-2 py-1.5"
    >
      {TOOLBAR_BUTTONS.map(({ name, label, icon: Icon }) => (
        <ToolbarButton
          key={name}
          label={label}
          disabled={disabled}
          active={editor.isActive(name)}
          onClick={() => {
            // Every entry above maps to a toggleX command of the same name.
            const command = `toggle${name[0].toUpperCase()}${name.slice(1)}` as 'toggleBold';
            editor.chain().focus()[command]().run();
          }}
        >
          <Icon className="size-4" />
        </ToolbarButton>
      ))}
      <ColorButton
        label="Font colour"
        disabled={disabled}
        icon={<Palette className="size-4" />}
        value={editor.getAttributes('textStyle').color ?? '#242424'}
        onChange={(color) => editor.chain().focus().setColor(color).run()}
      />
      <ColorButton
        label="Background colour"
        disabled={disabled}
        icon={<Highlighter className="size-4" />}
        value={editor.getAttributes('textStyle').backgroundColor ?? '#ffffff'}
        onChange={(color) => editor.chain().focus().setBackgroundColor(color).run()}
      />
    </div>
  );
}

function ToolbarButton({
  label,
  active,
  disabled,
  onClick,
  children,
}: {
  label: string;
  active: boolean;
  disabled?: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      aria-label={label}
      aria-pressed={active}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        'cursor-pointer rounded-2 p-1.5 text-neutral-800 outline-none hover:bg-neutral-200 focus-visible:ring-3 focus-visible:ring-ring/50',
        active && 'bg-blue-10 text-blue-400',
        'disabled:pointer-events-none disabled:opacity-50',
      )}
    >
      {children}
    </button>
  );
}

/** A native colour well behind an icon, so no popover machinery is needed. */
function ColorButton({
  label,
  icon,
  value,
  disabled,
  onChange,
}: {
  label: string;
  icon: React.ReactNode;
  value: string;
  disabled?: boolean;
  onChange: (color: string) => void;
}) {
  return (
    <label
      className={cn(
        'relative flex items-center rounded-2 p-1.5 text-neutral-800 focus-within:ring-3 focus-within:ring-ring/50',
        disabled ? 'pointer-events-none opacity-50' : 'cursor-pointer hover:bg-neutral-200',
      )}
      title={label}
    >
      {icon}
      <input
        type="color"
        aria-label={label}
        value={value}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
        className="absolute inset-0 cursor-pointer opacity-0"
      />
    </label>
  );
}

export { RichTextEditor };
