import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  inject,
} from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {
  DictionaryFull,
  DictionaryFullItem,
  DictionaryListItem,
} from '@/core/types/entities/dictionary.i';
import { DictionaryService } from '@/core/services/dictionary/dictionary.service';
import { MatIcon } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CardComponent } from '@/core/components/common/card/card.component';
import { InputComponent } from '@/core/components/common/input/input.component';
import { MatButtonModule } from '@angular/material/button';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface UiDictionaryItem extends DictionaryFullItem {
  validationError?: string;
}

@Component({
  selector: 'eln-dictionary-drawer',
  templateUrl: './dictionary-drawer.component.html',
  imports: [
    MatIcon,
    CommonModule,
    MatTableModule,
    MatCheckboxModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    CardComponent,
    InputComponent,
    MatProgressSpinnerModule,
    MatButtonModule,
    ButtonComponent,
    ReactiveFormsModule,
  ],
  styleUrls: ['./dictionary-drawer.component.scss'],
  standalone: true,
})
export class DictionaryDrawerComponent implements OnChanges {
  @Input() dictionary: DictionaryListItem | null = null;
  @Output() close = new EventEmitter<void>();

  private dictionaryService = inject(DictionaryService);
  private snackBar = inject(MatSnackBar);
  private lastId: string | null = null;
  /* Cache the original data to support filtering */
  private originalData: UiDictionaryItem[] = [];
  dataSource$ = new BehaviorSubject<DictionaryFull>([]);

  displayedColumns: string[] = [
    'rank',
    'name',
    'description',
    'active',
    'createdAt',
    'delete',
  ];

  isLoadingDictionaryDetails = false;

  formRows: FormGroup[] = [];

  constructor(private fb: FormBuilder) {}

  ngOnChanges(): void {
    if (this.dictionary) {
      if (this.dictionary.id !== this.lastId) {
        this.lastId = this.dictionary.id;
        this.getDictionaryDetails();
      }
    } else {
      this.lastId = null;
      this.originalData = [];
      this.dataSource$.next([]);
      this.isLoadingDictionaryDetails = false;
    }
  }

  /**
   * Helper method to retrieve FormControl from FormGroup
   * This ensures type safety when used in the template
   */
  getFormControl(form: FormGroup, controlName: string): FormControl {
    return form.get(controlName) as FormControl;
  }

  private getDictionaryDetails(): void {
    this.isLoadingDictionaryDetails = true;
    this.dictionaryService.getDictionaryDetails(this.dictionary!.id).subscribe({
      next: (response) => {
        this.originalData = response;
        this.dataSource$.next(response);
        this.isLoadingDictionaryDetails = false;
        this.formRows = this.originalData.map((item) =>
          this.createRowForm(item.name),
        );
      },
      error: (err) => {
        console.error('Error fetching dictionary details:', err);
        this.isLoadingDictionaryDetails = false;
      },
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value.toLowerCase();

    if (!filterValue) {
      this.dataSource$.next([...this.originalData]);
      return;
    }

    const filteredData = this.originalData.filter((item) =>
      item.name.toLowerCase().includes(filterValue),
    );

    this.dataSource$.next(filteredData);
  }

  deleteItemFromTable(itemId: string | null, index: number): void {
    if (!itemId) {
      // new unsaved item, just remove from local data
      this.originalData.splice(index, 1);
      this.formRows.splice(index, 1);
      this.dataSource$.next([...this.originalData]);
    } else {
      // existing item, call API to delete
      this.dictionaryService
        .deleteDictionaryItem(this.lastId, itemId)
        .subscribe({
          next: (updatedDictionary) => {
            const deletedItem = this.originalData[index];
            this.originalData = updatedDictionary;
            this.dataSource$.next(updatedDictionary);
            this.formRows.splice(index, 1);

            this.snackBar.open(
              `Word '${deletedItem.name}' has been successfully deleted.`,
              'Close',
              {
                duration: 5000,
              },
            );
          },
          error: (err) => {
            console.error('Error deleting dictionary item:', err);
          },
        });
    }
  }

  addNewWordRowToTable(): void {
    if (!this.dictionary?.id) {
      console.error('Dictionary ID is not available');
      return;
    }

    const currentDate = new Date();
    const newItem = {
      name: '',
      description: '',
      active: true,
      ordinal:
        this.originalData.length > 0
          ? Math.max(...this.originalData.map((item) => item.ordinal)) + 1
          : 1,
      createdAt: currentDate,
      id: '',
      validationError: '',
    };

    this.originalData.push(newItem);
    this.dataSource$.next([...this.originalData]);

    this.formRows.push(this.createRowForm(newItem.name));
  }

  private createRowForm(name: string) {
    return this.fb.group({
      name: [
        name,
        [
          Validators.required,
          this.nameUniqueValidator.bind(this),
          Validators.maxLength(256),
        ],
      ],
    });
  }

  handleInputChange(rowForm: FormGroup, item: UiDictionaryItem, event?: Event) {
    if (event && event.type === 'keydown') {
      event.preventDefault();
    }

    const nameControl = rowForm.get('name');
    if (nameControl) {
      nameControl.updateValueAndValidity();
    }
    const updatedValues = rowForm.value;
    const updatedItem = { ...item, ...updatedValues };

    if (rowForm.valid) {
      this.postNewWordToApi(rowForm, updatedItem);
    }
  }

  postNewWordToApi(rowForm: FormGroup, item: DictionaryListItem) {
    const formValue = rowForm.value;

    if (!item.id) {
      this.dictionaryService
        .addDictionaryItem(this.lastId, formValue.name)
        .subscribe({
          next: (updatedDictionary) => {
            this.originalData = updatedDictionary;
            this.dataSource$.next(updatedDictionary);

            const index = this.formRows.indexOf(rowForm);
            if (index !== -1) {
              const savedWord = updatedDictionary.find(
                (entry) => entry.name === formValue.name,
              );
              if (savedWord) {
                this.formRows[index] = this.createRowForm(savedWord.name);
              }
            }
          },
          error: (err) => {
            console.error('Error adding dictionary item:', err);
          },
        });
    }
  }

  private nameUniqueValidator(control: AbstractControl) {
    const name = control.value;
    const isDuplicate = this.originalData.some(
      (item) => item.name.toLowerCase() === name.toLowerCase(),
    );

    return isDuplicate ? { notUnique: true } : null;
  }
}
