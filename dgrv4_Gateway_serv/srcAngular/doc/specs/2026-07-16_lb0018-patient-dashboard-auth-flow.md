# lb0018 Fhir Viewer：Tab 2 Patient Dashboard 與 Auth Method 測試流程調整

> 延續 `9e9454bb6 feat: (labs)新增 lb0018 Fhir Viewer 頁面` 的後續開發；Tab 1 的
> Authorization 測試區與 Tab 2 佔位頁為前案產物，本次在其上完成 Tab 2 實際內容
> 與 Tab 1 的多項互動調整。

## 修改內容

### 問題描述

依對話先後順序，本次共處理八個需求：

1. 製作 Tab 2 畫面，排版參考 https://cylab-tw.github.io/fhirfox/ ，病人清單來自
   `{{FHIR Base URL}}/Patient?_sort=_id&_count=20&_offset=0`（`_format=json`），
   選定病人後以 `{{FHIR Base URL}}/Patient/{id}/$everything` 取回所有 resource。
2. 使用者指出方向錯誤，改為參考內部 digiCare 的
   `https://10.20.30.116/website/digicare/twcore-patient-dashboard` 的做法重做 Tab 2。
3. Tab 1 的 Client Secret 欄位比照 `ac0002.component.html` 213–218 行改為
   密碼遮罩 + 眼睛切換。
4. Client Secret 輸入框高度過高，需優化。
5. `client_secret_basic` 自動帶入的內文參數 `grant_type` 改為寫入 `keyValueForm`
   （移除原本送出時隱性帶入與 template 唯讀假列的作法）。
6. 切換 Auth Method 時清除「回應」區內容；`client_secret_post` 時請求表身帶入
   `grant_type` / `client_id` / `client_secret` 三列（client 憑證值與上方欄位即時
   同步），且表頭不得帶 `Authorization`。
7. 切換 Auth Method 時要先清空 `keyValueForm` 全部內容再帶入自動列。
8. 切換 Auth Method 後 keyValueForm 第一行殘留一列空白 key/value，需移除。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | Tab 2 Patient Dashboard 全部狀態與邏輯；Client Secret 遮罩旗標；`syncAuthBodyRows()`／`clearResponse()`／`reqBodyHandler()` 調整 |
| `src/app/layout/labs/lb0018/lb0018.component.html` | Tab 2 版面（左欄病人搜尋/摘要/過篩資料、右欄三種檢視）；Client Secret 遮罩欄位；移除 grant_type 唯讀假列 |
| `src/app/layout/labs/lb0018/lb0018.component.css` | `pd-*` 系列樣式（時間軸、病人清單、資源樹、pill/tag、JSON 檢視）；`ui-inputgroup` 高度修正 |
| `src/app/layout/labs/lb0018/patient-dashboard.helpers.ts` | 新增：FHIR 解析純函式與 view model（PatientVM、參照樹、就診明細、單行摘要、日期/年齡） |
| `src/app/shared/key-value/key-value.component.ts` | `add()` 帶資料時一律以資料建列；`writeValue()` 收到陣列時先清空再逐列帶入 |
| `src/app/shared/key-value/key-value-detail/key-value-detail.component.ts` | `ngOnInit` 以 `patchValue` 補寫入動態掛載後才指定的 `data` |
| `src/assets/i18n/{zh-tw,zh-cn,en-us,ja}.json` | `fhir_viewer.*` 整組更新（Tab 2 UI 文案 + `gender`/`enc_class`/`enc_status`/`med_status` 巢狀代碼對照） |

### 詳細程式碼異動

#### 1. Tab 2：Patient Dashboard（需求 1、2）

第一版依 fhirfox 做成「下拉選擇情境 + resourceType chips + 單筆資源導覽」；
使用者指正後改依 digiCare `twcore-patient-dashboard` 全面重做，最終版結構：

- 左欄：病人搜尋清單（關鍵字 → `identifier` 或 `name:contains`、「載入更多」累積
  翻頁、自動選第一位）、病人摘要卡（生日/歲數、身分證、電話、地址）、過篩資料卡
  （以 reference 組出以 Patient 為根的資源參照樹，＋/− 展開）。
- 右欄：病歷摘要（就診時間軸，展開組出診斷/用藥/檢驗/報告/媒體）、資源檢視
  （與樹同步的卡片 + 單筆 JSON）、完整 JSON（合併 Bundle + 複製鈕）。

資料面維持使用者指定的直連 FHIR 路徑（digiCare 原版走自家後端 proxy API）：

