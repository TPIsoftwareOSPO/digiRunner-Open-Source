# TokenInterceptor bypass 流程優化待作計畫

本文件記錄 `TokenInterceptor` 目前以 `digiRunner` request header 判斷是否略過額外攔截流程的調查結果與後續實作計畫。此項目目前尚未實作。

## 修改內容

### 問題描述

使用者希望評估 `src/app/shared/Interceptors/token-interceptor.ts` 的 `intercept()` 方法是否能改善以下設計：

1. 呼叫端加入 `digiRunner` header，讓 `TokenInterceptor` 略過 response 判斷、全域 alert 等額外處理。
2. API 測試區的 header 建立方式可參考 `src/app/layout/ac03/ac0316/api-test/api-test.component.ts`。
3. 需要提出可落地的優化方案與實作計畫。
4. 將本計畫保存於 `doc` 下新建的英文命名待作資料夾。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `doc/todo/2026-08-11_token-interceptor-bypass-context.md` | 新增本次調查結果、優化計畫、驗證方式與實作紀錄 |

下列程式檔案僅為預計影響範圍，目前均未修改：

| 預計影響檔案／範圍 | 預計異動內容 |
|---------------------|--------------|
| `src/app/shared/Interceptors/token-interceptor.ts` | 改用 request-local context 判斷 bypass，移除 `apiTestProcess` 與 `digiRunner` header 判斷 |
| `src/app/shared/Interceptors/` 下的共用 context token 檔案 | 新增 `HttpContextToken<boolean>` |
| API 測試、Token、LDAP、GTW IdP、SMART FHIR、mTLS 說明檔等呼叫端 | 將 `digiRunner` 逃生門遷移為 `HttpContext` |
| `src/app/layout/ac03/ac0316/api-test/stream-api.service.ts` 及其呼叫端 | 確保 `fetch` 串流不送出內部 bypass 標記 |
| TokenInterceptor 對應測試檔 | 新增正常與 bypass 流程測試 |

### 詳細程式碼異動

目前沒有修改產品程式碼，因此沒有「修改前／修改後」的實際 diff。

預計核心改動如下，最終識別字與檔案位置仍須在實作時依鄰近程式碼確認。

目前做法：

```ts
if (request.headers.has('digiRunner')) {
  this.apiTestProcess = true;
  request = request.clone({
    headers: request.headers.delete('digiRunner'),
  });
}

if (this.apiTestProcess) {
  this.apiTestProcess = false;
  return next.handle(request);
}
```

預計方向：

```ts
export const BYPASS_TOKEN_INTERCEPTOR =
  new HttpContextToken<boolean>(() => false);
```

```ts
const shouldBypass = request.context.get(BYPASS_TOKEN_INTERCEPTOR);

if (shouldBypass) {
  return next.handle(request);
}
```

呼叫端預計透過 `HttpContext` 設定內部 metadata，不再加入會進入 HTTP headers 的 `digiRunner` 標記。

## 預計修改方式

### 需求 1：改善 bypass 判斷方式

**Context**

目前 `TokenInterceptor.intercept()` 以 `request.headers.has('digiRunner')` 判斷請求是否要略過後續攔截流程，並先把 header 刪除後再送出請求。

**分析**

- 專案使用 Angular `20.3.25`，本機 Angular declarations 已確認提供 `HttpContext` 與 `HttpContextToken`。
- `apiTestProcess` 放在 singleton Interceptor instance 上，是不必要的共享 mutable state；目前設定與重設都在同一段同步程式內，沒有一般非同步請求交錯的空檔，但 request-local 變數或 context 仍較不易留下狀態，語意也更清楚。
- `digiRunner` 是產品名稱，無法直接表達「略過 TokenInterceptor」的用途。
- API 測試功能允許使用者自訂 request headers；若使用者真的要測試名為 `digiRunner` 的 header，目前會被前端攔截並刪除。
- `HttpContextToken` 是 Angular HttpClient request-local metadata，不會成為送往後端的 HTTP header，適合表示 Interceptor policy。

**修正方案**

新增預設為 `false` 的 `BYPASS_TOKEN_INTERCEPTOR` context token。`TokenInterceptor` 直接讀取當次 request context，移除 `apiTestProcess` 欄位與 `digiRunner` header 的檢查／刪除流程。第一階段使用單一 boolean 並保留既有 bypass 範圍，避免同時改變既有功能行為。

