import { FieldType, FieldTypeConfig } from "@ngx-formly/core";
import { Component } from "@angular/core";
import { NgOptionComponent, NgSelectModule } from "@ng-select/ng-select";
import { ChipComponent } from "../../common/chip/chip.component";
import { CommonModule } from "@angular/common";

@Component ({
    selector: 'eln-formly-chip-grid',
    standalone: true,
    imports: [NgSelectModule, NgOptionComponent, ChipComponent, CommonModule],
    template: `
        <ng-select multiple="true">
            @for (item of items; track item) {
            <ng-option [value]="item">
                {{ item.name }}
            </ng-option>
            } 

            <ng-template ng-multi-label-tmp let-items="items" let-clear="clear">
                <div class="flex flex-wrap gap-[4px] max-h-[64px] overflow-y-auto">
                    <eln-chip
                    *ngFor="let item of items"
                    [deleteable]="true"
                    >
                        {{ item.name }}
                    </eln-chip>
                </div>
            </ng-template>
        </ng-select>
    `
})

export class SelectChipsComponent extends FieldType<FieldTypeConfig> {
    items = [
        { id: 1, name: 'option1' },
		{ id: 2, name: 'option2'},
		{ id: 3, name: 'option3' },
		{ id: 4, name: 'option4' },
        { id: 5, name: 'option5' },
        { id: 6, name: 'option6' },
        { id: 7, name: 'option7' },
        { id: 8, name: 'option8' },
    ]

    ngOnInit() {}
}