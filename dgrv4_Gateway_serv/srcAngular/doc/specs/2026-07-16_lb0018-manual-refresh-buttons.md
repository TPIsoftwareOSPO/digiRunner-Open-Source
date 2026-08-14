# lb0018 Fhir Viewer：FHIR Base URL 取消輸入時自動驗證，改以手動刷新鈕觸發

延續同日的 [`2026-07-16_lb0018-patient-dashboard-auth-flow.md`](./2026-07-16_lb0018-patient-dashboard-auth-flow.md)（Patient Dashboard 分頁與 Auth Method 測試流程）與 [`2026-07-16_lb0018-everything-paginator-patient-search.md`](./2026-07-16_lb0018-everything-paginator-patient-search.md)（`$everything` 分頁與病人搜尋優化）。本次聚焦於區塊一「Fhir base URL or well know」的驗證觸發時機，以及 Tab 2「Patient Dashboard」的手動刷新機制。

## 修改內容

### 問題描述

使用者在同一次工作階段中依序提出以下需求（含中途的修正）：

1. Patient Dashboard 的查詢資料路徑應參考「FHIR Base URL」輸入框內的值，主要考量使用者未挑選 Smart On FHIR Proxy 下拉選單、直接填寫 FHIR Base URL 的情境。
2. （經調查說明需求 1 已是現況、並指出真正的缺口後）在 Tab 2 增加一個刷新按鈕來觸發重新查詢，不要自動觸發。
3. 刷新鈕改放在 Tab 2 分頁標題旁邊；FHIR Base URL 在 keydown 後不要執行自動刷新，並在該 input 旁也放一個刷新按鈕讓使用者手動點選。
4. 刷新鈕要有 icon（實際上是原本填的 icon class 在本專案的 FontAwesome 版本下不存在，導致按鈕呈現為空白）。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | 移除 `fhirBaseUrl.valueChanges` 自動驗證鏈，改為手動 `refreshFhirBaseUrl()`；`sofProxyUrl` 選取後改為顯式呼叫驗證；新增 `onRefreshPatients()` |
| `src/app/layout/labs/lb0018/lb0018.component.html` | FHIR Base URL input 旁新增刷新鈕；Tab 2 分頁標題旁新增刷新鈕 |

i18n 直接重用既有的 `button.refresh`，四份語系檔皆已存在，**未新增任何 key**。

### 詳細程式碼異動

#### 1. 移除 FHIR Base URL 的輸入時自動驗證鏈

`lb0018.component.ts` 修改前：輸入後 debounce 500ms 自動打 `GET {base}/metadata` 驗證。

```ts
// FHIR Base URL 驗證鏈：輸入後 debounce 再以 GET {base}/metadata 確認是否為有效的 FHIR base url
this.fhirBaseUrl.valueChanges
  .pipe(
    debounceTime(500),
    distinctUntilChanged(),
    switchMap((url: string) => {
      const base = this.trimTrailingSlash((url || '').trim());
      if (!base) return of({ base, ok: null as boolean | null });
      this.beginCheckLoading();
      return this.httpClient
        .get(`${base}/metadata`, { headers: this.bypassHeaders() })
        .pipe(
          map(() => ({ base, ok: true as boolean | null })),
          catchError(() => of({ base, ok: false as boolean | null })),
          finalize(() => this.endCheckLoading()),
        );
    }),
    takeUntil(this.destroy$),
  )
  .subscribe(({ base, ok }) => {
    this.baseUrlNotFound = ok === false;
    if (ok) {
      this.wellKnownUrl.setValue(`${base}/.well-known/smart-configuration`);
    } else {
      this.wellKnownUrl.setValue('');
    }
  });
```

修改後：整段刪除，改為手動觸發的方法。行為（驗證成功才組 well-known URL、失敗顯示 `base_url_not_found`、`finalize` 確保 loading 關閉）維持一致：

```ts
/**
 * 手動刷新 FHIR Base URL（由輸入框旁的刷新鈕、或選取 proxy 下拉時觸發）：
 * 以 GET {base}/metadata 確認是否為有效的 FHIR base url，
 * 驗證成功才組出 well-known URL，觸發後續的 well-known 驗證鏈取得 token_endpoint。
 * 註：輸入時不自動驗證，避免每次 keydown 都打 metadata。
 */
refreshFhirBaseUrl(): void {
  const base = this.currentBaseUrl();
  if (!base) {
    this.baseUrlNotFound = false;
    this.wellKnownUrl.setValue('');
    return;
  }
  this.beginCheckLoading();
  this.httpClient
    .get(`${base}/metadata`, { headers: this.bypassHeaders() })
    .pipe(
      map(() => true),
      catchError(() => of(false)),
      finalize(() => this.endCheckLoading()),
      takeUntil(this.destroy$),
    )
    .subscribe((ok) => {
      this.baseUrlNotFound = !ok;
      this.wellKnownUrl.setValue(ok ? `${base}/.well-known/smart-configuration` : '');
    });
}
```

