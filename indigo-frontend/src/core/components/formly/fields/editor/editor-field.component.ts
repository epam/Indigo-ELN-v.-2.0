import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType, FieldTypeConfig, FormlyModule } from '@ngx-formly/core';
import { Editor, NgxEditorModule, Toolbar } from 'ngx-editor';

@Component({
  selector: 'eln-formly-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule, NgxEditorModule, FormsModule, ClassPickerPipe],
  template: `
    <div class="flex flex-col gap-2 w-full">
      <div class="NgxEditor__Wrapper border rounded-sm" [class]="{ 'border-red-200!': showError } | classPicker">
        <ngx-editor-menu [editor]="editor" [toolbar]="toolbar"> </ngx-editor-menu>
        <ngx-editor
          [editor]="editor"
          [formControl]="formControl"
          [formlyAttributes]="field"
          [placeholder]="props.placeholder || 'Type here...'"
          [disabled]="props.disabled || false"
          outputFormat="html"
        ></ngx-editor>
      </div>
    </div>
  `,
  styleUrls: ['./editor-field.component.scss'],
})
export class EditorFormlyFieldComponent extends FieldType<FieldTypeConfig> implements OnInit, OnDestroy {
  editor: Editor;

  toolbar: Toolbar = [
    ['bold', 'italic', 'underline', 'strike'],
    ['code', 'blockquote'],
    ['superscript', 'subscript'],
    ['text_color', 'background_color'],
    ['horizontal_rule', 'format_clear'],
    ['indent', 'outdent'],
  ];

  ngOnInit(): void {
    this.editor = new Editor({
      history: true,
      keyboardShortcuts: true,
    });

    if (this.props['toolbar']) {
      this.toolbar = this.props['toolbar'];
    }
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
  }
}
