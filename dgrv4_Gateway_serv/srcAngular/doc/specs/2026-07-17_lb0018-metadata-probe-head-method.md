# lb0018 Fhir Viewer：FHIR Base URL 探針由 GET /metadata 改為 HEAD

延續 [`2026-07-16_lb0018-manual-refresh-buttons.md`](./2026-07-16_lb0018-manual-refresh-buttons.md)（FHIR Base URL 改為手動刷新）與 [`2026-07-17_lb0018-refresh-clears-and-reloads-dashboard.md`](./2026-07-17_lb0018-refresh-clears-and-reloads-dashboard.md)（刷新後同步清空／重載 Patient Dashboard）。前兩案確立了 `refreshFhirBaseUrl()` 的觸發時機與後續連動，本次只改動其中「如何探測 base URL」這一段。

## 修改內容

### 問題描述

使用者提出：lb0018 用 GET 取 metadata 的探針流程，請評估改用 HEAD 方法；並明確表示「如果有回 405 的風險也沒關係，因為那也代表網站存在」——即接受把判定標準從「這是有效的 FHIR server」放寬為「這個站台存在」。

評估後使用者追加指示：先確認 gateway 的 CORS（因為 gateway 本身是一個代理，應該能處理 CORS 問題）。CORS 確認無虞後，定案為「改成 HEAD，405/401/403 都算存在，註解一併更新」。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | `refreshFhirBaseUrl()` 探針由 `get<FhirResource>` 改為 `head` + `observe: 'response'`；判定改看 HTTP status，401/403/405 視為存在；移除 `resourceType === 'CapabilityStatement'` 檢查；`HttpErrorResponse` 加入 import；探針與 `bypassHeaders()` 註解更新 |

本次未改動後端 Java 程式碼（僅為確認 CORS 而讀取）、未新增 i18n key、未調整 template。

### 詳細程式碼異動

#### 1. import 補上 `HttpErrorResponse`

```ts
// 修改前
import { HttpClient, HttpHeaders } from '@angular/common/http';

// 修改後
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
```

#### 2. `refreshFhirBaseUrl()` 探針改為 HEAD

修改前——以 GET 取回 CapabilityStatement 並檢查 `resourceType`：

```ts
/**
 * 手動刷新 FHIR Base URL（由輸入框旁的刷新鈕、或選取 proxy 下拉時觸發）：
 * 以 GET {base}/metadata 確認是否為有效的 FHIR base url，
 * 驗證成功才組出 well-known URL，觸發後續的 well-known 驗證鏈取得 token_endpoint。
 * 註：輸入時不自動驗證，避免每次 keydown 都打 metadata。
 */
this.httpClient
  .get<FhirResource>(`${base}/metadata`, { headers: this.bypassHeaders() })
  .pipe(
    // 僅回 2xx 不足以認定是 FHIR server（任何網頁都可能回 200），
    // 必須確認回應確實是 CapabilityStatement 資源
    map((res) => res?.resourceType === 'CapabilityStatement'),
    catchError(() => of(false)),
    finalize(() => this.endCheckLoading()),
    takeUntil(this.destroy$),
  )
```

修改後——以 HEAD 判定站台存在，依 status 分類：

```ts
/**
 * 手動刷新 FHIR Base URL（由輸入框旁的刷新鈕、或選取 proxy 下拉時觸發）：
 * 以 HEAD {base}/metadata 確認站台是否存在，確認成功才組出 well-known URL，
 * 觸發後續的 well-known 驗證鏈取得 token_endpoint。
 * 註：HEAD 不回內文，故本段僅能判定「站台存在」，無法確認對方確實是 FHIR server
 *（改用 HEAD 是為了省下 CapabilityStatement 的龐大回應；
 * 是否為 FHIR server 交由後續 well-known 驗證鏈把關）。
 * 註：輸入時不自動驗證，避免每次 keydown 都打 metadata。
 */
this.httpClient
  .head(`${base}/metadata`, { headers: this.bypassHeaders(), observe: 'response' })
  .pipe(
    map(() => true),
    // 405（不接受 HEAD）、401/403（需驗證）都代表端點確實存在，一律視為通過；
    // 404 與 status 0（CORS / DNS / 連線失敗）才視為不存在
    catchError((err: HttpErrorResponse) => of([401, 403, 405].includes(err.status))),
    // finalize 在完成或被取消時都會執行，確保 loading 一定會關閉
    finalize(() => this.endCheckLoading()),
    takeUntil(this.destroy$),
  )
```

