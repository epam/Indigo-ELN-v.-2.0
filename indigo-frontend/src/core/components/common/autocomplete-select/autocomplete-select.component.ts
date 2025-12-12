import {
  Component,
  ContentChild,
  DestroyRef,
  forwardRef,
  inject,
  Input,
  OnInit,
  TemplateRef,
} from '@angular/core';
import { MatOption, MatPrefix } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatChipGrid, MatChipInput, MatChipRow } from '@angular/material/chips';
import { MatIcon } from '@angular/material/icon';
import {
  MatAutocomplete,
  MatAutocompleteTrigger,
} from '@angular/material/autocomplete';
import { combineLatestWith, debounce, map } from 'rxjs/operators';
import {
  BehaviorSubject,
  distinctUntilChanged,
  filter,
  interval,
  Observable,
  of,
  switchMap,
} from 'rxjs';
import { AsyncPipe, NgIf, NgTemplateOutlet } from '@angular/common';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export interface HasId {
  id: string;
}

@Component({
  selector: 'eln-autocomplete-select',
  imports: [
    MatOption,
    MatInput,
    FormsModule,
    MatChipGrid,
    MatChipRow,
    MatIcon,
    MatAutocompleteTrigger,
    MatChipInput,
    MatPrefix,
    MatAutocomplete,
    ReactiveFormsModule,
    AsyncPipe,
    NgTemplateOutlet,
    NgIf,
  ],
  templateUrl: './autocomplete-select.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => AutocompleteSelectComponent),
      multi: true,
    },
  ],
})
export class AutocompleteSelectComponent<T extends HasId>
  extends DelegatingControlBase<T[]>
  implements OnInit
{
  @Input({ required: true }) search: (query: string) => Observable<T[]>;
  @Input({ required: true }) display: (item: T) => string;
  @Input() allowEmptySearch = false;
  @ContentChild('optionTemplate') optionTemplate: TemplateRef<any>;

  form = new FormGroup({
    query: new FormControl<string | null>(null),
  });
  options$: Observable<T[]>;
  defaultOptions: Observable<T[]>;
  selected$ = new BehaviorSubject<T[]>([]);
  destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.defaultOptions = this.allowEmptySearch ? this.search('') : of([]); // create observable, but don't subscribe yet
    this.options$ = this.form.get('query').valueChanges.pipe(
      debounce((query) => (query === null ? interval(0) : interval(300))),
      distinctUntilChanged(),
      filter((query) => query == null || typeof query === 'string'), // mat-autocomplete pushes selected values to the input; ignore them for filtering purposes
      switchMap((query) => {
        query = (query || '').trim();
        if (query === '') {
          return this.defaultOptions;
        }
        return this.search(query);
      }),
      combineLatestWith(this.selected$),
      map(([items, selected]) => {
        const selectedIds = new Set(selected.map((item) => item.id));
        return items.filter((item) => !selectedIds.has(item.id));
      }),
      takeUntilDestroyed(this.destroyRef),
    );
  }

  setValue(obj: T[] | null): void {
    this.selected$.next(obj || []);
  }

  protected getControlsToDisable(): AbstractControl[] {
    return Object.values(this.form.controls);
  }

  onItemSelected(item: T) {
    const currentValue = this.selected$.value;
    if (item && !currentValue.some((u) => u.id === item.id)) {
      const nextValue = [...currentValue, item];
      this.selected$.next(nextValue);
      this.form.get('query').setValue('');
      this.triggerChange(nextValue);
      this.triggerTouched();
    }
  }

  removeItem(item: T) {
    const currentValue = this.selected$.value;
    const index = currentValue.findIndex((u) => u.id === item.id);
    if (index >= 0) {
      const nextValue = [...currentValue];
      nextValue.splice(index, 1);
      this.selected$.next(nextValue);
      this.triggerChange(nextValue);
      this.triggerTouched();
    }
  }

  inputFocused() {
    if (this.form.get('query').value === null) {
      this.form.get('query').setValue('');
    }
  }
}
