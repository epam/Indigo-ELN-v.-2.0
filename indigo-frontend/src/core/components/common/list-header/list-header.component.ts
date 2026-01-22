import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import {
  ButtonToggleComponent,
  ToggleOption,
} from '../button-toggle/button-toggle.component';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component';
import { DropdownMenuItem } from '../dropdown-menu/dropdown-menu.i';
import { InputComponent } from '../input/input.component';
import { ToggleComponent } from '../toggle/toggle.component';

export interface SortChangeEvent {
  sortBy: string;
  sort: 'EARLIEST' | 'LATEST';
}

@Component({
  styles: `
    :host {
      display: contents;
    }
  `,
  selector: 'eln-list-header',
  templateUrl: './list-header.component.html',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonToggleComponent,
    MatSlideToggleModule,
    ToggleComponent,
    InputComponent,
    DropdownMenuComponent,
    ReactiveFormsModule,
  ],
})
export class ListHeaderComponent implements OnInit, OnChanges, OnDestroy {
  @Input() sortOptions: DropdownMenuItem[] = [];
  @Input() currentSort: {
    sortBy: string;
    sort: 'EARLIEST' | 'LATEST';
  } | null = null;
  @Input() enableViewToggle = true;
  @Input() enableSearch = true;
  @Input() enableSort = true;
  @Input() myEntitiesOnly = false;

  sortControl = new FormControl('');
  @Output() sortChange = new EventEmitter<SortChangeEvent>();
  @Output() viewChange = new EventEmitter<string>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() myEntitiesOnlyChange = new EventEmitter<boolean>();

  selectedView: 'grid' | 'list' = 'grid';
  searchModel = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  viewOptions: ToggleOption[] = [
    { value: 'grid', icon: 'indicon-grid' },
    { value: 'list', icon: 'indicon-list' },
  ];

  ngOnInit() {
    this.searchSubject
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe((value) => {
        this.searchChange.emit(value);
      });

    this.sortControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        if (value) {
          this.onSortChange(value);
        }
      });

    this.updateSortControl();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['currentSort'] || changes['sortOptions']) {
      this.updateSortControl();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateSortControl() {
    if (this.currentSort && this.sortOptions.length > 0) {
      const matchingOption = this.sortOptions.find((option) => {
        const [sortBy, sort] = option.value.split(':');
        return (
          sortBy === this.currentSort!.sortBy &&
          sort === this.currentSort!.sort
        );
      });

      if (matchingOption) {
        // do not emit valueChanges to prevent recursive calls
        this.sortControl.setValue(matchingOption.value, { emitEvent: false });
      }
    }
  }

  onSearch(value: string) {
    this.searchSubject.next(value);
  }

  onSortChange(value: string) {
    if (!value) return;

    const [sortBy, sort] = value.split(':');
    if (sortBy && sort) {
      this.sortChange.emit({
        sortBy,
        sort: sort as 'EARLIEST' | 'LATEST',
      });
    }
  }

  onViewChange(view: string) {
    this.selectedView = view as 'grid' | 'list';
    this.viewChange.emit(view);
  }

  onMyEntitiesOnlyChanged(newValue: boolean) {
    this.myEntitiesOnly = newValue;
    this.myEntitiesOnlyChange.emit(this.myEntitiesOnly);
  }
}
