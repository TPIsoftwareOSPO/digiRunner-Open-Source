# lb0013 SMART on FHIR Proxy：Auth Method 改下拉、Fhir Server驗證依 Auth 模式連動

> 本次工作延續 [2026-07-06_lb0013-sofproxy-auth-relay-token.md](./2026-07-06_lb0013-sofproxy-auth-relay-token.md)（該次建立了 `sofProxyAuth` 下拉、`sofProxyOutRelay` checkbox 與 out relay 條件欄位群組），在既有機制上追加欄位型態與連動規則調整。

## 修改內容

### 問題描述

使用者在本次對話依序提出：

1. `sofProxyOutAuthMethod` 欄位可用值為 `client_secret_basic`／`client_secret_post`，預設 `client_secret_basic`（現況為自由輸入的文字框、無預設值）。
2. `sofProxyOutRelay` 僅在 `sofProxyAuth` 為 `none` 時可供使用者操作；當 `sofProxyOutRelay` 不可被操作或值為 `N` 時，須清除相關欄位（`sofProxyOutAuthMethod`／`sofProxyOutWellKnown`／`sofProxyOutClientId`／`sofProxyOutClientSecret`／`sofProxyOutScope`）內的資料。
3. 將目前的修改上傳（commit + push）。
4. 產出本工作日誌。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0013/lb0013.component.ts` | 新增 `sofProxyOutAuthMethodOptions` 選項陣列；`sofProxyOutRelay.valueChanges` 的 `Y` 分支補 Auth Method 預設值；`sofProxyAuth.valueChanges` 追加 relay enable/disable 連動；`changePage` 的 update/detail 帶入 relay 與 out 欄位改為僅 Auth=none 時執行 |
| `src/app/layout/labs/lb0013/lb0013.component.html` | Auth Method 的 `<input type="text">` 改為 `p-select` |

（同批推送另含使用者自行開發的 `lb0018` Fhir Viewer 新頁面：`src/app/layout/labs/lb0018/`、`layout.routing.ts` 路由、四份 i18n 的 `fhir_viewer.*` key，非本次 AI 實作範圍，僅代為 commit。）

### 詳細程式碼異動

#### 1. `sofProxyOutAuthMethod` 下拉化與預設值

選項陣列（比照既有 `sofProxyInIntrospectionAuthOptions`）：

```ts
// Fhir Server驗證 Auth Method 下拉選項（label 與 value 相同）
sofProxyOutAuthMethodOptions: string[] = [
  'client_secret_basic',
  'client_secret_post',
];
```

Template（修改前 → 修改後）：

```html
<!-- 修改前 -->
<input type="text" class="form-control" id="sofProxyOutAuthMethod"
  formControlName="sofProxyOutAuthMethod">

<!-- 修改後 -->
<p-select inputId="sofProxyOutAuthMethod" [options]="sofProxyOutAuthMethodOptions"
  formControlName="sofProxyOutAuthMethod" [style]="{'width':'100%'}"></p-select>
