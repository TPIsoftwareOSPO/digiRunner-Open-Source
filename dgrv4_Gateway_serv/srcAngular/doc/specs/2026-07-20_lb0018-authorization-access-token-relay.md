# lb0018 Authorization 分頁取得的 access_token 記錄並帶入 Patient Dashboard 查詢

延續前案 [`2026-07-16_lb0018-patient-dashboard-auth-flow.md`](./2026-07-16_lb0018-patient-dashboard-auth-flow.md)。前案建立了 lb0018 的兩段式流程（區塊一 FHIR Base URL 驗證 / well-known 取得 token_endpoint，Authorization 分頁仿 api-test 測試取 token，Tab 2 Patient Dashboard 查詢病人與 `$everything`）。本次補上「取得 token → 記錄 → 帶入查詢」的串接，讓 Authorization 分頁拿到的 `access_token` 自動用於 Patient Dashboard 的 FHIR 查詢。

## 修改內容

### 問題描述

依對話順序，使用者提出的需求：

1. **主需求**：在 lb0018 的 Authorization 分頁，若回應拿回 `access_token`，請記錄下來；之後打 Patient Dashboard 時要帶入這個 token。請設計一個流程。
2. **追加需求一**：把「驗證階段」與「查詢階段」的 HTTP header 拆開（驗證階段不要帶查詢用的 token）。
3. **追加需求二**：在 Authorization 區塊，如果原本已有記錄的 token，按下「測試」打 API 的動作時要先清除既有 token 紀錄。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | 新增 `accessToken` / `tokenType` 欄位、`captureAccessToken()`、`clearAccessToken()`；拆分 `bypassHeaders()`（驗證）與新增 `fhirQueryHeaders()`（查詢帶 token）；`bindResResult()` 成功時擷取 token；`send()` 打 API 前清除既有 token |
| `src/app/layout/labs/lb0018/lb0018.component.html` | Authorization 回應區新增「已取得 token」標籤＋清除鈕；Patient Dashboard 搜尋卡新增「查詢將帶入 token」提示 |
| `src/assets/i18n/zh-tw.json`、`zh-cn.json`、`en-us.json`、`ja.json` | 新增 `fhir_viewer.token_captured`、`token_clear`、`token_in_use` 三個 key |

### 詳細程式碼異動

#### 1. 新增 token 狀態欄位

```ts
// 修改後（新增於 smartConfig 欄位之後）
accessToken: string | null = null;
tokenType: string = 'Bearer';
```

#### 2. 擷取 access_token（成功回應時）

`bindResResult()` 設定完 resBody / resStatus / headerList 後新增：

```ts
// 成功回應時嘗試擷取 access_token，供 Patient Dashboard 的 FHIR 查詢帶入
this.captureAccessToken(data.body, data.status);
```

新增方法：

```ts
private captureAccessToken(bodyText: string, status: number): void {
  if (status < 200 || status >= 300 || !bodyText) return;
  try {
    const json = JSON.parse(bodyText);
    const token = json?.access_token;
    if (typeof token === 'string' && token) {
      this.accessToken = token;
      const type = json?.token_type;
      this.tokenType =
        typeof type === 'string' && type.toLowerCase() !== 'bearer' && type.trim() !== ''
          ? type
          : 'Bearer';
    }
  } catch {
    // 回應非 JSON（或不含 access_token）時不更新既有 token
  }
}

clearAccessToken(): void {
  this.accessToken = null;
  this.tokenType = 'Bearer';
}
```

#### 3. 驗證 / 查詢 header 拆分（追加需求一）

```ts
// 修改前：bypassHeaders() 內就地判斷 accessToken 並 append Authorization
// （驗證階段與查詢階段共用同一組 header）

// 修改後：bypassHeaders() 還原為不帶 token 的基礎版（驗證階段用）
private bypassHeaders(): HttpHeaders {
  return new HttpHeaders({
    digiRunner: 'Test Process',
    'X-Smart-Launcher': 'true',
    Accept: 'application/fhir+json, application/json',
  });
}

// 新增：查詢階段專用，有 token 才 append Authorization
private fhirQueryHeaders(): HttpHeaders {
  let headers = this.bypassHeaders();
  if (this.accessToken) {
    headers = headers.append('Authorization', `${this.tokenType} ${this.accessToken}`);
  }
  return headers;
}
```