> 原本以 `switchMap` 取得的「新輸入自動取消前一次請求」特性，在改為手動觸發後不再具備。因為觸發來源已改為使用者明確點擊，連點造成的重複請求風險低，故未額外加入去重處理。

#### 2. `sofProxyUrl` 下拉選取後改為顯式呼叫驗證

原本下拉只 `setValue`，靠 `fhirBaseUrl.valueChanges` 連鎖觸發驗證；驗證鏈移除後，需顯式呼叫：

```ts
// 下拉選擇 proxy 後自動帶入 FHIR Base URL（仍可手動修改）並立即驗證
this.sofProxyUrl.valueChanges
  .pipe(takeUntil(this.destroy$))
  .subscribe((url: string) => {
    if (url) {
      this.fhirBaseUrl.setValue(url);
      this.refreshFhirBaseUrl();
    }
  });
```

#### 3. 新增 `onRefreshPatients()`

```ts
/**
 * 刷新按鈕：以區塊一「FHIR Base URL」輸入框當下的值重新查詢病人清單。
 * 停留在本分頁時修改 base URL 不會自動重載（重載僅由 switchTab 觸發），
 * 故提供手動刷新；查詢路徑一律取 currentBaseUrl()，與是否使用 proxy 下拉無關。
 */
onRefreshPatients(): void {
  this.onSearchPatients();
}
```

實作上等同 `onSearchPatients()`（因 `loadPatientPage()` 一律以 `currentBaseUrl()` 組 URL），保留獨立方法是為了讓 template 有語意明確的 handler，並附上「為何需要手動刷新」的註解。

#### 4. Template 兩顆刷新鈕

FHIR Base URL input 旁（以既有共用 class `.field-flex-start` 包住 input + 按鈕）：

```html
<div class="field-flex-start">
  <input class="form-control" id="fhirBaseUrl" formControlName="fhirBaseUrl" autocomplete="off" />
  <button type="button" class="btn-icon tpi-second" title="{{ 'button.refresh' | translate }}"
    (click)="refreshFhirBaseUrl()">
    <i class="fa fa-sync"></i>
  </button>
</div>
```

Tab 2 分頁標題旁（外層 `<li>` 本身有 `switchTab('tab2')` 的 click，故需 `stopPropagation`）：

```html
<li role="presentation" [class.active]="activeTab === 'tab2'" (click)="switchTab('tab2')">
  <b role="tab">{{ 'fhir_viewer.tab_dashboard' | translate }}
    <!-- 停留在本分頁時修改 FHIR Base URL 不會自動重載，由此鈕手動觸發；stopPropagation 避免同時觸發外層 li 的切換 -->
    @if (activeTab === 'tab2' && hasFhirBase) {
      <button type="button" class="btn-icon tpi-second ms-2" title="{{ 'button.refresh' | translate }}"
        (click)="$event.stopPropagation(); onRefreshPatients()">
        <i class="fa fa-sync"></i>
      </button>
    }
  </b>
</li>
```

## 預計修改方式

### 需求 1：Patient Dashboard 查詢路徑應取 FHIR Base URL 輸入框的值

**Context**

使用者一開始只給了片段訊息「FHIR Base URL 的 base url 來源請參考 Fhir base URL or well know 區塊的 FHIR Base URL」，未指明頁面；經詢問後確認是 lb0018，並補充考量點：使用者可能不挑 proxy 下拉、直接手打 base URL。

**分析（已對照原始碼確認）**

