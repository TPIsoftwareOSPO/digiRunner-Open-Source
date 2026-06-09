import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { SmartFhirService } from '../smart-fhir.service';

@Component({
  selector: 'app-sample-app',
  template: `
    <div style="padding: 40px; text-align: center; font-family: sans-serif;">
      <h2>{{ 'smart_sample_app.title' | translate }}</h2>
      <p *ngIf="status === 'loading'">{{ 'smart_sample_app.discovering' | translate }}</p>
      <p *ngIf="status === 'redirecting'">{{ 'smart_sample_app.redirecting' | translate }}</p>
      <p *ngIf="status === 'error'" style="color: red;">{{ errorMessage }}</p>
    </div>
  `,
  standalone: false,
})
export class SampleAppComponent implements OnInit {

  status: 'loading' | 'redirecting' | 'error' = 'loading';
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private translate: TranslateService,
    private fhir: SmartFhirService,
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    const iss = params['iss'];
    const launchToken = params['launch'] || '';

    if (!iss) {
      this.status = 'error';
      this.errorMessage = this.translate.instant('smart_sample_app.error_missing_iss');
      return;
    }

    const redirectUri = window.location.origin + window.location.pathname.replace(/\/sample$/, '/sample/callback');
    const clientId = params['client_id'] || 'dg_sample_app';
    const scope = params['scope'] || 'openid profile launch patient/*.cruds';
    const simError = params['sim_error'] || '';

    sessionStorage.setItem('smart_sample_token_endpoint', '');
    sessionStorage.setItem('smart_sample_iss', iss);
    sessionStorage.setItem('smart_sample_sim_error', simError);

    this.discover(iss, clientId, redirectUri, scope, launchToken, simError);
  }

  private async discover(iss: string, clientId: string, redirectUri: string, scope: string, launchToken: string, simError: string): Promise<void> {
    try {
      // 外部 App 視角：透過 HttpClient 探索 SMART configuration（無 admin auth）
      const config = await firstValueFrom(this.fhir.discoverSmartConfig(iss));

      const authorizeUrl = config.authorization_endpoint;
      const tokenEndpoint = config.token_endpoint;
      if (!authorizeUrl || !tokenEndpoint) {
        throw new Error(this.translate.instant('smart_sample_app.error_missing_endpoints'));
      }

      sessionStorage.setItem('smart_sample_token_endpoint', tokenEndpoint);
      sessionStorage.setItem('smart_sample_client_id', clientId);
      sessionStorage.setItem('smart_sample_redirect_uri', redirectUri);

      const state = crypto.randomUUID();
      sessionStorage.setItem('smart_sample_state', state);

      const authParams = new URLSearchParams({
        response_type: 'code',
        client_id: clientId,
        redirect_uri: redirectUri,
        scope,
        state,
        aud: iss,
      });
      if (launchToken) {
        authParams.set('launch', launchToken);
      }
      if (simError) {
        authParams.set('sim_error', simError);
      }

      this.status = 'redirecting';
      window.location.href = `${authorizeUrl}?${authParams.toString()}`;
    } catch (e: unknown) {
      this.status = 'error';
      if (e instanceof HttpErrorResponse) {
        this.errorMessage = this.translate.instant('smart_sample_app.error_discovery_failed', { status: e.status });
      } else {
        this.errorMessage = e instanceof Error ? e.message : String(e);
      }
    }
  }
}