呼叫端改動：

- `loadPatientPage()` 的病人清單 GET：`this.bypassHeaders()` → `this.fhirQueryHeaders()`
- `fetchEverythingPage()` 的 `$everything` GET：`this.bypassHeaders()` → `this.fhirQueryHeaders()`
- `refreshFhirBaseUrl()` 的 `metadata` HEAD、well-known GET：維持 `this.bypassHeaders()`（不帶 token）

#### 4. 打 API 前清除既有 token（追加需求二）

`send()` 於通過欄位驗證、發出請求前：

```ts
// 每次實際打 API 前先清除既有 access_token；若本次成功取得新 token，會於回應時重新記錄
this.clearAccessToken();

this.startTimestamp = new Date().getTime();
this.ngxService.start();
this.httpClientHandle(path, header, body);
```

#### 5. HTML 狀態提示

Authorization 回應區塊（resBody 之後）：

```html
@if (accessToken) {
  <div class="field-flex-start mt-3">
    <span class="pd-tag" style="background:#00824c">
      <i class="fa fa-check me-1"></i>{{ 'fhir_viewer.token_captured' | translate }}
    </span>
    <button type="button" class="btn-icon tpi-cancel" title="{{ 'fhir_viewer.token_clear' | translate }}"
      (click)="clearAccessToken()">
      <i class="fa fa-times"></i>
    </button>
  </div>
}
```

Patient Dashboard 搜尋卡（結果統計 small 之前）：

```html
@if (accessToken) {
  <small class="d-block mt-1" style="color:#00824c">
    <i class="fa fa-key me-1"></i>{{ 'fhir_viewer.token_in_use' | translate }}
  </small>
}
```

## 預計修改方式

### 需求一：記錄 token 並帶入查詢

**Context** — 使用者在 lb0018.component.html 開啟的情況下，說明 Authorization 若拿回 access_token 要記錄，之後打 patient dashboard 要帶入，請設計流程。

