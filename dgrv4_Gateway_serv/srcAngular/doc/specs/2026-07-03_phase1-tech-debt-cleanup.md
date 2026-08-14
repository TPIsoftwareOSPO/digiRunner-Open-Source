# Angular 13→20 技術債整理 Phase 1：低風險建置層級清理

工作計畫全文：`.claude`（session 內 Plan 產物，未存入儲存庫）依此摘要重建於下。

## 背景

專案從 Angular 13 逐步升級到 Angular 20，套件版本已跟上，但程式碼與建置設定仍保留大量升級前的舊寫法（standalone 採用率低、`*ngIf`/`*ngFor` 未轉 `@if`/`@for`、`inject()` 幾乎未用、RxJS subscription 清理不足、`any` 型別氾濫、舊版 PrimeNG grid class、webpack-based builder、IE 專用 polyfill、jQuery 全域依賴等）。已規劃六個 Phase 的分階段整理計畫，本次完成 **Phase 1：低風險、全域受益的建置層級清理**。

Phase 1 原定範圍只有三項（皆為零業務邏輯影響的清理），但驗證過程中額外發現並修正了兩個「與本次改動無關、但會直接擋住 `npm run build`/`npm test` 的既有問題」。

## 修改內容

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/polyfills.ts` | 移除 IE9/10/11 專用的 `core-js/es6`、`core-js/es7` polyfill import（16 行）與 `Element.prototype.matches` fallback |
| `package.json` / `package-lock.json` | 移除已無任何程式碼引用的 `core-js` 依賴 |
| `src/styles/styles.scss` | 移除整段「Deprecated Grid CSS」（`ui-grid*`、`ui-g*`、`ui-sm-*`/`ui-md-*`/`ui-lg-*`/`ui-xl-*`，共 779 行），保留仍在使用的 `.ui-inputgroup`、`.ui-helper-clearfix` |
| `src/app/layout/ac04/ac0432/ac0432.component.html` | 僅存的一處「活躍」`ui-g`/`ui-g-6` 用法，改為 Bootstrap `row`/`col-6` |
| `src/app/layout/components/frame/frame.component.ts` | 移除未使用、造成 jQuery 重複打包的 `import { event } from 'jquery'` |
| `tsconfig.app.json` / `tsconfig.spec.json` | **（計畫外，見下）** `types` 陣列補上 `"jquery"` |
| `src/app/layout/ac05/ac0509/ac0509.component.spec.ts` | **（計畫外，見下）** 已移除的 `async` 改為 `waitForAsync` |

### 詳細程式碼異動

#### `src/polyfills.ts`

修改前（節錄）：

```ts
/** IE9, IE10 and IE11 requires all of the following polyfills. **/
import 'core-js/es6/symbol';
import 'core-js/es6/object';
... // 共 16 行 core-js import
import { Buffer } from 'buffer';
...
 if (!Element.prototype.matches) {
  Element.prototype.matches = (<any>Element.prototype).msMatchesSelector ||
      Element.prototype.webkitMatchesSelector;
}
```

修改後：

```ts
import { Buffer } from 'buffer';
```

`(window as any).process = ...` 與 `(window as any).Buffer = ...` 兩行保留不動——這兩行是否仍必要，留待 Phase 3（esbuild builder 遷移）用實際 trial build 驗證後再決定，本次不處理。

#### `src/styles/styles.scss`

移除範圍：`.ui-grid`/`.ui-grid-*`（舊版 grid）、`.ui-g`/`.ui-g-*`/`.ui-g-offset-*`（"New Grid CSS"）、`.ui-sm-*`/`.ui-md-*`/`.ui-lg-*`/`.ui-xl-*`（RWD 斷點）、`.ui-g-nopad`，以及檔案後段重複的 minified `.ui-g{...}` 規則與其註解副本。

**移除前先逐一確認全站使用範圍**：

- `ui-sm-*`/`ui-md-*`/`ui-lg-*`/`ui-xl-*`/`ui-g-nopad`：全站 0 處引用，直接刪除。
- `ui-g`/`ui-g-6`：全站 17 個檔案有 class 字面比對，但實際展開後發現其中 **16 個檔案的用法都包在已停用的 HTML 註解區塊內**（舊版 `<p-toast>` 刪除確認樣板，已被 `p-confirmDialog` 取代），只有 `ac0432.component.html` 是真正會渲染的活躍用法。依「拒絕無關重構」原則，16 處註解內容維持不動，只處理這 1 處活躍用法。
- `.ui-inputgroup`（49 個檔案在用）、`.ui-helper-clearfix`（14 個檔案在用）：雖然這兩個 class 的 CSS 規則實體位置與待刪除的 grid CSS 相鄰，但屬於不同用途的既有 PrimeNG 輔助 class，**予以保留**，未被誤刪。

#### `src/app/layout/ac04/ac0432/ac0432.component.html`

```html
<!-- 修改前 -->
<div class="ui-g ui-fluid">
    <div class="ui-g-6">
        <button type="button" pButton (click)="onStartStopConfirm()" label="Yes" class="ui-button-success"></button>
    </div>
    <div class="ui-g-6">
        <button type="button" pButton (click)="onReject()" label="No" class="ui-button-secondary"></button>
    </div>
