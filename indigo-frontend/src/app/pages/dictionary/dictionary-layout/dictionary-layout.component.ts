import { DictionaryListItem } from '@/core/types/entities/dictionary.i';
import { Component } from '@angular/core';
import { MatDrawer, MatDrawerContainer, MatDrawerContent } from '@angular/material/sidenav';
import { DictionaryListComponent } from '../dictionary-list/dictionary-list.component';
import { DictionaryDrawerComponent } from '../dictionary-drawer/dictionary-drawer.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'eln-dictionary-layout',
  templateUrl: './dictionary-layout.component.html',
  styleUrls: ['./dictionary-layout.component.scss'],
  imports: [
    MatDrawerContent,
    MatDrawerContainer,
    MatDrawer,
    DictionaryListComponent,
    DictionaryDrawerComponent,
    CommonModule,
  ],
  standalone: true,
})
export class DictionaryLayoutComponent {
  isDrawerOpen = false;
  selectedDictionary: DictionaryListItem | null = null;

  onDictionarySelected(dictionary: DictionaryListItem): void {
    this.selectedDictionary = { ...dictionary };
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
    this.selectedDictionary = null;
  }
}
