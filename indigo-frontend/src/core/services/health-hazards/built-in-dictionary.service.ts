import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from '@/core/services/api.service';
import { DictionaryItemRef, BuiltInDictionary } from '@/core/types/entities/dictionary.i';

@Injectable({
  providedIn: 'root',
})
export class BuiltInDictionaryService {
  private service = inject(ApiService);

  // Signals for dictionary state - keyed by dictionary type
  readonly dictionaries = signal<Map<BuiltInDictionary, DictionaryItemRef[]>>(new Map());
  readonly isLoading = signal<Map<BuiltInDictionary, boolean>>(new Map());
  readonly hasError = signal<Map<BuiltInDictionary, boolean>>(new Map());

  // Private cache to track loaded dictionaries
  private cache = new Map<BuiltInDictionary, DictionaryItemRef[]>();
  
  // Helper method to check if dictionary is cached
  private isCached(dictionary: BuiltInDictionary): boolean {
    return this.cache.has(dictionary) && (this.cache.get(dictionary)?.length ?? 0) > 0;
  }

  // Query methods
  load(dictionaries: BuiltInDictionary[], forceReload = false) {
    dictionaries.forEach(dict => this.loadSingle(dict, forceReload));
  }

  private loadSingle(dictionary: BuiltInDictionary, forceReload = false) {
    // Return early if already cached and not forcing reload
    if (!forceReload && this.isCached(dictionary)) {
      return;
    }

    // Prevent duplicate requests
    if (this.isLoading().get(dictionary)) {
      return;
    }

    this.setLoading(dictionary, true);
    this.setError(dictionary, false);

    this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`)
      .subscribe({
        next: (items) => {
          this.cache.set(dictionary, items);
          this.setDictionary(dictionary, items);
          this.setLoading(dictionary, false);
        },
        error: (error) => {
          console.warn(
            `Error loading dictionary ${dictionary}:`,
            error,
          );
          this.setDictionary(dictionary, []);
          this.setError(dictionary, true);
          this.setLoading(dictionary, false);
        },
      });
  }

  // Getter methods
  getDictionary(dictionary: BuiltInDictionary): Observable<DictionaryItemRef[]> {
    // Return observable from the dictionary endpoint
    return this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`)
      .pipe(
        tap({
          next: (items) => {
            this.cache.set(dictionary, items);
            this.setDictionary(dictionary, items);
            this.setLoading(dictionary, false);
          },
          error: (error) => {
            console.error(`Error fetching dictionary ${dictionary}:`, error);
            this.setError(dictionary, true);
            this.setLoading(dictionary, false);
          },
        }),
      );
  }

  getDictionaryItems(dictionaries: BuiltInDictionary[]): Map<BuiltInDictionary, DictionaryItemRef[]> {
    const result = new Map<BuiltInDictionary, DictionaryItemRef[]>();

    dictionaries.forEach(dictionary => {
      result.set(dictionary, this.getDictionaryItem(dictionary));
    });

    return result;
  }

  getDictionaryItem(dictionary: BuiltInDictionary): DictionaryItemRef[] {
    // Return from cache if available
    if (this.isCached(dictionary)) {
      return this.cache.get(dictionary) ?? [];
    }
    // Otherwise return from signal (in case it was set by getDictionary observable)
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
  refresh(dictionaries: BuiltInDictionary[]) {
    dictionaries.forEach(dict => this.cache.delete(dict));
    this.load(dictionaries, true);
  }

  reset(dictionaries: BuiltInDictionary[]) {
    dictionaries.forEach(dict => {
      this.cache.delete(dict);
      this.setDictionary(dict, []);
      this.setLoading(dict, false);
      this.setError(dict, false);
    });
  }

  resetAll() {
    this.cache.clear();
    this.dictionaries.set(new Map());
    this.isLoading.set(new Map());
    this.hasError.set(new Map());
  }
}
