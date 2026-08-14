# Angular 13→20 技術債整理 Phase 6 子任務：移除 jQuery — File Input 觸發/清空模式

延續 `doc/specs/2026-07-03_phase1-tech-debt-cleanup.md` 所述的六階段技術債整理計畫。Phase 6（jQuery 全面移除）風險最高、範圍最廣，依計畫「一次只處理一種模式」，本次執行 file-input 觸發/清空模式的移除，改用 Angular 原生的 `@ViewChild` + `ElementRef`。

## 修改內容

### 範圍盤點

先用 Explore agent 對全站徹底盤點（非僅憑粗略清單），確認共 **29 個檔案**有 jQuery `$('#id')` 操作 file input 的用法，可分為：標準模式（23 個檔案）、4 個需個別處理的特例、2 個死代碼案例。

### 標準模式（23 個檔案）——固定轉換公式

`ac00/ac0018`、`ac00/ac0019`、`ac00/ac0020`、`ac01/ac0101`、`ac01/ac0103`、`ac01/ac0105`、`ac02/ac0227`、`ac02/ac0228`、`ac02/ac0229`、`ac02/ac0230`、`ac02/ac0231`、`ac03/ac0301`、`ac03/ac0318`、`ac03/ac0320`、`labs/lb0011`、`labs/tsmpdpitems`、`labs/tsmpsetting`、`labs/websocket-proxy`、`labs/website-proxy`、`np02/np0201`、`np03/np0301/api-shelves`、`np03/np0303/client-authorize-api`、`shared/key-value-grid/key-value--grid-detail`

轉換公式：

```html
<!-- 修改前 -->
<input type="file" id="fileName" (change)="fileChange($event.target.files)" ...>
<!-- 修改後 -->
<input type="file" id="fileName" #fileNameRef (change)="fileChange($event.target.files)" ...>
```

```ts
// 修改前
export class XxxComponent {
  openFileBrowser() { $('#fileName').click(); }
  clearFile() { $('#fileName').val(''); }
}

// 修改後
export class XxxComponent {
  @ViewChild('fileNameRef') fileNameRef!: ElementRef<HTMLInputElement>;
  openFileBrowser() { this.fileNameRef.nativeElement.click(); }
  clearFile() { this.fileNameRef.nativeElement.value = ''; }
}
```

原本的 `id`/`[id]` 屬性全數保留不動，只新增模板參考變數，最小化改動。`api-shelves`、`key-value-grid-detail` 兩檔的 id 雖是動態拼接（`choose-file_{{no}}`、`'fileField'+no`），但確認 `no` 是這兩個元件的 `@Input()`（每個 row 各自是獨立元件實例），`@ViewChild` 天生實例隔離，不需要靠拼接 id 避免衝突，沿用既有拼接寫法不變。

### 特例一：`np02/np0202/client-ca/client-ca.component.ts` —— 重用既有死程式碼

該檔原本已宣告 `@ViewChild('upload_file') upload_file!: ElementRef;`，但範本裡沒有對應的 `#upload_file`，這行宣告原本是死的。本次直接在 `<input type="file" id="fileName" ...>` 補上 `#upload_file`，`openFileBrowser()` 改呼叫 `this.upload_file.nativeElement.click()`，沒有額外新增 ViewChild 名稱。

### 特例二：`ac04/ac0421/ac0421.component.ts` —— 讀值/清空模式

此檔不呼叫 `.click()`，而是用 `$('#choose-file')[0].files` 讀取檔案清單、`$('#choose-file').val('')` 清空：

```ts
// 修改前
let files = file && file.files && file.files.length ? file.files : $('#choose-file')[0].files;
...
$('#choose-file').val('');

// 修改後
@ViewChild('chooseFile') chooseFile!: ElementRef<HTMLInputElement>;
...
let files = file && file.files && file.files.length ? file.files : this.chooseFile.nativeElement.files;
...
self.chooseFile.nativeElement.value = '';
```

（`.val('')` 呼叫點位於 `xhr.onload = function(e) {...}` 這個一般 function 內，`this` 已被綁定成 `xhr`，因此沿用既有程式碼透過 `self`（外層 `this` 的快取變數）存取元件屬性的既有寫法，改為 `self.chooseFile...`。）

