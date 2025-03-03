import { dropdownAnimation } from '@/core/animations/control-animations';
import { DropdownBaseComponent } from '@/core/components/common/dropdown/dropdown-base.component';
import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { DropdownMenuItem } from './dropdown-menu.i';

@Component({
  selector: 'app-dropdown-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dropdown-menu.component.html',
  animations: [dropdownAnimation],
})
export class DropdownMenuComponent
  extends DropdownBaseComponent
  implements AfterViewInit
{
  @Input() items: DropdownMenuItem[] = [];
  @Input() selected?: string;
  @Input() placeholder = 'Select Item';
  @Input() disabled = false;
  @Output() itemSelected = new EventEmitter<string>();
  @Output() dropdownToggled = new EventEmitter<boolean>();

  readonly containerSelector = '.dd-container';

  ngAfterViewInit() {
    // Initial check
    this.checkDropdownPosition();
  }

  override toggleDropdown(event: MouseEvent): void {
    if (this.disabled) return;

    super.toggleDropdown(event);
    this.dropdownToggled.emit(this.isOpen);
  }

  selectItem(item: DropdownMenuItem, event: MouseEvent): void {
    if (this.disabled || item.disabled) return;

    event.stopPropagation();
    if (this.selected !== item.label) {
      this.selected = item.label;
      this.itemSelected.emit(item.value);
    }
    this.isOpen = false;
  }
}
