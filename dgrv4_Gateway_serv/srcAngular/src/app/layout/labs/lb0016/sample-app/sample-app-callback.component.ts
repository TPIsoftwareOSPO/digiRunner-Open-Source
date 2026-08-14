import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { SmartFhirService } from '../smart-fhir.service';
import { SmartTokenResponse, Patient } from '../smart-fhir.types';

@Component({
  selector: 'app-sample-app-callback',
  templateUrl: './sample-app-callback.component.html',
  styleUrls: ['./sample-app-callback.component.scss'],
  standalone: false,
})
export class SampleAppCallbackComponent implements OnInit {

  status: 'loading' | 'ready' | 'error' = 'loading';
  errorMessage: string = '';

  tokenResponse: SmartTokenResponse = {};
  accessTokenDecoded: string = '';
  idTokenDecoded: string = '';

  patientData: Patient | null = null;
  patientLoading: boolean = false;

  fhirBaseUrl: string = '';

  constructor(
    private route: ActivatedRoute,
    private translate: TranslateService,
    private fhir: SmartFhirService,
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    const code = params['code'];
    const state = params['state'];
    const error = params['error'];

    if (error) {
      this.status = 'error';
      this.errorMessage = params['error_description'] || error;
      return;
    }

    if (!code) {
      this.status = 'error';
      this.errorMessage = this.translate.instant('smart_sample_app.error_no_code');
      return;
    }

    const savedState = sessionStorage.getItem('smart_sample_state');
    if (state && savedState && state !== savedState) {
      this.status = 'error';
      this.errorMessage = this.translate.instant('smart_sample_app.error_state_mismatch');
      return;
    }

    this.fhirBaseUrl = sessionStorage.getItem('smart_sample_iss') || '';
    this.exchangeCode(code);
  }

  private async exchangeCode(code: string): Promise<void> {
    const tokenEndpoint = sessionStorage.getItem('smart_sample_token_endpoint') || '';
    const clientId = sessionStorage.getItem('smart_sample_client_id') || '';
    const redirectUri = sessionStorage.getItem('smart_sample_redirect_uri') || '';

    if (!tokenEndpoint) {
      this.status = 'error';
      this.errorMessage = this.translate.instant('smart_sample_app.error_token_endpoint_missing');
      return;
    }

    const simError = sessionStorage.getItem('smart_sample_sim_error') || '';
    // PKCE：auth code 為一次性，verifier 讀出後即清除（重試也不會成功）
    const codeVerifier = sessionStorage.getItem('smart_sample_pkce_verifier') || '';
    sessionStorage.removeItem('smart_sample_pkce_verifier');

    try {
      // 外部 App 視角：以 HttpClient 交換 token（無 admin auth）
      const form: Record<string, string> = {
        grant_type: 'authorization_code',
        code,
        redirect_uri: redirectUri,
        client_id: clientId,
      };
      if (codeVerifier) form['code_verifier'] = codeVerifier;
      if (simError) form['sim_error'] = simError;
      // confidential demo：附上 client 憑證（symmetric secret 或 asymmetric assertion）。aud = token 端點。
      const basicAuth = await this.applyClientAuth(form, clientId, tokenEndpoint);

      const data = await firstValueFrom(this.fhir.postToken(tokenEndpoint, form, basicAuth));
      if (data.error) {
        this.status = 'error';
        this.errorMessage = data.error_description || data.error;
        return;
      }

      this.tokenResponse = data;
      this.accessTokenDecoded = this.decodeJwt(data.access_token);
      this.idTokenDecoded = this.decodeJwt(data.id_token);
      this.status = 'ready';

      if (data.patient && this.fhirBaseUrl) {
        this.loadPatient(data.patient, data.access_token || '');
      }
    } catch (e: unknown) {
      this.status = 'error';
      this.errorMessage = this.extractError(e);
    }
  }

