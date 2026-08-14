# 共用組織圖連接線辨識度優化

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 優化 `p-organization-chart` 外觀，讓 node 之間的連接線更明顯，例如增加線條 px。
2. 實作樣式調整。
3. node 外觀不需變動，只調整連接線。
4. 將連接線色系調淡，改用灰階。
5. 建立本次修改的工作日誌，執行 `npm run build` 成功後上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/shared/organization/organization.component.css` | 將組織圖垂直與水平連接線加粗為 `2px`，並統一使用淡灰色 `#b8b8b8` |
| `doc/specs/2026-07-16_shared-organization-chart-connectors.md` | 記錄本次需求、調查、實作、驗證與上傳流程 |
| `../src/main/resources/static/dgrv4/ac4/` | `npm run build` 成功後更新 Angular 部署產物 |

### 詳細程式碼異動

#### 1. 加粗垂直連接線

修改前由 PrimeNG v20 預設樣式提供 `1px` 寬度與主題色：

```css
.p-organizationchart-connector-down {
  width: 1px;
  background: dt('organizationchart.connector.color');
}
```

修改後：

```css
.p-organizationchart-connector-down {
  width: 2px;
  background: #b8b8b8;
}
```

#### 2. 加粗水平與左右連接線

修改前由 PrimeNG v20 預設樣式使用 `1px` 邊框與主題色；修改後覆寫實際使用的 connector class：

```css
.p-organizationchart-connector-top {
  border-block-start-width: 2px;
  border-block-start-color: #b8b8b8;
}

.p-organizationchart-connector-left {
  border-inline-end-width: 2px;
  border-inline-end-color: #b8b8b8;
}

.p-organizationchart-connectors
  :nth-child(1 of .p-organizationchart-connector-right) {
  border-inline-start-width: 2px;
  border-inline-start-color: #b8b8b8;
}
```

#### 3. 維持 node 原有外觀

曾在第一版實作中為 `.p-organizationchart-node` 加入 `2px` 外框；依使用者後續回饋已移除，最終版本未變更 node 的 border、背景色或選取樣式。

## 預計修改方式

### 需求 1：提高 node 連接線辨識度

**Context** — 使用者希望 `p-organization-chart` 的 node 與 node 之間線條更明顯，並提出增加 px 作為示例。

**分析** — 搜尋 repository 後確認共用組織圖位於 `src/app/shared/organization/`，專案使用 PrimeNG `^20.4.0`。讀取已安裝套件的 `primeng-organizationchart.mjs` 與 `@primeuix/styles/dist/organizationchart/index.mjs`，確認 v20 實際 connector class 為 `.p-organizationchart-connector-down`、`.p-organizationchart-connector-top`、`.p-organizationchart-connector-left` 與 `.p-organizationchart-connector-right`，預設寬度為 `1px`。

**修正方案** — 在現有 `OrganizationComponent` 的 CSS 中精準覆寫 connector selector，將線寬改為 `2px`，避免修改 template、資料結構或元件邏輯。

### 需求 2：node 外觀保持不變

**Context** — 第一版同時將 node 外框加粗為 `2px`，使用者隨後指定 node 外觀不用變動。

**分析** — node 外框調整不是提高「node 之間連接線」辨識度的必要變更，且超出使用者最後確認的範圍。

**修正方案** — 移除新增的 node `border-width` 與 `border-color`，保留既有 padding、hover 與 selected 樣式。

### 需求 3：連接線改為淡灰階

**Context** — 使用者確認線寬效果後，希望線條色系再淡一點，例如灰階。

**分析** — 原先使用 `var(--text-color)`，其值為較深的文字色；不符合「調淡」的視覺方向。

**修正方案** — 將四組 connector selector 統一改為中性淡灰色 `#b8b8b8`，並維持已確認的 `2px` 線寬。

### 需求 4：build 成功後上傳

