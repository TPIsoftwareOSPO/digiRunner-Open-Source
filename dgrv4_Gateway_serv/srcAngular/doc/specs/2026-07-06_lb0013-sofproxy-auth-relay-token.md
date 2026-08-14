# lb0013 SMART on FHIR Proxy：Auth 模式改版、Fhir Server驗證與測試取 token（DPB0341）

## 修改內容

### 問題描述

使用者針對 `lb0013`（SMART on FHIR Proxy 管理頁）配合後端改版，在同一次工作階段中依序提出以下 9 個需求：

1. 移除 `sofProxyAccessToken` 欄位，另建「Auth」區塊對應新增的 `sofProxyAuth`，UI 用下拉選單，選項 `none`（預設）/`dgr`/`third-party`，label 與 value 相同。
2. 補回 `sofProxyAccessToken`，寫死送 `'N'`，不需要 UI 畫面。
3. `sofProxyAuth = third-party` 時畫面增加 `sofProxyInIntrospectionAuth`／`sofProxyInWellKnown`／`sofProxyInIntrospectionClientId`／`sofProxyInIntrospectionSecret` 四個必填 input（含後續補充：切回 none/dgr 時要清空值；DPB0310 介面需補 `sofProxyInIntrospectionSecret` 欄位）。
4. Auth 旁增加「Fhir Server驗證」checkbox，對應 `sofProxyOutRelay`，勾選 `Y`／未勾 `N`，預設 `N`。
5. 修正：`sofProxyInIntrospectionSecret` 在 DPB0310 其實不會回傳，update 帶入時固定空白。
6. 兩個主要項目：(a) `sofProxyOutRelay = Y` 時提供 `sofProxyOutAuthMethod`／`sofProxyOutWellKnown`／`sofProxyOutClientId`／`sofProxyOutClientSecret`／`sofProxyOutScope` 五個 input，其中 ClientId/ClientSecret 必填，`N` 時隱藏並清空；(b) 同時建立【測試取token】按鈕呼叫 `testSmartOnFhirProxyRelay`（使用者提供的 Tx ID 為 DPB0314，經查後端原始碼實際為 **DPB0341**），依回傳的 `success` 與 `tokenObtained` 組 alert 提示。
7. 使用者不小心移除了部分改動（HTML 的 out relay 區塊），要求復原。
8. 排版優化：Auth=third-party 與 Fhir Server驗證同時開啟時，兩個條件區塊的欄位混在一起造成混淆。
9. 優化編輯頁 Sticky List 按鈕的呈現風格與排版。
10. `sofProxyInIntrospectionAuth` 改成下拉選單，值為 `client_secret_basic`／`bearer`／`none`，預設 `client_secret_basic`。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0013/lb0013.component.ts` | Auth 下拉／Fhir Server驗證 checkbox 的 FormControl 與 getter；兩組 `valueChanges` 動態必填驗證與清空邏輯；update/detail 帶值；create/update payload 條件組裝；`testRelayToken()` 方法 |
| `src/app/layout/labs/lb0013/lb0013.component.html` | 移除 Auth checkbox；新增 Auth 下拉、Fhir Server驗證 checkbox、introspection 與 out relay 兩個條件區塊（含測試取token 按鈕）；表格 Auth 欄；Sticky List 按鈕改版 |
| `src/app/models/api/ServerService/dpb0310.interface.ts` | 移除 `sofProxyAccessToken`；（`sofProxyInIntrospectionSecret` 先加後移除，最終不存在） |
| `src/app/models/api/ServerService/dpb0311.interface.ts` | 移除後補回 `sofProxyAccessToken`；新增 `sofProxyInIntrospectionSecret?`、`sofProxyOutClientSecret?` |
| `src/app/models/api/ServerService/dpb0312.interface.ts` | 同 dpb0311（`SmartOnFhirProxyUpdateDto` 為 optional；回應用 DTO 的 `sofProxyAccessToken` 移除） |
| `src/app/models/api/ServerService/dpb0341.interface.ts` | **新增**：`ReqDPB0341`（`Partial<SmartOnFhirProxyDto>`）、`RespDPB0341`／`DPB0341Resp` |
| `src/app/models/common.enum.ts` | `TxID` 新增 `testSmartOnFhirProxyRelay = 'DPB0341'` |
| `src/app/shared/services/api-server.service.ts` | 新增 `testSmartOnFhirProxyRelay()`（`npPost`，比照既有 SmartOnFhirProxy 系列寫法） |
| `src/assets/i18n/zh-tw.json` 等四份語系檔 | 移除 `sof.access_token`；新增 `sof.test_token` |

### 詳細程式碼異動

#### 1. Auth 下拉選單（`sofProxyAuth`）

- `formE` 以 `sofProxyAuth: new FormControl('none')` 取代原 `sofProxyAccessToken` checkbox 控制項；下拉選項為欄位 `sofProxyAuthOptions: string[] = ['none', 'dgr', 'third-party']`（`p-select` 給字串陣列時 label=value）。
- 送後端時包成單元素陣列：`sofProxyAuth: [this.sofProxyAuth.value || 'none']`（介面型別為 `Array<string>`）；update/detail 帶入時取 `sofProxyAuth?.[0] ?? 'none'`。
- 查詢列表與匯入列表原本的 access token 打勾欄，改為顯示 `{{rowData.sofProxyAuth?.join(', ')}}` 文字，欄位標題用字面 `'Auth'`（比照既有 `ID`/`TPS` 不走 i18n）。

#### 2. `sofProxyAccessToken` 無 UI、寫死 `'N'`

create/update payload 固定帶 `sofProxyAccessToken: 'N'`（後端仍需此欄位）；dpb0311 為必填 `string`、dpb0312 UpdateDto 為 optional。無任何畫面呈現。

#### 3. third-party introspection 條件欄位

- `ngOnInit` 訂閱 `sofProxyAuth.valueChanges`：值為 `'third-party'` 時四個 introspection 控制項掛 `ValidatorFns.requiredValidator()`；其他值時 `clearValidators()` 並 `setValue('')` 清空。`changePage` 的 `formE.reset('')` 會觸發此訂閱自動清驗證，機制自洽。
- update/detail 帶值順序刻意安排在 `sofProxyAuth.setValue(...)` **之後**，避免帶入的值被清空邏輯洗掉。
- `sofProxyInIntrospectionSecret`：DPB0310 查詢回應不回傳（以 `hasIntrospectionSecret: boolean` 遮罩），update/detail 帶入固定 `setValue('')`；即更新 third-party Proxy 時使用者需重新輸入 secret。
- payload 以條件 spread 處理：`...(this.sofProxyAuth.value === 'third-party' ? { 四欄位 } : {})`，非 third-party 完全省略。
- `sofProxyInIntrospectionAuth` 後續改為 `p-select` 下拉（需求 10）：選項 `sofProxyInIntrospectionAuthOptions = ['client_secret_basic', 'bearer', 'none']`（label=value）；Auth 切到 third-party 且值為空時自動帶入 `client_secret_basic`，update/detail 帶入既有值（空值 fallback 同預設）；因下拉恆有值，移除該欄位的必填錯誤提示區塊（驗證器保留）。

#### 4. Fhir Server驗證 checkbox（`sofProxyOutRelay`）

- `formE` 新增 `sofProxyOutRelay: new FormControl('N')`；create 進入頁面補 `setValue('N')`（因 `formE.reset('')` 會清成空字串）。
- Template 在 Auth 下拉同列右側以 `p-checkbox [binary]="true" trueValue="Y" falseValue="N"` 呈現，label「Fhir Server驗證」。
- payload 一律送 `sofProxyOutRelay: value == 'Y' ? 'Y' : 'N'`。

#### 5. out relay 條件欄位與測試取 token

- 訂閱 `sofProxyOutRelay.valueChanges`：`'Y'` 時 `sofProxyOutClientId`/`sofProxyOutClientSecret` 掛必填；其他值時移除驗證並清空五個 out 欄位。
- `sofProxyOutClientSecret` 同 introspection secret：後端以 `hasOutClientSecret` 遮罩不回傳，update 帶入固定空白。
- `testRelayToken()`：呼叫 `serverService.testSmartOnFhirProxyRelay()` 送六個欄位（五個輸入框 + `sofProxyOutTokenEndpoint`——無輸入框，update 帶 `selectedItem` 既有值、create 留空由後端從 well-known 解析），依結果分三種 alert：
  - `success && tokenObtained` → `alertService.ok(成功, message, AlertType.success, 'tokenEndpoint: ...<br>expiresInSec: ...')`
  - `success && !tokenObtained` → warning
  - `!success` → error（顯示後端 `message`）
- 測試按鈕 `[hidden]="currentAction=='detail'"`、`[disabled]="sofProxyOutClientId.invalid || sofProxyOutClientSecret.invalid"`，文字走 i18n key `sof.test_token`。

#### 6. DPB0341 API 層

```ts
// api-server.service.ts（比照 addSmartOnFhirProxy 等既有寫法）
testSmartOnFhirProxyRelay(ReqBody: Partial<SmartOnFhirProxyDto>): Observable<RespDPB0341> {
  let body = {
    ReqHeader: this.api.getReqHeader(TxID.testSmartOnFhirProxyRelay),
    ReqBody: ReqBody,
  } as ReqDPB0341;
  const path = `${this.indexPath}/DPB0341`;
  return this.api.npPost<RespDPB0341>(path, body);
}
```

`DPB0341Resp`：`success: boolean`、`message: string`、`tokenEndpoint: string`、`tokenObtained: boolean`、`token: string`、`expiresInSec: number`（欄位與後端 `DPB0341Resp.java` 一致）。

#### 7. 排版優化

兩個條件區塊各自包進 Bootstrap `border rounded-3 p-3 mb-3` 邊框容器，加粗體群組標題（「Auth: Third-Party Introspection」／「Fhir Server驗證（Token 代取設定）」），欄位統一為 `row g-3` + `col-3` 網格；未新增任何 CSS。順帶修正 introspection 區塊一個錯位的 `</div>`。

#### 8. Sticky List 按鈕

編輯頁原本是「label + 灰底假輸入框（inline `background: #F6F6F6`）包無文字圖示鈕」，改為 `btn tpi-btn tpi-second` 品牌按鈕（`fa fa-list` 圖示 + 「Sticky List」文字），`align-self-end` 與同列輸入框底部對齊。查詢列表內的圖示鈕與相鄰操作鈕一致，不動。

