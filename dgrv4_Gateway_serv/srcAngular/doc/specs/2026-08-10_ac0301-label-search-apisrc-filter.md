# AC0301「依標籤搜尋」分頁新增 API 來源篩選條件

## 修改內容

### 問題描述

使用者在本次對話中依序提出下列需求：

1. `AA0427Req` 增加 `apiSrc` 參數，並**檢查後端相對應的 API 確認欄位格式**；`AA0428Req` 也同樣增加 `apiSrc`。
2. 前端 UI/UX：在 AC0301 頁面「關鍵字搜尋」分頁既有的「API 來源」，於「依標籤搜尋」分頁產生一份相同的操作；每次值改變都要刷新 AA0427，並清空原本下拉選單內已選的資料及表單查詢結果。
3. 調整排版：把「包含」、「排除」兩個 radio button 放在標籤右邊，並把「API 來源」跟「標籤」同行呈現，排一個適當比例。
4. 再調整：把標籤的下拉選單跟 radio button 區塊左右位置互換。
5. 回報 Bug：在「依標籤搜尋」頁面已勾選 API 來源的項目，但 AA0427 的 Req `apiSrc` 沒有值。
6. 執行 `npm run build`、建立工作日誌，成功後上傳到目前分支。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/models/api/ApiService/aa0427.interface.ts` | `AA0427Req` 由空介面補上 `apiSrc?: Array<string>` |
| `src/app/models/api/ApiService/aa0428.interfcae.ts` | `AA0428Req` 補上 `apiSrc?: Array<string>` |
| `src/app/shared/services/api-api.service.ts` | `queryAllLabel_ignore1298()` 改為可帶入 `AA0427Req`（預設 `{}`）；補 import `AA0427Req` |
| `src/app/layout/ac03/ac0301/ac0301.component.ts` | 新增 `labelApiSrc` FormArray、`getSelectedApiSrc()`、`labelApiSrcChange()`；`queryAllLabel()`／`queryAPIListByLabel()`／`moreApiListData()` 帶入 `apiSrc` |
| `src/app/layout/ac03/ac0301/ac0301.component.html` | 「依標籤搜尋」分頁新增 API 來源 checkbox 區塊，並重排為「API 來源(col-4) ｜ 包含·排除 + 標籤(col-8)」單列 |
| `doc/specs/2026-08-10_ac0301-label-search-apisrc-filter.md` | 新增本次工作日誌 |

後端（`dgrv4_Gateway_serv/src/main/java/...`、`dgrv4_Entity_lib/...`）經查證**已支援 `apiSrc`，本次未修改任何 Java 檔案**。

### 詳細程式碼異動

#### 1. `AA0427Req` 新增 `apiSrc`

修改前：

```ts
export interface AA0427Req { }
```

修改後：

```ts
export interface AA0427Req {
    /** API 來源，空陣列或未帶則不做來源過濾 */
    apiSrc?: Array<string>;
}
```

#### 2. `AA0428Req` 新增 `apiSrc`

```ts
export interface AA0428Req {
  include:  string;
  labelList: Array<string>;
  /** API 來源，空陣列或未帶則不做來源過濾 */
  apiSrc?: Array<string>;
  moduleName?: string;
  apiKey?: string;
  paging?: string; // 前端固定傳 'Y'
}
```

#### 3. `queryAllLabel_ignore1298()` 改為可帶 request body

修改前：

```ts
queryAllLabel_ignore1298(): Observable<RespAA0427> {
  let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAllLabel),
      ReqBody: {}
  } as ReqAA0427
  ...
}
```

修改後（預設 `{}`，既有 6 處無參數呼叫不受影響）：

```ts
queryAllLabel_ignore1298(ReqBody: AA0427Req = {}): Observable<RespAA0427> {
  let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAllLabel),
      ReqBody: ReqBody
  } as ReqAA0427
  ...
}
```

#### 4. 新增獨立的 `labelApiSrc` FormArray

`queryForm` 內新增（與關鍵字分頁的 `apiSrc` 選取狀態互不影響）：

```ts
include: new UntypedFormControl('Y'),
labelList: new UntypedFormControl([]),
labelApiSrc: new UntypedFormArray([]), // 「依標籤搜尋」分頁的 API 來源
```

`ngOnInit` 的 `API_SRC` 選項載入處，同一迴圈內同時 push 到兩個 FormArray；Express 版本兩者都固定勾選並 `disable()`，並額外補一次 `queryAllLabel()` 讓標籤清單符合固定的來源條件：

```ts
const arrayApiSrc = <UntypedFormArray>this.queryForm.get('apiSrc');
// 「依標籤搜尋」分頁使用同一份 API 來源選項，但選取狀態獨立
const arrayLabelApiSrc = <UntypedFormArray>(this.queryForm.get('labelApiSrc'));
for (let item of this.apiSrcOpt) {
  if (this.acConf.edition == 'Express') {
    arrayApiSrc.push(this.fb.group({ name: item.label, value: item.value, selected: true }));
    arrayLabelApiSrc.push(this.fb.group({ name: item.label, value: item.value, selected: true }));
    this.queryForm.get('apiSrc')!.disable();
    this.queryForm.get('labelApiSrc')!.disable();
  } else {
    // ...selected: false，兩個 FormArray 各 push 一筆
  }
}
// Express 版本的來源為固定勾選，需重新取得標籤清單使其符合來源條件
if (this.acConf.edition == 'Express') {
  this.queryAllLabel();
}
```

#### 5. `getSelectedApiSrc()` 與 `labelApiSrcChange()`

```ts
/**
 * 取出指定 apiSrc FormArray 中已勾選的來源值
 * - FormArray 在 Express 版本會被 disable，因此直接讀 controls 而非 form value
 * - p-checkbox 未設 binary，勾選後 control 值會是陣列(勾選:[value]、取消:[])，
 *   初始值則是建立 FormGroup 時給的布林值，兩種型別都要判斷
 */