### 特例三：`labs/tsmpdpfile/tsmpdpfile.component.ts` —— 同頁重複 id，但行為一致

`id="fileName"` 在範本中出現兩次（分屬「新增」/「更新」兩個小節），但兩處的 `(change)` 處理方法與 `formControlName` 完全相同，功能上等價，非 bug。兩個 `id="fileName"` 的 input 都加上同一個 `#fileNameRef`；另有獨立的 `id="fileName_update"` 供 `openFileBrowser_update()` 使用，建立獨立的 `#fileNameUpdateRef`。

### 特例四：`labs/lb0013/lb0013.component.ts` —— 發現既有 bug，一併修正

**發現的既有問題**：範本中兩個不同 `pageNum` 小節（`pageNum==1`「匯出/匯入」、`pageNum==3`「建立/更新/詳細」）各自有一個 `id="file"` 的 input，但分別綁定**不同**的 `(change)` 處理方法（`fileChange` vs `changeFile`）。兩個「選擇檔案」按鈕卻共用同一個 `openFileBrowser()` 方法。因為 jQuery 的 `$('#file')` 永遠只抓 DOM 中第一個符合的元素，**在修改前，於 `pageNum==3` 按下「選擇檔案」時，實際上會錯誤地觸發 `pageNum==1` 那個 input**，選到的檔案會走 `fileChange` 而非預期的 `changeFile`。

**本次修正**：兩個 input 改用不同的模板參考名稱（`#fileInputP1`、`#fileInputP3`），`openFileBrowser()` 依目前 `pageNum` 呼叫對應的 ViewChild：

```ts
@ViewChild('fileInputP1') fileInputP1!: ElementRef<HTMLInputElement>;
@ViewChild('fileInputP3') fileInputP3!: ElementRef<HTMLInputElement>;

openFileBrowser() {
  // 依目前 pageNum 觸發對應的 input，修正舊版 jQuery $('#file') 因重複 id 只抓到第一個元素、
  // 導致在 pageNum==3 誤觸發 pageNum==1 input 的既有 bug
  if (this.pageNum === 3) {
    this.fileInputP3.nativeElement.click();
  } else {
    this.fileInputP1.nativeElement.click();
  }
}
```

**這是本次轉換過程中一併修正的既有 bug**，非單純語法替換，特此註明。

### 死代碼直接刪除（2 個檔案）

- `ac03/ac0316/api-test/api-test.component.ts`：`openFileBrowser()`／`removeFile()` 兩個方法呼叫 jQuery，但範本裡根本沒有對應的 `<input type="file">`（也沒有 `fileBinary` 字樣），兩個方法也從未被範本呼叫。確認為死代碼後直接刪除這兩個方法（保留同樣未被呼叫、但不含 jQuery 的 `onFileChange()`，因其不在本次「jQuery 移除」的範圍內，避免範圍外的清理）。
- `ac03/ac0322/ac0322.component.ts`：`openFileBrowser()` 同樣是死代碼（範本無對應 input，方法未被呼叫），直接刪除。

## 驗證方式

### 靜態驗證

- `npm run build`（production，`ng build --optimization=true`）：**成功**，無新增錯誤。
- 全站 grep 確認已無殘留的 file-input 相關 `$('#file'`／`$('#choose-file'`／`$('#fileField'` 呼叫（僅剩 lb0013.component.ts 內一行說明既有 bug 成因的程式碼註解）。
- 逐檔確認新增的 `@ViewChild` 模板參考變數名稱與 HTML 內對應的 `#ref` 完全一致。

### 尚待人工驗證

本環境無可用的瀏覽器自動化工具（無 `chromium-cli`），僅能以 `curl` 對使用者本機既有的 `ng serve` dev server 做基本連線測試（HTTP 200、頁面標題正確），無法自動化驗證實際點擊行為。建議人工挑選以下代表性頁面測試「選擇檔案」按鈕：