</div>
<!-- 修改後 -->
<div class="row">
    <div class="col-6">
        <button type="button" pButton (click)="onStartStopConfirm()" label="Yes" class="ui-button-success"></button>
    </div>
    <div class="col-6">
        <button type="button" pButton (click)="onReject()" label="No" class="ui-button-secondary"></button>
    </div>
</div>
```

同一檔案內第二處刪除確認用的 `<p-toast>`（`onDeleteConfirm`/`onReject`）比照處理。

#### `src/app/layout/components/frame/frame.component.ts`

```ts
// 修改前
import { delay, Subscription } from 'rxjs';
import { event } from 'jquery';

// 修改後
import { delay, Subscription } from 'rxjs';
```

確認檔案內所有 `event` 出現處都是 `(event: MessageEvent)` 型別的**本地參數名稱**，從未實際引用這個 import 進來的 `event` symbol，純屬死代碼。

### 計畫外的既有問題修正

執行 `npm run build` 驗證 Phase 1 改動時，發現 **production build 在改動前就已經失敗**（與本次任何改動無關），共 72 處 `TS2592: Cannot find name '$'`。已徵得使用者同意後一併修正：

**根因**：`tsconfig.app.json` 設定 `"types": []`，`tsconfig.spec.json` 只設定 `"types": ["jasmine"]`。這會讓 TypeScript 完全不載入 `@types/jquery` 的環境宣告——即使套件已安裝，全站約 40 個檔案直接呼叫全域 `$()`/`jQuery` 的既有程式碼仍會型別檢查失敗。

```jsonc
// tsconfig.app.json 修改前
"types": []
// 修改後
"types": ["jquery"]

// tsconfig.spec.json 修改前
"types": ["jasmine"]
// 修改後
"types": ["jasmine", "jquery"]
```

修正後接續驗證 `npm test`，又發現 `src/app/layout/ac05/ac0509/ac0509.component.spec.ts` 使用新版 Angular（v20）已移除的 `async` API（`@angular/core/testing` 已無此 export），會讓整個 Karma 測試無法啟動編譯。確認全站僅此 1 個檔案受影響，改為官方建議的等效寫法：

```ts
// 修改前
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
...
beforeEach(async(() => { ... }));