getSelectedApiSrc(controlName: string): Array<string> {
  const array = <UntypedFormArray>this.queryForm.get(controlName);
  if (!array) return [];
  return array.controls
    .filter((ctrl) => {
      const selected = ctrl.get('selected')!.value;
      return Array.isArray(selected) ? selected.length > 0 : selected === true;
    })
    .map((ctrl) => ctrl.get('value')!.value);
}

/**
 * 「依標籤搜尋」分頁的 API 來源異動：
 * 清空已選標籤與查詢結果，並依新的來源條件重新取得標籤清單
 */
labelApiSrcChange() {
  this.selected = [];
  this.dataList = [];
  this.rowcount = this.dataList.length;
  this.q_labelList.setValue([]);
  this.queryAllLabel();
}
```

#### 6. 三處查詢帶入 `apiSrc`

```ts
// queryAllLabel()：AA0427
const apiSrc = this.getSelectedApiSrc('labelApiSrc');
this.apiService.queryAllLabel_ignore1298({ apiSrc }).subscribe((res) => { ... });

// queryAPIListByLabel()：AA0428
let req = {
  include: this.include?.value,
  labelList: labelList,
  apiSrc: this.getSelectedApiSrc('labelApiSrc'),
  paging: 'Y',
} as AA0428Req;

// moreApiListData() 的 searchByLabel 分支：AA0428 續頁
paging: 'Y',
 include: this.include?.value,
apiSrc: this.getSelectedApiSrc('labelApiSrc'),
```

#### 7. 「依標籤搜尋」分頁排版

最終版本（一列兩欄，radio 在標籤下拉選單左側）：

```html
<div class="form-group row">
  <div class="col-4 col-md-4 col-xl-4">
    <label class="control-label">{{ "api_src" | translate }}</label>
    <div formArrayName="labelApiSrc">
      <div class="p-form-check-line">
        <div class="p-form-check"
          *ngFor="let item of getControls(queryForm, 'labelApiSrc'); let j = index" [formGroupName]="j">
          <p-checkbox formControlName="selected" [value]="item.get('value').value"
            inputId="labelApiSrc_{{j}}" (onChange)="labelApiSrcChange()"></p-checkbox>
          <label class="ms-2 mb-0" for="labelApiSrc_{{j}}">{{item.get("name").value}}</label>
        </div>
      </div>
    </div>
  </div>
  <div class="col-8 col-md-8 col-xl-8">
    <label class="control-label">{{'labelList'|translate}}</label>
    <div class="field-flex-start">
      <div class="p-form-check-line flex-shrink-0">
        <!-- 包含 / 排除 radio -->
      </div>
      <div class="flex-grow-1">
        <p-multiSelect #labelFilter ... [style]="{'width':'100%'}"></p-multiSelect>
      </div>
    </div>
    <div *ngIf="include?.invalid && (include?.dirty || include?.touched)" class="text-danger">...</div>
  </div>
