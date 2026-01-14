import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import { FieldTypeConfig, FormlyModule, FieldType } from "@ngx-formly/core";
import { SelectComponent } from "../../common/select/select.component";

@Component({
    selector: 'eln-formly-select',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormlyModule, SelectComponent],
    template: `
        <eln-select
            [formControl]="formControl"
            [formlyAttributes]="field"
            [items]="props['items']"
            [placeholder]="props.placeholder"
            [required]="props.required"
            [multiple]="props['multiple']"
            [renderChips]="props['renderChips']"
            [suffixStyle]="props['suffixStyle']"
            [hasError]="showError">
            
        </eln-select>
    `
})
export class SelectFieldComponent extends FieldType<FieldTypeConfig> {}