- grep `well.?known`／`fhir_viewer` 定位到 `lb0018`（另有 `lb0016` SMART Launcher 也有一個「FHIR Base URL」唯讀框，值由 `${window.location.origin}/smart-on-fhir/${proxyName}${path}` 組成，與本需求無關）。
- 讀 `lb0018.component.ts` 確認：`currentBaseUrl()`（`:653`）就是 `trimTrailingSlash(fhirBaseUrl.value.trim())`，註解明寫「區塊一輸入的 FHIR Base URL」。
- 所有查詢皆走 `currentBaseUrl()`：`loadPatientPage`、`loadEverything`、`onEverythingPageChange`、`switchTab`、`hasFhirBase`。
- `sofProxyUrl` 下拉的唯一作用是 `this.fhirBaseUrl.setValue(url)`（`:214-218`），只是「預先填入」，之後查詢並不讀下拉的值。

**結論與修正方案**

需求 1 **已是現況**，不需要修改。為避免改動一段本來就正確的程式碼（違反「最小化改動」「嚴禁無關重構」），回報現況並指出實際存在的相關缺口：**停留在 Tab 2 時修改 FHIR Base URL 不會重新查詢**——因為重載觸發點只在 `switchTab()`（`:360-369`，條件為 `base !== lastPatientListBase`），而區塊一在分頁上方永遠可見。此缺口與「手打或下拉」無關，兩種方式都會遇到。

### 需求 2：Tab 2 增加刷新按鈕，不要自動觸發

**Context**

使用者確認上述缺口，但明確要求「增加一個刷新按鈕來觸發，不要自動觸發」（即不採用「base 變更後自動重載」的方案）。

**分析**

- `loadPatientPage(0, false)` 一律以 `currentBaseUrl()` 取當下輸入值，並在成功後更新 `lastPatientListBase`（`:436`），因此手動刷新後再切分頁不會重複查詢。
- 既有 `onSearchPatients()` 的內容即為「取關鍵字 + `loadPatientPage(0, false)`」，功能上已等同刷新。
- i18n：grep 四份語系檔確認 `button.refresh` 皆存在（zh-tw「重新整理」/ zh-cn「重新整理」/ en-us「Refresh」/ ja「リフレッシュ」），依「優先使用既有資源」不新增 key。

**修正方案**

新增 `onRefreshPatients()` 委派給 `onSearchPatients()`，並在 template 加刷新鈕。第一版誤放在左欄病人搜尋列（搜尋鈕旁），後於需求 3 被要求移位。

### 需求 3：刷新鈕移到分頁標題旁；FHIR Base URL 取消 keydown 自動刷新

**Context**

使用者中斷了 build 驗證，直接提出三點修正：刷新鈕放到 Tab 2 分頁標題旁、FHIR Base URL 不要在 keydown 後自動刷新、該 input 旁也要有手動刷新鈕。

**分析**

- 移除 `fhirBaseUrl.valueChanges` 驗證鏈後，`sofProxyUrl` 下拉的連鎖驗證會一併失效，需顯式補上 `refreshFhirBaseUrl()`。
- `wellKnownUrl.valueChanges` 驗證鏈（`debounceTime(500)` → 取 `token_endpoint` → 帶入 `testUrl`）維持不動：使用者只提到 FHIR Base URL，且 `refreshFhirBaseUrl()` 以 `setValue` 寫入 well-known URL 時仍會觸發該鏈，原有串接行為得以保留。
- grep 確認 `debounceTime`／`distinctUntilChanged`／`switchMap`／`catchError`／`finalize` 於 well-known 鏈仍有使用，import 無須調整。
- 分頁標題的 `<li>` 有 `(click)="switchTab('tab2')"`，按鈕置於其內必須 `$event.stopPropagation()`，否則點刷新會連帶觸發切換。

**修正方案**

如「詳細程式碼異動」1～4 所述。下拉選取仍維持自動驗證（判斷依據：使用者要求的是「keydown 後不要自動刷新」，而選取下拉是一次明確的使用者動作，保留可維持原本操作手感）——此決定已於回報時明確告知使用者，待其確認。

### 需求 4：刷新鈕要有 icon

**Context**

使用者反應「刷新鈕請幫我放個 icon」，但當下 template 其實已寫 `<i class="fa fa-refresh"></i>`——推測畫面上按鈕呈現空白。

**分析（已實際查證 node_modules）**

