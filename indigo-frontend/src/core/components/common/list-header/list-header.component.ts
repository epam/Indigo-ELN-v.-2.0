import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import {
  ButtonToggleComponent,
  ToggleOption,
} from '../button-toggle/button-toggle.component';
import { ToggleComponent } from '../toggle/toggle.component';

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
  ],
})
export class ListHeaderComponent {
  @Output() viewChange = new EventEmitter<string>();
  options: ToggleOption[] = [{ value: 'grid', icon: 'indicon-grid' }];
  selectedView: 'grid' | 'list' = 'grid';

  viewOptions: ToggleOption[] = [
    { value: 'grid', icon: 'indicon-grid' },
    { value: 'list', icon: 'indicon-list' },
  ];
}
