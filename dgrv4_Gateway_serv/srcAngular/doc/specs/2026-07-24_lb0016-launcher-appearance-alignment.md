# lb0016 SMART Launcher 外觀與搜尋按鈕一致化

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 使用專案現有外觀風格調整 `lb0016`。
2. 確認 `Launch App`、`啟動範例應用程式`、`儲存 SMART 設定`三個按鈕的文字及大小，並讓「啟動範例應用程式」在下一行獨立呈現。
3. 病人與醫療人員輸入框旁的搜尋按鈕，參考 `ac0002` 角色名稱欄位的作法。
4. 病人與醫療人員選取視窗內的搜尋按鈕，也比照 `ac0002` 的作法。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0016/lb0016.component.html` | 套用 `app-container`、調整頁面面板與操作按鈕結構、統一病人及醫療人員搜尋按鈕 |
| `src/app/layout/labs/lb0016/lb0016.component.scss` | 將原 SMART Launcher 藍色皮膚收斂為 DGR 品牌面板與按鈕外觀，補上操作按鈕尺寸與響應式規則 |
| `../src/main/resources/static/dgrv4/ac4/` | `npm run build` 產生的 `index.html`、新 hash bundles 及被取代的舊 bundles |
| `doc/specs/2026-07-24_lb0016-launcher-appearance-alignment.md` | 本次工作日誌 |

### 詳細程式碼異動

#### 1. 頁面容器與面板外觀

修改前由元件自行顯示標題，並保留原 SMART Launcher 的 Bootstrap 3 藍色視覺：

```html
<div class="smart-launcher">
  <h2 class="text-primary launcher-title">
    {{ 'smart_launcher.title' | translate }}
  </h2>
```

修改後改用專案共用容器，Tab 與 Launch 區塊套用 DGR 面板邊界、灰底與品牌色：

```html
<app-container [title]="'smart_launcher.title' | translate">
  <div class="smart-launcher">
    <div class="launcher-panel">
```

```scss
.launcher-panel,
.launch-box {
  border: 1px solid #e3e3e3;
  border-radius: 6px;
  background: #fff;
}

.nav-tabs {
  border-left: 3px solid var(--tpi-primary);
  background: #f6f6f6;
}
```

#### 2. 主要操作按鈕與範例應用程式版面

修改前 Launch 與範例應用程式按鈕同列，範例按鈕位於較窄欄位：

```html
<div class="launch-box-row">
  <div class="launch-box-input">...</div>
  <div class="launch-box-sample">
    <button pButton [outlined]="true">...</button>
  </div>
</div>
```

修改後三個指定操作按鈕沿用 `tpi-btn` 品牌樣式及共用尺寸；範例應用程式按鈕移至提示文字下方的獨立一行：

```html
<button type="button"
  class="btn tpi-btn tpi-primary smart-action-button">
  {{ 'smart_launcher.cfg_save' | translate }}
</button>

<div class="mt-3">
  <button type="button"
    class="btn tpi-btn tpi-second smart-action-button">
    {{ 'smart_launcher.launch_sample_app' | translate }}
  </button>
</div>
```

```scss
.smart-action-button {
  min-width: 10rem;
  white-space: nowrap;
}
```

繁中既有 i18n 文案經查證為 `Launch App`、`啟動範例應用程式`、`儲存 SMART 設定`，未修改語系檔。

#### 3. 病人與醫療人員輸入欄位搜尋按鈕

修改前使用相連的 PrimeNG input group 與預設按鈕：

```html
<div class="p-inputgroup">
  <input pInputText ... />
  <button pButton icon="fa fa-search"></button>
</div>
```

修改後比照 `ac0002` 角色名稱欄位，重用既有 `.ui-inputgroup`、`.form-control`、`.tpi-night` 與 `.input-btn`：

```html
<div class="ui-inputgroup">
  <input type="text" class="form-control" ... />
  <button pButton type="button" icon="pi pi-search"
    class="ms-1 tpi-night input-btn"></button>
</div>
```

#### 4. 選取視窗內搜尋按鈕

修改前以文字顯示搜尋／搜尋中：

```html
<button pButton type="button" [disabled]="patientSearching">
  {{ patientSearching ? ('smart_launcher.searching' | translate)
    : ('smart_launcher.search' | translate) }}
</button>
```

修改後改用相同的圖示按鈕，保留 loading 期間 disabled 行為，並以 tooltip 及 `aria-label` 保留「搜尋」語意：

```html
<button pButton type="button" icon="pi pi-search"
  class="ms-1 tpi-night input-btn"
  [pTooltip]="'smart_launcher.search' | translate"
  [attr.aria-label]="'smart_launcher.search' | translate"
  [disabled]="patientSearching"></button>