## 預計修改方式（需求脈絡與決策）

### 需求 1：sofProxyAuth 下拉

使用者選取 `lb0013.component.html` 中 `sofProxyAccessToken` 所在行提出需求。經 AskUserQuestion 確認三個決策：(1) 值包成單元素陣列送後端（維持介面 `Array<string>`）；(2) 表格欄位改顯示 `sofProxyAuth` 文字；(3) `sofProxyAccessToken` 連介面欄位一併移除。i18n key `sof.access_token` 經 grep 確認僅 lb0013 使用，一併從四份語系檔移除。

### 需求 2：補回 sofProxyAccessToken

需求 1 完成後使用者告知後端仍需此欄位，要求補回但寫死 `'N'`、無 UI。介面（dpb0311 必填、dpb0312 optional）與 payload 補回；dpb0310（查詢回應）不補，因前端已不讀取。

### 需求 3：introspection 條件欄位

計畫階段發現 `sofProxyInIntrospectionSecret` 不存在於任何前端介面（只有 `hasIntrospectionSecret`）。第一次詢問使用者未回應，先採「建立/更新都必填」與「非 third-party 時 payload 省略」的預設；後續使用者補充「切回 none/dgr 要清空值」並更正「DPB0310 要補 `sofProxyInIntrospectionSecret`」——但需求 5 又再更正 DPB0310 其實不會回傳，最終該欄位僅存在於 dpb0311/0312 request 介面。

