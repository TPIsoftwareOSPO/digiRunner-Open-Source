# Angular 13→20 技術債整理 Phase 6 子任務：移除 jQuery — Required Label 標記

延續六階段技術債整理計畫，本次處理 Phase 6 規劃中風險評估最高的最後一種 jQuery 模式：`base-component.ts` 的必填欄位（required）標籤標記邏輯。之所以列為風險最高，是因為 `BaseComponent` 被全站約 61 個檔案、170 處呼叫點透過繼承共用（`addFormValidator`/`resetFormValidator`），任何改動的影響面遠大於前四個子任務。

## 修改內容

### 根因與範圍

`src/app/layout/base-component.ts` 的 `addFormValidator()`/`resetFormValidator()` 會依後端回傳的欄位限制（`constraints`）動態設定 Angular 表單驗證器，並用 jQuery 在對應的 `<label id="{fieldName}_label">` 元素上加/移除 `required` class，讓 CSS（`.form-group .required.control-label:after { content: "*"; color: red; }`）顯示紅色星號：

```ts
// 修改前
if (item.isRequired) {
    _validator.push(ValidatorFns.isRequiredValidator(item.isRequired.msg));
    $(`#${item.field}_label`).addClass('required');
}
...
public resetFormValidator(form: UntypedFormGroup, formReset = true) {
    for (const formControlName in form.controls) {
        $(`#${formControlName}_label`).removeClass('required');
        ...
    }
}
```

### 修正方案：直接替換為原生 DOM API，不改變架構

`BaseComponent` 本身不是 `@Component`（沒有自己的樣板），是被大量元件繼承的純 TypeScript 基底類別，原本就是靠「用字串 id 查全域 DOM」的方式操作標籤（而不是透過該元件自己的樣板/`ViewChild`）。這代表 jQuery 的 `$('#id')` 在語意上就是 `document.getElementById('id')`，`.addClass()`/`.removeClass()` 對應 `classList.add()`/`classList.remove()`——**是一個可以逐行對應、不需要改變任何架構或影響任何呼叫端的直譯替換**，不需要像先前 file-input／chart-canvas 子任務那樣導入 `@ViewChild`。

```ts
// 修改後
if (item.isRequired) {
    _validator.push(ValidatorFns.isRequiredValidator(item.isRequired.msg));
    document.getElementById(`${item.field}_label`)?.classList.add('required');
}
...
public resetFormValidator(form: UntypedFormGroup, formReset = true) {
    for (const formControlName in form.controls) {
        document.getElementById(`${formControlName}_label`)?.classList.remove('required');
        ...
    }
}
```

用 `?.`（optional chaining）保留 jQuery 原本「找不到元素時靜默不做事」的行為（jQuery 選擇器找不到元素時 `.addClass()`/`.removeClass()` 是無害的 no-op；若直接用 `document.getElementById(...).classList...` 在找不到元素時會因為 `null` 而丟出例外，因此必須加 `?.` 才能維持原本行為）。

### 順便修正：另外 2 個檔案有相同模式的獨立重複實作

盤點時發現 `BaseComponent` 之外，還有 2 個檔案**沒有透過繼承呼叫 `addFormValidator`**，而是自行重複實作了完全相同的「對 `#{key}_label` 加上 `required` class」邏輯：

- `src/app/layout/labs/lb0011/mapping-url-form/mapping-url-form.component.ts`：4 處 `$(`#${row.key}_label`).addClass('required');`（分屬 `EMAIL`/`SLACK`/`DISCORD`/`LINE` 四種通知管道的表單初始化邏輯）
- `src/app/layout/labs/lb0011/key-value-form/key-value-form.component.ts`：1 處 `$(`#url_label`).addClass('required');`

這兩個檔案都只有「加上」required class，沒有對應的「移除」呼叫（不影響行為，原樣保留），一併改為：

```ts
document.getElementById(`${row.key}_label`)?.classList.add('required');
document.getElementById(`url_label`)?.classList.add('required');
```

## 驗證方式

### 靜態驗證

- `npm run build`（production）：**成功**，無新增錯誤。這是本次 Phase 6 系列中影響面最廣的一次改動（`base-component.ts` 被 61 個檔案繼承），build 通過代表所有呼叫端的型別/呼叫方式都相容。
- grep 確認 `base-component.ts`、`mapping-url-form.component.ts`、`key-value-form.component.ts` 皆已無殘留 `$(` 呼叫。
- 交叉確認先前 Phase 6 子任務已處理過的檔案（`lb0013.component.ts`、`ac0103.component.ts`）目前殘留的 `$(` 命中皆為誤判（分別是說明既有 bug 成因的程式碼註解、與一個以 `$` 結尾的 RxJS 慣例方法名稱 `openDialog$`），確認沒有殘留的真實 jQuery 呼叫或先前子任務的遺漏。

