import { CardComponent } from '@/core/components/card/card.component';
import { Component } from '@angular/core';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  standalone: true,
  imports: [CardComponent],
})
export class ProjectListComponent {}