### 需求 2：遷移所有現有逃生門呼叫端

**Context**

`digiRunner` header 不只存在於 API 測試區，也用於測試 Token、LDAP、GTW IdP、SMART FHIR、FHIR labs 與 mTLS 說明檔等請求。

**分析**

本次以 `rg -n -F 'digiRunner' src/app --glob '*.ts'` 查到實際逃生門使用位置，包括：

- `api-test.component.ts`
- `api-token.service.ts`
- `smart-fhir-client.service.ts`
- `ldap.component.ts`
- `gtwidp.component.ts`
- `lb0018.component.ts`
- `np0205.component.ts`

其中 SMART FHIR 註解明確表示，略過 Interceptor 是為了避免外部授權流程在 401 時被導向後台登入頁；因此不能只修改 API 測試元件。

**修正方案**

逐一把經由 Angular `HttpClient` 發出的特殊請求改為設定 `HttpContext`。API 測試區應在共用的 `httpClientHandle()` request options 集中設定，避免 GET、POST、PUT、DELETE、PATCH 重複遺漏；真正要送往目標 API 的 headers 則維持原樣。

### 需求 3：區分「忽略業務錯誤」與「略過整個 Interceptor」

**Context**

目前 bypass 會略過 response `ResHeader` 判斷、SignBlock retry、401 token refresh、全域 HTTP error alert、登入導向與部分 loading 清理，而不只是停止 alert。

**分析**

- `ApiBaseService` 已提供 `rtnCodes` 白名單，讓指定業務回傳碼不由底層彈窗處理。
- `ApiBaseService.excutePost_bg()` 已提供完全不彈出業務提示視窗的背景呼叫模式。
- API 測試、OAuth／SMART 等需要自行接收原始 response/error 的流程，才適合完全略過 `TokenInterceptor`。

**修正方案**

建立使用原則：

1. 只忽略預期的 DGR 業務 `rtnCode`：使用現有 `rtnCodes`。
2. DGR 背景請求完全不顯示業務提示：優先使用既有 `excutePost_bg()` 或對應 ignore 方法。
3. 呼叫端必須自行處理原始 response/error：使用 `BYPASS_TOKEN_INTERCEPTOR`。

第一階段不拆成多個 context tokens；待各呼叫端需求確認後，再評估是否需要分為 `SKIP_AUTH_HANDLING`、`SKIP_RESPONSE_HANDLING`、`SKIP_GLOBAL_ERROR_ALERT` 等不同 policy。

### 需求 4：處理 `fetch` 串流請求

**Context**

API 測試的 SSE 流程會把由 `HttpHeaders` 組成的物件傳給 `StreamApiService`，而該 service 使用原生 `fetch()`。

**分析**

原生 `fetch()` 不會經過 Angular `HttpInterceptor`，因此 `digiRunner` header 不會被 `TokenInterceptor` 刪除，可能實際送往目標 API。對 `fetch()` 設定 `HttpContext` 也沒有作用。

**修正方案**

串流流程不加入任何 Interceptor bypass header；傳給 `StreamApiService` 的 headers 僅包含使用者指定或協定真正需要的值。由於該請求本來就不經過 Interceptor，不需要額外 bypass 設定。

### 需求 5：控制本次重構範圍

**Context**

調查 `TokenInterceptor` 時發現 `tap()` callback 內存在回傳／訂閱 Observable 的 SignBlock retry 寫法。

**分析**

RxJS `tap()` callback 的回傳值不會取代原始 Observable，這段流程值得另外驗證。不過它與 bypass metadata 遷移是不同風險，若同時修改會擴大回歸範圍。

**修正方案**

本待作項目只處理 bypass 判斷與呼叫端遷移。SignBlock retry、Interceptor 職責拆分、unused imports 等整理另開待作項目，不在同一批改動中處理。

## 驗證方式

### 靜態驗證

本次只新增計畫文件，未修改或編譯產品程式碼。

已執行：

