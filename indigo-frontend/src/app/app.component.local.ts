import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'eln-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.local.html',
  standalone: true,
})
export class AppComponent implements OnInit {
  title = 'indigo-frontend';

  /**
   * Initialize Keycloak authentication when the component is loaded.
   */
  async ngOnInit(): Promise<void> {
    //const authenticated = await provideKeycloak.init();
    //if (authenticated) {
    //    console.log('User is authenticated');
    //} else {
    //    console.log('User is not authenticated');
    //}
  }
}
