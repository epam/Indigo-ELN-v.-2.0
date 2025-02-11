import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoginComponent } from '@pages/account/login/login.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: true,
})
export class AppComponent {
  title = 'indigo-frontend';
}