```

預設值掛在既有 `sofProxyOutRelay.valueChanges` 的 `Y` 分支（比照 third-party 預設 introspection auth 的既有模式）：

```ts
if (val === 'Y') {
  requiredCtrls.forEach((ctrl) =>
    ctrl.setValidators(ValidatorFns.requiredValidator()),
  );
  // 啟用 Fhir Server驗證時 Auth Method 下拉預設 client_secret_basic
  if (!this.sofProxyOutAuthMethod.value) {
    this.sofProxyOutAuthMethod.setValue('client_secret_basic');
  }
} else { /* 既有清空邏輯不變 */ }
```

update/detail 帶入的 fallback 由 `?? ''` 改為 `|| 'client_secret_basic'`。

#### 2. `sofProxyOutRelay` 依 `sofProxyAuth` 連動

在既有 `sofProxyAuth.valueChanges` 訂閱（third-party introspection 清空邏輯之後）追加：

```ts
// Fhir Server驗證（sofProxyOutRelay）僅 Auth = none 時可操作；
// 其餘模式強制設為 N 並 disable（觸發既有 valueChanges 清空 out 欄位）
if (val === 'none') {
  // detail 模式整份表單為 disabled，不得在此重新啟用
  if (this.currentAction !== 'detail') {
    this.sofProxyOutRelay.enable({ emitEvent: false });
  }
} else {
  if (this.sofProxyOutRelay.value !== 'N') {
    this.sofProxyOutRelay.setValue('N');
  }
  this.sofProxyOutRelay.disable({ emitEvent: false });
}
```

清空 out 欄位**不需另寫**：`setValue('N')` 會觸發既有 `sofProxyOutRelay.valueChanges` 完成清空與移除必填驗證；template 的 out 群組 `*ngIf="sofProxyOutRelay.value === 'Y'"` 亦自動收合。

#### 3. `changePage` update/detail 帶入條件化

原本無條件把後端資料帶入 relay 與五個 out 欄位；因 `sofProxyAuth.setValue(...)` 先執行、已依新規則把 relay 強制為 `N` 並清空 out 欄位，帶入段改為僅當該筆資料 Auth 為 none 時執行，避免被規則清空的欄位又被舊資料填回：

```ts
// Auth 非 none 時 relay 已被連動邏輯強制為 N/disable，不再帶入舊資料
if ((this.selectedItem!.sofProxyAuth?.[0] ?? 'none') === 'none') {
  this.sofProxyOutRelay.setValue(this.selectedItem!.sofProxyOutRelay ?? 'N');
  this.sofProxyOutAuthMethod.setValue(
    this.selectedItem!.sofProxyOutAuthMethod || 'client_secret_basic',
  );
  // ...其餘 out 欄位帶入照舊（client secret 仍固定空白）
}
```

update 與 detail 兩個 case 皆同樣處理。

## 預計修改方式

### 需求 1：`sofProxyOutAuthMethod` 下拉化

**Context** — 使用者敘述欄位可用值僅 `client_secret_basic`／`client_secret_post`、預設 `client_secret_basic`；現況（前案建立時）為純文字 input。

**分析** — 讀取 `lb0013.component.ts` 與 `.html` 全文確認：(1) 同檔已有 `sofProxyInIntrospectionAuthOptions` + `p-select` 的相同情境可比照；(2) third-party 預設 introspection auth 的既有模式（`valueChanges` 中值為空時 `setValue('client_secret_basic')`）可套用到 relay 勾選時機；(3) 該群組 label 為英文硬字串，不需動 i18n。

**修正方案** — 新增選項陣列、template 換 `p-select`、預設值掛在 relay `valueChanges` 的 `Y` 分支、update/detail fallback 改 `|| 'client_secret_basic'`。全部比照同檔既有寫法，不引入新模式。

### 需求 2：`sofProxyOutRelay` 僅 Auth=none 可操作

**Context** — 使用者同時選取了 template 中 `sofProxyOutRelay` checkbox 所在行；規則為僅 `sofProxyAuth === 'none'` 時可操作，不可操作或為 `N` 時清空相關欄位。

**分析** — 確認既有機制：(1) `sofProxyOutRelay.valueChanges`（前案建立）在值非 `Y` 時已會清空五個 out 欄位並移除必填驗證，可直接重用；(2) `changePage` 開頭有 `formE.enable()` + `formE.reset('')`，detail 分支有 `formE.disable()`，enable/disable 時序需要特別處理，否則 detail 模式會被 `sofProxyAuth.valueChanges` 誤重新啟用 relay；(3) update/detail 帶入順序是 auth 先、relay/out 後，若不條件化會把清空的欄位填回。

**修正方案** — 採 FormControl 的 `enable()`/`disable()`（而非 template `[disabled]` 綁定，reactive forms 不建議）；連動邏輯併入既有 `sofProxyAuth.valueChanges` 訂閱；非 none 時先 `setValue('N')`（借既有 valueChanges 清空）再 disable；`enable` 加 `currentAction !== 'detail'` guard；update/detail 帶入段以 `selectedItem.sofProxyAuth?.[0] === 'none'` 條件包裹。disabled control 不納入 `formE.valid`，且 `procData` 既有的 `sofProxyOutRelay.value == 'Y'` 條件在值為 N 時本就不送 out 欄位，送出流程無需改動。

## 驗證方式

### 靜態驗證

- `npm run build`（production build）：**成功**，無編譯錯誤（Time: 43789ms，Hash `0e31c58d055e5107`）。
- IDE 診斷：改動後僅存在改動前即有的歷史警告（未使用 import、`ngIf` deprecated 等），無新增錯誤。

### 尚待人工驗證（建議）

1. 建立頁：Auth 預設 none → relay checkbox 可勾；勾選後 Auth Method 為下拉且預設 `client_secret_basic`，可切 `client_secret_post`。
2. Auth 切到 `dgr`／`third-party` → relay checkbox 變 disabled 且自動取消勾選、out 群組收合；切回 none 重新勾選時欄位為空、Auth Method 回預設。
3. 更新頁：auth=none 且 relay=Y 的資料 out 欄位正常帶入；auth 非 none 的資料 relay 為 N 且 disabled。
4. 詳細資料頁：relay 維持 disabled（不因 auth=none 被重新啟用）。
5. 送出 create/update 後檢查 payload：relay=N 時不含 out 欄位。

## 實作紀錄

### 時間軸

1. 使用者提出需求 1、2（plan mode）。讀取 `lb0013.component.ts`／`.html` 全文，確認既有 `valueChanges` 清空機制與 `changePage` 帶入時序後，撰寫計畫（含 detail 模式 enable guard、update/detail 帶入條件化兩個時序陷阱的處理），計畫一次獲核准，無修改。
2. 依計畫實作四處 TS 修改與一處 HTML 修改，過程無返工。
3. `npm run build` 驗證通過。
4. 使用者要求上傳。檢查 git 狀態發現工作區另有使用者自行開發的 lb0018（Fhir Viewer）新頁面（元件、路由、四份 i18n），拆成兩個 commit 以區隔來源：
   - `288ac33b2` `feat: (labs)lb0013 Auth Method 改下拉並依 Auth 連動 Fhir Server驗證`
   - `9e9454bb6` `feat: (labs)新增 lb0018 Fhir Viewer 頁面`
5. 推送至 `eapdd2025/feature/kevincc-smartonfhir1004`（`8a8badbfc..9e9454bb6`），工作區乾淨。
6. 產出本工作日誌。

### 經驗紀錄（給未來類似任務參考）

- **lb0013 的欄位連動一律借道既有 `valueChanges`**：清空／必填切換邏輯已集中在 `sofProxyAuth` 與 `sofProxyOutRelay` 兩個訂閱，新增連動規則時用 `setValue()` 觸發既有訂閱即可，不要在新位置重複清空邏輯。
- **`changePage` 的時序陷阱**：開頭 `formE.enable()` → `formE.reset('')`（會觸發 auth valueChanges、值短暫為 `''`）→ 各 case 的 `setValue` 依序觸發連動。任何依 auth 值 enable/disable 子控制項的邏輯，必須考慮 (1) detail 分支的 `formE.disable()` 會被後續 enable 破壞（需 `currentAction !== 'detail'` guard）、(2) update/detail 帶入若在連動清空之後執行，會把值填回，需條件化。
- **disabled FormControl 不納入 `form.valid`**，且本頁 payload 組裝以 `value == 'Y'` 判斷而非控制項狀態，disable 欄位不影響送出流程——改動前先確認這兩點可省去額外防護碼。
- **混雜他人改動的工作區要拆 commit**：上傳前先 `git status` 區分本次 AI 改動與使用者自行開發的檔案，分別成 commit，訊息各自對應實際內容。
