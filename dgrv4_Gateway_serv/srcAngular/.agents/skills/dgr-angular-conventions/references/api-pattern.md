# API 檔案規範

## 核心原則

所有 HTTP 請求必須透過 `src/app/shared/services/api-base.service.ts` 的 `ApiBaseService`，不得在元件或其他 service 中直接注入 `HttpClient` 呼叫 API。新增 API 呼叫時，優先在對應領域既有的 `api-*.service.ts`（`src/app/shared/services/`）中新增方法，而不是另開新檔案或另建 HTTP client 邏輯。

## `ApiBaseService` 方法對照表

`ApiBaseService` 內部依「後端來源」與「行為」提供多種方法，命名不完全一致（歷史包袱），實作新功能時請對照下表挑選，不要自行發明新的呼叫模式：

| 方法 | Base URL 來源 | HTTP | 用途 |
|------|---------------|------|------|
| `get<T>(url, params?, headers?)` | `baseUrl`（`environment.apiUrl`） | GET | 原生呼叫，不處理 `ResHeader`/`rtnCode`，較少用 |
| `post<T>(path, body?, rtnCodes?)` | `baseUrl` | POST | **主要 POST 入口**，內部呼叫 `excutePost`，`rtnCode != '0000'` 時彈出提示並記錄稽核事件 |
| `npPost<T>(path, body?, rtnCodes?)` | `npBaseUrl`（`environment.dpPath`） | POST | **NP 後端 POST 入口**，內部呼叫 `excuteNpPost`，額外支援 `rtnMsg` 內 `{{formControlName}}` 佔位符自動替換成欄位標籤 |
| `dotNetPost<T extends BaseDotNetRes>(path, body?, rtnCodes?)` | `baseUrl` | POST | 呼叫 .NET 後端，回應為小寫 `resHeader`（非 `ResHeader`），並多帶 `backAuth` header |
| `excutePost_bg<T>(path, body?, rtnCodes?)` | `baseUrl` | POST | 與 `post` 相同但**永不彈出提示視窗**，用於背景/靜默請求 |
| `excuteNpPost_ignoreAll<T>(path, body?, rtnCodes?)` | `npBaseUrl` | POST | 忽略 `1100`/`1297`/`1298` 三種底層碼；`rtnCode == '0001'` 時自動登出 |
| `excuteNpPost_ignore1298<T>(path, body?, rtnCodes?)` | `npBaseUrl` | POST | 忽略 `1100`/`1298`；`rtnCode == '0001'` 時自動登出 |
| `excuteDpGet<T>(path, txID: TxID)` | `baseUrl` | GET | 依 `TxID` 自動組出 `ReqHeader` 並帶入 query，用於 `Dp` 系列 GET |
| `excuteDpPut<T>(path, body?)` | `baseUrl` | PUT | `Dp` 系列 PUT |
| `excuteDpDelete<T>(path, primarykey: number)` | `baseUrl` | DELETE | body 固定為 `{ primarykey }` |
| `excuteDpUpload<T>(path, body?)` | `baseUrl` | POST | 送出 `FormData` 時使用（不手動設定 `Content-Type`） |
| `excuteFileUpload<T>(path, file: File, rtnCodes?)` | `baseUrl` | POST | 標準檔案上傳，自動組出 `txSN`/`txDate`/`txID='DPB0082'`/`cID`/`file` |
| `excuteTsmpSetting<T>(path, file, req, rtnCodes?)` | `baseUrl` | POST | 檔案 + `req` JSON 一併以 `FormData` 送出 |
| `excuteDpGetFile(path)` / `excutePostGetFile(path, body?)` / `excuteDpGetPEMFile(path, body?)` | `baseUrl` | GET/POST | 回傳 `Blob`，用於檔案下載／匯出 |
| `excuteSignBlockGet<T>(path, token?)` | `baseUrl` | GET | 取得 sign block 專用 |

