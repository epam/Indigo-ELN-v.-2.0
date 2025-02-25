import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';
import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { DropdownMenuItem } from './dropdown-menu.i';

@Component({
  selector: 'app-dropdown-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dropdown-menu.component.html',
  animations: [
    trigger('dropdownAnimation', [
      state(
        'void',
        style({
          transform: 'translateY(-10px)',
          opacity: 0,
        }),
      ),
      state(
        '*',
        style({
          transform: 'translateY(0)',
          opacity: 1,
        }),
      ),
      transition('void => *', animate('200ms ease-out')),
      transition('* => void', animate('150ms ease-in')),
    ]),
  ],
})
export class DropdownMenuComponent implements AfterViewInit {
  @Input() items: DropdownMenuItem[] = [];
  @Input() selected?: string;
  @Output() itemSelected = new EventEmitter<string>();

  @ViewChild('dropdownContainer') dropdownContainer!: ElementRef;
  @ViewChild('dropdownList') dropdownList!: ElementRef;

  isOpen = false;
  dropdownDirection: 'down' | 'up' = 'down';
  dropdownAlignment: 'left' | 'right' = 'left';

  ngAfterViewInit() {
    // Initial check
    this.checkDropdownPosition();
  }

  @HostListener('window:resize')
  onResize() {
    if (this.isOpen) {
      this.checkDropdownPosition();
    }
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!(event.target as HTMLElement).closest('.dropdown-container')) {
      this.isOpen = false;
    }
  }

  toggleDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.isOpen = !this.isOpen;

    if (this.isOpen) {
      setTimeout(() => this.checkDropdownPosition(), 0);
    }
  }

  selectItem(item: DropdownMenuItem, event: MouseEvent): void {
    event.stopPropagation();
    if (this.selected !== item.label) {
      this.selected = item.label;
      this.itemSelected.emit(item.value);
    }
    this.isOpen = false;
  }

  private checkDropdownPosition(): void {
    if (!this.dropdownContainer || !this.dropdownList) return;

    const containerRect =
      this.dropdownContainer.nativeElement.getBoundingClientRect();
    const dropdownRect =
      this.dropdownList.nativeElement.getBoundingClientRect();

    // Check vertical position
    const spaceBelow = window.innerHeight - containerRect.bottom;
    const spaceAbove = containerRect.top;

    if (spaceBelow < 200 && spaceAbove > spaceBelow) {
      this.dropdownDirection = 'up';
    } else {
      this.dropdownDirection = 'down';
    }

    // Check horizontal position
    const viewportWidth = window.innerWidth;
    const rightEdge = containerRect.left + dropdownRect.width;

    if (rightEdge > viewportWidth - 10) {
      // Add 10px buffer
      this.dropdownAlignment = 'right';
    } else {
      this.dropdownAlignment = 'left';
    }
  }
}
