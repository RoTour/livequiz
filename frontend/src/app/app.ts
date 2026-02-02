import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HealthService } from './health.service';
import { CreateQuizComponent } from './create-quiz.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CreateQuizComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  protected readonly healthService = inject(HealthService);

  ngOnInit() {
    this.healthService.checkHealth();
  }
}