```ts
// lb0018.component.ts（節錄）
private patientSearchUrl(base: string, offset: number): string {
  let url = `${base}/Patient?_sort=_id&_count=${this.patientPageSize}&_offset=${offset}&_format=json`;
  const kw = this.patientQuery;
  if (kw) {
    const param = /^[A-Za-z]\d{9}$/.test(kw) || /^\d{3,}$/.test(kw) ? 'identifier' : 'name:contains';
    url += `&${param}=${encodeURIComponent(kw)}`;
  }
  return url;
}
// $everything 沿 bundle.link 的 next 遞迴抓齊（上限 30 頁），
// next URL host 可能指向上游 server，僅保留 query 接回 proxy base（rebaseLink()）
```

解析邏輯（參照樹、就診明細、單行摘要）抽到新檔
`patient-dashboard.helpers.ts` 作為純函式；FHIR code → 顯示文字統一走
`codeLabel(group, code)`（`translate.instant('fhir_viewer.{group}.{code}')`，
查無 key 時顯示原始 code）。

#### 2. Client Secret 遮罩 + 眼睛（需求 3）

```html
<!-- 修改前 -->
<input class="form-control me-2" id="clientSecret" formControlName="clientSecret" ... />

<!-- 修改後（比照 ac0002 密碼欄位） -->
<div class="ui-inputgroup">
  <input [type]="clientSecretMask ? 'text' : 'password'" class="form-control" id="clientSecret"
    formControlName="clientSecret" autocomplete="off" />
  <button pButton type="button" [icon]="clientSecretMask ? 'pi pi-eye-slash' : 'pi pi-eye'"
    class="ms-1 tpi-night input-btn" (click)="clientSecretMask = !clientSecretMask"></button>
</div>
```

#### 3. 高度修正（需求 4）

全域 `.ui-inputgroup` 是 `display:flex`（子元素預設 stretch），PrimeNG 按鈕比
Bootstrap `form-control` 高，把輸入框一起撐高。在 lb0018 scoped CSS 局部修正：

```css
.ui-inputgroup { align-items: center; }
.ui-inputgroup .input-btn {
  height: calc(1.5em + 0.75rem + 2px); /* Bootstrap form-control 標準高度 */
  padding-top: 0;
  padding-bottom: 0;
}
```

#### 4. OAuth 參數寫入 keyValueForm（需求 5、6、7）

```ts
// 修改前（reqBodyHandler 內隱性帶入）
params.set('grant_type', 'client_credentials');
if (this.authMethod.value === 'client_secret_post') {
  params.set('client_id', this.clientId.value || '');
  params.set('client_secret', this.clientSecret.value || '');
}

// 修改後：改由 syncAuthBodyRows() 寫入 keyValueForm 可見列
private syncAuthBodyRows(clearUserRows: boolean = false): void {
  if (this.form.get('requestBody')!.value !== 'formurl') return;
  const autoKeys = ['grant_type', 'client_id', 'client_secret'];
  const userRows = clearUserRows ? [] : rows.filter((r) => r && !autoKeys.includes(r.key));
  const autoRows = [{ key: 'grant_type', value: 'client_credentials', selected: true }];
  if (this.authMethod.value === 'client_secret_post') {
    autoRows.push(
      { key: 'client_id', value: this.clientId.value || '', selected: true },
      { key: 'client_secret', value: this.clientSecret.value || '', selected: true },
    );
  }
  ctrl.setValue([...autoRows, ...userRows]);
}
```

觸發點：頁面初始化、Auth Method 切換（`clearUserRows = true`，並先
`clearResponse()` 清回應區）、`clientId`/`clientSecret` `valueChanges`（post 模式
即時同步）、內文型態切回 `formurl`。`Authorization` 表頭維持只在
`client_secret_basic` 附加（`send()` 既有邏輯），post 模式送出即無 Authorization。

#### 5. 共用 key-value 元件修正（需求 5、8 的前置）

全專案原本沒有任何頁面程式化寫值進 `app-key-value`，該路徑有三個潛在問題，
依序修正：

```ts
// key-value.component.ts add()：修改前，清單為空時帶資料會誤存成空列
if (keyvalue && this.keyvalues.length) { ... }
// 修改後
if (keyvalue) { this.keyvalues.push({ key: keyvalue.key, value: keyvalue.value, ... }); }

// key-value-detail.component.ts ngOnInit()：data 由 createComponent 動態掛載後
// 才指定，constructor 建表單當下拿不到，補寫入
if (this.data) {
  this.form.patchValue({ key: this.data.key, value: this.data.value });
}

// key-value.component.ts writeValue()：修改前收到陣列不清空（須先 setValue(null)
// 重置，但 null 路徑會補一列空白 → 產生需求 8 的殘留空白列）；修改後：
if (this.value) {
  this.keyValueRef.clear();
  this.keyvalues = [];
  this.value.forEach(val => this.add(val));
}
```

