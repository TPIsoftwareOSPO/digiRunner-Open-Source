# lb0018 Fhir Viewer：版面優化，將 FHIR Base URL 區與分頁共用區改為明確的面板容器

同日另有 [`2026-07-17_lb0018-refresh-clears-and-reloads-dashboard.md`](./2026-07-17_lb0018-refresh-clears-and-reloads-dashboard.md)（刷新後同步清空／重載 Dashboard），該案處理行為邏輯，本次僅處理版面外觀，兩者無程式碼重疊。

## 修改內容

### 問題描述

使用者提出「優化 lb0018 版面，使其更美觀」，並給出兩個方向：

1. 「Fhir base URL or well know」及「分頁共用區」的區塊要更明顯。
2. 分頁共用區被包在 tab 內的區塊也要更明確。

後續追加需求：

3. `lb-section__head` 的文字請改粗體。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.html` | 兩個區塊各自包進 `.lb-section` 面板（標頭 + 內文）；移除區塊間的 `<hr />`；tab 列移入 `.lb-section__tabs`；請求／回應卡片加上 `.lb-subcard`；清掉 Tab 2 標題內撐高版面的空行 |
| `src/app/layout/labs/lb0018/lb0018.component.css` | 新增 `.lb-section` 系列與 `.lb-subcard`；調整 `.nav-tabs` / `.tab-content` 使 tab 與內容連成同一面板 |

本次未新增 i18n key、未變動任何 TS 邏輯。

### 詳細程式碼異動

#### 1. 新增區塊面板容器（CSS）

原本兩個區塊只有一個裸的 `<label class="area-title">` 當標題，沒有任何容器；而 tab 內部大量使用 `card`，導致外層區塊比內層元素還不明顯。新增：

```css
.lb-section {
  border: 1px solid #e3e3e3;
  border-radius: 6px;
  background: #fff;
  margin-bottom: 1rem;
  overflow: hidden;
}

/* 區塊標頭：淺灰底 + 左側品牌色條，標示區塊起點 */
.lb-section__head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #f6f6f6;
  border-bottom: 1px solid #e3e3e3;
  border-left: 3px solid var(--tpi-primary);
}

.lb-section__head .area-title {
  margin-bottom: 0;
  font-weight: 700;
}
```

色條與 icon 使用 `var(--tpi-primary)` 而非寫死 `#EA7448`，依 `.claude/RULES/style.md`「新增樣式時應優先用變數」。

#### 2. 區塊一改為面板結構（HTML）

修改前——裸標籤 + 四段各自獨立的 `form-group row`，中間以 `<hr />` 與區塊二分隔：

```html
<label class="area-title">{{ 'fhir_viewer.base_block_title' | translate }}</label>
<div class="form-group row">
  <div class="col-6">
    <label for="sofProxyUrl" class="col-form-label">...</label>
    ...
  </div>
</div>
<!-- 其餘三段同結構 -->
<hr />
```

修改後——包進面板，四個欄位收進同一個 `row`，`<hr />` 移除（面板邊界本身已足夠分隔）：

```html
<div class="lb-section">
  <div class="lb-section__head">
    <i class="fa fa-link"></i>
    <label class="area-title">{{ 'fhir_viewer.base_block_title' | translate }}</label>
  </div>
  <div class="lb-section__body">
    <div class="row">
      <div class="col-12">...</div>
      <!-- 其餘三欄 -->
    </div>
  </div>
</div>
```

> 欄寬最終為 `col-12`（各欄獨佔一列）。詳見「預計修改方式 > 需求 1」與時間軸第 7 點。

#### 3. tab 列併入面板（HTML + CSS）

tab 列移進 `.lb-section__tabs`，貼齊標頭下緣；`tab-content` 補白底與 padding，使「tab → 內容」成為同一實體面板：

```html
<div class="lb-section__tabs">
  <ul class="nav nav-tabs" role="tablist">...</ul>
</div>
<div class="tab-content">...</div>
```

```css
.lb-section__tabs {
  padding: 0.5rem 0.75rem 0;
  background: #f6f6f6;
  border-bottom: 1px solid #ddd;
}

.tab-content {
  padding: 1rem 0.75rem;
  background: #fff;
}
```

`.nav-tabs` 由原本自帶 `border-bottom: 1px solid #ddd` 改為 `border-bottom: 0`，改由外層 `.lb-section__tabs` 提供該線，否則會出現兩條重疊的分隔線。

#### 4. active tab 標示與對齊

