# lb0013 Auth 下拉的 `dgr` 選項改為 value `digirunner`、顯示 `digiRunner`

本次工作延續 [`2026-07-06_lb0013-sofproxy-auth-relay-token.md`](./2026-07-06_lb0013-sofproxy-auth-relay-token.md)。該份 spec 建立了 lb0013 的 `sofProxyAuth` 下拉（`sofProxyAuthOptions: string[] = ['none', 'dgr', 'third-party']`，因為是純字串陣列，`p-select` 的 label 與 value 相同）。本次僅調整其中 `dgr` 這個選項的值與顯示文字。

## 修改內容

### 問題描述

使用者提出一項需求：

1. lb0013 的 `sofProxyAuthOptions` 資料中，原本的 `dgr` 選項，**value 要改成 `digirunner`，但畫面上要呈現 `digiRunner`**（value 小寫、label 駝峰）。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0013/lb0013.component.ts` | `sofProxyAuthOptions` 由 `string[]` 改為 `{ label, value }[]`；新增 `getSofProxyAuthLabel()` 將後端值轉為顯示文字 |
| `src/app/layout/labs/lb0013/lb0013.component.html` | Auth 的 `p-select` 補上 `optionLabel` / `optionValue`；查詢列表與匯入列表的 Auth 欄位改用 `getSofProxyAuthLabel()` |

### 詳細程式碼異動

#### 1. `sofProxyAuthOptions` 改為物件陣列

修改前（`lb0013.component.ts:84-85`）：

```ts
// Auth 下拉選項（label 與 value 相同）
sofProxyAuthOptions: string[] = ['none', 'dgr', 'third-party'];
```

修改後：

```ts
// Auth 下拉選項（digirunner 送後端值為小寫，畫面顯示 digiRunner）
sofProxyAuthOptions: Array<{ label: string; value: string }> = [
  { label: 'none', value: 'none' },
  { label: 'digiRunner', value: 'digirunner' },
  { label: 'third-party', value: 'third-party' },
];
```

`none` 與 `third-party` 維持 label 等於 value，只有原 `dgr` 拆成不同的 label／value。同檔案另外兩組下拉（`sofProxyInIntrospectionAuthOptions`、`sofProxyOutAuthMethodOptions`）維持 `string[]` 不動。

#### 2. `p-select` 明確指定 `optionLabel` / `optionValue`

修改前（`lb0013.component.html:214-215`）：

```html
<p-select inputId="sofProxyAuth" [options]="sofProxyAuthOptions" formControlName="sofProxyAuth"
  [style]="{'width':'100%'}"></p-select>
```

修改後：

```html
<p-select inputId="sofProxyAuth" [options]="sofProxyAuthOptions" optionLabel="label" optionValue="value"
  formControlName="sofProxyAuth" [style]="{'width':'100%'}"></p-select>