`''`（初始一列空白）與 `null`（reset 後一列空白，ac0311 依賴）兩條路徑維持原樣。

## 預計修改方式

### 需求 1、2：Tab 2 Patient Dashboard

**Context** — Tab 2 原為佔位頁（「內容待定」）。第一版依 fhirfox 完成後，使用者
回饋「方向錯了」，改指定參考內部 digiCare 的 twcore-patient-dashboard。

**分析** — fhirfox 與 digiCare 都是 SPA，WebFetch 抓不到畫面；先後改抓 GitHub
原始碼（fhirfox 的 `ScenarioBrowserApp.tsx` 等）與 digiCare 的 production JS
bundle（`index-CN_AE1O0.js`，3.4MB），從 minified 原始碼還原出版面結構、文案與
資料層：digiCare 走自家後端（`/patients`、`/patients/{id}/everything`、
`/encounters/{id}/related` 等 proxy API），前端有 `sZ`（Patient 顯示欄位，含
NNTWN/NI 身分證 identifier 判斷）、`QKe`（reference 參照樹）、`hU`（就診明細
分組）等純函式。另確認 lb0016 已有可重用的 `smart-fhir.types.ts` 精簡 FHIR 型別
與 `SmartFhirService.nextLink()` 等 helper。

**修正方案** — UI/UX 移植 digiCare 的三檢視版面；資料面維持使用者指定的直連
FHIR 路徑，`$everything` 一次抓齊後全部在前端解析（省去 digiCare 的逐段懶載
入）。解析函式 port 成 `patient-dashboard.helpers.ts` 純函式，型別重用 lb0016。
未移植：查詢來源 banner（區塊一已有 base URL）、JSON 語法上色/行號、Media
卡片格（簡化為連結清單）。

### 需求 3、4：Client Secret 欄位

**Context** — 使用者選取 `ac0002.component.html` 213–218 行要求比照；套用後又
回饋輸入框高度過高。

**分析** — `ac0002` 的作法是 `ui-inputgroup` + `[type]` 切換 + `pButton` 眼睛；
查 `styles.scss` 確認 `.ui-inputgroup` 只有 `width:100%; display:flex`、
`.input-btn` 只有 `border-radius:5px`，高度差異來自 PrimeNG 按鈕預設 padding 高於
Bootstrap form-control，flex stretch 把 input 撐高（ac0002 頁面同樣存在，只是該頁
無同列對照欄位不明顯）。

**修正方案** — template 照抄 ac0002 模式（單一欄位改用 inline 切換旗標
`clientSecretMask`，不需要 ac0002 多目標的 `toggleMask(tar)`）；高度在 lb0018
scoped CSS 修正，不動全域（避免影響 ac0002 等既有頁面）。

### 需求 5～8：OAuth 參數列與 key-value 元件

**Context** — 使用者要求把送出時隱性帶入的 OAuth 參數改成 keyValueForm 可見列，
之後陸續追加：切換清回應、post 三列即時同步、切換時整份清空、移除殘留空白列。

**分析** — grep 全專案確認沒有任何頁面對 `keyValueForm`/`keyValueRequest` 呼叫
`setValue`/`patchValue`，即「程式化寫值」路徑從未被使用；細讀
`key-value.component.ts` 與 `key-value-detail.component.ts` 找出三個潛在缺陷
（`add()` 空清單吃掉資料、detail constructor 拿不到 `data`、`writeValue` 不清空
既有列）。連動方式（即時同步 vs 切換時帶一次）以 AskUserQuestion 與使用者確認，
選定即時同步。

**修正方案** — lb0018 以 `syncAuthBodyRows(clearUserRows)` 統一維護自動列；共用
元件僅修正「帶資料路徑」的死碼缺陷，`''`/`null` 路徑（其他頁面依賴）不動。
需求 8 的空白列即是先前用 `setValue(null)` 重置的副作用，最終把「清空」責任移進
`writeValue` 陣列路徑後，`setValue(null)` 前置步驟一併移除。

## 驗證方式

### 靜態驗證

- 每輪修改後執行 `npx ng build --configuration development --output-path=<scratchpad>`，
  全部成功（exit 0、無 TypeScript / template 錯誤），未污染正式封裝目錄。
- i18n：`git diff --stat src/assets/i18n/` 確認四檔各 +71/-8 行、僅 `fhir_viewer`
  單一 hunk，四份語系 key 同步。
- 提交前執行 `npm run build`（production，輸出至
  `../src/main/resources/static/dgrv4/ac4/`），結果見時間軸。

### 尚待人工驗證（建議）

1. Tab 2：輸入有效 FHIR Base URL → 切到 Patient Dashboard，確認病人清單載入、
   關鍵字搜尋（名稱與 identifier 兩種）、「載入更多」、病人摘要欄位。