```css
/* active tab 頂端以品牌色標示，底部去邊與下方內容區連成一體 */
.nav-tabs > li.active > * {
  opacity: 1;
  background-color: #fff;
  border: 1px solid #ddd;
  border-top: 2px solid var(--tpi-primary);
  border-bottom-color: #fff;   /* 原本為 transparent */
}
```

`border-bottom-color` 由 `transparent` 改為 `#fff`：原本 `.nav-tabs` 自帶底線時，transparent 搭配 `li { margin-bottom: -1px }` 可露出下層線；改由外層提供底線後，需用不透明白色才能覆蓋，達成 active tab 與內容區連通的效果。

`> li > *` 由 `display: block` 改為 `display: flex; align-items: center`，讓 Tab 2 標題內的刷新鈕垂直置中；同時清掉 template 中該 `<b>` 內原有的兩行空白（會把 tab 撐高）。

#### 5. 請求／回應卡片降級

區塊二包成面板後，Authorization 分頁最外層兩個 `card card-body` 會變成同權重的「卡中卡」，加上 `.lb-subcard` 降級：

```css
.lb-subcard {
  border: 1px solid #eee;
  border-radius: 4px;
  box-shadow: none;
}
```

Tab 2 Patient Dashboard 內的卡片屬清單項目性質（病人清單、就診時間軸卡），維持原樣未動。

## 預計修改方式

### 需求 1：兩個區塊要更明顯

**Context** — 使用者開著 `lb0018.component.html`，反映版面不夠美觀，指名兩個區塊的邊界不明顯。

**分析** — 讀 `lb0018.component.html` 與 `.css` 後確認：兩個區塊都只是「一個 `area-title` 純文字標籤 + 一堆 form row」，中間靠一條 `<hr />` 分隔，沒有任何容器。grep `src/styles` 確認 `.area-title` 定義於 `_form-element.scss`，僅 `font-weight: 500; font-size: 0.9rem; margin-bottom: 0.5rem`——確實只是個小標籤，不具區塊感。同時 grep 確認 `area-title|panel-title|card-header` 在 `labs/` 底下只有 lb0018 這一個檔案命中，代表沒有既有的「區塊面板」共用 class 可重用。

**修正方案** — 新增元件 scoped 的 `.lb-section` 系列（面板 + 淺灰標頭 + 左側品牌色條 + icon）。不寫進全域 `styles.scss`／`_custom-prime.scss`，因為這是 lb0018 自身版面、非 PrimeNG 覆寫、目前也無第二處使用，依 `.claude/RULES/style.md`「只有真的是該元件獨有的樣式才寫進元件 CSS」。

### 需求 2：tab 內的區塊要更明確

**Context** — 同一則需求的第 2 點。

**分析** — 原結構是裸 `<ul class="nav nav-tabs">` 後接裸 `<div class="tab-content">`，兩者都沒有背景，浮在頁面上；而內容區裡面又全是 `card`，視覺層級是反過來的。`.nav-tabs` 的 BS3 風格是比照 lb0016 自帶的 scoped CSS（元件 CSS 為 scoped，故各自帶一份）。

**修正方案** — 讓 tab 列與內容成為同一面板的上下兩段：tab 列包進 `.lb-section__tabs`（與標頭同為 `#f6f6f6`）、`tab-content` 給白底。保留既有 BS3 tab 外觀以與 lb0016 一致，只加 active tab 頂端品牌色條與底邊連通。內層 `card` 則以 `.lb-subcard` 降級，恢復正確的視覺層級。

### 需求 3：標頭文字改粗體

**Context** — 使用者驗收前兩項後，選取 template 中 `lb-section__head` 該行，要求標頭文字改粗體。

**分析** — 全域 `.area-title` 為 `font-weight: 500`。

**修正方案** — 在 `.lb-section__head .area-title` 加 `font-weight: 700`。限定在 `.lb-section__head` 之下且為 scoped CSS，故不影響 Authorization 分頁內其他 `.area-title`（請求 URL、請求表頭等）的字重，也不影響其他頁面。

## 驗證方式

### 靜態驗證

- `npx ng build --configuration development` — **成功**，無 template / TypeScript 錯誤（Hash: 670191ecc0707db9，Time: 50188ms）。此次於需求 1、2 實作後執行。
- 需求 3（`font-weight: 700`）為單行 CSS 屬性新增，未再單獨跑 build。
- ⚠️ 注意：依 `angular.json`，build 產物直接輸出到 `../src/main/resources/static/dgrv4/ac4/`。上述 development build 已覆寫該目錄，需以 `npm run build`（production）覆蓋回去。

### 尚待人工驗證（建議）