`.subscribe((ok) => {...})` 的內容（設定 `baseUrlNotFound`、組 well-known URL、`clearPatientDashboard()`、Tab 2 時重查）完全未動。

#### 3. `bypassHeaders()` 註解更新

該方法同時被 metadata 探針（現為 HEAD）、well-known 驗證鏈、病人清單與 `$everything` 查詢（皆為 GET）共用，原註解寫死「GET」已不準確：

```ts
// 修改前
/**
 * 區塊一驗證用 GET 的共用 headers：

// 修改後
/**
 * 區塊一驗證與 FHIR 查詢請求的共用 headers：
```

## 預計修改方式

### 需求：GET /metadata 探針改為 HEAD

**Context**
使用者指出 lb0018 的 `refreshFhirBaseUrl()` 用 GET 打 `{base}/metadata` 當探針，希望評估改 HEAD，並預先聲明可接受 405（405 亦代表站台存在）。

**分析**

讀 `src/app/layout/labs/lb0018/lb0018.component.ts:349-378` 確認現況：探針不是判斷「有無回應」，而是 `map((res) => res?.resourceType === 'CapabilityStatement')`，程式碼原註解明寫理由是「僅回 2xx 不足以認定是 FHIR server（任何網頁都可能回 200）」。因此改 HEAD 必然使此檢查失效（HEAD 無 body，`map` 恆得 `null`），判定語意會降級成「URL 存在」。

追查降級的連帶影響：`ok` 為 true 時會設定 `wellKnownUrl` 並在停留於 Tab 2 時觸發 `onSearchPatients()`。但 well-known 驗證鏈（同檔 `ngOnInit` 內，行 237-264）與病人清單查詢各自都有失敗處理與紅字提示（`wellKnownNotFound`、`patientsFailed`），故誤判的代價僅為「第一段綠燈、第二段才紅字」，不會有錯誤資料混入。

初次評估時提出的最大疑慮是 **CORS preflight**：`bypassHeaders()` 帶了 `digiRunner`、`X-Smart-Launcher` 等自訂 header，本就會觸發 preflight；若後端 `Access-Control-Allow-Methods` 未列 HEAD，preflight 會失敗並得到 status 0，而 status 0 無法區分「站台不存在」與「站台存在但不允許 HEAD」，屬假陰性，比 405 嚴重。使用者遂要求先確認 gateway CORS。

**修正方案（CORS 調查結論）**

逐層調查後端（僅讀取，未修改），確認 CORS 不會擋 HEAD：

1. `dgrv4_Gateway_serv/.../config/DisallowedMethodsHandlerFactory.java:40` — 方法黑名單只有 `TRACE`、`TRACK`，HEAD 不受影響。
2. `dgrv4_Gateway_serv/.../controller/WebsiteController.java:44-46` — `@CrossOrigin` + `@RequestMapping(value = "/smart-on-fhir/{websiteName}/**")`，未指定 `method`，故 HEAD 進得來，不會由 Spring 回 405。`@CrossOrigin` 未指定 `methods` 時 Spring 套用 `CorsConfiguration.applyPermitDefaultValues()`，其預設允許方法為 GET / HEAD / POST。
3. `dgrv4_Gateway_serv/.../service/CommForwardProcService.java:3042` — `getCorsAllowMethods()` 預設值為 `"POST,GET,OPTIONS,PUT,PATCH,DELETE"`，**確實沒有 HEAD**。但依 Fetch 規範的 CORS-preflight 檢查，只有在「方法不在 `Access-Control-Allow-Methods` 清單中 **且** 該方法不是 CORS-safelisted method」時才回 network error；HEAD 與 GET、POST 同屬 safelisted method，故清單未列它仍放行。初次評估的疑慮到此排除。
4. `dgrv4_Gateway_serv/.../service/SmartOnFhirProxyService.java:182` — 直接取 `request.getMethod()` 原樣往下傳，無方法白名單；行 655-656 另會剝除上游回應的 `access-control-*` header，統一由 gateway 注入，不會有上下游兩份 CORS header 衝突。
5. `dgrv4_Gateway_serv/.../service/SmartOnFhirProxyHttpClient.java:117` — 以 `builder.method(method.toUpperCase(), bodyPublisher)` 送出，`java.net.http` 支援 HEAD。
6. `dgrv4_Gateway_serv/.../service/SmartOnFhirProxyRoutingService.java:176-182` — 雖然該檔行 27 註解寫「GET/PUT/PATCH/DELETE 自動發現」，令人一度懷疑 HEAD 會走到不同分支；實際讀碼確認 `/metadata` 因 `requiresSticky()` 為 false 而直接 `selectByProbability` 回傳，根本不會進 autodiscovery，HEAD 與 GET 行為一致。