2. Tab 2 三檢視：病歷摘要時間軸展開明細（用藥/檢驗偏高偏低/報告）、過篩資料樹
   ＋/− 與右側資源卡同步、完整 JSON 複製。
3. Tab 1：切換 Auth Method → 回應區清空、keyValueForm 整份重建（basic 一列 /
   post 三列、無殘留空白列）；post 模式修改 Client ID/Secret 表身即時跟動；
   post 送出時 DevTools 確認無 `Authorization` 表頭、body 含三參數；basic 送出
   時帶 Basic Authorization。
4. 共用元件回歸：ac0311 / ac0316 / api-test 的 key-value 區塊新增列、reset 後
   仍有一列空白（`null` 路徑未動，理論上不受影響，仍建議抽查）。

## 實作紀錄

### 時間軸

1. **(2026-07-14) 需求 1 提出**：依 fhirfox 排版做 Tab 2。fhirfox 為 React SPA，
   改由 GitHub 原始碼還原版面，實作第一版（p-select 選擇情境、resourceType
   chips、資源單筆導覽 + 完整 JSON），dev build 通過。
2. **(2026-07-15) 需求 2 — 方向修正**：使用者指出應參考 digiCare
   twcore-patient-dashboard。內部站 HTTPS 憑證過期，`curl -k` 下載頁面與 3.4MB
   JS bundle，從 minified 碼中定位路由元件與各子元件，還原完整 UI/資料層後整個
   重寫 Tab 2：新增 `patient-dashboard.helpers.ts`，component/template/css/i18n
   全面改版，移除第一版的下拉與 chips。此輪也把第一版的 19 個 i18n key 整組
   換掉，是一次較大的返工——教訓見經驗紀錄第 1 點。
3. **需求 3**：Client Secret 比照 ac0002 改遮罩 + 眼睛，加 `clientSecretMask`。
4. **需求 4**：回饋高度過高；查全域樣式定位為 flex stretch + PrimeNG 按鈕高度，
   scoped CSS 修正。
5. **需求 5**：grant_type 改寫入 keyValueForm。調查發現共用元件「帶資料」路徑
   從未被使用且有缺陷，修正 `add()` 與 detail 的 `data` 補寫；此時重置列採
   「先 `setValue(null)` 再 `setValue(rows)`」，已知會多一列空白（當時視為與
   元件初始狀態一致而接受）——為需求 8 埋下伏筆。
6. **需求 6（plan mode）**：切換清回應、post 三列、無 Authorization 表頭。以
   AskUserQuestion 確認 client 憑證「即時同步」；計畫核准後實作
   `syncAuthBodyRows()`（保留使用者自訂列）、`clearResponse()`、`reqBodyHandler()`
   移除 post 隱性帶入。
7. **需求 7**：使用者要求切換時整份清空再帶入 → `syncAuthBodyRows(clearUserRows)`
   參數化，僅 Auth Method 切換傳 `true`。
8. **需求 8**：使用者回報切換後第一行殘留空白列 → 把「清空既有列」責任移入
   `writeValue()` 陣列路徑，移除 `setValue(null)` 前置步驟，空白列消失。
9. 每輪均以 dev build（輸出 scratchpad）驗證編譯通過。
10. **(2026-07-16) 本紀錄撰寫當下**：`npm run build` 與 commit/push 尚未執行，
    將於紀錄產出後進行（結果由 commit 本身呈現）。

### 經驗紀錄（給未來類似任務參考）

1. **「參考某頁面的做法」先確認參考對象再動工**：需求 1→2 的返工整輪重寫了
   Tab 2。對照站台若是 SPA，WebFetch 只能拿到殼；GitHub 原始碼或 production
   bundle（`curl -k` + 定位 route/component）都能還原出可依循的版面與資料流。
2. **共用元件的「死碼路徑」要先驗證再依賴**：`app-key-value` 的程式化寫值路徑
   全專案沒人用過，直接呼叫 `setValue` 資料根本不會顯示。動手前先 grep 既有
   呼叫端，確認路徑是否真的可用；修死碼缺陷風險低，但要在回報中標註影響面。
3. **`writeValue` 的清空語意要一次做對**：需求 5 用 `setValue(null)` 繞過不清空
   的問題，留下的空白列在需求 8 又被退回來。碰到 CVA 元件行為不符預期時，優先
   把行為修正在元件內（單一職責），不要在呼叫端疊補丁。
4. **digiCare 的 FHIR 顯示對照表**（gender / enc_class / enc_status / med_status）
   已轉成 `fhir_viewer.*` 巢狀 i18n key，未來其他 FHIR 畫面可直接重用
   `codeLabel(group, code)`（查無 key 顯示原始 code）的模式。
