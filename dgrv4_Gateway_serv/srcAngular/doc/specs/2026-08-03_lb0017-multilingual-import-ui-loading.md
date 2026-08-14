# lb0017 多語系匯入介面與 Loading 生命週期修正

本次工作延續 [2026-07-02_lb0017-multilingual-export-select-toast.md](2026-07-02_lb0017-multilingual-export-select-toast.md)，補強 `lb0017` 的檔案選擇、欄位命名、匯入說明、Loading 與共用上傳 Observable 生命週期。

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 調查 `lb0017.component.ts` 使用 `$('#file').click()` 造成 `TS2592: Cannot find name '$'`，並確認為何 `develop` 已有相關修正但目前分支仍出現。
2. 將 `lb0017` 的檔案選擇從 jQuery 改為 Angular `ViewChild`。
3. 將「語系」更名為「匯出語系」。
4. 匯入功能加入 loading 效果。
5. 在操作區下方新增 setting、items、mailTplt、func、embeddedFunc、rtnCode 等 sheet 的資料來源與更新基準說明。
6. 優化說明文字區塊的視覺呈現，注意字型、字級、層級與響應式版面。
7. 調查匯入完成後 loading 持續顯示的原因，先採頁面層最小修正，再進一步檢測並修正根本問題。
8. 建立本工作日誌，執行 `npm run build`，成功後保留 build 產物並提交、上傳目前分支。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0017/lb0017.component.ts` | 改用 `ViewChild` 觸發檔案選擇；匯入 API 加入 `NgxUiLoaderService` 與 `finalize()` |
| `src/app/layout/labs/lb0017/lb0017.component.html` | 新增 file input template reference；欄位改用「匯出語系」；新增響應式匯入說明卡片 |
| `src/app/layout/labs/lb0017/lb0017.component.css` | 新增說明卡片的字型、字級、間距、框線與層級樣式 |
| `src/app/shared/services/api-base.service.ts` | 修正 `excuteTsmpSetting()` 對 `next`、`error`、`complete` 與 unsubscribe 的轉送 |
| `src/assets/i18n/zh-tw.json` | 新增匯出語系、匯入說明標題與內容 |
| `src/assets/i18n/zh-cn.json` | 同步簡體中文翻譯 |
| `src/assets/i18n/en-us.json` | 同步英文翻譯 |
| `src/assets/i18n/ja.json` | 同步日文翻譯 |
| `../src/main/resources/static/dgrv4/ac4/` | `npm run build` 產生的部署用 index、i18n assets 與 hash bundles |
| `doc/specs/2026-08-03_lb0017-multilingual-import-ui-loading.md` | 本次需求、調查、實作與驗證紀錄 |

### 詳細程式碼異動

#### 1. 移除 jQuery file input 觸發

修改前：

```ts
openFileBrowser() {
  $('#file').click();
}
```

修改後：

```ts
@ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

openFileBrowser() {
  this.fileInput.nativeElement.click();
}
```

Template 同步新增 `#fileInput`：

```html
<input type="file" id="file" #fileInput ...>
```

#### 2. 匯出語系與匯入說明 i18n

原本 template 使用會影響全站的共用 key：

```html
{{ 'locale' | translate }}
```

修改後改用 `lb0017` 專屬 key：

```html
{{ 'ml_export_locale_list' | translate }}
```

並在四份語系檔同步新增 `ml_import_guide_title`、`ml_import_note_1` 至 `ml_import_note_6_1`，避免非繁中環境顯示原始 key。

#### 3. 匯入說明改為響應式資訊卡

原本為連續的純文字 `<div>`；修改後加入語意化 `<section>`、標題、提示 icon，並以 Bootstrap grid 排版：

```html
<section class="card shadow-sm mt-4 import-guide" role="note" aria-labelledby="importGuideTitle">
  <div class="card-header bg-light d-flex align-items-center gap-2 py-3">...</div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-12 col-xl-6">...</div>
    </div>
  </div>
</section>
```

元件 CSS 繼承專案字型，標題使用 `1rem`、主項使用 `0.9375rem`、詳細規則使用 `0.875rem`，並以 `var(--tpi-primary)` 呈現品牌色層級。

#### 4. 匯入 loading 與根因修正

頁面層使用 `finalize()` 統一結束 loading：

```ts
this.ngxService.start();
this.serverService
  .importMultilingual(req, this.file!)
  .pipe(finalize(() => this.ngxService.stop()))
  .subscribe(...);
```

初次加入後，loading 仍持續顯示。調查發現 `excuteTsmpSetting()` 的外層 Observable 原本只轉送 `next`：