```

有了 `optionValue="value"`，綁進 form control 的仍是小寫字串，因此 `sofProxyAuth.value === 'third-party'` 的條件判斷、送後端的 `sofProxyAuth: [this.sofProxyAuth.value || 'none']` 都不需要跟著改。

#### 3. 列表顯示的 value → label 轉換

修改前，查詢列表（`lb0013.component.html:68`）與匯入列表（同檔 `:449`）都直接把後端值印出來：

```html
<td>{{rowData.sofProxyAuth?.join(', ')}}</td>
<td rowspan="2">{{rowData.sofProxyAuth?.join(', ')}}</td>
```

修改後改走同一個轉換方法：

```html
<td>{{getSofProxyAuthLabel(rowData.sofProxyAuth)}}</td>
<td rowspan="2">{{getSofProxyAuthLabel(rowData.sofProxyAuth)}}</td>
```

方法放在 `lb0013.component.ts` 表單 getter 區塊之前（依 `.claude/rules/angular-component.md` 的類別內部順序，業務方法在 getter 之前）：

```ts
/** 將後端回傳的 Auth 值轉為畫面顯示文字（無對應選項時直接顯示原值） */
getSofProxyAuthLabel(values?: Array<string>): string {
  return (values ?? [])
    .map(
      (val) =>
        this.sofProxyAuthOptions.find((opt) => opt.value === val)?.label ??
        val,
    )
    .join(', ');
}
```

`?? val` 的 fallback 是刻意保留的：資料庫若還有舊值（例如 `dgr`）或後端回傳未知值時，列表會原樣顯示而不是變成空白，方便辨識資料狀態。

## 預計修改方式

### 需求：`dgr` → value `digirunner` / 顯示 `digiRunner`

**Context** — 使用者指名 lb0013 的 `sofProxyAuthOptions`，要求「value 是 digirunner，但呈現是 digiRunner」。

**分析** — 實際查證過程：

1. `Grep sofProxyAuthOptions` 找到定義在 `lb0013.component.ts:85`，型別為 `string[]`，註解寫明「label 與 value 相同」；使用處在 `lb0013.component.html:214` 的 `p-select`。
2. `Grep sofProxyAuth`（限 `src/app`）盤點所有相關位置，確認值會流向：
   - form control 預設值 `new FormControl('none')`（`:128`）
   - 條件判斷 `this.sofProxyAuth.value === 'third-party'`（`:581`、`:647`）與 template 的 `*ngIf="sofProxyAuth.value === 'third-party'"`（html `:228`）
   - create/update payload `sofProxyAuth: [this.sofProxyAuth.value || 'none']`（`:568`、`:634`）
   - detail/update 帶值 `this.selectedItem!.sofProxyAuth?.[0] ?? 'none'`（`:390`、`:452`）
   - 兩處列表顯示 `rowData.sofProxyAuth?.join(', ')`（html `:68`、`:449`）
   
   結論：只要下拉綁定的仍是字串 value，上述邏輯全部不受影響，唯一會「露出原始值」的是兩處列表顯示。
3. 查專案內物件型 options 的既有慣例：`Grep optionValue=` 在 `labs/` 底下找到 `lb0016.component.html` 多處使用 `[options]="xxxOptions" optionValue="value"`，確認本專案已有此寫法，不需另創新模式。

**修正方案** — 把 `sofProxyAuthOptions` 改成 `{ label, value }[]` 並在 `p-select` 明確標註 `optionLabel="label"` / `optionValue="value"`。之所以不依賴 PrimeNG 對物件 options 的預設 fallback，是因為 `Select` 的 `getOptionValue` 在未指定 `optionValue` 時的推導規則不直觀（會受 `optionLabel` 是否設定影響），明確標註兩個屬性最不容易出錯，也與 `lb0016` 的既有寫法一致。

列表顯示部分，雖然使用者只提到下拉選項，但若不處理會出現「編輯畫面顯示 `digiRunner`、列表顯示 `digirunner`」的不一致，屬於同一個「呈現」需求範圍，因此一併以 `getSofProxyAuthLabel()` 轉換，並保留未知值原樣顯示的 fallback。

## 驗證方式

### 靜態驗證

執行 `npx ng build --configuration development`：

```
Build at: 2026-07-28T05:36:39.126Z - Hash: c99dfb94a93ef7e6 - Time: 59909ms
```

建置成功，無 TypeScript / template 型別錯誤（`getSofProxyAuthLabel` 的參數型別 `Array<string> | undefined` 相容於 `dpb0310` 的 `sofProxyAuth: Array<string>` 與 `dpb0311`/`dpb0312` 的 optional 版本）。

### 尚待人工驗證（建議）

1. 進入 lb0013（SMART on FHIR Proxy）新增／編輯畫面，確認 Auth 下拉第二個選項顯示為 `digiRunner`。
2. 選 `digiRunner` 後儲存，於瀏覽器 DevTools Network 確認送出的 `sofProxyAuth` 為 `["digirunner"]`（小寫）。
3. 回到查詢列表，確認 Auth 欄位顯示 `digiRunner`；匯入檔案的預覽列表同樣顯示 `digiRunner`。
4. 切換到 `third-party`，確認 introspection 四個必填欄位仍正常掛上／清除驗證（此邏輯未改動，但值的變更牽涉同一個 control，需回歸確認）。
5. **舊資料相容性**：若資料庫仍有以 `dgr` 儲存的紀錄，開啟編輯畫面時下拉會因找不到對應選項而顯示空白（列表則會原樣顯示 `dgr`）。需與後端確認是否同步改用 `digirunner`、既有資料是否需要轉換。

## 實作紀錄

### 時間軸

1. 使用者提出需求：lb0013 的 `sofProxyAuthOptions`，`dgr` 的 value 改 `digirunner`、顯示 `digiRunner`。
2. `Grep sofProxyAuthOptions` 定位定義與使用處；再 `Grep sofProxyAuth` 全面盤點值的流向，確認改 value 對業務邏輯無影響、只有兩處列表會露出原始值。
3. `Grep optionLabel="label"` / `optionValue=` 查專案既有慣例，於 `lb0016.component.html` 找到參考寫法，決定沿用而非自創。
4. 修改 `sofProxyAuthOptions` 為物件陣列（同時更新該行上方的註解，原註解「label 與 value 相同」已不成立）。
5. 修改 `p-select`，補 `optionLabel` / `optionValue`。
6. 新增 `getSofProxyAuthLabel()` 並套用到查詢列表與匯入列表兩處 `<td>`，避免下拉與列表顯示不一致。
7. 執行 `npx ng build --configuration development`，建置成功（59.9s）。
8. 回報改動，並主動標註舊資料 `dgr` 在編輯畫面會顯示空白的相容性風險，提醒需與後端確認。
9. 使用者要求建立工作日誌，產出本文件。

**本次改動尚未 commit / push。**

### 經驗紀錄（給未來類似任務參考）

1. **「value 與顯示文字要不同」是把 `string[]` options 改成 `{label, value}[]` 的訊號**。本專案有兩種寫法並存：純字串陣列（label = value，如 lb0013 的另外兩組下拉）與物件陣列（如 lb0016）。只有在確實需要拆開時才改型別，不要順手把同檔案其他下拉一起改掉。
2. **改 option value 前，先 grep 該欄位名稱把值的流向盤一遍**。下拉值往往同時出現在條件判斷、送後端 payload、帶值回填、列表顯示等多處；本次就是靠這步發現兩處列表會直接印出原始值。
3. **`p-select` 用物件 options 時明確寫 `optionLabel` / `optionValue`**，不要依賴 PrimeNG 的預設推導 —— `getOptionValue` 未指定 `optionValue` 時的行為會受 `optionLabel` 是否設定牽動，明寫最安全。
4. **value → label 的轉換方法要保留未知值的 fallback**（`?? val`）。改變既有 value 等於讓資料庫裡的舊值變成「未知值」，直接顯示原值比顯示空白更容易在測試階段發現資料未轉換的問題。
5. **前端改動選項 value 時，務必同步確認後端／資料庫的相容性**並在回報中明講。前端 build 過不代表功能正確，這類「值本身變了」的改動風險在資料面而不在編譯期。
