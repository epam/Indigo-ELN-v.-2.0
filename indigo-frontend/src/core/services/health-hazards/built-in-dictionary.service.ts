import { inject, Injectable, signal } from '@angular/core';
import { finalize, Observable, tap } from 'rxjs';
import { ApiService } from '@/core/services/api.service';
import {
  BuiltInDictionary,
  DictionaryItemRef,
} from '@/core/types/entities/dictionary.i';

@Injectable({
  providedIn: 'root',
})
export class BuiltInDictionaryService {
  private service = inject(ApiService);

  // Signals for dictionary state - keyed by dictionary type
  readonly dictionaries = signal<Map<BuiltInDictionary, DictionaryItemRef[]>>(
    new Map(),
  );
  readonly isLoading = signal<Map<BuiltInDictionary, boolean>>(new Map());
  readonly hasError = signal<Map<BuiltInDictionary, boolean>>(new Map());

  // Query methods
  load(dictionary: BuiltInDictionary) {
    console.log(`BuiltInDictionaryService.load(${dictionary})`);
    this.setLoading(dictionary, true);
    this.setError(dictionary, false);

    this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`)
      .pipe(
        tap({
          error: () => {
            this.setDictionary(dictionary, []);
            this.setError(dictionary, true);
          },
        }),
        finalize(() => this.setLoading(dictionary, false)),
      )
      .subscribe((items) => this.setDictionary(dictionary, items));
  }

  // Getter methods
  getDictionary(
    dictionary: BuiltInDictionary,
  ): Observable<DictionaryItemRef[]> {
    // Return observable from the dictionary endpoint
    return this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`)
      .pipe(
        tap({
          next: (items) => {
            this.setDictionary(dictionary, items);
          },
          error: () => {
            this.setError(dictionary, true);
          },
        }),
        finalize(() => this.setLoading(dictionary, false)),
      );
  }

  getDictionaryItems(dictionary: BuiltInDictionary): DictionaryItemRef[] {
    const dicts = this.dictionaries();
    return dicts.get(dictionary) ?? [];
  }

  // Setter methods
  setDictionary(dictionary: BuiltInDictionary, items: DictionaryItemRef[]) {
    const dicts = this.dictionaries();
    dicts.set(dictionary, items);
    this.dictionaries.set(new Map(dicts));
  }

  setLoading(dictionary: BuiltInDictionary, value: boolean) {
    const loadingMap = this.isLoading();
    loadingMap.set(dictionary, value);
    this.isLoading.set(new Map(loadingMap));
  }

  setError(dictionary: BuiltInDictionary, value: boolean) {
    const errorMap = this.hasError();
    errorMap.set(dictionary, value);
    this.hasError.set(new Map(errorMap));
  }

  // Utility methods
  refresh(dictionary: BuiltInDictionary) {
    this.load(dictionary);
  }

  reset(dictionary: BuiltInDictionary) {
    this.setDictionary(dictionary, []);
    this.setLoading(dictionary, false);
    this.setError(dictionary, false);
  }

  resetAll() {
    this.dictionaries.set(new Map());
    this.isLoading.set(new Map());
    this.hasError.set(new Map());
  }
}
