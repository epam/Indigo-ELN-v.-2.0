import { RouterOutlet } from '@angular/router';
import { AmplifyAuthenticatorModule } from '@aws-amplify/ui-angular';
import { defaultStorage, sessionStorage } from 'aws-amplify/utils';
import { cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit } from '@angular/core';

@Component({
  selector: 'eln-root',
  imports: [RouterOutlet, AmplifyAuthenticatorModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  title = 'indigo-frontend';
  isRemembered = false;

  private authFieldsObserver?: MutationObserver;
  private readonly clearButtonClass = 'eln-auth-clear-button';

  constructor(private elementRef: ElementRef<HTMLElement>) {}

  ngOnInit() {
    const hasLocalStorage = Object.keys(localStorage).some((key) => key.startsWith('CognitoIdentityServiceProvider'));

    if (hasLocalStorage) {
      this.isRemembered = true;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(defaultStorage);
    } else {
      this.isRemembered = false;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(sessionStorage);
    }
  }

  ngAfterViewInit(): void {
    this.addClearButtonsToAuthFields();
    this.authFieldsObserver = new MutationObserver(() => this.addClearButtonsToAuthFields());
    this.authFieldsObserver.observe(this.elementRef.nativeElement, { childList: true, subtree: true });
  }

  ngOnDestroy(): void {
    this.authFieldsObserver?.disconnect();
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

  private addClearButtonsToAuthFields(): void {
    const inputs = this.elementRef.nativeElement.querySelectorAll<HTMLInputElement>(
      'amplify-sign-in amplify-text-field input, amplify-sign-in amplify-password-field input',
    );

    inputs.forEach((input) => {
      const fieldWrapper = this.getClearButtonWrapper(input);
      if (!fieldWrapper || fieldWrapper.querySelector(`.${this.clearButtonClass}`)) {
        return;
      }

      const clearButton = document.createElement('button');
      clearButton.type = 'button';
      clearButton.className = this.clearButtonClass;
      clearButton.setAttribute('aria-label', `Clear ${input.name}`);

      const clearIcon = document.createElement('img');
      clearIcon.src = 'assets/close.svg';
      clearIcon.alt = '';
      clearIcon.setAttribute('aria-hidden', 'true');
      clearButton.appendChild(clearIcon);

      const toggleClearButton = () => {
        // Եթե input-ը դատարկ չէ, կամ ունի autofill, ապա ցույց տալ
        const hasValue = !!input.value || input.matches(':-webkit-autofill') || input.matches(':autofill');
        clearButton.hidden = !hasValue || input.disabled;
      };

      clearButton.addEventListener('click', () => {
        input.value = '';
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.focus();
        toggleClearButton();
      });

      input.addEventListener('input', toggleClearButton);
      // Ավելացնում ենք նաև change event-ը autofill-ի համար
      input.addEventListener('change', toggleClearButton);

      fieldWrapper.appendChild(clearButton);

      // Ստուգում ենք մի քանի միլիվայրկյան անց, երբ բրաուզերը վերջնական կլրացնի
      setTimeout(() => toggleClearButton(), 100);
      setTimeout(() => toggleClearButton(), 500); // Որոշ բրաուզերների համար մի փոքր ուշ է լինում
    });
  }

  private getClearButtonWrapper(input: HTMLInputElement): HTMLElement | null {
    const fieldWrapper = input.closest<HTMLElement>('.amplify-field-group__field-wrapper');
    if (fieldWrapper) {
      return fieldWrapper;
    }

    const parent = input.parentElement;
    if (!parent) {
      return null;
    }

    if (parent.classList.contains('eln-auth-text-field-wrapper')) {
      return parent;
    }

    const wrapper = document.createElement('div');
    wrapper.className = 'eln-auth-text-field-wrapper';
    parent.insertBefore(wrapper, input);
    wrapper.appendChild(input);

    return wrapper;
  }
}
