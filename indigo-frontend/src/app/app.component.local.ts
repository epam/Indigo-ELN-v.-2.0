import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'eln-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.local.html',
  standalone: true,
})
export class AppComponent {
  title = 'indigo-frontend';
}