  /**
   * confidential demo：依可用憑證完成 client 認證，回傳 Basic 憑證（若適用）。
   *
   * - asymmetric（sessionStorage 有 private_key）：用私鑰簽 private_key_jwt client assertion，
   *   放入 form 的 client_assertion / client_assertion_type；不回 Basic。aud 用傳入的 audience（token 端點）。
   * - symmetric（有 client_secret）：body 塞 client_secret + 回傳 Basic 憑證，兩者都給，
   *   讓 client 不論註冊成 client_secret_basic / client_secret_post 都能通過。
   * - public / 未帶憑證：不動 form、回傳 undefined。
   *
   * @param audience assertion 的 aud（此處為 token 端點 URL，需與後端逐字相符）
   * @returns base64("client_id:client_secret") 供 Authorization: Basic 使用；不適用時 undefined
   */
  private async applyClientAuth(form: Record<string, string>, clientId: string, audience: string): Promise<string | undefined> {
    const privateKey = sessionStorage.getItem('smart_sample_private_key') || '';
    if (privateKey) {
      form['client_assertion_type'] = 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer';
      form['client_assertion'] = await this.fhir.signClientAssertion(clientId, audience, privateKey);
      return undefined;
    }
    const clientSecret = sessionStorage.getItem('smart_sample_client_secret') || '';
    if (clientSecret) {
      form['client_secret'] = clientSecret;
      return btoa(`${clientId}:${clientSecret}`);
    }
    return undefined;
  }

  /** 從 HttpErrorResponse / Error 取出可顯示的錯誤訊息（保留 token 端點的 error_description） */
  private extractError(e: unknown): string {
    if (e instanceof HttpErrorResponse) {
      const body = e.error as SmartTokenResponse | undefined;
      return body?.error_description || body?.error || e.message;
    }
    return e instanceof Error ? e.message : String(e);
  }

  private decodeJwt(token: string | undefined): string {
    return this.fhir.decodeJwtPayload(token);
  }

  private async loadPatient(patientId: string, accessToken: string): Promise<void> {
    this.patientLoading = true;
    try {
      const url = `${this.fhirBaseUrl}/Patient/${patientId}`;
      const simErr = sessionStorage.getItem('smart_sample_sim_error') || '';
      // 用 App 自身 access token 取 Patient（非 admin token）
      this.patientData = await firstValueFrom(this.fhir.loadPatientWithToken(url, accessToken, simErr || undefined));
    } catch {
    } finally {
      this.patientLoading = false;
    }
  }

  async refreshToken(): Promise<void> {
    if (!this.tokenResponse.refresh_token) return;
    const tokenEndpoint = sessionStorage.getItem('smart_sample_token_endpoint') || '';
    const clientId = sessionStorage.getItem('smart_sample_client_id') || '';

    try {
      const form: Record<string, string> = {
        grant_type: 'refresh_token',
        refresh_token: this.tokenResponse.refresh_token,
        client_id: clientId,
      };
      const simErr = sessionStorage.getItem('smart_sample_sim_error') || '';
      if (simErr) form['sim_error'] = simErr;
      // confidential：refresh 亦需 client 認證（symmetric secret 或 asymmetric assertion）。aud = token 端點。
      const basicAuth = await this.applyClientAuth(form, clientId, tokenEndpoint);
      const data = await firstValueFrom(this.fhir.postToken(tokenEndpoint, form, basicAuth));
      if (data.access_token) {
        this.tokenResponse = { ...this.tokenResponse, ...data };
        this.accessTokenDecoded = this.decodeJwt(data.access_token);
        if (data.id_token) this.idTokenDecoded = this.decodeJwt(data.id_token);
      }
    } catch {
    }
  }

  get contextEntries(): { key: string; value: string }[] {
    const keys: (keyof SmartTokenResponse)[] = ['patient', 'encounter', 'scope', 'token_type', 'expires_in'];
    return keys
      .filter(k => this.tokenResponse[k] != null)
      .map(k => ({ key: k, value: String(this.tokenResponse[k]) }));
  }
}
