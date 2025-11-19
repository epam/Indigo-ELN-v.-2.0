import { Component, forwardRef, inject, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AutocompleteSelectComponent } from '@core/components/common/autocomplete-select/autocomplete-select.component';
import { Observable } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { AbstractControl, FormControl, FormGroup, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { map } from 'rxjs/operators';
import { DropdownValueComponent } from '@core/components/experiment/dropdown-value/dropdown-value.component';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';

@Component({
  selector: 'eln-dictionary-select',
  templateUrl: './dictionary-select.component.html',
  imports: [CommonModule, AutocompleteSelectComponent, DropdownValueComponent, ReactiveFormsModule],
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DictionarySelectComponent),
      multi: true,
    },
  ],
})
export class DictionarySelectComponent extends DelegatingControlBase<DictionaryItemRef | DictionaryItemRef[]> implements OnInit {
  @Input() multiple = false;
  @Input({ required: true }) dictionaryId: string;

  form = new FormGroup({
    single: new FormControl<DictionaryItemRef | null>(null),
    multiple: new FormControl<DictionaryItemRef[] | null>(null)
  })

  private api = inject(ApiService);

  allItems$: Observable<DictionaryItemRef[]>;

  ngOnInit() {
    this.form.get('single').valueChanges.subscribe(value => this.triggerChange(value));
    this.form.get('multiple').valueChanges.subscribe(value => this.triggerChange(value));
    this.allItems$ = this.api.request<DictionaryItemRef[]>(
      'get',
      `dictionaries/${this.dictionaryId}`,
    );
  }

  search(query: string): Observable<DictionaryItemRef[]> {
    return this.allItems$.pipe(
      map((allItems) => {
        if (query === '') {
          return allItems;
        }
        const queryLower = query.toLowerCase();
        return allItems.filter((x) =>
          x.name.toLowerCase().startsWith(queryLower),
        );
      }),
    );
  }

  displayFn(item: DictionaryItemRef | null): string {
    return item?.name;
  }

  setValue(obj: DictionaryItemRef | DictionaryItemRef[] | null): void {
    if (this.multiple) {
      this.form.get('multiple').setValue(obj as DictionaryItemRef[]);
    } else {
      this.form.get('single').setValue(obj as DictionaryItemRef);
    }
  }

  protected getControlsToDisable(): AbstractControl[] {
      return Object.values(this.form.controls);
  }
}