> ⚠️ 命名容易誤解：`excuteDp*` 系列用的是 `baseUrl`（`environment.apiUrl`），`excuteNp*` 系列用的是 `npBaseUrl`（`environment.dpPath`）。這是既有命名慣例，新增方法時沿用即可，不需要「修正」成看似更合理的名稱。

## 請求 / 回應信封慣例

- Request 一律帶 `ReqHeader` + `ReqBody`；Response 一律帶 `ResHeader`（.NET 後端為小寫 `resHeader`）+ 對應的 `Res*` 欄位。
- `ResHeader.rtnCode !== '0000'` 視為錯誤：
  - 預設會呼叫 `this.alert.ok(...)` 顯示全域提示視窗，並呼叫 `AA0206`（`eventLog`）記錄稽核事件。
  - `1100`、`9914`、`9929` 等碼有特殊處理（`9914`/`9929` 會觸發重新取得 sign block 並自動重送）。
  - 若呼叫端已知某些 `rtnCode` 屬於正常業務流程（例如「資料已存在」），可透過 `rtnCodes` 參數傳入白名單，讓底層不攔截、不彈窗。
- `SignCode` header 由 `cryptSignCode(body)`（SHA-256 對 sign block + JSON body）產生，`post`/`npPost` 系列方法會自動處理，呼叫端不需自行計算。
- Bearer token 由 `ToolService.getToken()` 自動附加，呼叫端不需手動組 `Authorization` header。

## `TxID` 與 Model 對照

- `TxID`（定義於 `src/app/models/common.enum.ts`）列舉後端交易代碼（如 `AA0001`、`AA0206`…），用於 `excuteDpGet` 自動組 `ReqHeader.txID`，或作為 `npPost`/`post` 呼叫時手動組 `ReqHeader` 的欄位。
- Request/response payload 介面放在 `src/app/models/api/<ServiceName>/aaNNNN.interface.ts`，一個交易代碼一個檔案，依後端 service 分資料夾（`ClientService`、`UserService`、`RoleService` 等），並提供 `index.ts` barrel 匯出。新增交易時比照既有資料夾的檔名與介面命名（`BaseReq`/`BaseRes` 擴充）。

## 在 `api-*.service.ts` 中呼叫的標準寫法

```ts
// src/app/shared/services/api-xxx.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import { ReqAA0201, ResAA0201 } from 'src/app/models/api/ClientService';

@Injectable({ providedIn: 'root' })
export class XxxService {
  constructor(private apiBase: ApiBaseService) {}

  queryXxx(body: ReqAA0201): Observable<ResAA0201> {
    return this.apiBase.post<ResAA0201>('dgrv4/11/AA0201', {
      ReqHeader: this.apiBase.getReqHeader(TxID.someTxId),
      ReqBody: body,
    });
  }
}
```

在元件中呼叫時，維持既有的「先 `subscribe`、再用 `toolService.checkDpSuccess(res.ResHeader)`/`checkSuccess` 判斷成功與否」流程，並在需要時手動控制 `ngxService.start()`/`ngxService.stop()`（見 `angular-component.md` 的「API 呼叫與 loading」一節）。

## 常見錯誤

```ts
// ❌ 錯誤：直接注入 HttpClient 繞過 ApiBaseService
constructor(private http: HttpClient) {}
this.http.post('/dgrv4/11/AA0201', body);

// ❌ 錯誤：手動組 Authorization / SignCode header
const header = new HttpHeaders({ Authorization: `Bearer ${token}` });

// ❌ 錯誤：用 excuteDp* 呼叫 NP 後端路徑（base URL 會錯）
this.apiBase.excuteDpPut('tsmpdpaa/11/XXX', body);

// ✓ 正確：透過 ApiBaseService 對應方法，並重用既有 api-*.service.ts
constructor(private xxxService: XxxService) {}
this.xxxService.queryXxx(body).subscribe(res => { ... });
```
