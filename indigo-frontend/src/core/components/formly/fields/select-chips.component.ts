import { FieldType, FieldTypeConfig } from "@ngx-formly/core";
import { Component } from "@angular/core";
import { NgSelectComponent, NgOptionComponent } from "@ng-select/ng-select";

@Component ({
    selector: 'eln-formly-chip-grid',
    standalone: true,
    imports: [NgSelectComponent, NgOptionComponent],
    template: `
        <ng-select multiple="true">
            @for (item of items; track item) {
            <ng-option [value]="item.id">
                {{ item.name }}
            </ng-option>
            } 
        </ng-select>
    `
})

export class SelectChipsComponent extends FieldType<FieldTypeConfig> {
    items = [
        { id: 1, name: 'option1' },
		{ id: 2, name: 'option2'},
		{ id: 3, name: 'option3' },
		{ id: 4, name: 'option4' },
    ]

    ngOnInit() {}
}