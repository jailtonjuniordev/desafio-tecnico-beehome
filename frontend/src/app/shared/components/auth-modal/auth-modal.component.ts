import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  templateUrl: './auth-modal.component.html',
  styleUrl: './auth-modal.component.scss',
})
export class AuthModalComponent {
  readonly subtitle = input.required<string>();
  readonly btnText = input.required<string>();
  readonly loading = input(false);
  readonly footerText = input.required<string>();
  readonly footerLinkText = input.required<string>();
  readonly footerLink = input.required<string>();

  readonly btnAction = output<void>();
}