- `Get-Date -Format yyyy-MM-dd`：確認文件日期為 `2026-08-11`。
- `Test-Path doc/todo`：建立前結果為 `False`。
- `Test-Path doc/todo/2026-08-11_token-interceptor-bypass-context.md`：建立前結果為 `False`，確認沒有覆蓋既有文件。
- `rg -n -F 'digiRunner' src/app --glob '*.ts'`：確認逃生門實際使用範圍。
- 查閱 `token-interceptor.ts`、`api-test.component.ts`、`stream-api.service.ts`、`api-base.service.ts` 與相關呼叫端。
- 查閱本機 `node_modules/@angular/common/module.d.d.ts`：確認 `HttpContext` 與 `HttpContextToken` declarations 存在。

尚未執行：

- Interceptor 單元測試。
- `npm run build`。
- 實際頁面與網路請求驗證。

以上驗證應在未來實作程式修改後執行。

### 尚待人工驗證（建議）

未來完成實作後，至少人工確認：

1. API 測試區的 GET、POST、PUT、DELETE、PATCH 成功與錯誤 response 都能原樣顯示於畫面，且不出現全域 alert。
2. API 測試區可以真的送出名稱為 `digiRunner` 的使用者自訂 header，不會再被前端吃掉。
3. 一般後台 API 遇到 401、SignBlock 錯誤與非成功 `rtnCode` 時，既有處理仍正常。
4. LDAP、GTW IdP 與 SMART FHIR 流程不會因 401 被錯誤導向後台登入頁。
5. 瀏覽器 Network panel 中，內部 bypass metadata 不會出現在 request headers。
6. SSE／串流請求不會送出前端內部逃生門 header。

## 實作紀錄

### 時間軸

1. 使用者提出 `TokenInterceptor.intercept()` 以 `digiRunner` header 略過額外處理的流程是否能優化，並要求先提供計畫。
2. 讀取 `dgr-angular-conventions` skill 及 Angular、API、單元測試相關規範。
3. 讀取 `token-interceptor.ts` 與 `api-test.component.ts`，確認 header 會先被刪除，再直接呼叫 `next.handle(request)`。
4. 初步注意到 `apiTestProcess` 是 Interceptor instance 上的 mutable state，曾先把它視為可能的並行交錯風險；進一步檢視後確認目前設定與重設之間沒有非同步邊界，因此一般請求不會在該區塊交錯。最終將問題修正為「不必要且語意不清的共享狀態」，而非宣稱現況必然存在 concurrency bug。
5. 全域搜尋 `digiRunner`，確認逃生門已有多個非 API 測試功能依賴，不能只修改單一元件。
6. 讀取 `stream-api.service.ts`，確認 SSE 使用原生 `fetch()`，不經過 Angular Interceptor。
7. 讀取 `api-base.service.ts`，確認專案已有 `rtnCodes` 與 `excutePost_bg()` 可處理較窄的「不要彈業務錯誤」需求。
8. 確認 Angular 版本為 `20.3.25`，並從本機 declarations 確認 `HttpContext`／`HttpContextToken` 可用。
9. 向使用者提出以 `HttpContextToken<boolean>` 取代 transport header、分階段遷移與補測試的計畫；沒有修改程式碼。
10. 使用者要求在 `doc` 下建立英文命名的待作資料夾並保存計畫。
11. 讀取 `spec-recorder` skill；其預設位置為 `doc/specs/`，但依使用者明確指示改用新建的 `doc/todo/`。
12. 建立本文件；產品程式碼尚未實作、尚未 build、尚未 commit 或 push。

### 經驗紀錄（給未來類似任務參考）

- Interceptor 專用控制資訊應優先使用 `HttpContextToken`，避免以會進入 transport layer 的假 header 傳遞前端內部 policy。
- 發現 class-level mutable state 時，先確認設定與使用之間是否真的存在非同步邊界，再判斷是否構成 concurrency bug；不要只因為是 singleton 就直接下結論。
- 「不要處理特定業務碼」與「完全略過認證、重試、錯誤提示及導頁」是不同需求，應使用不同層級的既有機制。
- 變更共用 Interceptor 前必須搜尋所有逃生門呼叫端；本例的依賴範圍包含登入、SMART FHIR、測試工具與靜態資源，不是單一畫面功能。
- 原生 `fetch()` 不經過 Angular Interceptor。共用 header builder 若同時服務 `HttpClient` 與 `fetch()`，必須額外檢查內部 header 是否會外洩。
- 相鄰發現的 RxJS 或 SignBlock 問題應另案處理，避免讓低風險 metadata 遷移演變成高風險的大型 Interceptor 重構。