```ts
$object.subscribe(r => {
  obser.next(r);
});
```

修改後完整轉送生命週期並提供 teardown：

```ts
const subscription = $object.subscribe({
  next: r => obser.next(r),
  error: error => obser.error(error),
  complete: () => obser.complete(),
});
return () => subscription.unsubscribe();
```

此修正讓呼叫端的 `finalize()` 能在成功、HTTP error 或取消訂閱時執行。

## 預計修改方式

### 需求 1、2：調查並移除 jQuery

**Context** — Angular build 在 `lb0017.component.ts` 的 `$('#file').click()` 回報 `TS2592`；使用者認為 `develop` 已修正類似問題。

**分析** — 比對目前分支與 `develop` 後，確認 `develop` 並沒有 `lb0017`；`lb0017` 由 commit `fc1a4e650` 在 feature branch 新增。`develop` 的 commit `bae95ff43` 僅將 `lb0013` 的 jQuery file input 改為 `ViewChild`。兩個 commit 原本位於平行歷史，後續合併不會自動將 `lb0013` 的程式碼修正套用到新檔案。專案也沒有 `@types/jquery`，`tsconfig.app.json` 的 `types` 為空陣列。

**修正方案** — 比照 `lb0013` 的既有模式，在 template 加入 reference，元件以 `ViewChild` 與 `ElementRef<HTMLInputElement>` 觸發 click，不安裝 jQuery 型別。

### 需求 3：欄位更名

**Context** — 使用者要求「語系」顯示為「匯出語系」。

**分析** — 原本使用的 `locale` 是全站共用 key，直接修改會影響其他頁面；四份語系檔均有此 key。

**修正方案** — 新增專屬 `ml_export_locale_list`，只替換 `lb0017` template，並同步繁中、簡中、英文、日文。

### 需求 4、7：匯入 loading 與 Observable 根因

**Context** — 使用者要求匯入時顯示 loading；加入 `start()` 與 `finalize(stop)` 後，實際匯入完成仍持續顯示。

**分析** — `serverService.importMultilingual()` 會呼叫 `ApiBaseService.excuteTsmpSetting()`。該方法以 `new Observable()` 包住 `HttpClient.post()`，但 inner subscription 只執行 `obser.next(r)`，沒有轉送 `complete`、`error`，也沒有回傳 unsubscribe teardown。成功碼 `1100` 又不會進入共用 `stopAll()` 的錯誤分支，因此呼叫端 `finalize()` 不會執行。相同方法目前被多個匯入 API 共用。

**修正方案** — 使用者先指定最小化處理，因此一度在 `lb0017` 的 `subscribe` callback 尾端直接 `stop()`，能處理正常回應但無法處理 HTTP error。使用者要求根治後，改由 `excuteTsmpSetting()` 完整轉送 `next/error/complete` 並提供 teardown，再讓 `lb0017` 恢復 `finalize()`。

### 需求 5、6：匯入說明與視覺優化

**Context** — 使用者提供 14 行 sheet 說明，之後要求呈現更美觀並注意字型及大小。

**分析** — 專案樣式規範要求優先使用 Bootstrap grid/utility 與既有 CSS 變數；新增 i18n key 必須同步四份語系檔。`lb0017.component.css` 原本為空檔。

**修正方案** — 先將每行說明建立獨立 i18n key，再用 Bootstrap card、`row`/`col-*` 與 PrimeIcons 建立雙欄資訊卡；窄螢幕自動轉單欄。元件 CSS 僅處理此頁專屬字級、行距、縮排與品牌色。

### 需求 8：工作日誌、build 與上傳

**Context** — 使用者明確要求建立工作日誌、執行 `npm run build`，成功後上傳。

**分析** — `angular.json` 的 outputPath 指向 tracked 的 `../src/main/resources/static/dgrv4/ac4/`。依專案規範，此情境需保留並提交 build 產物。檢查分支後確認目前分支追蹤 `eapdd2025/feature/tom/tom_i18n_maintainance`，本機落後遠端 1 個 commit；該遠端 commit `b9718715e` 僅修改後端 `MultilingualManagementService.java`，與本次檔案不重疊。

**修正方案** — production build 成功後保留 static 差異；只暫存本次來源碼、i18n、spec 與完整 build 產物，排除既有 `application.properties`、`.claude/settings.local.json`、`srcVite/`。提交後整合遠端 commit，再推送追蹤分支。

## 驗證方式

### 靜態驗證

