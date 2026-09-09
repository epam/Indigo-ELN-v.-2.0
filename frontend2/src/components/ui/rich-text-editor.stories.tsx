import { useState } from 'react';
import { expect, userEvent, waitFor, within } from 'storybook/test';

import { RichTextEditor } from '@/components/ui/rich-text-editor';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** Every value the editor has reported, so a story can assert it reported none. */
const changes: string[] = [];

function EditorHarness({
  initial = '',
  onUploadImage,
  disabled,
}: {
  initial?: string;
  onUploadImage?: (file: File) => Promise<string>;
  disabled?: boolean;
}) {
  const [value, setValue] = useState(initial);

  return (
    <div className="w-[520px]">
      <label id="description-label" htmlFor="description" className="text-[14px]/6">
        Project Description
      </label>
      <RichTextEditor
        id="description"
        aria-labelledby="description-label"
        value={value}
        onChange={(html) => {
          changes.push(html);
          setValue(html);
        }}
        placeholder="Text"
        disabled={disabled}
        onUploadImage={onUploadImage}
      />
    </div>
  );
}

const meta = {
  title: 'UI/RichTextEditor',
  component: EditorHarness,
  args: {},
} satisfies Meta<typeof EditorHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Empty: Story = {};

/**
 * **Regression guard.** `onChange` means *the user changed it*, and must never fire for the
 * editor's own parsing of the value it was given.
 *
 * Tiptap rewrites incoming HTML into its canonical form — plain text gains a `<p>` — and
 * `setEditable(editable, emitUpdate = true)` emits an `update` unconditionally, even when the
 * editable state did not change. Together those reported that phantom rewrite as a user edit on
 * mount, which made the experiment description PATCH on a blur that changed nothing and both edit
 * dialogs send a normalised copy of untouched text.
 */
export const ReportsNoChangeOnMount: Story = {
  args: { initial: 'Plain text with no markup.' },
  play: async ({ canvasElement }) => {
    changes.length = 0;
    const canvas = within(canvasElement);
    const editor = await canvas.findByRole('textbox');

    // The editor has certainly normalised it — the DOM has a paragraph the input never had.
    await expect(editor.innerHTML).toBe('<p>Plain text with no markup.</p>');
    // …and said nothing about it.
    await waitFor(() => expect(changes).toEqual([]));
  },
};

/** Nor when the editor is frozen and released, which is `setEditable` firing twice more. */
export const ReportsNoChangeWhenDisabledToggles: Story = {
  args: { initial: 'Plain text with no markup.', disabled: true },
  play: async ({ canvasElement }) => {
    changes.length = 0;
    const canvas = within(canvasElement);
    const editor = await canvas.findByRole('textbox');

    await expect(editor).toHaveAttribute('contenteditable', 'false');
    await waitFor(() => expect(changes).toEqual([]));
  },
};

export const WithContent: Story = {
  args: {
    initial:
      '<p>To a suspension of <strong>salicylic acid</strong> (2 g) in acetic anhydride (4.5 mL) add ' +
      'anhydrous sodium acetate (0.4 g) with stirring. Record the <sup>1</sup>H-NMR spectrum.</p>',
  },
};

/** Every mark in the toolbar, so the rendered styling is visible at a glance. */
export const AllMarks: Story = {
  args: {
    initial:
      '<p><strong>bold</strong> <em>italic</em> <u>underline</u> <s>strike</s> <code>code</code> ' +
      'H<sub>2</sub>O and 10<sup>-3</sup> <span style="color: #b326ff">colour</span></p>' +
      '<blockquote><p>A quoted note.</p></blockquote>',
  },
};

/** The toolbar applies a mark to the selection and reflects it as pressed. */
export const ToolbarAppliesBold: Story = {
  args: { initial: '<p>Salicylic acid</p>' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const editor = canvas.getByRole('textbox');

    await userEvent.click(editor);
    await userEvent.keyboard('{Control>}a{/Control}');
    await userEvent.click(canvas.getByLabelText('Bold'));

    await waitFor(() => expect(editor.querySelector('strong')).toBeInTheDocument());
    await expect(canvas.getByLabelText('Bold')).toHaveAttribute('aria-pressed', 'true');
  },
};