**Context** — 使用者明確要求執行 `npm run build` 成功後上傳。

**分析** — 專案 `angular.json` 的 `outputPath` 指向 tracked 部署目錄 `../src/main/resources/static/dgrv4/ac4/`；專案規範要求此情境將更新的 `index.html`、新 hash bundles 與被取代的舊 bundles 納入同次提交與推送。

**修正方案** — 先確認 static 目錄在 build 前沒有既存差異，再執行 build；成功後檢查變更範圍，只暫存本次 CSS、工作日誌與 build 產物，提交至目前的 `develop` 分支並推送到既有 remote `eapdd2025`。

## 驗證方式

### 靜態驗證

- `git diff --check -- src/app/shared/organization/organization.component.css`：exit code 0。
- 第一次在 sandbox 內執行 `npm run build`：因 Node.js 無法 `lstat C:\Users\atirl` 而回傳 `EPERM`，exit code 1；這是執行環境權限問題，並非 Angular 編譯錯誤。
- 第一次嘗試在 sandbox 外重新執行 build：權限核准流程被使用者中止，沒有取得成功結果。
- 本次交付執行 `npm run build`：exit code 0，耗時約 39 秒；Angular build hash 為 `64cdca2ae2c2893a`，browser bundles、assets 與 `index.html` 均成功產生。

### 尚待人工驗證（建議）

- 開啟任何使用 `app-organization` 的頁面，確認各階層的水平、垂直連接線均為 `2px` 淡灰色。
- 確認 node 的一般、hover 與 selected 外觀與修改前一致。
- 在深色或自訂主題下確認 `#b8b8b8` 仍有足夠對比度。

## 實作紀錄

### 時間軸

1. 使用者提出優化 `p-organization-chart` 外觀、加粗 node 間連接線的需求。
2. 讀取專案 Angular 與樣式規範；搜尋後定位到共用 `OrganizationComponent`，並確認現有 node hover、selected 樣式。
3. 查閱本機安裝的 PrimeNG v20 程式與樣式，確認 connector class 和預設 `1px` 實作。
4. 先提供 `2px` 連接線與 node 外框的建議，當時未修改檔案。
5. 使用者要求實作後，第一版同時加入 `2px` node 外框及 `2px` 連接線。
6. 執行 sandbox 內 build 時遇到 `EPERM: operation not permitted, lstat 'C:\Users\atirl'`；改為申請 sandbox 外執行，但核准流程被使用者中止。
7. 使用者說明 node 外觀不用變動；移除 node 外框調整，只保留連接線覆寫。
8. 使用者要求線條色系調淡；將 `var(--text-color)` 改為淡灰色 `#b8b8b8`。
9. 使用者確認效果並要求建立工作日誌、build 成功後上傳；確認目前分支為 `develop`、remote 為 `eapdd2025`，且 build 前 static 目錄沒有既存差異。
10. 建立本工作日誌草稿。
11. 在 sandbox 外執行 `npm run build` 成功，exit code 0，build hash 為 `64cdca2ae2c2893a`。
12. 檢查 build 產物，確認更新 `index.html`、4 個新 hash bundles，並刪除其對應的 4 個舊 hash bundles；準備與 CSS、工作日誌一併提交並推送至 `eapdd2025/develop`。

### 經驗紀錄（給未來類似任務參考）

- PrimeNG v20 的 OrganizationChart connector class 與舊版 `.ui-tree` selector 不同，應依實際安裝版本查驗，不應沿用舊版 selector。
- 當需求只針對連接線辨識度時，不應同步調整 node 外框；視覺調整也要維持最小修改範圍。
- 線條粗細與顏色是兩個獨立視覺變因。本次最後採用 `2px` 提升辨識度，再用 `#b8b8b8` 降低視覺重量。
- build 產物位於 tracked 後端 static 目錄；只有使用者明確要求 build 且上傳時，才應連同新舊 hash bundles 一起提交。