### 尚待人工驗證

本環境無可用瀏覽器自動化工具，建議人工測試：任一會呼叫 `addFormValidator`（例如查詢後端 `_before` 系列 API 取得 `constraints` 並套用的頁面，如 `ac1301`～`ac1305`、`ac0501` 等）的頁面，確認：
- 必填欄位的標籤仍正確顯示紅色星號（`.required:after` 樣式）。
- 切換頁面模式（例如查詢→建立→編輯）觸發 `resetFormValidator` 時，星號能正確被移除。
- `labs/lb0011` 的 webhook 設定頁面（mapping-url-form、key-value-form），切換 `EMAIL`/`SLACK`/`DISCORD`/`LINE` 等通知管道類型時，對應必填欄位仍正確顯示星號。

## 已知未涵蓋範圍（如實記錄，非本次任務範圍）

全站掃描顯示，除本次處理的 5 種既定模式（file-input、欄位 enable/disable、chart canvas、sidebar active state、required-label）外，仍有少數檔案存在**尚未分類的 jQuery 用法**（例如 Phase 1 盤點階段記錄的「其他 toggling/visibility」模式：`np02/ca-stat`、`np02/np0202`、`np01/np0115` 等的 `.toggleClass`/`.hasClass`/`.trigger('click')`/`.hide()`/`.show()` 等）。這些不在使用者本次要求的「base-component.ts 的 required-label 邏輯」範圍內，維持現狀、如實記錄，待日後有需要再另行處理。

## 實作紀錄

### 時間軸

1. 使用者選擇處理 Phase 6 最後、也是原始盤點階段評估風險最高的模式：`base-component.ts` 的 required-label 邏輯。
2. 重新讀取 `base-component.ts` 確認現況與先前盤點一致，grep 全站確認 `addFormValidator`/`resetFormValidator` 共 170 處呼叫、61 個檔案透過繼承使用，且 CSS `.form-group .required.control-label:after` 確認這個 class 的視覺意義（顯示紅色星號）。
3. 判斷：雖然呼叫端數量龐大（風險評估的「高風險」主要來自這個廣度），但 `BaseComponent` 本身不是元件、無樣板，其 jQuery 操作本來就是「用全域 id 查 DOM」，因此可以用 `document.getElementById` 逐行直譯取代，不需要導入 `@ViewChild` 或改變任何架構，也完全不影響任何一個呼叫端的使用方式——實際改動風險遠低於原始評估的「高風險」印象，因為風險來源是「廣度造成的回歸測試負擔」，不是「改法本身有架構不確定性」。
4. 額外用 grep 確認全站是否有其他地方複製了同一段 required-label 邏輯（而非透過繼承呼叫 `BaseComponent` 的方法），找到 `labs/lb0011` 底下兩個獨立重複實作的檔案，一併修正，讓「required-label」這個 jQuery 模式在全站範圍內確實移除乾淨，而不只是修好共用基底類別。
5. `npm run build` 一次驗證通過，交叉檢查先前子任務已處理過的檔案沒有殘留或遺漏，清理驗證動作弄髒的已追蹤建置產物目錄。

### 經驗紀錄（給未來類似任務參考）

- **「風險評估」中的風險來源要拆解清楚，不能一概而論**：這次任務被列為「風險最高」，但拆解後發現風險完全來自「呼叫端數量龐大、需要更多回歸測試」，而不是「改法本身有多種方案、需要抉擇」。這種情況下，實際的程式碼改動可以維持跟其他小範圍子任務一樣的簡單直譯策略，不需要因為「風險最高」這個標籤就自動假設要做更複雜的架構設計——判斷風險類型比套用制式的「風險越高、方案要越保守/複雜」更重要。
- **修完共用基底類別後，要主動搜尋是否有「重複造輪子」的獨立實作**：`BaseComponent` 提供了 `addFormValidator`，理論上所有需要必填標籤邏輯的元件都該透過繼承呼叫它，但實際上 `labs/lb0011` 底下兩個檔案各自複製貼上了同一段 jQuery 邏輯，沒有走共用方法。只修 `base-component.ts` 不會抓到這兩個複製品，需要額外用「這個 jQuery 呼叫的具體樣式」（`#{xxx}_label` + `addClass('required')`）反向搜尋全站，才能確保同一個視覺/行為模式被徹底移除，而不是只移除了「透過繼承呼叫」的那條路徑。