1. `npm start` 開 `http://localhost:4701/`，進入 lb0018，確認兩個區塊的面板邊界、標頭色條與粗體標題呈現符合預期。
2. 切換 Authorization / Patient Dashboard 兩個 tab，確認 active tab 頂端品牌色條、底邊與內容區連通處**沒有多餘或斷裂的分隔線**（本次改動了 `.nav-tabs` 底線的提供者，此處最容易出錯）。
3. 停留在 Tab 2 且 base URL 有值時，確認標題內的刷新鈕垂直置中、未把 tab 撐高，且點擊只觸發刷新、不會誤觸 tab 切換（`stopPropagation` 行為未改，屬回歸確認）。
4. 區塊一四個欄位在實際使用的螢幕寬度下的排列是否符合期待（見時間軸第 7 點）。

## 實作紀錄

### 時間軸

1. 使用者要求優化 lb0018 版面並給出兩個方向，先請我提供修改計畫。
2. 讀 `lb0018.component.html`（577 行）與 `lb0018.component.css`（406 行），確認兩個區塊都無容器、tab 內部卻大量使用 `card`，視覺層級反置。
3. grep `src/styles` 確認 `.area-title` / `.panel-title` 的實際定義；grep `labs/` 確認無既有區塊面板 class 可重用（避免重造輪子，也避免憑空假設有共用元件）。
4. 提出計畫：兩區塊各包 `.lb-section`、區塊一改 grid、移除 `<hr />`、tab 併入面板、內層 card 降級為 `.lb-subcard`。使用者同意執行。
5. 實作 CSS 與 HTML。過程中的一個判斷點：`.nav-tabs` 原本自帶 `border-bottom`，改由 `.lb-section__tabs` 提供後，active tab 的 `border-bottom-color: transparent` 會失效（透明會露出外層的線而非覆蓋），必須改成 `#fff`——這是「把底線提供者上移」時容易漏掉的連帶修正。
6. `npx ng build --configuration development` 通過，回報成果，並主動提醒 build 產物已覆寫後端靜態資源目錄。
7. **使用者自行把區塊一的欄寬由我提案的 `col-12 col-lg-4`（三欄並排）改回 `col-12`（各欄獨佔一列）。** 我原本認為四個 `col-6` 垂直堆疊、右半邊全空是最浪費版面之處，但使用者的選擇顯示：這幾個欄位放的是 URL 這類長字串，獨佔整列的可讀性優先於填滿橫向空間。此改動予以保留，未還原。
8. 使用者追加需求：標頭文字改粗體。在 `.lb-section__head .area-title` 加 `font-weight: 700`。
9. 使用者要求建立本工作日誌，並在 `npm run build` 成功後上傳。

**本次工作截至日誌撰寫時點尚未 commit / push。** build 與提交結果見後續操作。

### 經驗紀錄（給未來類似任務參考）

1. **「區塊不明顯」多半是層級反置，而非缺樣式**：lb0018 的癥結不是外層太樸素，而是內層 `card` 太重。單純加強外層會變成「卡中卡」互相打架，必須同時降級內層（`.lb-subcard`）才會有層級感。往後遇到類似訴求，先盤點內外層的視覺權重再決定加什麼。
2. **搬移邊框的提供者時，記得檢查依賴該邊框的 `transparent` 技巧**：BS3 tab 的「active tab 與內容連通」是靠 `margin-bottom: -1px` + `border-bottom-color: transparent` 露出下層線達成的。一旦底線改由外層容器提供，transparent 就必須換成不透明底色，否則會露出錯誤的線。這類耦合不會有編譯錯誤，只能靠人工目視。
3. **不要替使用者決定「填滿空間比較好」**：我以「右半邊全空很浪費」為由提案三欄並排，使用者改回單欄。長 URL 欄位的可讀性 > 版面密度。版面提案涉及主觀取捨時，可先問或直接標注為可選項，而非當成理所當然的優化一併實作。
4. **元件 scoped CSS vs 全域**：判斷依據是「是否為該元件獨有 + 是否為 PrimeNG 覆寫」。`.lb-section` 目前只有 lb0018 使用且非 PrimeNG 覆寫，故留在元件 CSS；若日後第二個 labs 頁面需要相同面板，再考慮上抽到全域。
5. **本專案 build 會覆寫後端靜態資源目錄**：`ng build --configuration development` 的產物直接落在 `../src/main/resources/static/dgrv4/ac4/`。純為驗證編譯而跑 development build 時，記得最後補一次 `npm run build`，否則會留下 development 產物。
