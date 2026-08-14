# 修正 key-value 元件 Comment 節點 crash 與 p-input-icon-right 失效問題（遷移至 p-iconfield）

本次工作的第一個問題是 `doc/specs/2026-07-03_phase6-jquery-field-disable-removal.md`（jQuery 欄位 enable/disable 移除重構）遺留的回歸 bug：該次重構將 jQuery 換成原生 `querySelectorAll` 時，未察覺 `KeyValueComponent` 的 `#content` 掛在 `<ng-container>` 上、`nativeElement` 是 Comment 節點的差異。第二、三個問題則是 PrimeNG 升級後 `p-input-icon-right` 舊 class 失效的樣式修正。

## 修改內容

### 問題描述

1. 在 ac0316 api-test 畫面，請求表身的 form-data 點選檔案後切換表身狀態（如切到 x-www-form-urlencoded），console 拋出：
   `TypeError: this.content.nativeElement.querySelectorAll is not a function at KeyValueComponent.ngAfterViewInit (key-value.component.ts:75)`
2. `key-value-grid-detail.component.html`（form-data 選檔後顯示檔名的欄位）中 `p-input-icon-right` class 失效，垃圾桶 icon 沒有定位在 input 內上下置中靠右，要求修正。
3. 使用者要求把另外兩個同樣使用失效 class 的檔案（`notify-list`、`lb0011`）一併修正。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/shared/key-value/key-value.component.html` | `#content` 從 `<ng-container>` 改掛到真實 `<div>`，修正 `querySelectorAll` TypeError |
| `src/app/shared/key-value-grid/key-value--grid-detail/key-value-grid-detail.component.html` | 失效的 `p-input-icon-right` span 改為 `p-iconfield`/`p-inputicon`；`[hidden]` 改為 `[class.d-none]` |
| `src/app/shared/notify-list/notify-list.component.html` | 搜尋欄 `p-input-icon-right` span 改為 `p-iconfield`/`p-inputicon` |
| `src/app/layout/labs/lb0011/lb0011.component.html` | 同上（與 notify-list 為相同的搜尋欄結構） |
| `src/styles/custom/_custom-prime.scss` | 移除已無使用處的 `.p-input-icon-right > i` 覆寫規則 |

### 詳細程式碼異動

#### 1. `key-value.component.html` — `#content` 改掛真實元素

```html
<!-- 修改前 -->
<ng-container #content>
  <label *ngIf="label">{{ label }}</label>
  <ng-template #keyvalue></ng-template>
</ng-container>

<!-- 修改後 -->
<!-- #content 必須是真實 DOM 元素（比照 key-value-grid）：
     ngAfterViewInit/setDisabledState 會對 nativeElement 呼叫 querySelectorAll，
     若掛在 ng-container 上 nativeElement 是 Comment 節點，執行期會拋 TypeError -->
<div #content>
  <label *ngIf="label">{{ label }}</label>
  <ng-template #keyvalue></ng-template>
</div>
```

#### 2. `key-value-grid-detail.component.html` — 檔名欄位改用 p-iconfield

```html
<!-- 修改前 -->
<span class="p-input-icon-right me-2" style="width: 100%;" [hidden]="_fileName==''">
  <i class="pi pi-trash tpi-i-search" (click)="removeFile();"></i>
  <input type="text" class="form-control tpi-i-input" readonly [value]="_fileName">
</span>

<!-- 修改後 -->
<p-iconfield iconPosition="right" class="me-2" style="width: 100%;" [class.d-none]="_fileName==''">
  <input type="text" class="form-control tpi-i-input" readonly [value]="_fileName">
  <p-inputicon styleClass="pi pi-trash tpi-i-search" style="cursor: pointer;" (click)="removeFile();" />
</p-iconfield>
```

#### 3. `notify-list.component.html` / `lb0011.component.html` — 搜尋欄改用 p-iconfield

兩檔結構相同，以 notify-list 為例：

