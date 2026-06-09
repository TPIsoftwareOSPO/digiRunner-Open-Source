import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, SmartSelection } from '../smart-shared/smart-fhir-client.service';

type ConsentState = 'loading' | 'ready' | 'no-state' | 'error' | 'auto-submit';

@Component({
  selector: 'app-smart-consent',
  templateUrl: './smart-consent.component.html',
  styleUrls: ['./smart-consent.component.scss'],
  standalone: false,
})
export class SmartConsentComponent implements OnInit {

  state: string = '';
  consentState: ConsentState = 'loading';

  clientName: string = '';
  scopes: { scope: string; description: string }[] = [];

  /** 從 sessionStorage 帶過來的選取結果 */
  selection: SmartSelection = {};

  readonly approveUrl = '/dgrv4/ssotoken/smart/approve';
  readonly denyUrl = '/dgrv4/ssotoken/smart/deny';

  /** 自動送出用的隱藏表單 ref */
  @ViewChild('autoForm') autoForm?: ElementRef<HTMLFormElement>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fhir: SmartFhirClientService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const state = params['state'];
      if (!state) {
        this.consentState = 'no-state';
        return;
      }
      this.state = state;
      this.selection = this.fhir.loadSelection(state);
      this.dispatch();
    });
  }

  /**
   * 入口 dispatcher：依需求轉到下一個未滿足的 picker，或進入 consent UI / 自動送出。
   *
   * 「未滿足」= backend 說 needs* 為 true 且 sessionStorage 尚未存到該選擇。
   * Back 是由各頁直接 router.navigate 到目標頁，不會繞回 consent dispatcher。
   */
  private dispatch(): void {
    this.fhir.getConsentInfo(this.state).subscribe({
      next: (info: ConsentInfo) => {
        // 缺 patient → 轉去 patient 選取頁
        if (info.needsPatientSelection && !this.selection.patientId) {
          this.router.navigate(['/smart/select-patient'], { queryParams: { state: this.state } });
          return;
        }
        // 缺 provider → 轉去 provider 選取頁
        if (info.needsProviderSelection && !this.selection.fhirUser) {
          this.router.navigate(['/smart/select-provider'], { queryParams: { state: this.state } });
          return;
        }

        // 都齊了：autoApprove=Y 自動送 approve，N 顯示 consent 畫面
        this.clientName = info.clientName;
        this.scopes = info.scopes;

        if (info.autoApprove) {
          this.consentState = 'auto-submit';
          // Angular 渲染完隱藏表單後送出
          setTimeout(() => this.autoForm?.nativeElement?.submit(), 0);
        } else {
          this.consentState = 'ready';
        }
      },
      error: () => this.consentState = 'error',
    });
  }


  get approvedScopes(): string {
    return this.scopes.map(s => s.scope).join(' ');
  }

  /** 送出前清掉 sessionStorage，避免下次流程沿用舊資料 */
  onSubmit(): void {
    this.fhir.clearSelection(this.state);
  }
}
