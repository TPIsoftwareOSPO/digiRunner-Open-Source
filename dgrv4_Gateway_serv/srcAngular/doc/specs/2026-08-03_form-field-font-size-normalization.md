# 統一系統表單附加文字字級

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 檢查 `ac0702` 建立／更新畫面的字級不一致，特別是 `Threshold`、`Duration`、`Alert Interval` 輸入框後方文字偏大，先調查原因並列出修正方式。
2. 依調查結果實作 `ac0702` 修正。
3. 掃描系統中是否還有相同的表單文字未對應字級、因而顯示較大的情況。
4. 修正掃描確認的同型問題，但保留可能刻意放大的月份導覽標題。
5. 保留 production build 產物，之後與原始碼一併上傳。
6. 產生本工作日誌，並提交、推送本次修改。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/styles/custom/_custom-bootstrap.scss` | 新增全域 `.form-field-affix`，統一表單單位、前綴、分隔符與補充文字為 `0.8rem` |
| `src/app/layout/ac07/ac0702/ac0702.component.html` | 統一建立／更新數值單位、欄位分隔符、時間分隔符及例外日期說明字級；數值列改用 Bootstrap 5 flex |
| `src/app/layout/ac07/ac0706/ac0706.component.html` | 比照 `ac0702` 統一建立／更新數值單位、分隔符及說明文字字級；數值列改用 Bootstrap 5 flex |
| `src/app/layout/components/timepicker/timepicker.component.html` | 共用時分輸入框的全形冒號套用表單附加文字字級 |
| `src/app/layout/ac02/ac0202/ac0202.component.html` | 服務時間區間分隔符套用表單附加文字字級 |
| `src/app/layout/np02/np0201/np0201.component.html` | 日期區間分隔符套用表單附加文字字級 |
| `src/app/layout/np02/np0203/np0203.component.html` | 日期區間分隔符套用表單附加文字字級 |
| `src/app/layout/ac04/ac0432/site-detail/site-detail.component.html` | `nev.` 前綴套用統一字級，並將已失效的 `.form-inline` 改為 Bootstrap 5 flex |
| `src/app/layout/labs/tsmpdpfile/tsmpdpfile.component.html` | 三處檔案大小文字套用表單附加文字字級 |
| `src/main/resources/static/dgrv4/ac4/` | 保留本次 `npm run build` 產生的 `index.html`、新 hash bundles，以及被取代舊 bundles 的刪除 |

### 詳細程式碼異動

#### 1. 建立可跨頁重用的表單附加文字 class

修改前只有表單控制項與表單 label 被設定為 `0.8rem`：

```scss
.form-control,
.form-group label,.col-form-label {
  font-size: 0.8rem;
}
```

修改後新增語意化共用 class：

```scss
.form-field-affix {
  font-size: 0.8rem;
}
```

#### 2. 統一數值欄位單位與排列

修改前：

```html
<div class="form-inline">
  <input formControlName="duration" type="number" class="form-control w-75">
  <span class="ms-1">{{'sec'|translate}}</span>
</div>
```

修改後：

```html
<div class="d-flex align-items-center">
  <input formControlName="duration" type="number" class="form-control w-75">
  <span class="ms-1 form-field-affix">{{'sec'|translate}}</span>