- 四份 `src/assets/i18n/*.json` 均以 `JSON.parse()` 驗證成功，且新增 key 無缺漏。
- 對本次來源檔執行 `git diff --check`，exit code 0。
- 多次執行 `npm run build` 驗證各階段變更；最後交付 build：
  - 指令：`npm run build`
  - exit code：`0`
  - build hash：`6a2cb0599c75b8d0`
  - `labs-lb0017-lb0017-component` chunk：`7322.98571e1649ba60b2.js`
- 專案沒有 `api-base.service.spec.ts` 或 `lb0017.component.spec.ts`，因此沒有可執行的對應單元測試。

### 尚待人工驗證（建議）

1. 開啟 `lb0017`，確認「匯出語系」文字在四種語系切換下正確顯示。
2. 確認匯入說明卡片在桌面為雙欄、窄螢幕為單欄，字型與其他頁面一致。
3. 選擇合法 xlsx 匯入，確認 loading 在請求期間顯示，成功 toast 出現後消失。
4. 模擬 HTTP 4xx/5xx 或斷線，確認 error 發生後 loading 仍會由 `finalize()` 關閉。
5. 抽查其他使用 `excuteTsmpSetting()` 的匯入頁面，確認成功、錯誤與取消訂閱行為無迴歸。

## 實作紀錄

### 時間軸

1. 使用者回報 `lb0017.component.ts:158` 的 jQuery `TS2592`，要求確認為何 feature branch 仍有問題。
2. 比對 git 歷史後確認 `develop` 的 `bae95ff43` 只修正 `lb0013`，而 `lb0017` 是另一條歷史新增的檔案；因此合併不會自動套用類似修正。
3. 使用者授權修改後，將 file input 改為 `ViewChild`，build 成功並清理當次僅供驗證的產物。
4. 使用者追加「匯出語系」、匯入 loading 與 14 行說明；新增專屬 i18n key、loading 與說明區塊，四份 JSON 驗證及 build 成功。
5. 使用者要求優化說明呈現；改成有標題、icon、主次文字層級與響應式雙欄的資訊卡，build 成功。
6. 使用者反應匯入完成後 loading 持續顯示。初步判斷 `finalize()` 未被觸發，查到 `excuteTsmpSetting()` 只轉送 `next`、不轉送 `complete/error`。
7. 依使用者「最小化處理」指示，一度移除 `finalize()` 並在 `lb0017` 收到回應後直接 `stop()`；如實說明此做法無法涵蓋 inner HTTP error 未轉送的情境。
8. 使用者要求檢測根本問題。進一步確認 `excuteTsmpSetting()` 也沒有 teardown，影響多個共用匯入 API；建議修正完整 Observable lifecycle。
9. 使用者授權根因修正。第一次 `apply_patch` 因 `api-base.service.ts` 存在多段相同 `$object.subscribe(...)`，誤命中較前面的 upload wrapper；在 build 前透過精確 diff 發現並立即還原，之後以 `excuteTsmpSetting()` 方法 context 鎖定正確位置。另用 `unix2dos` 恢復原檔 CRLF，避免產生無關換行差異。
10. 正確修正 `excuteTsmpSetting()` 的 `next/error/complete` 與 unsubscribe teardown，`lb0017` 恢復 `finalize()`；production build 成功。
11. 使用者要求建立工作日誌並 build 後上傳。確認目前分支落後遠端 1 個不重疊的後端 commit；再次執行最終 production build，exit code 0，保留部署產物。
12. 本文件建立時尚未 commit/push；依使用者本次指示，將在確認 staged 範圍後提交、整合遠端 commit 並推送目前追蹤分支。

### 經驗紀錄（給未來類似任務參考）

- `develop` 有修正「相似程式」不代表新 feature 檔案已自動得到相同修正；必須比對檔案存在性、commit containment 與實際 diff。
- Angular 專案不應為單一 `$('#file')` 補裝 jQuery 型別；既有 `ViewChild<ElementRef<HTMLInputElement>>` 模式更符合專案方向。
- 共用 i18n key 不應為單頁需求直接改值；新增功能專屬 key 可避免全站副作用，且必須同步四份語系。
- `finalize()` 依賴 Observable 的 complete、error 或 unsubscribe；若共用 service 手動包裝 Observable 卻只轉送 `next`，會讓 loading、錯誤處理與資源釋放全部失效。
- 手動包裝 inner Observable 時，除了 `next/error/complete`，還必須回傳 teardown，否則外層取消訂閱不會取消實際 HTTP request。
- `api-base.service.ts` 有多段高度相似 wrapper；使用文字 patch 時必須以方法名稱與鄰近 context 鎖定，並在 build 前檢查精確 diff。
- 使用者明確要求 build 後上傳時，tracked static 產物是交付內容，不能沿用「僅驗證 build」的清理流程。