```html
<!-- 修改前 -->
<span class="p-input-icon-right" style="width: 100%;">
  <i class="pi pi-search tpi-i-search" (click)="queryWebhookNotify();"></i>
  <input type="search" class="form-control tpi-i-input" id="keyword" formControlName="keyword" placeholder=""
    (keydown.enter)="queryWebhookNotify()">
</span>

<!-- 修改後 -->
<p-iconfield iconPosition="right" style="width: 100%;">
  <input type="search" class="form-control tpi-i-input" id="keyword" formControlName="keyword" placeholder=""
    (keydown.enter)="queryWebhookNotify()">
  <p-inputicon styleClass="pi pi-search tpi-i-search" style="cursor: pointer;" (click)="queryWebhookNotify();" />
</p-iconfield>
```

#### 4. `_custom-prime.scss` — 移除舊 class 殘留覆寫

```scss
/* 修改前 */
.p-dropdown-clear-icon,
.p-input-icon-right > i {
  transform: translateY(25%);
}

/* 修改後 */
.p-dropdown-clear-icon {
  transform: translateY(25%);
}
```

## 預計修改方式

### 需求 1：KeyValueComponent `querySelectorAll` TypeError

**Context** — 使用者回報在 api-test 的 form-data 選檔後切換表身狀態時，`KeyValueComponent.ngAfterViewInit`（key-value.component.ts:75）拋出 `querySelectorAll is not a function`。

**分析** —
- `key-value.component.html` 的 `#content` 模板參考掛在 `<ng-container>` 上；`ng-container` 不產生真實 DOM，`@ViewChild('content') content!: ElementRef` 拿到的 `nativeElement` 是 **Comment 節點**，沒有 `querySelectorAll` 方法。姊妹元件 `key-value-grid` 的 `#content` 掛在 `<div style="width: 100%">` 上，所以不受影響。
- `git log` 追到 commit `cdf23eee1`（2026-07-03 Phase 6 jQuery 移除）：原寫法 `$(this.content.nativeElement).find('input , button')` 對 Comment 節點是**靜默回傳空集合的 no-op**，改成原生 `querySelectorAll` 後同一情境變成直接拋 TypeError。也就是說這段 disable 傳遞機制在 `KeyValueComponent` 上從 jQuery 時代就從未實際生效，只是以前不會炸。
- 影響面查證：全專案唯一會 `disable()` 綁定到 `app-key-value` 之 control 的地方是 `ac0311.component.ts:418`（`keyValueForm.disable()`），但該 `app-key-value` 只在 `requestBody == 'form' && requestBodyCheck == true` 時渲染（ac0311.component.html:958），而 valueChanges handler 在 `'form'` case 一定會接著 `enable()`（ac0311.component.ts:428），因此「disable 機制從失效變生效」不會產生可見行為變化。
- 也確認全專案沒有任何 CSS selector 以 `app-key-value` 為目標（grep `*.css/*.scss` 無結果），多包一層 `<div>` 沒有樣式風險。

**修正方案** — 比照 `key-value-grid` 既有寫法，把 `#content` 改掛到真實 `<div>` 上。評估過的另一方案是在 TS 加 `nativeElement instanceof Element` 防呆（保留 no-op 語意、零行為變化），但那會讓 CVA 的 `setDisabledState` 機制在此元件上永久失效，與前案 spec 明確記載「這是必要機制」的結論矛盾；改掛真實元素同時修 crash 並讓機制如設計運作，且與姊妹元件一致。

### 需求 2：key-value-grid-detail 的 `p-input-icon-right` 失效

**Context** — 使用者指出檔名欄位的 `p-input-icon-right` 失效，垃圾桶 icon 應在 input 內上下置中靠右。