</div>
```

同樣做法套用於 `times`、`sec`、`milli_sec`，涵蓋 `ac0702` 與 `ac0706` 的建立／更新畫面。

#### 3. 統一前綴、分隔符及補充文字

修改前：

```html
<span class="px-1">～</span>
<span class="me-1">nev.</span>
<span *ngIf="_fileData">{{ _fileData.size | fileSize }}</span>
```

修改後：

```html
<span class="px-1 form-field-affix">～</span>
<span class="me-1 form-field-affix">nev.</span>
<span *ngIf="_fileData" class="form-field-affix">{{ _fileData.size | fileSize }}</span>
```

#### 4. 修正共用 Timepicker 冒號

修改前：

```html
<div>：</div>
```

修改後：

```html
<div class="form-field-affix">：</div>
```

此修改會涵蓋 `ac0202`、`ac0702`、`ac0706` 中共 12 個 `app-timepicker` 使用位置。

## 預計修改方式

### 需求 1：調查 `ac0702` 字級不一致

**Context** — 使用者指出 `Threshold`、`Duration`、`Alert Interval` 輸入框後方的單位文字偏大。

**分析** — 實際讀取 `ac0702.component.html`、元件 CSS、`styles.scss` 與 `_custom-bootstrap.scss`。確認 `body` 字級為 `1rem`，而 `.form-control` 與 `.form-group label` 為 `0.8rem`；單位使用沒有字級 class 的 `<span>`，因此繼承 `1rem`。另確認專案使用 Bootstrap 5.3，但更新畫面仍有 Bootstrap 5 已移除的 `.form-inline`。

**修正方案** — 先以頁面 class 修正 `ac0702`，並用 `d-flex align-items-center` 統一排列；後續全系統掃描確認問題跨頁後，再提升為全域 `.form-field-affix`。

### 需求 2：掃描並修正全系統同型問題

**Context** — 使用者要求確認系統內是否還有未套表單字級而偏大的文字，並同意修正。

**分析** — 使用 `rg` 掃描約 545 個有內容的 `<span>`，並另外檢查輸入框旁的 `<div>`、`-`、`～`、冒號、前綴和檔案大小。確認 `ac0706` 有相同的 8 個數值單位問題；共用 `timepicker` 冒號影響 12 個使用位置；其餘明確問題分布於 `ac0702`、`ac0202`、`np0201`、`np0203`、`site-detail` 與 `tsmpdpfile`。`ac0702`／`ac0706` 詳細畫面的單位已在 `.form-control` 內，`np0205` 憑證文字在 `.p-component` 內，API test 回應時間的父層已有 `0.8rem`，因此排除。

**修正方案** — 建立全域 `.form-field-affix` 並只套用已確認位置，共 38 個文字節點。月份導覽標題可能是刻意的視覺層級，依調查結論保留原樣。

### 需求 3：保留並上傳 Build 產物

**Context** — 初次 build 後原依「僅驗證」規則準備清理 static 產物，使用者隨後明確要求保留，之後一併上傳。

**分析** — build 前 `src/main/resources/static/dgrv4/ac4/` 為乾淨狀態，因此 build 後的 tracked 刪除、`index.html` 更新及新 hash bundles 都可明確歸因本次修改。清理 tracked 產物的指令被使用者中止；後續重新檢查確認 build 產物仍完整，`index.html` 引用新 `styles` 與 `runtime`，9 個新 hash bundles 均存在。

**修正方案** — 不再清理 static 目錄，將新檔、舊檔刪除與 `index.html` 一併納入同次提交及推送。

## 驗證方式

### 靜態驗證

- `git diff --check`：exit code 0。
- 第一次修正 `ac0702` 後執行 `npm run build`：前兩次因工具逾時設定過短而以 exit code 124 中止；調整逾時後成功完成，exit code 0。
- 全系統修正後執行 `npm run build`：成功，exit code 0；build hash 為 `ffa5601db3cc71ae`。
- 檢查 build 產物：`index.html` 正確引用 `styles.2abd3c72649cea8c.css` 與 `runtime.8b5b098ffdcfe9cf.js`；本次新增的 9 個 hash bundles 均存在。
- 檢查 `.form-field-affix`：已套用於 38 個實際文字位置。

### 尚待人工驗證（建議）

- `ac0702`、`ac0706`：分別開啟建立與更新畫面，確認 `Threshold`、`Duration`、`Alert Interval`、`Response Time` 的單位字級與輸入框一致。
- `ac0702`、`ac0706`：切換 `keyword`／`responseTime` 類型，確認 `-` 分隔符大小及欄位排列。
- `ac0202`、`np0201`、`np0203`：確認時間／日期區間的 `～` 字級一致，月份導覽標題維持原設計。
- `ac0432`：確認 `nev.` 與 `siteCode` 輸入框垂直對齊。
- `tsmpdpfile`：選擇檔案後確認檔案大小文字字級。
- 所有含 `app-timepicker` 的頁面：確認全形冒號字級及垂直位置。

## 實作紀錄

### 時間軸

1. 使用者提出 `ac0702` 建立／更新畫面字級不一致，要求先調查原因與修正方法。
2. 讀取 Angular／樣式規範及相關程式碼，確認根因是 `body: 1rem` 與表單控制項 `0.8rem` 的 CSS 繼承差異，並發現 `.form-inline` 已不適用 Bootstrap 5。
3. 使用者同意實作；在 `ac0702` 建立頁面專用 `.form-field-unit`，修正 8 個單位並統一 flex 排列。
4. 執行 build；前兩次工具逾時設定過短而中止，第三次成功。依當時「只驗證」的規則清理 build 產物。
5. 使用者要求檢查全系統同型問題；使用 `rg` 掃描表單附加文字並逐一讀取父層與元件 CSS，區分確定問題與刻意設計。
6. 確認 `ac0706`、共用 `timepicker`、日期／時間分隔符、`nev.` 前綴、檔案大小與例外日期說明存在相同字級落差；排除父層已提供 `0.8rem` 的候選。
7. 使用者同意修正；將頁面專用 class 提升為全域 `.form-field-affix`，套用 38 個位置，並移除 `ac0702` 的重複 scoped CSS。
8. 執行全系統 production build，成功產出 hash `ffa5601db3cc71ae`。
9. 原準備依驗證規範清理 build 產物，使用者中止還原指令並明確改為保留 build 產物、之後一併上傳。
10. 重新檢查 static 目錄，確認中止的還原沒有破壞產物；`index.html` 與 9 個新 hash bundles 完整。
11. 使用者要求產生工作日誌並上傳。本文件產出當下尚未 commit／push，將由本次後續流程暫存、提交並推送。

### 經驗紀錄（給未來類似任務參考）

- 表單控制項統一為 `0.8rem` 時，同行的單位、前綴與分隔文字不會自動命中 `.form-control`；應使用語意化共用 class，避免每頁重複宣告。
- 掃描字級不能只搜尋 `<span>`；共用 `timepicker` 的冒號使用 `<div>`，同樣會繼承 `body` 的 `1rem`。
- 不宜使用 `.form-group span` 這類過寬 selector，否則會影響驗證訊息、chips 與其他刻意使用一般字級的內容。
- Bootstrap 5 已移除 `.form-inline`；維護相關欄位時應使用 `d-flex`、`align-items-center` 等 utility classes。
- Build 產物是否清理取決於交付需求。使用者要求 build 後上傳時，必須保留新 hash bundles、被取代舊 bundles 的刪除及 `index.html` 更新，三者缺一不可。
- 執行清理前先記錄 static 目錄基線；使用者在流程中改變交付要求時，應立即停止清理並重新驗證產物完整性。
