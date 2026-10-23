import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

/**
 * Root shell component: hosts the primary router outlet and sets the default UI language.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private readonly translate = inject(TranslateService);

  ngOnInit(): void {
    this.translate.setFallbackLang('fr');
    this.translate.use('fr');
  }
}