// 修改後
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
...
beforeEach(waitForAsync(() => { ... }));
```

**未處理的既有問題（如實記錄，不在本次範圍）**：`npm test` 排除上述兩個編譯期問題後，仍會在執行期遇到 `AppComponent` 測試缺少 `TranslateService` provider、`LayoutComponent` 循環依賴等錯誤。這是專案既有 `.claude/RULES/unit-test.md` 已記載的現況（「本專案目前沒有成熟的單元測試文化或基礎設施」），非本次任務範圍，未修正。

## 驗證方式

### 靜態驗證

- `npm run build`（production，`ng build --optimization=true`）：**成功**，無錯誤。
- 逐檔確認 `.ui-inputgroup`/`.ui-helper-clearfix` 移除前後使用範圍未受影響（grep 全站引用數量一致）。
- 已用 `netstat`/`curl` 對使用者本機既有的 `ng serve`（port 4701）dev server 發出請求，確認 HTTP 200 且回傳預期的 `<title>digiRunner v4</title>`，證明改動未造成應用程式無法啟動或編譯期崩潰。

### 尚待人工驗證

- 本環境無可用的瀏覽器自動化工具（無 `chromium-cli`），且 dev server 由使用者自行佔用中，**未能實際截圖確認畫面排版**。建議人工開啟 `npm start`，檢查：
  - 登入頁、任一 `ac0*` 列表頁排版是否正常（確認 `ui-inputgroup`/`.ui-helper-clearfix` 未受影響）。
  - `ac04/ac0432`（Site 啟用/停用、刪除的確認彈窗）Yes/No 按鈕排版是否維持左右各半（`row`/`col-6`）。

### 測試套件現況

- `npm test`：型別檢查與編譯期問題已排除（jQuery 型別、`async` API），但受限於既有測試基礎設施問題（見上），無法達到全綠燈，如實記錄現況、未強行讓斷言通過或刪除既有測試案例。

## 實作紀錄

### 時間軸

1. 使用者要求針對 Angular 13→20 升級後的技術債產出分析與分階段執行計畫，透過 Grep 統計全專案的 standalone 採用率、新版 control flow 語法、`inject()` 使用率、RxJS subscription 清理狀況、`any` 型別數量、舊版 PrimeNG grid class、build 工具鏈設定等具體數據，並用 Explore agent 調查 esbuild builder 遷移與 jQuery 使用範圍的風險。
2. 依「拒絕無關重構」「最小化改動」原則，將發現的問題整理為六個 Phase，其中 Phase 1（IE polyfill 清理、舊版 grid class 替換、死代碼移除）風險最低、全域受益，優先執行；jQuery 全面移除因範圍最廣、風險最高，列為最後一個 Phase 且先暫緩執行。
3. 執行 Phase 1 三項既定工作。
4. 執行 `npm run build` 驗證時，發現移除 `ui-g`/`ui-grid` CSS 前，17 個檔案中有 16 個其實是包在已停用 HTML 註解內的用法，僅 1 個是活躍用法，遂修正原計畫的執行方式（只改動活躍用法，註解內容不動），避免對非本人改動的歷史程式碼做不必要的變更。
5. `npm run build` 意外發現 72 處既有的 `TS2592` 型別錯誤（與本次改動無關），先用 `git stash` 排查確認不是自己造成的，再向使用者確認是否要一併修正，獲同意後修正 `tsconfig.app.json`/`tsconfig.spec.json` 的 `types` 設定。
6. 接續驗證 `npm test`，又發現 `ac0509.component.spec.ts` 使用已移除的 `async` API 導致整個測試無法編譯，確認全站僅此 1 檔受影響後直接修正為 `waitForAsync`。
7. `npm test` 排除編譯期問題後仍有既有測試基礎設施問題（`TranslateService` provider 缺失、`LayoutComponent` 循環依賴），對照專案 `.claude/RULES/unit-test.md` 現況說明後判斷不在本次範圍，如實記錄、未繼續深入修正。
8. 因應環境限制（無 `chromium-cli`、dev server 被使用者佔用），改用 `curl` 對現有 dev server 做 smoke test，確認應用程式可正常回應，並如實告知使用者尚需自行做視覺排版確認。
9. 驗證過程中反覆執行 `npm run build`，意外污染了儲存庫內被追蹤的建置產物目錄（`../src/main/resources/static/dgrv4/ac4/`，內含 hash 檔名的 build 輸出）。確認這是本次 session 內自己造成的副作用（非使用者既有未提交工作）後，用 `git checkout --` 與 `git clean -fd` 將該目錄還原為原本已提交的狀態，避免把無關的建置產物雜訊帶進本次 commit。

### 經驗紀錄（給未來類似任務參考）

- **grep 命中不代表「活躍使用」**：`ui-g`/`ui-g-6` 在 17 個檔案有字面命中，但實際逐一展開後發現絕大多數落在已停用的 HTML 註解區塊內。若只憑 grep 數字就「機械式替換全部 17 檔」，會變成無意義地修改一堆早已停用的歷史程式碼，違反「拒絕無關重構」原則；正確作法是逐一確認每個命中是否真的會渲染。
- **CSS 區塊物理相鄰 ≠ 邏輯上同屬同一組**：待刪除的舊版 grid CSS 與仍在使用的 `.ui-inputgroup`/`.ui-helper-clearfix` 在檔案裡緊鄰在一起（甚至同一個註解標題底下），若不逐一 grep 確認每個 class 的實際使用範圍，很容易「順手」把還在用的 class 一併刪掉。
- **驗證步驟本身可能揭露計畫外的既有問題**：跑 `npm run build`/`npm test` 做「改動後驗證」時，發現的錯誤不一定是自己造成的。遇到這種情況，應先用 `git stash` 等方式排查因果關係，確認是否為既有問題，再決定是否要處理、以及是否需要先跟使用者確認範圍，而不是預設either「不關我事，不用管」或「直接動手修」。
- **驗證動作本身可能產生需要清理的副作用**：本專案的建置產物目錄被追蹤進 git，反覆執行 `npm run build` 做驗證會弄髒這個目錄。在準備 commit 前，務必重新檢查 `git status`，把純粹因為「執行驗證指令」而產生、與本次實際程式碼改動無關的檔案變動排除或還原，避免污染 commit 內容。
