import { Component, OnInit, Output, EventEmitter, inject } from '@angular/core';
import {
  DictionaryList,
  DictionaryListItem,
} from '@/core/types/entities/dictionary.i';
import { DictionaryService } from '@/core/services/dictionary/dictionary.service';
import { CardComponent } from '@/core/components/common/card/card.component';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'eln-dictionary-list',
  templateUrl: './dictionary-list.component.html',
  imports: [CardComponent, DatePipe, MatIconModule],
  standalone: true,
})
export class DictionaryListComponent implements OnInit {
  private dictionaryService = inject(DictionaryService);
  dictionaries: DictionaryList = [];
  selectedDictionary: DictionaryListItem | null = null;
  isLoading = true;
  hasError = false;

  @Output() selectDictionaryEvent = new EventEmitter<DictionaryListItem>();

  ngOnInit(): void {
    this.fetchDictionaries();
  }

  fetchDictionaries(): void {
    this.isLoading = true;
    this.hasError = false;

    this.dictionaryService.getDictionaries().subscribe({
      next: (dictionaries) => {
        this.dictionaries = dictionaries;
        this.isLoading = false;
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
      },
    });
  }

  selectDictionary(dictionary: DictionaryListItem): void {
    this.selectedDictionary = dictionary;
    this.selectDictionaryEvent.emit(dictionary);
  }
}
