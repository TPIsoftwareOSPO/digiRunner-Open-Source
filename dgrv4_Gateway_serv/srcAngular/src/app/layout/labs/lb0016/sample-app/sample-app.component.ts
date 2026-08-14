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
    // PKCE 模式：none 不帶（可示範 public client 被 authorize 拒絕）；auto / always 帶 S256
    const pkceMode = params['pkce'] || 'auto';
    // confidential demo：lb0016 直接把憑證帶過來，換 token 時附上（見 callback）。
    // symmetric → client_secret；asymmetric → private_key（簽 client assertion 用）。
    // 純瀏覽器環境無法保密憑證，此為 launcher 測試工具的刻意取捨。
    const clientSecret = params['client_secret'] || '';
    const privateKey = params['private_key'] || '';

    sessionStorage.setItem('smart_sample_token_endpoint', '');
    sessionStorage.setItem('smart_sample_iss', iss);
    sessionStorage.setItem('smart_sample_sim_error', simError);
    sessionStorage.setItem('smart_sample_client_secret', clientSecret);
    sessionStorage.setItem('smart_sample_private_key', privateKey);
    sessionStorage.removeItem('smart_sample_pkce_verifier');

    this.discover(iss, clientId, redirectUri, scope, launchToken, simError, pkceMode);
  }

  private async discover(iss: string, clientId: string, redirectUri: string, scope: string, launchToken: string, simError: string, pkceMode: string): Promise<void> {
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

      // PKCE S256（RFC 7636）：後端 discovery 只廣告 S256，public client 未帶 code_challenge 會被拒
      if (pkceMode !== 'none') {
        const verifier = this.generateCodeVerifier();
        const challenge = await this.computeS256Challenge(verifier);
        if (challenge) {
          sessionStorage.setItem('smart_sample_pkce_verifier', verifier);
          authParams.set('code_challenge', challenge);
          authParams.set('code_challenge_method', 'S256');
        } else if (pkceMode === 'always') {
          // crypto.subtle 只在 secure context（https / localhost）可用
          throw new Error(this.translate.instant('smart_sample_app.error_pkce_unavailable'));
        }
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

  // ==================== PKCE（RFC 7636） ====================

  /** 產生 code_verifier：32 bytes 亂數 → base64url（43 字元） */
  private generateCodeVerifier(): string {
    const bytes = new Uint8Array(32);
    crypto.getRandomValues(bytes);
    return this.base64UrlEncode(bytes);
  }

  /** 計算 S256 code_challenge；非 secure context（無 crypto.subtle）時回傳 null */
  private async computeS256Challenge(verifier: string): Promise<string | null> {
    if (!crypto.subtle) return null;
    const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier));
    return this.base64UrlEncode(new Uint8Array(digest));
  }

  private base64UrlEncode(bytes: Uint8Array): string {
    let binary = '';
    bytes.forEach(b => binary += String.fromCharCode(b));
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }
}
