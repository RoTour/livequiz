import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BackendStatus } from './shared/backend-status/backend-status';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BackendStatus],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
