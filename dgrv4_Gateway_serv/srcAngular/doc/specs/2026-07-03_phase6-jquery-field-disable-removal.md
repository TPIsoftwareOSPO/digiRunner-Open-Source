# Angular 13→20 技術債整理 Phase 6 子任務：移除 jQuery — 欄位 enable/disable 模式

延續 `doc/specs/2026-07-03_phase1-tech-debt-cleanup.md` 所述的六階段技術債整理計畫，接續 `doc/specs/2026-07-03_phase6-jquery-file-input-removal.md` 的 file-input 模式，本次處理 Phase 6 的第二種 jQuery 模式：欄位 enable/disable。

## 修改內容

### 範圍

`src/app/shared/key-value-grid/key-value-grid.component.ts`、`src/app/shared/key-value/key-value.component.ts` 兩個檔案。

### 重要澄清：這段 jQuery 並非冗餘操作

先前的 Phase 1 盤點階段曾推測「Reactive Forms 的 `.disable()/.enable()` 已涵蓋大部分邏輯，jQuery 部分可能是冗餘操作」。實際查看程式碼後，**這個推測不成立**：

- 這兩個元件都是自訂的 `ControlValueAccessor`（`KeyValueGridComponent`/`KeyValueComponent`），`setDisabledState?(isDisabled)` 是 Angular Forms 模組在對應的 `FormControl` 被 disable/enable 時，**自動呼叫**的生命週期方法。
- 元件內部的實際輸入欄位是透過 `ViewContainerRef.createComponent()` **動態建立**的子元件（`KeyValueGridDetailComponent`/`KeyValueDetailComponent`），並非直接綁定 `formControlName` 的原生 `<input>`。
- Angular 對 `[disabled]` 的自動化處理，只在原生表單元素直接綁定 `formControlName`/`formControl` 時才生效；對於這種「透過 CVA 包裝、內部動態產生一整組子元件」的結構，**normal 的表單停用機制根本不會觸及這些動態子元件內部的 input/button**。
- 因此，`setDisabledState`/`ngAfterViewInit` 內用 jQuery 找出 `content` 容器內所有 `input`/`button` 並批次設定 `disabled` 屬性，**是讓停用狀態能正確反映到動態子元件的必要機制**，不是可以直接刪除的冗餘程式碼。

本次任務範圍僅限於「把 jQuery 換成原生 DOM API」，功能語意完全保留。

### 程式碼異動

兩個檔案的 `setDisabledState`/`ngAfterViewInit` 內容完全相同，異動也相同：

```ts
// 修改前
setDisabledState?(isDisabled: boolean): void {
  this.disabled = isDisabled;
  if (this.content) {
    if (isDisabled) {
      $(this.content.nativeElement).find('input , button').prop('disabled', true);
    } else {
      $(this.content.nativeElement).find('input , button').prop('disabled', false);
    }
  }
}

ngAfterViewInit(): void {
  if (this.disabled) {
    $(this.content.nativeElement).find('input , button').prop('disabled', true);
  } else {
    $(this.content.nativeElement).find('input , button').prop('disabled', false);
  }
}

// 修改後
setDisabledState?(isDisabled: boolean): void {
  this.disabled = isDisabled;
  if (this.content) {
    this.content.nativeElement.querySelectorAll('input, button').forEach(
      (el: HTMLInputElement | HTMLButtonElement) => el.disabled = isDisabled
    );
  }
}

ngAfterViewInit(): void {
  this.content.nativeElement.querySelectorAll('input, button').forEach(
    (el: HTMLInputElement | HTMLButtonElement) => el.disabled = this.disabled
  );
}
```

改用原生 `Element.querySelectorAll` + `forEach` 取代 jQuery 的 `.find().prop('disabled', bool)`，行為等價。原本 `if (isDisabled) { ...true } else { ...false }` 的分支寫法（實質上就是 `.prop('disabled', isDisabled)`）在改寫時一併簡化為直接賦值，這是同一行敘述的等價壓縮，不是額外的邏輯重構。同時移除了緊鄰在旁、已經不具意義的 `.off('click')`/`.on('click')` 註解殘留（這兩行本來就是被註解掉的舊 jQuery 呼叫，隨著整段邏輯換成原生 DOM API，繼續留著這些引用 jQuery API 的死註解已無意義）。

## 驗證方式

### 靜態驗證

- `npm run build`（production）：**成功**，無新增錯誤。
- grep 確認兩個檔案已無殘留 `$(` 呼叫。

### 尚待人工驗證

本環境無可用瀏覽器自動化工具，建議人工測試：任一使用 `app-key-value-grid`/`app-key-value` 元件、且該表單/欄位會在特定情境下整體停用（例如「詳細資料」唯讀模式）的頁面，確認：
- 停用時，動態產生的 key-value 列表內所有 input/button 確實變成 disabled（無法輸入、無法點擊新增/刪除按鈕）。
- 啟用時，同樣的欄位恢復可操作。
- 元件初始載入（`ngAfterViewInit`）時，若表單一開始就是 disabled 狀態，欄位也要正確呈現為停用（而非要等到下一次狀態變化才生效）。

## 實作紀錄

### 時間軸

1. 使用者選擇 Phase 6 剩餘模式清單中的「欄位 enable/disable」繼續處理。
2. 讀取 `key-value-grid.component.ts`／`key-value.component.ts` 完整內容，發現這兩個元件是自訂 `ControlValueAccessor`，jQuery 呼叫位於 `setDisabledState`/`ngAfterViewInit` 兩個生命週期方法內。
3. 推翻 Phase 1 盤點階段「可能是冗餘操作」的初步猜測——實際分析後確認這是 CVA 停用狀態要正確傳遞到動態建立的子元件所必須的機制，因此本次僅做 API 替換（jQuery → 原生 DOM），不刪除邏輯。
4. 將兩處 `if (isDisabled) {...true} else {...false}` 的重複分支簡化為直接賦值（等價壓縮，非邏輯變更），並移除同一位置已無意義的 jQuery 死註解。
5. `npm run build` 驗證通過，比照先前慣例清理驗證動作弄髒的已追蹤建置產物目錄。

### 經驗紀錄（給未來類似任務參考）

- **稽核階段的猜測需要在實作前重新驗證**：Phase 1 盤點時基於「大部分情境下 Reactive Forms 已經處理停用邏輯」的一般認知，推測這段 jQuery 可能多餘。實際讀程式碼後發現，這裡的情境是「CVA + 動態建立子元件」的組合，Angular 的自動化停用機制並不覆蓋這種結構，猜測並不成立。批次盤點報告只能當作起點，實際動手前一定要重新確認每個案例的真實行為，不能照單全收先前的推測結論。
- **緊鄰的死註解可以一併清除**：與本次直接修改的敘述位於同一行、且註解內容本身就是同一組被停用的舊 jQuery 呼叫時，清掉它們不算「無關重構」，因為它們本來就是這幾行程式碼的一部分，留著只會讓人誤以為還有 `.off('click')`/`.on('click')` 的呼叫邏輯待處理。
