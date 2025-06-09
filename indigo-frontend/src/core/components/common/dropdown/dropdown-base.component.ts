import { calculatePosition } from '@/core/utils/dropdown-position.util';
import { Directive, ElementRef, HostListener, ViewChild } from '@angular/core';

@Directive()
export abstract class DropdownBaseComponent {
  @ViewChild('dropdownContainer') dropdownContainer!: ElementRef;
  @ViewChild('dropdownList') dropdownList!: ElementRef;

  isOpen = false;
  dropdownDirection: 'down' | 'up' = 'down';
  dropdownAlignment: 'left' | 'right' = 'left';

  abstract get containerSelector(): string;

  @HostListener('window:resize')
  onResize() {
    if (this.isOpen) {
      this.checkDropdownPosition();
    }
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!(event.target as HTMLElement).closest(this.containerSelector)) {
      this.isOpen = false;
    }
  }

  toggleDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.isOpen = !this.isOpen;

    if (this.isOpen) {
      setTimeout(() => this.checkDropdownPosition(), 1);
    }
  }

  protected checkDropdownPosition(): void {
    if (!this.dropdownContainer || !this.dropdownList) return;

    const position = calculatePosition(
      this.dropdownContainer,
      this.dropdownList,
    );

    this.dropdownDirection = position.direction;
    this.dropdownAlignment = position.alignment;
  }
}
