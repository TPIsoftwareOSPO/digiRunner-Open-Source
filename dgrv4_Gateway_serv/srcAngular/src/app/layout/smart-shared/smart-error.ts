import { HttpErrorResponse } from '@angular/common/http';

/**
 * SMART picker 的錯誤分類。
 * 決定畫面顯示哪段文案，以及該錯誤是否致命（需中斷整個授權流程）。
 */
export type SmartErrorKind = 'no-state' | 'expired' | 'network' | 'server';

/** 共用狀態面板可顯示的狀態（載入中 + 四種錯誤） */
export type SmartPanelStatus = 'loading' | SmartErrorKind;

/** proxyGet 缺少 state 時丟出的哨兵錯誤訊息 */
export const SMART_MISSING_STATE = 'smart_missing_state';

/**
 * 狀態 → i18n key 對照。
 * 狀態面板與清單內聯錯誤共用同一份對照，確保四個頁面文案一致。
 */
export const SMART_MESSAGE_KEYS: Record<SmartPanelStatus, { main: string; sub: string | null }> = {
  'loading': { main: 'smart_picker.loading', sub: null },
  'no-state': { main: 'smart_picker.no_state', sub: 'smart_picker.no_state_hint' },
  'expired': { main: 'smart_picker.expired', sub: 'smart_picker.expired_hint' },
  'network': { main: 'smart_picker.network_error', sub: 'smart_picker.network_error_hint' },
  'server': { main: 'smart_picker.server_error', sub: 'smart_picker.server_error_hint' },
};

/** 本流程會遇到的兩種錯誤 body 形狀：DGR 的 {error} 與 FHIR 的 OperationOutcome */
interface SmartErrorBody {
  error?: string;
  resourceType?: string;
}

/**
 * 把 HTTP 失敗歸類成四種使用者可理解的情境。
 *
 * 判斷順序刻意「先看 body 再看 status」：上游 FHIR server 自己回的 400/401
 * （body 為 OperationOutcome）與「授權 state 失效」是完全不同的事，
 * 若只看 status code 會把前者誤報成「授權已逾時」，誤導使用者重跑整個授權流程。
 *
 * 後端契約依據：
 * - consent-info state 失效 → 400 + {error:'invalid_state'}（SmartConsentController）
 * - proxy 入站驗證失敗 → 401 + {error:'invalid_token'}（SmartOnFhirProxyService）
 */
export function classifySmartError(err: unknown): SmartErrorKind {
  // proxyGet 的前端防呆（缺 state），與後端回的授權失效要分開，避免把前端 bug 顯示成「授權逾時」
  if (err instanceof Error && err.message === SMART_MISSING_STATE) {
    return 'no-state';
  }

  // 非 HttpErrorResponse：例如 state 含非 ISO-8859-1 字元導致 setRequestHeader 同步 throw
  if (!(err instanceof HttpErrorResponse)) {
    return 'server';
  }

  // status 0：離線 / DNS 失敗 / 連線被中斷，瀏覽器不提供更多細節
  if (err.status === 0) {
    return 'network';
  }

  const body = err.error as SmartErrorBody | string | null;
  const jsonBody = body && typeof body === 'object' ? body : null;

  // 上游 FHIR server 的錯誤原樣透傳，與 state 無關
  if (jsonBody?.resourceType === 'OperationOutcome') {
    return 'server';
  }

  if (jsonBody?.error === 'invalid_state' || jsonBody?.error === 'invalid_token') {
    return 'expired';
  }

  // 後備：proxy 的 state 旁路沒過就是 401。
  // 403 不列入 —— 安全檢查（IP 黑名單等）也會回 403，那不是 state 的問題。
  if (err.status === 401) {
    return 'expired';
  }

  return 'server';
}
