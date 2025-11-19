import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AutocompleteSelectComponent,
  HasId,
} from '@core/components/common/autocomplete-select/autocomplete-select.component';
import { Observable, of } from 'rxjs';
import { AbstractControl, FormControl, FormGroup, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { DropdownValueComponent } from '@core/components/experiment/dropdown-value/dropdown-value.component';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';
import { SelectComponent } from '@core/components/common/select/select.component';

interface EnumItem extends HasId {
  name: string;
}

@Component({
  selector: 'eln-enum-select',
  templateUrl: './enum-select.component.html',
  imports: [CommonModule, AutocompleteSelectComponent, DropdownValueComponent, ReactiveFormsModule, SelectComponent],
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EnumSelectComponent),
      multi: true,
    },
  ],
})
export class EnumSelectComponent<T extends string> extends DelegatingControlBase<T | T[]> implements OnInit {
  @Input() multiple = false;
  @Input() allowEmpty = false;
  @Input({ required: true }) enumType: { [key: string]: string };
  @Input() displayNames: Record<T, string> | null = null;

  form = new FormGroup({
    single: new FormControl<EnumItem | null>(null),
    multiple: new FormControl<EnumItem[] | null>(null)
  })

  allItems: EnumItem[];

  ngOnInit() {
    this.form.get('single').valueChanges.subscribe(value => this.triggerChange(value?.id as T));
    this.form.get('multiple').valueChanges.subscribe(value => this.triggerChange(value?.map(x => x.id as T)));
    this.allItems = Object.keys(this.enumType)
      .map(x => this.generateEnumItem(x as T));
  }

  search(query: string): Observable<EnumItem[]> {
    if (query === '') {
      return of(this.allItems);
    }
    const queryLower = query.toLowerCase();
    return of(this.allItems
      .filter((x) => this.displayFn(x).toLowerCase().startsWith(queryLower))
    );
  }

  displayFn(item: EnumItem | null): string | null {
    return item?.name;
  }

  generateEnumItem(value: T | null): EnumItem | null {
    return value != null ? {id: value, name: this.displayNames?.[value] || value } : null;
  }

  setValue(obj: T | T[] | null): void {
    if (this.multiple) {
      this.form.get('multiple').setValue(obj != null ? (obj as T[]).map(x => this.generateEnumItem(x)) : []);
    } else {
      this.form.get('single').setValue(this.generateEnumItem((obj as T)));
    }
  }

  protected getControlsToDisable(): AbstractControl[] {
      return Object.values(this.form.controls);
  }
}