定案做法：`head()` + `observe: 'response'`，`map(() => true)` 處理 2xx，`catchError` 中以 `[401, 403, 405].includes(err.status)` 判定「端點存在但不接受 HEAD 或需驗證」，其餘（404、status 0）為不存在。選擇明列 401/403/405 而非「非 404 即存在」，是為了讓意圖顯性、避免把未預期的 5xx 也誤判為通過。

**評估中提出但未採用的替代方案**

若改 HEAD 的動機是 CapabilityStatement 回應過大（HAPI 的 metadata 可達數 MB），另一個選項是維持 GET 但改打 `{base}/metadata?_summary=true`：屬 FHIR 標準用法，回應大幅縮小，且 `resourceType` 檢查可原封不動保留，完全不觸及 CORS 與方法支援問題。已向使用者提出並說明此為首選，使用者選擇 HEAD（目的就是「快速確認站台活著」），故未採用。

## 驗證方式

### 靜態驗證

- `npm run build`（production build）通過，無 TypeScript / 編譯錯誤。輸出 `Build at: 2026-07-17T06:40:44.197Z - Hash: d2c3f398e239f79b - Time: 43948ms`。
- 確認 `FhirResource` type import 在移除探針的 `get<FhirResource>` 後仍被使用（`fetchEverythingPage()` 的 `filter((r): r is FhirResource => !!r)` 等處），未產生未使用 import。

### 尚待人工驗證（建議）

本環境無法對實際 gateway / 上游 FHIR server 發出請求，以下需人工確認：

1. **HEAD 探針對 proxy URL 正常** — 開啟 lb0018（Labs → Fhir Viewer），從 SMART on FHIR Proxy 下拉選一個 proxy，確認 base URL 帶入後不出現紅字，且 well-known URL 與 token_endpoint 正常帶出。可於瀏覽器 DevTools Network 確認該筆請求 method 為 HEAD、preflight OPTIONS 未被擋。
2. **上游是否支援 HEAD /metadata** — gateway 只原樣轉發，最終由上游 FHIR server 決定。HAPI FHIR 支援；若上游回 405，依本次定案應仍判定為通過（不出現紅字），需實測確認。
3. **404 仍正確報錯** — 手動填入一個不存在的 base URL，確認紅字（`baseUrlNotFound`）照常出現、Patient Dashboard 被清空。
4. **手動輸入外部 FHIR server 直連網址的情境** — `currentBaseUrl()`（行 695）只取輸入框的值，與是否使用 proxy 下拉無關；若填外部站台則不經 gateway，CORS 由對方決定，HEAD 未必放行。此風險在原 GET 版本同樣存在，非本次引入，但值得一併實測。

## 實作紀錄

### 時間軸

