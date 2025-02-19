import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ApiService } from '@core/services/api.service';

@Component({
  selector: 'app-login',
  imports: [MatCardModule, MatFormFieldModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  standalone: true,
})
export class LoginComponent implements OnInit {
  public fb = inject(FormBuilder);

  public apiService = inject(ApiService);

  public form!: FormGroup;

  public ngOnInit() {
    this.form = this.fb.group({
      username: [],
      password: [],
    });
  }

  public submit(): void {
    const body =
      'j_username=' +
      encodeURIComponent(this.form.controls['username'].value) +
      '&j_password=' +
      encodeURIComponent(this.form.controls['password'].value) +
      '&remember-me=' +
      true +
      '&submit=Login';
    this.apiService
      .post('api/authentication', body, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      })
      .subscribe();
  }
}
