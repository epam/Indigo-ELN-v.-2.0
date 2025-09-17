import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
} from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {
  DictionaryFull,
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
  ],
  styleUrls: ['./dictionary-drawer.component.scss'],
  standalone: true,
})
export class DictionaryDrawerComponent implements OnChanges {
  @Input() dictionary: DictionaryListItem | null = null;
  @Output() close = new EventEmitter<void>();

  dataSource$ = new BehaviorSubject<DictionaryFull>([]);

  displayedColumns: string[] = [
    'rank',
    'name',
    'description',
    'active',
    'createdAt',
    'delete',
  ];
  private lastId: string | null = null;
  isLoadingDictionaryDetails = false;

  /* Cache the original data to support filtering */
  private originalData: DictionaryFull = [];

  constructor(private dictionaryService: DictionaryService) {}

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

  private getDictionaryDetails(): void {
    this.isLoadingDictionaryDetails = true;
    this.dictionaryService.getDictionaryDetails(this.dictionary!.id).subscribe({
      next: (response) => {
        this.originalData = response;
        this.dataSource$.next(response);
        this.isLoadingDictionaryDetails = false;
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

  deleteItem(itemId: string): void {
    if (!this.dictionary?.id) {
      return;
    }

    this.dictionaryService
      .deleteDictionaryItem(this.dictionary.id, itemId)
      .subscribe({
        next: (updatedDictionary) => {
          this.originalData = updatedDictionary;
          this.dataSource$.next(updatedDictionary);
        },
        error: (err) => console.error('Error deleting item:', err),
      });
  }
}