### 需求 6：Tx ID 查證（重要）

使用者提供的 Tx ID `DPB0314` 與既有匯出功能衝突（`TxID.exportSmartOnFhirProxy = 'DPB0314'`，`exportFile()` 走 streaming 呼叫同一路徑）。詢問使用者逾時未回應後，**直接查同 monorepo 的後端原始碼**：`dgrv4_Gateway_serv/src/main/java/.../SmartOnFhirProxyController.java` 中 `testSmartOnFhirProxyRelay` 實際註冊於 `@PostMapping("/dgrv4/11/DPB0341")`，req 沿用 `SmartOnFhirProxyDto`、resp 為 `DPB0341Resp`；同時從 `SmartOnFhirProxyRelayTestService.java` 確認後端驗證規則（clientSecret 必填、wellKnown 或 tokenEndpoint 擇一）。據此以 DPB0341 實作，計畫經使用者核准。

### 需求 7：誤刪復原

以 grep 比對 TS 與 HTML，確認僅 HTML 的 out relay 區塊遺失（TS、介面、i18n、service 完好），原位補回；build hash 與遺失前一致（`f11d092a`）證明內容完全還原。

## 驗證方式

### 靜態驗證

- 每個需求完成後皆執行 `npm run build`（production build），全部 exit code 0、無 TS/Linter 錯誤。
- 全域 grep 確認 `sofProxyAccessToken` 僅存在於「寫死 'N'」的 payload 與 dpb0311/0312 介面；`sof.access_token` 無殘留。

### 尚待人工驗證（建議）