- `package.json` 依賴為 `@fortawesome/fontawesome-free: ^5.15.4` 與 `@fortawesome/fontawesome-free-webfonts: ^1.0.9`（實際安裝版本 5.15.4 / 1.0.9，後者內容為 Font Awesome Free 5.0.13）。
- `angular.json` 載入 `fontawesome-free/css/all.min.css` 與 `fontawesome-free-webfonts/css/fontawesome.css`，**未載入 v4 shims**（`fontawesome-v4-shims.css`）。
- grep 兩個套件的 CSS：`fa-refresh` 出現 **0 次**（FA4 舊名，FA5 已改名），`fa-sync` 存在，glyph 為 `.fa-sync:before{content:"\f021"}`（與 FA4 `fa-refresh` 同一字碼，外觀一致）；另有 `fa-sync-alt`（`\f2f1`）。
- 確認 `.fa` 在 FA5 是 solid 別名（`all.min.css` 內 `.fa,.fas{font-weight:900}`），故沿用專案既有的 `fa fa-xxx` 寫法即可，不需改成 `fas`。
- grep `src/` 確認 `fa-refresh` 除了本次新增的兩處外，專案內無其他使用；既有的 `fa-search`／`fa-plus`／`fa-copy` 在 FA5 皆同名，未受影響。

**修正方案**

兩處 icon class 由 `fa fa-refresh` 改為 `fa fa-sync`。

## 驗證方式

### 靜態驗證

- `npx ng build --configuration development` 執行成功，無 TypeScript／template 編譯錯誤（`Build at: 2026-07-16T09:42:25` / Hash `394bc595f5544f05`）。
- 過程中 IDE 曾回報 `Property 'refreshFhirBaseUrl' does not exist on type 'Lb0018Component'`，經 grep 確認方法確實存在於 `lb0018.component.ts:336`，且 build 無此錯誤，判定為 Angular language service 的過期快取，非實際問題。
- icon 可用性以 grep 實際安裝的 FontAwesome CSS 確認（見需求 4 分析），非憑印象判斷。

### 尚待人工驗證（建議）

- 啟動 `npm start`，開啟 lb0018 頁面：
  - 在 FHIR Base URL 輸入框打字，確認 **不會**再自動打 `{base}/metadata`（可由 DevTools Network 觀察），且不再出現輸入中途閃現「找不到 fhir base url」的情形。
  - 點擊 input 旁刷新鈕，確認會驗證 base URL、成功後自動帶出 `.well-known/smart-configuration` 與 `token_endpoint`，失敗時顯示錯誤訊息。
  - 選取 Smart On FHIR Proxy 下拉，確認仍會自動帶入並驗證（此為本次保留的行為，待使用者確認是否符合預期）。
  - 停留在 Tab 2 修改 FHIR Base URL 後，點擊分頁標題旁的刷新鈕，確認病人清單以新的 base URL 重查。
  - 確認點擊分頁標題旁的刷新鈕**不會**觸發分頁切換（`stopPropagation` 是否生效）。
  - 確認兩顆按鈕的 `fa-sync` icon 正常顯示為循環箭頭。

## 實作紀錄

### 時間軸

