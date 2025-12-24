import { RouterOutlet } from '@angular/router';
import { AmplifyAuthenticatorModule } from '@aws-amplify/ui-angular';
import { sessionStorage, defaultStorage } from 'aws-amplify/utils';
import { cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import { Component, OnInit } from '@angular/core';
@Component({
  selector: 'eln-root',
  imports: [RouterOutlet, AmplifyAuthenticatorModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
})
export class AppComponent implements OnInit {
  title = 'indigo-frontend';
  isRemembered = false;
  ngOnInit() {
    const hasLocalStorage = Object.keys(localStorage).some((key) =>
      key.startsWith('CognitoIdentityServiceProvider'),
    );

    if (hasLocalStorage) {
      this.isRemembered = true;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(defaultStorage);
    } else {
      this.isRemembered = false;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(sessionStorage);
    }
  }
  onRememberMeChange(event: any) {
    const isChecked = (event.target as HTMLInputElement).checked;
    this.isRemembered = isChecked;
    if (isChecked) {
      cognitoUserPoolsTokenProvider.setKeyValueStorage(defaultStorage);
    } else {
      cognitoUserPoolsTokenProvider.setKeyValueStorage(sessionStorage);
    }
  }
}