`npm start` 開啟 lb0013：

- 建立頁：Auth 預設 `none`；切 `third-party` → introspection 邊框群組出現、四欄必填；勾「Fhir Server驗證」→ out relay 邊框群組出現、ClientId/Secret 必填；切回/取消勾選 → 區塊隱藏且值清空、表單恢復可送出。
- 送出 payload：`sofProxyAuth` 為單元素陣列、`sofProxyAccessToken` 恆為 `'N'`、條件欄位依開關存在與否。
- 更新頁：非 secret 欄位帶入既有值，兩個 secret 欄位空白需重填（必填）。
- 【測試取token】：三種結果的 alert 呈現（success 含 tokenEndpoint/expiresInSec、warning、error 顯示後端 message）。
- 查詢/匯入列表 Auth 欄顯示文字值；Sticky List 按鈕（更新頁）樣式與對齊。

## 實作紀錄

### 時間軸

1. **需求 1**（計畫模式）：探索 lb0013 結構與介面檔，AskUserQuestion 確認三個決策後實作並 build 通過。
2. **需求 2**：補回 `sofProxyAccessToken` 寫死 `'N'`。
3. **需求 3**（計畫模式）：發現 secret 欄位缺失；詢問逾時採預設 → 使用者透過計畫駁回補充「切回要清空」與「DPB0310 補 secret」→ 再以 AskUserQuestion 釐清（使用者說明先前為筆誤，是要確認 DPB0310 的 `sofProxyInIntrospectionSecret`）→ 依此實作。
4. **需求 4**：Fhir Server驗證 checkbox，介面已有 `sofProxyOutRelay` 不需改 model。
5. **需求 5**：更正 DPB0310 不回傳 secret → 移除 dpb0310 該欄位、update 帶入固定空白，並向使用者標註「更新每次需重輸 secret」的副作用。
6. **需求 6**（計畫模式）：發現 Tx ID 衝突，詢問逾時後改查後端原始碼確認為 DPB0341；計畫核准後實作五個 out 欄位、DPB0341 API 層與 `testRelayToken()`。
7. **需求 7**：使用者誤刪 HTML out relay 區塊，grep 定位遺失範圍後原位復原（build hash 一致證明完全還原）。
8. **需求 8**：兩個條件區塊改為邊框群組卡片式排版。
9. **需求 9**：Sticky List 按鈕改為品牌按鈕樣式。
10. 需求 1–9 以 commit `2c30a2e6e`（`feat(labs): lb0013 SMART on FHIR Proxy 新增 Auth 模式與代取 token 測試`）提交並推送至 `eapdd2025/feature/kevincc-smartonfhir1004`。
11. **需求 10**：`sofProxyInIntrospectionAuth` 改為下拉選單（client_secret_basic/bearer/none，預設 client_secret_basic），與本文件更新一併於後續 commit 提交。

### 經驗紀錄（給未來類似任務參考）

- **使用者提供的 API 規格（Tx ID、欄位）可能有筆誤**，且本專案前後端在同一個 monorepo：遇到與前端現況衝突的規格（如本例 DPB0314 已被匯出佔用），與其猜測或空等回覆，直接 grep 後端 Java 原始碼（controller 的 `@PostMapping`、resp VO）是最快且最可靠的查證方式。
- **「後端不回傳 secret、以 hasXxx 布林遮罩」是本模組的固定模式**（`hasIntrospectionSecret`／`hasOutClientSecret`）：對應的前端處理是「secret 只存在於 request 介面、update 帶入固定空白、必填時使用者需重輸」，新增類似欄位時直接套用。
- **條件顯示 + 動態必填**的既定模式：`valueChanges` 訂閱中依開關 `setValidators(ValidatorFns.requiredValidator())`／`clearValidators()+setValue('')`，並讓 `changePage` 的 `formE.reset('')` 自然觸發清理；update/detail 帶值必須排在開關欄位 `setValue` 之後，否則會被清空邏輯洗掉。
- **payload 用條件 spread**（`...(open ? {...} : {})`）省略關閉狀態的欄位，是比送空字串更不具破壞性的預設，除非後端明確要求清空。
- 使用者誤刪程式碼要求復原時，先 grep 全部相關識別字定位「實際遺失的範圍」再補，不要盲目重做全部；復原後 build hash 可作為「內容與遺失前完全一致」的客觀證據。
- 多個條件區塊同時展開會讓欄位歸屬混淆時，用 Bootstrap `border rounded-3 p-3` + 粗體群組標題做卡片式分組即可，不需要新寫 CSS。