```

## 預計修改方式

### 需求 1：使用現有外觀風格調整 lb0016

**Context** — `lb0016` 原先保留 SMART Launcher 的 Bootstrap 3 相容皮膚，頁面視覺與其他 DGR Labs 頁面不同。

**分析** — 讀取 `lb0016.component.html`、`lb0016.component.scss`，並比對 `lb0014`、`lb0018` 與全域 `_button.scss`；確認專案使用 `app-container`、`var(--tpi-primary)`、灰底面板及 `tpi-*` 按鈕。

**修正方案** — 保留原表單與 SMART on FHIR 行為，只調整容器、Tab、面板、文字色及主要按鈕，避免觸及 API 或資料處理。

### 需求 2：確認主要按鈕文字、大小及換行

**Context** — 使用者指定三個按鈕，並要求「啟動範例應用程式」在下一行獨立呈現。

**分析** — 查閱四份語系檔中的 `launch_btn`、`launch_sample_app`、`cfg_save`，以及 `_button.scss` 的 `.tpi-btn`；確認繁中文案已存在，基礎字級為 `0.8rem`、字重為 `600`。範例按鈕原本位於窄欄位，容易產生寬度不一致。

**修正方案** — 不變更 i18n，新增 `smart-action-button` 統一最小寬度與不換行，並以 Bootstrap `mt-3` 將範例按鈕移到下一行。

### 需求 3：輸入欄位按鈕比照 ac0002

**Context** — 使用者明確指定參考 `ac0002` 角色名稱欄位。

**分析** — 讀取 `ac0002.component.html`，確認角色名稱使用 `.ui-inputgroup`、`.form-control` 與 `pButton icon="pi pi-search" class="ms-1 tpi-night input-btn"`；再確認這些 class 已定義於全域 `styles.scss`。

**修正方案** — 原樣套用既有結構與 class 到病人、醫療人員欄位，保留原 tooltip、ngModel 與 click handler。

### 需求 4：視窗內搜尋按鈕比照辦理

**Context** — 使用者追加要求病人與醫療人員選取視窗內的搜尋按鈕也採相同外觀。

**分析** — 兩個按鈕原本顯示動態文字，並以 `patientSearching`／`providerSearching` 控制 disabled。

**修正方案** — 改為 `pi pi-search` 圖示與既有 `tpi-night input-btn`，保留 disabled，並補上 i18n tooltip 與 `aria-label`，避免移除文字後失去可理解名稱。

## 驗證方式

### 靜態驗證

- 每次修改後執行 `git diff --check`，結果皆為 exit code 0。
- 多次執行 `npm run build`（實際命令為 `ng build --optimization=true`），最後一次於 2026-07-24 成功完成，exit code 0。
- 第一次 build 嘗試因執行工具逾時而收到 exit code 124；隨後提高 timeout 重新執行即成功，並非 Angular 編譯錯誤。
- build 輸出包含 `labs-lb0016-lb0016-module` lazy chunk，無 template 或 SCSS 編譯錯誤。

### 尚待人工驗證（建議）

- 開啟 `lb0016`，確認頁面標題、Tab、主要面板與按鈕顏色符合其他 DGR 頁面。
- 切換繁中與英文，確認三個主要按鈕文字不換行且寬度足夠。
- 確認「啟動範例應用程式」位於 App Launch URL 提示文字下方的獨立一行。
- 點擊病人、醫療人員欄位旁的搜尋按鈕，確認選取視窗正常開啟。
- 在兩個選取視窗內以 Enter 與搜尋圖示執行查詢，確認 loading 期間按鈕 disabled，結果與分頁功能不受影響。

## 實作紀錄

### 時間軸

1. 使用者要求以現有風格調整 `lb0016`。
2. 讀取 Angular、PrimeNG 與樣式規範，檢查 `lb0016` 及鄰近 Labs 頁面，確認現況仍帶有原 SMART Launcher 的 Bootstrap 3 藍色皮膚。
3. 第一次嘗試以較大範圍 patch 同時修改 HTML 與 SCSS，因其中一段 context 無法匹配而失敗；改為拆分小型 patch 後成功套用。
4. 加入 `app-container`、品牌面板、Tab 與主要按鈕樣式，未修改 TypeScript 或 API。
5. 第一次 build 因工具 timeout 回傳 exit code 124；重新以較長 timeout 執行後成功。
6. 使用者要求確認三個主要按鈕文字與大小，並將範例應用程式按鈕移到下一行。
7. 查證 i18n 與 `_button.scss`，新增共用按鈕尺寸，調整範例按鈕版面；build 成功。
8. 使用者要求病人及醫療人員輸入框按鈕參考 `ac0002` 角色名稱。
9. 讀取 `ac0002.component.html` 與全域 class 定義，套用 `.ui-inputgroup`、`.form-control`、`tpi-night input-btn`；build 成功。
10. 使用者追加要求選取視窗內搜尋按鈕也比照辦理。
11. 兩個視窗內搜尋按鈕改為相同圖示樣式，保留 disabled 並補上 tooltip／`aria-label`；build 成功。
12. 使用者要求建立工作日誌並上傳此次修改。
13. 建立本文件時，來源碼與工作日誌尚未 commit／push；將於本文件完成後依同一使用者指示執行提交與上傳。
14. 原先依「只上傳修改內容」解讀，準備排除驗證 build 產物；清理動作在執行前被中止，未變更 static 目錄。
15. 使用者進一步明確指定 build 後檔案也要上傳，且 commit message 必須加入「更新前端封裝」；因此改為將完整 static build 差異納入同一提交。

### 經驗紀錄（給未來類似任務參考）

- 當使用者指定參考既有功能時，應直接查閱該功能的 template 與全域 class 定義；本次 `ac0002` 已提供完整可重用的輸入框按鈕組合，不需要新增元件專屬樣式。
- 長文 template 的 patch 應使用具辨識度的上下文並拆成小區塊，避免泛用的 `</div>` context 導致匹配失敗或難以審查。
- 圖示按鈕取代文字按鈕時，除了 tooltip，也應保留 `aria-label`，使按鈕仍有可理解的名稱。
- `npm run build` 的命令逾時與 Angular 編譯失敗不同；應查看 exit code 與輸出，再以合理 timeout 重試。
- 驗證 build 會改動後端 static 交付目錄；是否提交須依使用者語意判斷。本次使用者後續明確指定 build 檔案也要上傳，因此需納入 `index.html`、新 hash bundles 與被取代的舊 bundles。
