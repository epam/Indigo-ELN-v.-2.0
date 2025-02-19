import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-avatar',
  templateUrl: './avatar.component.html',
  standalone: true,
  imports: [CommonModule, TwsxPipe],
})
export class AvatarComponent {
  @Input() img = 'assets/avatar-placeholder.png';
  @Input() classList = '';
}