**分析** — 讀 `lb0018.component.ts` 確認：
- Authorization 分頁的 `send()` → `httpClientHandle()`，回應以 `responseType: 'text'` 進入 `bindResResult()`，內文字串寫入表單 `resBody`。OAuth `client_credentials` 的 token endpoint 回應為 JSON（含 `access_token`、`token_type`）。
- Patient Dashboard 的病人清單（`loadPatientPage`）、`$everything`（`fetchEverythingPage`）、以及區塊一驗證（`refreshFhirBaseUrl` 的 metadata HEAD、well-known GET）都共用 `bypassHeaders()`，且該方法原本把 `Authorization` 註解掉（僅留 `digiRunner` / `X-Smart-Launcher` / `Accept`），註解提到「比照 lb0016 讓 gateway 的 /smart-on-fhir/* 通過入站驗證」。

**修正方案** — 在 `bindResResult()` 成功時以 `captureAccessToken()` 解析內文擷取 token（僅 2xx 且能解析出 access_token 才更新，否則保留既有），並在 `bypassHeaders()` 有 token 時 append Bearer。i18n key 依 `.claude/rules/i18n.md` 四份語系檔同步新增，放在既有 `fhir_viewer.token_endpoint` 之後。

### 需求二：驗證 / 查詢 header 拆開

**Context** — 首版把 token 判斷寫在共用的 `bypassHeaders()`，導致區塊一的 metadata / well-known 驗證在有 token 時也會帶 Authorization。使用者要求拆開。

**分析** — grep `bypassHeaders()` 呼叫點共四處：`wellKnownUrl` GET（驗證）、`metadata` HEAD（驗證）、病人清單 GET（查詢）、`$everything` GET（查詢）。前兩者屬驗證階段，後兩者屬查詢階段。

**修正方案** — `bypassHeaders()` 還原為不帶 token 的基礎 header；新增 `fhirQueryHeaders()` 在其上疊加 token，僅兩個查詢呼叫點改用。驗證階段完全不帶查詢用 token。

### 需求三：打 API 前清除既有 token

**Context** — 使用者要求：Authorization 區塊若已有記錄 token，按「測試」打 API 時要先清除。

**分析** — `send()` 開頭已呼叫 `clearResponse()`（僅清回應顯示，不含 token）；欄位驗證（clientId / clientSecret / URL / header key）失敗時會提前 return。

**修正方案** — 將 `clearAccessToken()` 放在**通過所有驗證之後、`httpClientHandle()` 之前**，使其只在真正發出 API 請求時才清除；驗證未過（未打 API）時保留既有 token。新 token 於回應時由 `captureAccessToken()` 重新記錄。

## 驗證方式

### 靜態驗證

- IDE 診斷（VS Code / Angular Language Service）：`lb0018.component.ts` 與 `lb0018.component.html` 皆回傳空 diagnostics（無型別 / 模板錯誤）。四份語系檔以精準區塊替換新增 key。
- `npm run build`（production）：於本次提交前執行（結果見「時間軸」與「尚待人工驗證」，實際 exit code 以終端輸出為準）。

### 尚待人工驗證（建議）

1. 開啟 lb0018 → 區塊一選 / 輸入有效 FHIR Base URL，取得 well-known 與 token_endpoint。
2. Authorization 分頁填入 client_id / client_secret，按「測試」；回應為 2xx 且含 `access_token` 時，回應區應出現綠色「已取得 access_token」標籤。
3. 切到 Patient Dashboard 分頁，搜尋卡應顯示「查詢將帶入已取得的 access_token」；以瀏覽器 DevTools 檢查病人清單 / `$everything` 請求的 `Authorization: Bearer ...` 已帶上，而區塊一的 metadata / well-known 請求**未**帶。
4. 再次按「測試」（打 API 前）：既有 token 應先被清除；若本次回應非 2xx 或不含 access_token，Dashboard 不再帶 token。
5. 按標籤旁清除鈕：token 標籤消失，後續查詢不帶 Authorization。

## 實作紀錄

### 時間軸

1. 使用者提出主需求（記錄 token 並帶入 patient dashboard 查詢）。
2. 讀 `lb0018.component.ts`、`patient-dashboard.helpers.ts`、`lb0018.component.html` 釐清 send/回應流程與 `bypassHeaders()` 的共用情形。
3. 第一版實作：直接在共用的 `bypassHeaders()` 內判斷 `accessToken` 並 append Authorization；`bindResResult()` 呼叫新增的 `captureAccessToken()`；HTML 加 token 標籤與 Dashboard 提示；四份語系檔加 key。IDE 診斷通過。
4. 回報時主動提示副作用：`bypassHeaders()` 也被驗證階段共用，token 存在時 metadata / well-known 也會帶 Authorization，並詢問是否要拆開。
5. 使用者回覆要求拆開 → 將 `bypassHeaders()` 還原為驗證用（不帶 token），新增 `fhirQueryHeaders()` 供兩個查詢呼叫點使用。grep 確認四個呼叫點分屬正確階段，IDE 診斷通過。
6. 使用者追加需求：打 API 前先清除既有 token → 於 `send()` 驗證後、送出前加入 `clearAccessToken()`。
7. 產出本工作紀錄；接著執行 `npm run build` 並於成功後提交 / 推送至 `feature/kevincc-smartonfhir1004`。

### 經驗紀錄（給未來類似任務參考）

- **共用 header 方法要留意呼叫點的階段差異**：`bypassHeaders()` 同時服務「驗證探針」與「資料查詢」兩種語意。把認證資訊塞進共用方法會外溢到不需要的請求；以「基礎 header + 疊加」的方式（`fhirQueryHeaders()` 建立在 `bypassHeaders()` 之上）拆分，比複製整組 header 更精簡、也保留單一來源。
- **「清除既有狀態」要放對位置**：需求是「按下測試打 API 時清除」，放在 `send()` 最前面會連帶在欄位驗證失敗（未打 API）時也清除；放在通過驗證後、送出前，才精確對應「真正打 API」的語意。
- **解析外部回應要防禦**：token endpoint 回應可能非 JSON 或不含 `access_token`，`captureAccessToken()` 以 try/catch 與 2xx 檢查把關，失敗時保留既有 token 而非清成 null。
- i18n 新增 key 一律四份語系檔（zh-tw / zh-cn / en-us / ja）同步，插在同區塊既有 key 之後，避免執行期顯示原始 key 字串。
