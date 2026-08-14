# 樣式（CSS/SCSS）規範

## 版面配置：優先用 Bootstrap Grid + Utility Class

Template 版面一律優先使用 Bootstrap 5 的 grid 與 utility class（`row`/`col-*`、`d-flex`、`align-items-center`、`justify-content-*`、間距 `me-*`/`px-*`/`py-*`/`mb-*` 等），不要為了排版另外寫 `display:flex` 的 scoped CSS 或大量 inline `style="..."`。

```html
<!-- ✓ 正確：用 Bootstrap grid + utility class -->
<div class="col-12 d-flex align-items-center justify-content-between">
  <app-search-input class="col-9 col-lg-6"></app-search-input>
</div>

<!-- ❌ 避免：inline style 手刻排版 -->
<div style="display: flex; align-items: center; justify-content: space-between;">
```

> 既有程式碼中仍有不少 inline `style="..."` 排版（歷史包袱），修改該檔案時不必特地回頭清理，但**新增內容一律用 Bootstrap class**。

## 全域共用 flex 工具 class

`src/styles/styles.scss` 已定義以下共用 flex 排版 class，需要「置中對齊 + 間距」的常見排版時優先重用，不要重新宣告相同效果：

```
.field-flex-end             /* flex, align-items:center, justify-content:flex-end */
.field-flex-center          /* flex, align-items/justify-content:center, gap */
.field-flex-start           /* flex, align-items:center, justify-content:flex-start, gap */
.field-flex-space-between   /* flex, align-items:center, justify-content:space-between, gap */
.field-flex-column          /* flex-direction:column, justify-content:center, gap */
.field-flex-column-center   /* flex-direction:column, align-items/justify-content:center, gap */
```

## 已淘汰的排版 class（不要在新程式碼使用）

`styles.scss` 內的 `.ui-g`、`.ui-g-*`、`.ui-grid`、`.ui-grid-col-*` 系列標記為「Deprecated Grid CSS」，是舊版 PrimeNG 的 grid 系統，**新程式碼一律改用 Bootstrap grid**（`row`/`col-*`）。既有頁面若仍使用這些 class，維護時不必強制替換，除非該次任務就是要重構版面。

## 按鈕樣式

一般操作按鈕優先組合 Bootstrap `.btn` 與專案自訂的品牌色 class（定義於 `src/styles/custom/_button.scss`），而不是直接用 Bootstrap 的 `.btn-primary`/`.btn-secondary` 或自行刻顏色：

```html
<button type="button" class="btn tpi-btn tpi-primary" (click)="onSubmit()">{{ 'confirm' | translate }}</button>
```

| Class | 用途 |
|-------|------|
| `tpi-btn` | 按鈕基礎樣式（圓角、字重、最小寬度），需與色彩 class 搭配使用 |
| `tpi-primary` | 主要動作（橘色系品牌主色） |
| `tpi-second` | 次要動作（藍色系） |
| `tpi-cancel` | 取消 / 次要操作（灰色） |

## 品牌色與共用 CSS 變數

`styles.scss` 的 `:root` 定義了品牌色與版面尺寸變數，新增樣式時優先引用這些變數，而不是重複寫死相同色碼：

```scss
--tpi-primary: #EA7448;
--tpi-orange: #FFAC00;
--tpi-second: #6096E7;
--tpi-focus-color: #f8cb72;
--text-color: #5A5541;
--sidebar-w: 236px;
--header-h: 46px;
--footer-h: 24px;
```

```scss
// ✓ 正確：引用變數
.my-highlight { color: var(--tpi-primary); }

// ❌ 避免：重複寫死同樣的品牌色碼
.my-highlight { color: #EA7448; }
```

> 既有程式碼中大量 PrimeNG 元件覆寫仍是寫死色碼（例：`.p-checkbox-box.p-highlight { border-color: #FFAC00; }`），這是既有慣例、暫不強制回頭修正，但**新增樣式時應優先用變數**。

## PrimeNG 元件樣式覆寫：一律放在全域檔案，不用 `::ng-deep`

本專案**沒有**任何元件使用 `::ng-deep`（已確認）。需要覆寫 PrimeNG 元件的內部樣式（如 `.p-dialog`、`.p-checkbox`、`.p-radiobutton`、`.p-datatable` 等）時，統一寫在全域樣式檔，不要在元件的 scoped CSS 用 `::ng-deep` 穿透：

- 全站共用的 PrimeNG 覆寫 → `src/styles/custom/_custom-prime.scss`
- 其他零散但仍屬全域性質的覆寫 → `src/styles/styles.scss`

```scss
/* src/styles/styles.scss 或 _custom-prime.scss */
.p-dialog .p-dialog-footer {
  text-align: center !important;
}
```

只有當覆寫**真的只影響單一元件、且不會被其他頁面共用**時，才考慮寫在該元件自己的 `.css` 檔（不用 `::ng-deep`，改用元件內普通的巢狀 selector 命中該元件模板內的 PrimeNG DOM 即可，因為 Angular 預設的 view encapsulation 對元件模板本身渲染出的子元件內部結構通常仍可直接命中）。

## 元件層級 `.css` 檔

多數元件的 `.css` 檔案內容很精簡（通常只有個位數條規則），主要處理該頁面獨有、不具共用價值的細節調整（例：`.form-group label { background: #F6F6F6; }`）。新增元件樣式時：

- 先確認是否能用 Bootstrap utility class 或既有 `.tpi-*`/`.field-flex-*` class 達成，能達成就不要寫 CSS。
- 只有真的是該元件獨有、無法用既有 class 表達的樣式才寫進元件 `.css`。
- 不要把應該全域共用的樣式（例如又一次覆寫 PrimeNG 元件外觀）寫進單一元件的 CSS 檔，應歸類到 `styles.scss`/`_custom-prime.scss`（見上一節）。

## SCSS 全域檔案結構

```
src/styles/
├── styles.scss              # 全域進入點：CSS 變數、body 基礎樣式、大量 PrimeNG 覆寫
├── custom/
│   ├── _button.scss         # 按鈕相關 class（.tpi-btn、.tpi-primary 等）
│   ├── _custom-bootstrap.scss
│   ├── _custom-prime.scss   # PrimeNG 元件覆寫（優先寫在此檔或 styles.scss）
│   ├── _form-element.scss
│   └── _swal2.scss          # SweetAlert2 樣式
└── layout/
    ├── _header.scss
    ├── _footer.scss
    ├── _sidebar.scss
    ├── _layout.scss
    └── _login*.scss
```

新增全域樣式檔時，記得在 `styles.scss` 頂部用 `@use "./路徑" as *;` 引入，否則不會生效。

## 常見錯誤

```html
<!-- ❌ 錯誤：用舊版 PrimeNG grid -->
<div class="ui-g"><div class="ui-g-6">...</div></div>

<!-- ✓ 正確：用 Bootstrap grid -->
<div class="row"><div class="col-6">...</div></div>
```

```scss
/* ❌ 錯誤：元件 CSS 用 ::ng-deep 硬穿透 */
:host ::ng-deep .p-dialog-footer { text-align: center; }

/* ✓ 正確：寫在全域 _custom-prime.scss 或 styles.scss */
```