1. **需求提出（訊息不完整）**：使用者僅輸入「上傳」二字，無法判斷意圖，詢問後確認為 git 相關；接著要求把 `.mcp.json` 加入 commit 並 push（與 lb0018 無關，已完成，commit `c3663fb7c`）。
2. **需求 1 提出（訊息片段）**：使用者給出「FHIR Base URL 的 base url 來源請參考 Fhir base URL or well know 區塊的 FHIR Base URL」，未指明頁面。
3. **調查**：grep 定位到 `lb0018` 與 `lb0016` 兩處都有「FHIR Base URL」，因無法判斷而詢問使用者，得到回覆「lb0018」。
4. **再調查，發現需求已是現況**：讀 `lb0018.component.ts` 確認所有查詢都走 `currentBaseUrl()` → `fhirBaseUrl.value`，下拉僅作預填。回報現況，並主動指出真正的缺口（停留在 Tab 2 改 base URL 不會重載），提議在驗證鏈成功後自動觸發重載。
5. **使用者否決自動觸發**：明確要求改用刷新按鈕手動觸發。
6. **實作（第一版，位置後被要求修正）**：i18n 先 grep 確認 `button.refresh` 四語系皆有、不新增 key；在左欄病人搜尋列的搜尋鈕旁加刷新鈕，並新增 `onRefreshPatients()`。
7. **build 被中斷**：正要執行 `ng build` 驗證時被使用者中斷，改提出需求 3（移位 + 取消 keydown 自動刷新 + input 旁加鈕）。
8. **實作（第二版）**：移除左欄的刷新鈕；移除 `fhirBaseUrl.valueChanges` 驗證鏈改為 `refreshFhirBaseUrl()`；`sofProxyUrl` 補上顯式驗證呼叫；FHIR Base URL input 旁與 Tab 2 分頁標題旁各加一顆刷新鈕（含 `stopPropagation`）。
9. **驗證**：grep 確認 rxjs operator import 仍被 well-known 鏈使用；`ng build --configuration development` 通過。回報時主動點出「下拉選取仍保留自動驗證」與「well-known 輸入框仍是自動驗證」兩項未經使用者指示的判斷，供其確認。
10. **使用者微調 template**：使用者自行把 Tab 2 的刷新鈕移入 `<b>` 標籤內、按鈕色改為 `tpi-second`（與 input 旁那顆一致）。已保留此改動，未還原。
11. **需求 4 提出**：使用者要求「刷新鈕請幫我放個 icon」。
12. **調查（發現 icon class 無效）**：按鈕其實已有 `<i class="fa fa-refresh">`，推測是沒渲染出來。查 `package.json` 得知是 FA5，查 `angular.json` 確認未載 v4 shims，grep 實際安裝的 CSS 確認 `fa-refresh` 完全不存在、`fa-sync` 才是 FA5 的對應名稱且 glyph 相同。
13. **實作**：兩處 icon 改為 `fa fa-sync`；`ng build` 再次通過，無 error。
14. **建立本紀錄檔**：使用者要求建立工作日誌並上傳。
15. **發現 build 產物污染工作區**：執行 `git diff --stat` 準備 commit 時發現，前述兩次 `ng build --configuration development` 把 development 產物輸出到 `angular.json` 設定的 `outputPath`（`../src/main/resources/static/dgrv4/ac4/`），覆蓋／刪除了原本已納入版控的 production build 產物，造成 80+ 個非預期的檔案異動。此為本次驗證動作的副作用，需在 commit 前還原，**不得提交**。
16. **本次工作的 commit/push 狀態**：見本文件末尾補述（撰寫本文件當下尚未提交）。

### 經驗紀錄（給未來類似任務參考）

- **需求描述與現況不符時，先回報而不是照字面改**。本次需求 1「查詢路徑要取輸入框的值」實際上早已成立，若直接動手「修正」，等於改動一段正確的程式碼、卻沒解決使用者真正遇到的問題。先把現況（哪一行、哪個方法）攤開來講，反而引導出真正的缺口與使用者要的解法（手動刷新鈕）。
- **使用者的片段需求（如只有「上傳」「FHIR Base URL 的來源」）不要靠猜**。本次兩次靠詢問才定位到正確目標（git push、lb0018），若貿然猜測極可能改錯檔案。但詢問前應先做完可做的調查（grep 定位候選），把選項與依據一併提供，讓使用者只需回答關鍵字。
- **icon class 是否存在，要查實際安裝的套件 CSS，不要憑常識**。`fa fa-refresh` 是 FA4 時代最常見的寫法，在 FA5 專案裡不會有任何編譯期錯誤，只會靜靜地渲染成空白。本專案為 FA5.15.4 且未載入 v4 shims，`fa-refresh`／`fa-sync` 之類被改名的 icon 必須用 FA5 名稱。未來新增 icon 時，建議先 grep `node_modules/@fortawesome/fontawesome-free/css/all.min.css` 確認 class 存在。
- **移除 RxJS 自動觸發鏈時，要一併檢查誰在依賴這條鏈的連鎖效果**。本次 `fhirBaseUrl.valueChanges` 被移除後，`sofProxyUrl` 下拉的驗證會跟著失效（因為它只做 `setValue`、靠連鎖觸發），必須顯式補上呼叫。同理也要確認 operator import 是否還有其他使用者，再決定是否移除 import。
- **在 template 的可點擊容器內放按鈕，記得 `$event.stopPropagation()`**。本次分頁標題 `<li>` 本身綁了 `switchTab`，刷新鈕若不阻擋冒泡，點刷新會連帶切分頁。
- **`ng build` 在本專案會寫入 `../src/main/resources/static/dgrv4/ac4/`（版控中的 production 產物），不是 `dist/`**。用 `--configuration development` 做編譯驗證會以 dev 產物覆蓋掉已提交的 production 產物，污染工作區。未來若只是要確認「有沒有編譯錯誤」，應優先考慮不落地產物的方式（例如 `npx tsc --noEmit`、`ng build --dry-run` 類做法），或在 build 後立即還原該目錄，並且**commit 前務必看一次 `git status`／`git diff --stat`**，確認沒有把建置產物一起帶進去。
