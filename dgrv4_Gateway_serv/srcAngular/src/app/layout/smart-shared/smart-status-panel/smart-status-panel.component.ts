import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { SmartPanelStatus, SMART_MESSAGE_KEYS } from '../smart-error';

/**
 * SMART picker 四個頁面共用的狀態／錯誤面板。
 *
 * 只渲染 .form-wrapper 區塊本身，必須放在父層的 .login-center-page 內
 * （樣式來自全域 src/styles/layout/_login-center.scss，非元件 scoped）。
 *
 * 刻意不引入 SharedModule：這四頁是未登入的外部使用者會看到的公開頁面，
 * 引入會把整個後台元件樹與 PrimeNG 拉進這個 lazy chunk。
 */
@Component({
  selector: 'app-smart-status-panel',
  templateUrl: './smart-status-panel.component.html',
  imports: [TranslateModule],
  standalone: true,
})
export class SmartStatusPanelComponent {
  @Input() status: SmartPanelStatus = 'loading';

  get mainKey(): string {
    return SMART_MESSAGE_KEYS[this.status].main;
  }

  get subKey(): string | null {
    return SMART_MESSAGE_KEYS[this.status].sub;
  }
}
