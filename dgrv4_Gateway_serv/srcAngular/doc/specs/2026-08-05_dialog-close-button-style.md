# Dialog 關閉按鈕外觀統一

## 1. 修改內容

### 問題描述

PrimeNG 的 `p-confirmDialog` 與透過 `DialogService.open()` 開啟的 Dynamic Dialog，其 header 關閉按鈕外觀未與深色 header 一致。需求為讓按鈕背景及外框維持 header 色彩，hover 時不改變背景，只改變 close icon 顏色；AC0002 角色清單、AC1002 建立視窗及其他相同開窗模式皆需套用一致風格。

### 修改檔案清單

- `src/styles/custom/_custom-prime.scss`
- `doc/specs/2026-08-05_dialog-close-button-style.md`
- `../src/main/resources/static/dgrv4/ac4/`（完成 build 後更新封裝內容）

### 詳細程式碼異動

修改前僅有一般 Dialog 關閉按鈕的 hover 規則，未涵蓋 `p-confirmDialog` header actions 內的 `p-button`，也未統一 Dynamic Dialog：

```scss
.p-dialog-close-button:hover {
  background: #666;
}
```

修改後以 PrimeNG 實際 DOM 結構為準，鎖定 header actions 最後一個 `p-button`（close 按鈕）及其內層 `button`：

```scss
.p-dialog.p-confirmdialog .p-dialog-header-actions p-button:last-child,
.p-dialog.p-confirmdialog .p-dialog-header-actions p-button:last-child > button,
p-dynamicdialog .p-dialog .p-dialog-header-actions p-button:last-child,
p-dynamicdialog .p-dialog .p-dialog-header-actions p-button:last-child > button {
  background: #434343;
  border-color: #434343;
}

.p-dialog.p-confirmdialog .p-dialog-header-actions p-button:last-child:hover,
.p-dialog.p-confirmdialog .p-dialog-header-actions p-button:last-child:hover > button,
p-dynamicdialog .p-dialog .p-dialog-header-actions p-button:last-child:hover,
p-dynamicdialog .p-dialog .p-dialog-header-actions p-button:last-child:hover > button {
  background: #434343;
  border-color: #434343;
  color: var(--tpi-primary);
}
```

使用 `:last-child` 是因 PrimeNG header actions 可能同時包含 maximize 與 close，且 close 位於最後，藉此避免影響 maximize 按鈕。

## 2. 預計修改方式

### Context

- `p-confirmDialog` 的 close control 位於 `.p-dialog-header-actions` 內，外層為 `p-button`，實際可點擊元素為其內層 `button`。
- `DialogService.open()` 產生的視窗共同由 `p-dynamicdialog` host 包覆，因此可由共用樣式一次涵蓋，不需逐頁設定 `styleClass`。
- 專案共搜尋到 27 個使用 `DialogService.open()` 的 TypeScript 檔案。

### 分析

最初只鎖定 `.p-dialog-close-button` 時未命中 `p-confirmDialog` 的實際外層；後續依安裝中的 PrimeNG v20 template 確認結構，改為同時設定 `p-button` 與內層 `button`。逐一在 AC0002、AC1002 傳入 `styleClass` 雖可生效，但無法自然涵蓋其他同模式視窗，因此改採 Dynamic Dialog 共用 host selector。

### 修正方案

1. 對 `p-confirmDialog` 與 Dynamic Dialog 的 close 按鈕套用 `#434343` 背景與外框。
2. hover 時維持相同背景與外框，只將 icon 繼承色改為 `var(--tpi-primary)`。
3. 不修改各功能頁開窗參數，將影響集中於共用 PrimeNG 樣式。
4. 執行 `npm run build`；成功後保留並上傳 `ac4` 封裝產物。

## 3. 驗證方式

### 靜態驗證

- `git diff --check`：通過，未發現 whitespace error。
- PrimeNG v20 template 結構：已確認 confirm dialog 與 dynamic dialog 的 header actions、close 按鈕順序及 host selector。
- `DialogService.open()` 使用範圍：已確認共有 27 個檔案，均由共用 Dynamic Dialog selector 涵蓋。
- `npm run build`：成功；build hash 為 `f85048db2b7bb75a`，耗時約 39 秒。
- 封裝內容：`index.html` 已改指向 `styles.4dc0cf272567cfb1.css`，舊的 `styles.2abd3c72649cea8c.css` 已由新 hashed CSS 取代。
- 編譯後 CSS：已確認包含 `p-confirmDialog` 與 `p-dynamicdialog` 的 close 按鈕背景、外框及 hover icon 色彩規則。
- 封裝上傳：產品樣式與 `ac4` 封裝已由 commit `052ee1887` 推送至 `eapdd2025/develop`。

### 尚待人工驗證

- 開啟任一 `p-confirmDialog`，確認 close 按鈕背景與外框皆為 `#434343`。
- hover close 按鈕，確認背景不變，僅 close icon 變為主色。
- 分別開啟 AC0002 角色名稱搜尋的角色清單，以及 AC1002 建立視窗，確認外觀一致。
- 抽查另一個 `DialogService.open()` 視窗；若具 maximize 按鈕，確認 maximize 外觀未被 close 規則影響。

## 4. 實作紀錄

### 時間軸

1. 盤點專案 Dialog 實作，辨識 `p-confirmDialog`、`DialogService.open()` Dynamic Dialog 與舊式 Dialog 模式。
2. 初版使用 `.p-dialog-close-button`，因未命中 header actions 內的 `p-button` 而修正 selector。
3. 將 close 按鈕背景與外框調整為 `#434343`。
4. 初版 hover 曾調整背景色；依回饋改為背景與外框不變，只調整 icon 色彩。
5. AC0002、AC1002 曾採逐視窗 `styleClass`；盤點 27 個 `DialogService.open()` 使用處後，改成 `p-dynamicdialog` 共用 selector，並移除頁面個別設定。
6. 執行 `npm run build` 成功，產生 build hash `f85048db2b7bb75a`，並確認新規則已編譯進 hashed CSS。
7. 建立產品與封裝 commit `052ee1887`（`style(shared): 統一 Dialog 關閉按鈕外觀`）。
8. 將 commit `052ee1887` 成功推送至 `eapdd2025/develop`；工作日誌另以文件 commit 補充並一併上傳。

### 經驗紀錄

- PrimeNG 元件樣式需依目前安裝版本的 template 與 DOM 層級確認，不宜只依 class 名稱推測。
- Dynamic Dialog 的共同 host 適合承載跨頁一致樣式，可避免在多個 `DialogService.open()` 呼叫重複傳入設定。
- header actions 可能有 maximize 與 close，多按鈕情境下需精確鎖定 close，避免共用規則誤傷其他操作。
