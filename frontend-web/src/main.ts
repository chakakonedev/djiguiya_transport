/**
 * Browser entry point: bootstraps the standalone Angular root component
 * with application-wide providers (router, HTTP, i18n, auth interceptor).
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
