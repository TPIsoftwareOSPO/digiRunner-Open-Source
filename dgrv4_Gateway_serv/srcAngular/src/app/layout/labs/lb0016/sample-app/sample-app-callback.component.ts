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

    try {
      // 外部 App 視角：以 HttpClient 交換 token（無 admin auth）
      const form: Record<string, string> = {
        grant_type: 'authorization_code',
        code,
        redirect_uri: redirectUri,
        client_id: clientId,
      };
      if (simError) form['sim_error'] = simError;

      const data = await firstValueFrom(this.fhir.postToken(tokenEndpoint, form));
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

  /** 從 HttpErrorResponse / Error 取出可顯示的錯誤訊息（保留 token 端點的 error_description） */
  private extractError(e: unknown): string {
    if (e instanceof HttpErrorResponse) {
      const body = e.error as SmartTokenResponse | undefined;
      return body?.error_description || body?.error || e.message;
    }
    return e instanceof Error ? e.message : String(e);
  }

  private decodeJwt(token: string | undefined): string {
    if (!token) return '';
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return '(not a JWT)';
      const payload: unknown = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
      return JSON.stringify(payload, null, 2);
    } catch {
      return '(decode failed)';
    }
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
      const data = await firstValueFrom(this.fhir.postToken(tokenEndpoint, form));
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