**分析** —
- `p-input-icon-right` 是 PrimeNG 17 以前的 CSS class；本專案是 PrimeNG 20，v18 起官方**移除了 `p-input-icon-*` 的所有內建樣式**，改以 `IconField`/`InputIcon` 元件（`<p-iconfield>`/`<p-inputicon>`）取代，因此該 class 現在完全沒有定位效果。
- 專案內已有新寫法前例：`src/app/layout/components/search-input/search-input.component.html` 使用 `<p-iconfield iconPosition="right">` + `<p-inputicon>`；`PrimengModule` 已 import/export `IconFieldModule`、`InputIconModule`，`KeyValueGridDetailComponent` 由 `SharedModule` 宣告且該模組 import `PrimengModule`，可直接使用。
- 檢查 `node_modules/@primeuix/styles/dist/iconfield/index.mjs` 確認 v20 樣式：`.p-iconfield { position: relative; display: block; }`、`.p-inputicon { position: absolute; top: 50%; margin-top: calc(-1 * (icon.size / 2)); }`、以 `:first-child`/`:last-child` 決定左右定位 → **`<p-inputicon>` 必須放在 input 之後**才會靠右。
- 發現隱藏機制的坑：原本的 `[hidden]="_fileName==''"` 靠瀏覽器 UA stylesheet 的 `[hidden] { display: none }`，但 author stylesheet（`.p-iconfield { display: block }`）優先權一律高於 UA stylesheet，`[hidden]` 會失效 → 改用 Bootstrap `.d-none`（`display: none !important`）。
- PrimeNG 對 input 自動加 padding 的規則只針對 `.p-inputtext`，本處 input 是 Bootstrap `.form-control` 不適用，故保留既有 `tpi-i-input`（`padding-right: 30px`，styles.scss:439）避免文字壓到 icon；`tpi-i-search`（hover 橘色）也一併保留。

**修正方案** — 改為 `p-iconfield`/`p-inputicon` 結構（input 在前、icon 在後），`[hidden]` 換成 `[class.d-none]`，保留原有 class 與 `removeFile()` 行為。不採「在全域 scss 補回舊 class 樣式」方案，因為專案的前進方向已是 `p-iconfield`（search-input 前例），補回死 class 樣式會固化淘汰寫法。

### 需求 3：一併修正 notify-list 與 lb0011

**Context** — 前一輪回報時已指出這兩檔有同樣失效的 class，使用者要求一併修正。

**分析** — 兩檔是相同的搜尋欄結構（`pi pi-search` icon + `(click)` 查詢 + `(keydown.enter)`）；`NotifyListComponent` 由 `SharedModule` 宣告、`lb0011.module.ts` 有 import `PrimengModule`，皆可直接使用 `p-iconfield`。修完後 grep 確認全專案已無 `p-input-icon` 實際使用，`_custom-prime.scss:105` 的 `.p-input-icon-right > i { transform: translateY(25%) }` 成為死規則。

**修正方案** — 兩檔比照需求 2 的寫法改為 `p-iconfield`/`p-inputicon`（無隱藏需求，不需 `d-none` 處理），並移除 scss 中的死規則；同組選擇器中仍在使用的 `.p-dropdown-clear-icon` 保留不動。

## 驗證方式

### 靜態驗證

- `npm run build`（production）：三輪修改各跑一次，皆**成功**、無 error。
- `grep -rn "p-input-icon" src/`：確認僅剩三處 HTML 說明註解，無實際 class 使用。
- 每次 build 後依慣例 `git checkout` 還原被弄髒的 `src/main/resources/static/dgrv4/ac4/` 已追蹤產物，並刪除新產生的 untracked hash 檔名 bundle；最終 `git status` 僅剩本次五個來源檔的修改。

### 尚待人工驗證（建議）

本環境無瀏覽器自動化工具，建議人工測試：

1. **ac0316 api-test**：form-data 點選檔案 → 切換表身狀態（none / form-data / x-www-form-urlencoded / raw），確認 console 不再出現 `querySelectorAll is not a function`，且 x-www-form-urlencoded 的 key-value 欄位可正常輸入。
2. **api-test form-data 檔名欄位**：選檔後垃圾桶 icon 顯示於 input 內垂直置中靠右，點擊可移除檔案；未選檔時該欄位正確隱藏。
3. **lb0011（Labs > Webhook）與使用 notify-list 的畫面**：搜尋 icon 定位正確，點擊 icon 與按 Enter 均能觸發查詢，icon hover 變橘色。
4. **ac0311**：請求表身切到「表單」模式，key-value 欄位維持可輸入（disable 機制從 no-op 變為實際生效後的回歸確認）。

## 實作紀錄

### 時間軸

