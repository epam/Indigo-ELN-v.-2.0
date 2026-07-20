import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { CommonModule } from '@angular/common';
import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import {
  MatAutocompleteModule,
  MatAutocompleteSelectedEvent,
  MatAutocompleteTrigger,
} from '@angular/material/autocomplete';
import { FieldType, FieldTypeConfig, FormlyModule } from '@ngx-formly/core';
import { Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, switchMap } from 'rxjs/operators';
import { ChipComponent } from '../../common/chip/chip.component';

/** Optional suggestion source: given the current input, returns matching keyword strings. */
type SuggestFn = (query: string) => Observable<string[]>;

@Component({
  selector: 'eln-formly-chip-grid',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule, ChipComponent, MatAutocompleteModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: ChipGridFieldComponent,
      multi: true,
    },
  ],
  template: `
    <div class="flex flex-col w-full gap-2">
      <div
        class="flex flex-wrap  gap-2 min-h-[36px]  border border-neutral-300 rounded-sm"
        [class.border-red-200]="showError"
      >
        <ng-container *ngIf="items().length > 0">
          <div class="flex flex-wrap gap-2 p-2">
            <eln-chip
              *ngFor="let item of items(); let i = index"
              [deleteable]="true"
              [active]="true"
              [error]="false"
              [size]="'default'"
              (emitClose)="removeChip(i)"
            >
              {{ item }}
            </eln-chip>
          </div>
        </ng-container>
        <input
          #chipInput
          [formControl]="inputControl"
          [placeholder]="props.placeholder"
          [formlyAttributes]="field"
          [matAutocomplete]="auto"
          class="border-none w-full h-auto px-2"
          (keydown)="addChipFromInput($event, chipInput)"
        />
        <mat-autocomplete #auto="matAutocomplete" (optionSelected)="onSuggestionSelected($event, chipInput)">
          <mat-option *ngFor="let suggestion of suggestions$ | async" [value]="suggestion">
            {{ suggestion }}
          </mat-option>
        </mat-autocomplete>
      </div>

      <!-- Error message -->
      <div *ngIf="showError && formControl.errors" class="text-red-200 text-xs">
        <div *ngIf="formControl.errors['required']">This field is required</div>
        <div *ngIf="formControl.errors['min']">At least {{ formControl.errors['min'].min }} items required</div>
        <div *ngIf="formControl.errors['max']">Maximum {{ formControl.errors['max'].max }} items allowed</div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        width: 100%;
      }
    `,
  ],
})
export class ChipGridFieldComponent extends FieldType<FieldTypeConfig> implements ControlValueAccessor, OnInit {
  // Separator keys for adding chips
  readonly separatorKeyCodes = [ENTER, COMMA] as const;

  // Input control for new chips
  inputControl = new FormControl('');

  // Store chip items
  items = signal<string[]>([]);

  // Prefix-match suggestions (only populated when a `suggest` source is configured via props)
  suggestions$?: Observable<string[]>;

  @ViewChild(MatAutocompleteTrigger) autoTrigger?: MatAutocompleteTrigger;

  onChange = (value: string[]) => {};
  onTouched = () => {};

  ngOnInit(): void {
    this.items.set(this.field.defaultValue || []);

    const suggest = this.props['suggest'] as SuggestFn | undefined;
    if (suggest) {
      this.suggestions$ = this.inputControl.valueChanges.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((value) => {
          const query = (value || '').trim();
          if (!query) {
            return of([]);
          }
          return suggest(query).pipe(catchError(() => of([])));
        }),
        // Don't suggest keywords that are already selected
        map((suggestions) => suggestions.filter((s) => this.items().indexOf(s) === -1)),
      );
    }
  }

  // Add new chip from input
  addChipFromInput(event: KeyboardEvent, input: HTMLInputElement): void {
    // When a suggestion is highlighted, let (optionSelected) handle Enter instead of adding free text.
    if (event.key === 'Enter' && this.autoTrigger?.activeOption) {
      return;
    }

    const value = (input.value || '').trim();

    // Add chip when pressing Enter or comma, if there's a value
    if ((event.key === 'Enter' || event.key === ',') && value) {
      this.addChip(value);
      event.preventDefault();
    }
  }

  // Add a chip from a selected suggestion
  onSuggestionSelected(event: MatAutocompleteSelectedEvent, input: HTMLInputElement): void {
    this.addChip(event.option.value);
    input.value = '';
  }

  // Add a new chip
  addChip(value: string): void {
    if (!value) return;

    // Check for duplicates
    if (this.items().indexOf(value) === -1) {
      const currentItems = [...this.items()];
      currentItems.push(value);

      this.items.set(currentItems);
      this.onChange(currentItems);
      this.formControl.setValue(currentItems);
      this.formControl.markAsTouched();
    }

    // Reset the input field
    this.inputControl.setValue('');
  }

  // Remove a chip
  removeChip(index: number): void {
    const currentItems = [...this.items()];
    if (index >= 0) {
      currentItems.splice(index, 1);
      this.items.set(currentItems);
      this.onChange(currentItems);
      this.formControl.setValue(currentItems);
      this.formControl.markAsTouched();
    }
  }

  // Clear all chips
  clearChips(): void {
    this.items.set([]);
    this.onChange([]);
    this.formControl.setValue([]);
    this.formControl.markAsTouched();
  }

  // ControlValueAccessor methods
  writeValue(value: string[]): void {
    this.items.set(Array.isArray(value) ? value : []);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    if (isDisabled) {
      this.formControl.disable();
      this.inputControl.disable();
    } else {
      this.formControl.enable();
      this.inputControl.enable();
    }
  }
}
