import { FieldType, FieldTypeConfig } from "@ngx-formly/core";
import { SelectComponent } from "../../common/select/select.component";
import { Component } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";

@Component({
    selector: 'select-chips2',
    standalone: true,
    imports: [SelectComponent, ReactiveFormsModule],
    template: `
        <eln-select
          [items]="props['items']"
          [multiple]="true"
          [renderChips]="true"
          [disabled]="formControl.disabled"
          [formControl]="formControl"
        />
    `
})

export class SelectChipsComponent extends FieldType<FieldTypeConfig> {}