1. 使用者提出需求：評估 lb0018 的 GET metadata 探針改 HEAD，並聲明可接受 405。
2. 尋找 lb0018 檔案時走了彎路：`Glob` 以 `**/lb0018/**` 與 `src/app/layout/labs/*` 兩種 pattern 都回報 No files found，一度以為該功能不在當前分支（`git status` 顯示分支為 `feature/lyon_ai_proxy`，而 `doc/specs/2026-07-14_...` 提到 lb0018 是在另一分支開發的）。改用 `**/labs/**/*.component.ts` 才列出 `src\app\layout\labs\lb0018\lb0018.component.ts`。教訓見下節。
3. 讀完 `lb0018.component.ts` 全文，提出評估：可改，但需處理三點（判定改看 status、CORS preflight 風險、`Accept` header 失去意義），並建議先考慮 `?_summary=true` 這個保留 `resourceType` 檢查的折衷方案。當時把 CORS preflight 列為最大風險。
4. 使用者指示先確認 gateway CORS，並提出「這本身是一個代理，應該能處理 CORS 問題」的判斷。
5. 調查後端六個檔案（見「分析」一節），**推翻了自己第 3 步的 CORS 疑慮**：關鍵在於 HEAD 是 CORS-safelisted method，`Access-Control-Allow-Methods` 未列它也不影響 preflight 通過。使用者的判斷正確。過程中另一個一度誤判是 `SmartOnFhirProxyRoutingService.java:27` 的註解「GET/PUT/PATCH/DELETE 自動發現」讓人以為 HEAD 會走到不同路由分支，讀實際程式碼（行 176-182）後確認 `/metadata` 走 non-sticky 分支，與 method 無關。
6. 使用者定案：改成 HEAD，405/401/403 都算存在，註解一併更新。
7. 實作三處改動（import、探針、`bypassHeaders()` 註解），`npm run build` 通過，無返工。
8. 使用者要求產出本工作紀錄。

**本次改動尚未 commit / push**，工作區仍有未提交的變更。

### 經驗紀錄（給未來類似任務參考）

1. **`Glob` 找不到檔案不代表檔案不存在。** 本次 `**/lb0018/**` 與 `src/app/layout/labs/*` 都回 No files found，但 `**/labs/**/*.component.ts` 找得到。遇到「明明該有卻找不到」時，先換一種 pattern（例如放寬到副檔名層級）再下結論，不要急著推測是分支問題。
2. **判斷 CORS 能否放行某方法時，先確認該方法是不是 CORS-safelisted method（GET / HEAD / POST）。** 這三個方法即使不在 `Access-Control-Allow-Methods` 清單中，preflight 仍會通過（Fetch 規範的檢查條件是「不在清單 **且** 非 safelisted」才報錯）。只看伺服器設定的方法清單就下結論會得到錯誤答案——本次即因此對 HEAD 做出了過度保守的風險評估。
3. **註解寫的方法清單不等於程式碼行為。** `SmartOnFhirProxyRoutingService.java:27` 的「GET/PUT/PATCH/DELETE 自動發現」讓人誤判 HEAD 會走不同分支，實際上 `/metadata` 因 `requiresSticky()` 為 false 而完全不進該邏輯。涉及路由 / 分支判斷時務必讀實際程式碼。
4. **改動探針類邏輯時，注意「判定語意」是否隨之改變，並同步更新註解。** 本次 `baseUrlNotFound` 從「這是有效的 FHIR server」變成「這個 URL 存在」；原註解明確記載了「僅回 2xx 不足以認定是 FHIR server」這個設計理由，若只改程式碼不改註解，會讓下一個讀者誤以為驗證強度未變。
5. **降級驗證強度前，先確認是否有後續關卡接手。** 本次能接受降級，是因為 well-known 驗證鏈與病人清單查詢各自都有失敗提示，誤判只影響提示時機而非資料正確性。若第一段是唯一關卡，同樣的取捨就不成立。
6. **FHIR 探針若只是嫌回應太大，`GET {base}/metadata?_summary=true` 是風險更低的選項**（標準用法、保留 `resourceType` 檢查、不碰 CORS 與方法支援）。本次因使用者的目的是「確認站台活著」而選 HEAD，但未來若需求是「確認是 FHIR server 但別太慢」，優先考慮此方案。