</div>
```

順帶移除了 multiSelect 外層原本的 inline `style="display:block"`，改用 Bootstrap utility class（依 `.claude/rules/style.md`）。

## 預計修改方式

### 需求 1：AA0427／AA0428 增加 `apiSrc` 並確認後端欄位格式

**Context** — 使用者要求前端兩支 API 的 Req 增加 `apiSrc`，並強調要先檢查後端對應 API 的欄位格式，而非自行假設。

**分析** — 以 Glob／Grep 實際讀取後端原始碼，確認結果如下：

| 檔案 | 現況 |
|------|------|
| `dgrv4_Gateway_serv/src/main/java/tpi/dgrv4/dpaa/vo/AA0427Req.java` | 已有 `private List<String> apiSrc;` + getter/setter |
| `.../vo/AA0428Req.java:19` | 已有 `private List<String> apiSrc;` + getter/setter |
| `.../service/AA0427Service.java:37-42` | `req.getApiSrc()` 傳入 5 支 `query_AA0427LableN` |
| `.../service/AA0428Service.java:144` | `cri.setApiSrc(req.getApiSrc())` |
| `dgrv4_Entity_lib/.../TsmpApiDaoImpl.java:1012` | `if (apiSrc != null && apiSrc.size() > 0)` 才附加 `AND tsmpApi.apiSrc IN :apiSrc` |

由 DAO 的 JPQL 可確認 `apiSrc` 是**未加密的原始字串陣列**（`['R','C','M','N']`），與 AA0301 的 `apiSrc` 一致；**不是** `publicFlag`／`apiStatus` 那種 `Base64(Bcrypt(值)) + ',' + index` 格式。空陣列或不帶 = 不做來源過濾。

**修正方案** — 後端已就緒，不動任何 Java 檔案。只在前端兩份 interface 補 `apiSrc?: Array<string>`（optional，維持既有呼叫端相容），並讓 `queryAllLabel_ignore1298()` 可接受 request body。

### 需求 2：「依標籤搜尋」分頁複製一份 API 來源操作

**Context** — 使用者要求把關鍵字分頁的「API 來源」在標籤分頁做出相同操作，且值改變時要刷新 AA0427、清空已選標籤與查詢結果。

**分析** — 讀 `ac0301.component.ts` 確認：關鍵字分頁的 API 來源是 `apiSrc` FormArray，選項來自 `list.querySubItemsByItemNo({ encodeItemNo: Base64(Bcrypt('API_SRC')) + ',33' })`，Express 版本會過濾掉 `M`/`N`/`C` 並全部固定勾選 + `disable()`。標籤清單由 `queryAllLabel()` 呼叫 AA0427 取得，共有 6 處呼叫端（`ngOnInit`、刪除 API 後、`headerReturn()`、`tabChange()` 等）。

若直接共用同一個 `apiSrc` FormArray，兩個分頁的條件會互相污染，因此需要獨立的 FormArray。

**修正方案** — 新增 `labelApiSrc` FormArray，在既有的 `API_SRC` 訂閱裡一併建立（不另外多打一次字典 API）。`queryAllLabel()` 統一改為帶入 `labelApiSrc` 的勾選值，讓所有既有呼叫端自動一致。checkbox 加 `(onChange)="labelApiSrcChange()"` 觸發清空 + 刷新。

### 需求 3、4：排版調整

**Context** — 第一版把 API 來源獨立一列放在最上方，使用者要求「包含／排除」移到標籤右邊、且 API 來源與標籤同行；接著又要求下拉選單與 radio 區塊左右互換。

**分析** — 讀 `src/styles/styles.scss:581-587` 確認 `.p-form-check-line` 是 `display:flex; gap:1rem;`（**無 `flex-wrap`**），所以欄位太窄時 checkbox 會被壓縮而非換行；關鍵字分頁的 API 來源用的是 `col-4`，四個 checkbox 排得下，因此標籤分頁也維持 `col-4`。`.field-flex-start` 是 `styles.scss` 既有的全域 flex class（`.claude/rules/style.md` 有記載），直接重用。

**修正方案** — 一列兩欄 `col-4` + `col-8`；`col-8` 內用 `.field-flex-start` 水平排列，radio 群組 `flex-shrink-0` 固定寬、multiSelect 外層 `flex-grow-1` 吃掉剩餘寬度。互換時只調換兩個子 div 的順序，class 不變。

### 需求 5：`apiSrc` 送出時永遠是空陣列

**Context** — 使用者實測後回報：標籤分頁已勾選 API 來源，但 AA0427 的 Req `apiSrc` 沒有值。

**分析** — 讀 `node_modules/primeng/fesm2022/primeng-checkbox.mjs` 的 `updateModel()`（第 243-272 行）與 `get checked()`（第 207-209 行），確認這些 `p-checkbox` **沒有設定 `[binary]="true"`**，在非 binary 模式下 form control 的值會被寫成陣列：

| 狀態 | `selected` control 實際值 |
|------|--------------------------|
| 初始（建立 FormGroup 時） | `false`（布林） |
| 勾選後 | `['R']`（陣列） |
| 再取消 | `[]`（空陣列） |

我原本寫的 `ctrl.get('selected').value == true`，在 JS 的鬆散比較下 `['R'] == true` 會走 `Number('R') === NaN` → `false`，因此永遠篩不到任何項目。

同時確認既有 `submitForm()` 為何沒事：它寫的是 `if (value == false) continue;`，`['R'] == false` 為 `false`（不跳過）、`[] == false` 為 `true`（跳過），剛好符合預期，所以關鍵字分頁一直正常。

另確認 `updateModel()` 中 `onModelChange(newModelValue)` 在 `onChange.emit(...)` **之前**執行，所以 `(onChange)` 回呼讀到的 form control 值已是最新值，不是時序問題。

**修正方案** — `getSelectedApiSrc()` 改為同時處理兩種型別：陣列看 `length > 0`、布林看 `=== true`（後者涵蓋 Express 版本初始化為 `true` 且 FormArray 被 disable 的情況）。未沿用既有 `== false` 的鬆散比較寫法，因為那是巧合成立、可讀性差。此修正同時修好 AA0427、AA0428 首次查詢與 AA0428 續頁三處。

## 驗證方式

### 靜態驗證

- 每次程式碼異動後皆執行 `npx ng build --configuration development --output-path <scratchpad>`（輸出導向暫存目錄，避免污染 tracked 的 static 目錄），共 4 次，全部 **build 成功、無 error 無 warning**，驗證後即刪除暫存產物。
- 最終交付執行 `npm run build`（production，`angular.json` 的 outputPath = `../src/main/resources/static/dgrv4/ac4/`）：**exit code 0**，`Build at: 2026-08-10T08:40:16.871Z - Hash: 3da3784bfc2a7674 - Time: 82923ms`。產物中 `7583.7b6e2cf229ab09b8.js` 對應 `ac03-ac0301-ac0301-module`，即本次異動的 chunk。
- 以 Grep 確認 `api_src` i18n key 四份語系檔皆已存在（本次沒有新增任何 i18n key）。

### 尚待人工驗證（建議）

1. 進入 AC0301 →「依標籤搜尋」分頁，勾選任一「API 來源」，於 DevTools Network 確認 AA0427 的 `ReqBody.apiSrc` 有值（例如 `["R"]`），且回傳的標籤清單只剩該來源的標籤。
2. 確認每次勾選／取消來源時，標籤下拉選單已選項目被清空、下方查詢結果表格也被清空。
3. 選擇標籤後按查詢，確認 AA0428 的 `ReqBody.apiSrc` 同樣帶值，結果只含該來源的 API。
4. 捲動觸發續頁（`moreApiListData()`），確認第二頁之後仍套用相同的 `apiSrc` 條件。
5. 驗證「包含／排除」切換仍正常，且排版在不同螢幕寬度下 checkbox 不被壓爛。
6. 若有 Express 版本環境，確認來源為固定全勾選 + disabled，且標籤清單已依該來源過濾。
7. 確認「關鍵字搜尋」分頁的 API 來源行為完全未受影響（兩個 FormArray 互相獨立）。

## 實作紀錄

### 時間軸

1. 使用者提出需求 1、2（AA0427／AA0428 加 `apiSrc` + 標籤分頁 UI）。
2. Grep 前端 `AA0427`／`AA0428` 相關檔案，讀取兩份 interface、`api-api.service.ts`、`ac0301.component.ts/html`。
3. 依使用者要求查證後端：讀 `AA0427Req.java`、`AA0428Req.java`、`AA0427Service.java`、`AA0428Service.java`、`TsmpApiDaoImpl.java`，確認**後端早已支援 `apiSrc`**，格式為未加密字串陣列 → 本次不需改 Java。
4. 實作前端：兩份 interface 加欄位、service 方法加參數、component 新增 `labelApiSrc` FormArray／`getSelectedApiSrc()`／`labelApiSrcChange()`、三處查詢帶入 `apiSrc`、template 新增 checkbox 區塊（第一版放獨立一列）。
5. 執行 development build 驗證通過，刪除暫存產物。
6. 使用者提出需求 3（排版）：查 `styles.scss` 的 `.p-form-check-line` 與 `.field-flex-start`，重排為 `col-4` + `col-8`，radio 移到 multiSelect 右側。build 通過。
7. 使用者提出需求 4：左右互換，radio 移到 multiSelect 左側。build 通過。
8. **使用者回報 Bug**：勾選了來源但 AA0427 的 `apiSrc` 沒值。
9. 讀 PrimeNG 20 checkbox 原始碼，先確認 `onModelChange` 早於 `onChange.emit`（排除時序問題），再從 `updateModel()` 非 binary 分支發現 control 值是**陣列**而非布林 —— 我寫的 `== true` 對陣列永遠不成立，這是我這次自己引入的錯誤。
10. 修正 `getSelectedApiSrc()` 改用 `Array.isArray()` 分支判斷，build 通過。
11. 使用者要求執行 `npm run build`、建立工作日誌並上傳到目前分支。
12. 執行 `npm run build`（production）exit code 0，static 產物已更新；確認目前分支為 `feature/tom/tom_tag_query`（追蹤 `eapdd2025/feature/tom/tom_tag_query`，非 `main`／`develop`）。
13. 建立本文件。撰寫當下尚未 commit/push，最終提交結果以本次對話後續回報為準。

### 經驗紀錄（給未來類似任務參考）

- **「請檢查後端」要真的去讀後端原始碼**：這次查完才發現後端兩支 Req VO 早就有 `apiSrc`，省下不必要的 Java 改動。同時要一路讀到 DAO 才能確定欄位的**實際格式**（原始字串 vs. `Base64(Bcrypt(...))+index`），只看 VO 的型別 `List<String>` 不足以判斷。
- **PrimeNG `p-checkbox` 不加 `[binary]="true"` 時，form control 值是陣列不是布林**（勾選 `[value]`、取消 `[]`）。抄既有 template 寫法時，連帶要抄它讀值的邏輯，否則會踩到型別不一致。
- **既有程式碼用鬆散比較 `== false` 可能是「剛好會動」而非刻意設計**。本專案 `submitForm()` 的 `value == false` 對陣列恰好行為正確，但把它反寫成 `== true` 就會壞掉。新程式碼應明確判斷型別，不要依賴 JS 的隱式轉型。
- **同一份選項要在兩個分頁各自獨立選取時**，共用選項陣列（`apiSrcOpt`）但建立兩個 FormArray，並在原有的字典 API 訂閱中一併初始化，避免重複打 API。
- **把共用查詢方法（如 `queryAllLabel()`）改成內部自行取條件**，比在 6 個呼叫端各自傳參數穩妥，不會漏改。
- 排版優先重用 `styles.scss` 既有的 `.field-flex-start` 等全域 flex class 與 Bootstrap utility（`flex-grow-1`／`flex-shrink-0`），不要新寫 scoped CSS 或 inline style。
- 驗證用的 build 一律 `--output-path` 導到 scratchpad，避免污染 tracked 的 `src/main/resources/static/dgrv4/ac4/`；只有使用者明確要求「build 且上傳」時，才用 `npm run build` 產出正式產物並納入提交（見 [Codex Build 與上傳產物規範](./2026-07-16_agent-build-upload-artifact-rule.md)）。