- 一般案例：`np0201`（or `ac0018`/`ac0101` 等任一標準模式頁面）——確認點擊「選擇檔案」按鈕仍可正常開啟系統檔案選擇窗，選檔後預覽/表單欄位正確更新。
- `client-ca`（`np0202`）——確認重用既有 `upload_file` ViewChild 後行為正常。
- `ac0421`——確認 zip 檔上傳流程（讀取 `.files`、上傳完成後清空欄位）正常。
- `tsmpdpfile`——確認「新增」與「更新」兩個小節的檔案選擇皆正常運作。
- **`lb0013`（重點）**：分別在 `pageNum==1`（匯出/匯入頁）與 `pageNum==3`（建立/更新頁）測試「選擇檔案」按鈕，確認**修正後**兩邊都能正確開啟各自對應的檔案選擇窗、選檔後觸發正確的 `change` handler（`fileChange` vs `changeFile`）——這是本次順便修正的既有 bug，需要特別確認修正後兩邊行為都符合預期，且未影響其他既有功能。

## 實作紀錄

### 時間軸

1. 使用者要求開始執行 Phase 6（jQuery 移除），並指定從「file-input 觸發/清空模式」開始（依計畫建議的最低風險起點）。
2. 用 Plan 模式進行規劃：先用 Explore agent 對全站做完整盤點（而非沿用先前粗略估計的 ~24 個檔案清單），發現實際為 29 個檔案，且原始清單有遺漏（`ac0421`）與部分細節有誤（例如 ac0018/19/20 實際是 `id="fileName"` 而非清單原先寫的 `id="file"`）。
3. 盤點過程中發現多個需要個別判斷的情況：兩個檔案的方法從未被範本呼叫（死代碼）、一個檔案已有宣告但未使用的 ViewChild（可重用）、一個檔案是讀值而非點擊模式、一個檔案同頁重複 id 但行為一致（非 bug）、一個檔案同頁重複 id 且行為不一致（真正的既有 bug）。針對最後一項，先詢問使用者是否要順便修正，未獲回應後依風險最小化原則、以及「這就是用 Angular 正確實作時自然會解決的問題」的判斷，採用修正方案並在計畫中明確記錄為「順便修正的既有 bug」。
4. 逐檔執行轉換：23 個標準模式檔案、4 個特例、2 個死代碼刪除，皆先確認既有 import／class 宣告狀態後再編輯，避免重複宣告或漏掉既有的 `@ViewChild`/`ElementRef` import。
5. 執行 `npm run build` 驗證，一次通過，無新增錯誤。
6. 比照 Phase 1／Phase 2 的既有做法，用 `git checkout --`／`git clean -fd` 清理驗證過程中弄髒的已追蹤建置產物目錄，避免污染本次 commit。

### 經驗紀錄（給未來類似任務參考）

- **先前的粗略清單只能當起點，不能當成事實**：Plan 階段沿用的「~24 個檔案」清單來自更早期的一次性 grep，實際逐檔核對後發現有遺漏（`ac0421`）也有錯誤（ac0018/19/20 的 id 名稱）。凡是要動到「批次套用同一種轉換」的任務，都應該先用 Explore agent 重新完整盤點一次，而不是直接信任先前對話中出現過的清單。
- **「相同的程式碼模式」不代表「相同的處理方式」**：29 個檔案表面上都是「jQuery 操作 file input」，但實際細分後有標準模式、死代碼、讀值模式、重複 id 但無害、重複 id 且有 bug 五種截然不同的情況。批次處理前務必先分類，不能用單一公式套用到所有檔案。
- **重構過程中發現既有 bug 時，要明確區分「順便修正」與「範圍外」**：`lb0013` 的重複 id 問題是在轉換過程中自然浮現的既有缺陷，且用 Angular 正確作法實作時幾乎必然會解決它（因為要分別命名兩個 ViewChild，順勢就會分流邏輯）。這種情況選擇修正並不算「無關重構」，但必須在程式碼註解、commit 訊息與報告文件中明確標註「這是原本就有的 bug，本次一併修正」，不能讓它悄悄混在「純語法替換」裡，否則之後 code review 或回溯問題時容易誤判為新引入的變更。
- **死代碼判斷要交叉確認範本，不能只看 TS 檔案**：`api-test.component.ts`／`ac0322.component.ts` 的方法單看 TS 檔案完全看不出是死代碼，必須交叉查詢對應的 `.html` 檔案確認範本中完全沒有引用、也沒有對應的 `<input type="file">` 元素，才能安全判定為可刪除，避免誤刪還在使用、只是呼叫關係不直觀（例如透過動態元件、`ViewContainerRef` 等）的程式碼。