1. 使用者回報 api-test 切換表身狀態時 `KeyValueComponent.ngAfterViewInit` 拋 `querySelectorAll is not a function`。
2. 讀取 `key-value.component.ts/.html`，發現 `#content` 掛在 `<ng-container>` 上 → `nativeElement` 是 Comment 節點；對照 `key-value-grid` 的 `#content` 是真實 `<div>`，確立差異來源。
3. `git log` 追出 7/3 commit `cdf23eee1`（jQuery → 原生 DOM 重構）為觸發點；讀取該次的 spec（`2026-07-03_phase6-jquery-field-disable-removal.md`），發現重構時的「行為等價」假設漏掉了 jQuery 對 Comment 節點靜默 no-op、`querySelectorAll` 直接拋錯的差異。
4. 評估兩個修法：TS 防呆（零行為變化但機制永久失效）vs 模板改掛真實 `<div>`（修 crash 且讓機制生效）。逐一查證 `setDisabledState` 的實際使用面（僅 ac0311，且顯示時必為 enabled）與 CSS 依賴（無）後，採後者。
5. build 驗證通過，還原 build 產物。回報時附帶說明「disable 從未生效變為生效」的副作用與依據。
6. 使用者接著提出 `p-input-icon-right` 失效問題。grep 出全專案三處使用 + 一條 scss 覆寫；判定原因為 PrimeNG 18+ 移除 `p-input-icon-*` 樣式。
7. 找到專案內既有的 `p-iconfield` 前例（search-input 元件），並讀 `@primeuix/styles` 原始碼確認 v20 的定位規則（icon 需為 last-child 才靠右）與 `.p-iconfield { display: block }` 會使 `[hidden]` 失效的問題，決定改用 `[class.d-none]`。
8. 依使用者指示先只修 key-value-grid-detail 一檔，build 通過；回報時列出另外兩檔與 scss 死規則為後續建議。
9. 使用者要求一併修正。確認 notify-list / lb0011 所屬模組皆有 `PrimengModule` 後套用相同改法，並移除 scss 死規則。
10. build 通過。清理 build 產物時第一次指令因 `git status --porcelain` 輸出的路徑是相對 repo 根目錄、而 shell cwd 在 `srcAngular`，`rm` 找不到檔案（exit 123）；改為從 repo 根目錄執行後清理成功。
11. 產出本紀錄檔。**本次所有修改尚未 commit / push。**

### 經驗紀錄（給未來類似任務參考）

- **jQuery → 原生 DOM 遷移不只是 API 對應，還有容錯語意差異**：jQuery 對非 Element 輸入（Comment 節點、空集合）幾乎都靜默容忍，原生 DOM API 會直接拋錯。遷移時要逐一確認每個呼叫點的 `nativeElement` 實際是什麼節點——特別是 `@ViewChild` 掛在 `<ng-container>`／`<ng-template>` 上時拿到的不是 Element。
- **`#templateRef` 掛在 `ng-container` 上的 `ElementRef.nativeElement` 是 Comment 節點**：需要對容器做 DOM 查詢的 `#ref`，一律掛在真實元素上（本專案 `key-value-grid` 即正確示範）。
- **PrimeNG 18+ 已移除 `p-input-icon-left/right` 樣式**：升級後這類 class 不會報錯、只是靜默失效，要主動 grep 清查。本專案的標準替代寫法是 `<p-iconfield iconPosition="right">` + `<p-inputicon>`（參考 `search-input` 元件），icon 元素必須放在 input **之後**（`:last-child` 決定靠右定位）。
- **`[hidden]` 對自帶 `display` 樣式的元件無效**：author stylesheet 的 `display: block` 一律蓋過 UA stylesheet 的 `[hidden] { display: none }`，優先權與 specificity 無關。對 PrimeNG 元件（如 `p-iconfield`）要隱藏時改用 Bootstrap `.d-none`（`!important`）或 `*ngIf`。
- **修「從未生效的機制」前先查使用面**：讓休眠多年的功能（本例的 CVA disable 傳遞）開始生效也是一種行為變化，要先 grep 出所有實際呼叫點、確認每個情境下的最終狀態不變，才能安心採用根因修法而非防呆繞過。
- **`git status --porcelain` 的路徑相對於 repo 根目錄**：在 monorepo 子目錄下把它的輸出直接餵給 `rm`/`xargs` 會找錯路徑，應先 `cd` 到 `git rev-parse --show-toplevel` 再操作。
