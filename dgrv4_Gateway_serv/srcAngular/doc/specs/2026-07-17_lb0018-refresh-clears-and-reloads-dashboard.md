# lb0018 Fhir Viewer：FHIR Base URL 手動刷新後，同步清空／重載 Patient Dashboard

延續 [`2026-07-16_lb0018-manual-refresh-buttons.md`](./2026-07-16_lb0018-manual-refresh-buttons.md)（FHIR Base URL 改為手動刷新、新增兩顆刷新鈕）。前案只讓刷新鈕負責「驗證 base URL 並帶出 well-known / token_endpoint」，未處理刷新後 Patient Dashboard 的資料同步，本次補上。

## 修改內容

### 問題描述

使用者依序提出兩個需求：

1. 如果手動刷新找不到資料，請移除原本在 Patient Dashboard 的資料。
2. FHIR Base URL 輸入框旁的手動刷新鈕按下去，**如果有查找到資料**，下面的 Patient Dashboard 不會更新。

兩者合起來即：`refreshFhirBaseUrl()` 不論驗證成功或失敗，都應該讓 Patient Dashboard 與新的 base URL 保持一致，而不是停留在前一個 base 查回的內容。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | 新增 `clearPatientDashboard()`；`refreshFhirBaseUrl()` 在驗證成功／失敗／base 清空三種情況下清空 dashboard，成功且停留在 Tab 2 時立即重查 |

本次未新增 i18n key、未調整 template。

### 詳細程式碼異動

#### 1. 新增 `clearPatientDashboard()`

既有的 `clearSelection()` 只清「已選病人 + `$everything` 衍生狀態」，不含病人清單本身，故另外包一層：

```ts
/**
 * 清空 Patient Dashboard 的所有資料（病人清單 + 已選病人的 $everything），
 * 回到「尚未載入」的初始狀態。base URL 取不到資料時呼叫，避免殘留前一個 base 的內容。
 */
private clearPatientDashboard(): void {
  this.patients = [];
  this.patientTotal = null;
  this.patientsFailed = false;
  this.patientsLoaded = false;
  this.patientsLoadingMore = false;
  this.hasMorePatients = false;
  this.patientOffset = 0;
  // 一併清掉上次查詢的 base，使下次切回本分頁時必定重新查詢
  this.lastPatientListBase = '';
  this.clearSelection();
}
```

`lastPatientListBase = ''` 是關鍵：`switchTab()` 以 `base !== lastPatientListBase` 決定是否重載，若不重設，使用者切回 Tab 2 時會因為 base 比對相同而沿用已被清空的畫面。

#### 2. `refreshFhirBaseUrl()` 同步 dashboard

修改前（前案版本）：成功只更新 well-known URL，完全沒碰 dashboard。

```ts
.subscribe((ok) => {
  this.baseUrlNotFound = !ok;
  this.wellKnownUrl.setValue(ok ? `${base}/.well-known/smart-configuration` : '');
});
```

修改後：

```ts
.subscribe((ok) => {
  this.baseUrlNotFound = !ok;
  this.wellKnownUrl.setValue(ok ? `${base}/.well-known/smart-configuration` : '');
  // 不論驗證成功與否，都先清掉 Patient Dashboard 既有內容，避免殘留前一個 base 查回的資料；
  // 清除時一併重設 lastPatientListBase，未停留在本分頁時也會在切回時以新 base 重查。
  this.clearPatientDashboard();
  // 驗證成功且正停留在 Patient Dashboard 時，立即以新的 base 重新查詢病人清單
  if (ok && this.activeTab === 'tab2') this.onSearchPatients();
});
```

base 被清空的提前返回分支也補上 `clearPatientDashboard()`：

```ts
if (!base) {
  this.baseUrlNotFound = false;
  this.wellKnownUrl.setValue('');
  this.clearPatientDashboard();
  return;
}
```

行為對照表：

| 情況 | Patient Dashboard 行為 |
|------|------------------------|
| 驗證成功，且停留在 Tab 2 | 清空後立即以新 base 重查（`onSearchPatients()`，沿用輸入框當下關鍵字） |
| 驗證成功，但停留在 Authorization 分頁 | 只清空；切回 Tab 2 時由 `switchTab()` 以新 base 查詢（不在使用者沒看的分頁先打 API） |
| 驗證失敗（找不到 base url） | 只清空，不查詢 |
| base URL 被清空後按刷新 | 只清空，不查詢 |

#### 3. 修正前一輪造成的註解錯位

前一輪插入 `clearPatientDashboard()` 時，插入點落在既有註解與 `clearSelection()` 之間，導致 `/** 清除已選病人與右側檢視狀態 */` 懸在新方法上方、`clearSelection()` 反而沒有註解。本次已把該註解移回 `clearSelection()` 正上方。

## 預計修改方式

### 需求 1：手動刷新找不到資料時，移除 Patient Dashboard 既有資料

**Context**

使用者在前案完成後提出，未指明是兩顆刷新鈕中的哪一顆。

**分析（已對照原始碼確認）**

- 讀 `clearSelection()`（`:571` 當時行號）確認它只重設 `selectedPatient`／`viewMode`／`everything*`／`encounters`／`tree`／`byKey`／`revealed`／`visibleRows`／`expandedCards`／`cardJsonCache`，**不含 `patients` 清單本身**。
- 檢查 Tab 2 刷新鈕路徑（`onRefreshPatients()` → `onSearchPatients()` → `loadPatientPage(0, false)`）：error 分支已有 `this.patients = []` + `clearSelection()`（`:455-459`）；成功但回 0 筆時 `patients` 也會被空清單取代並 `clearSelection()`。**此路徑已符合需求，不需修改**。
- 因此唯一缺口是 `refreshFhirBaseUrl()`：驗證失敗時只設 `baseUrlNotFound` 與清空 well-known，dashboard 仍留著前一個 base 的資料。`hasFhirBase` 只判斷字串非空、不判斷有效性，所以填入無效 base 時畫面仍會顯示舊資料。

**修正方案**

新增 `clearPatientDashboard()`，於 `refreshFhirBaseUrl()` 驗證失敗與 base 清空兩種情況呼叫。Tab 2 刷新鈕路徑維持原狀不動（避免改動本來就正確的程式碼）。

### 需求 2：驗證成功時 Patient Dashboard 不會更新

**Context**

使用者實際操作後回報：輸入框旁刷新鈕按下去、有查到資料時，下面的 Patient Dashboard 不會更新。

**分析**

- 這是需求 1 修法的對稱缺口：只處理了失敗路徑，成功路徑仍只更新 well-known / token_endpoint，dashboard 不受影響，於是停留在舊 base 的資料上。
- 觸發來源仍是使用者明確點擊刷新鈕（或選取 proxy 下拉），不違反前案「不要自動觸發（keydown 不打 API）」的要求。
- 需區分是否停留在 Tab 2：若使用者在 Authorization 分頁按刷新就立刻查病人清單，等於在使用者沒看的分頁先打一輪 API；靠 `lastPatientListBase = ''` 讓 `switchTab()` 在切回時查詢即可。

**修正方案**

成功路徑一律 `clearPatientDashboard()`，並在 `ok && activeTab === 'tab2'` 時呼叫 `onSearchPatients()` 立即重查。

## 驗證方式

### 靜態驗證

- `npx tsc --noEmit -p tsconfig.app.json` 執行通過，exit code 0（兩次改動後各執行一次，皆為 0）。
- 本次改用 `tsc --noEmit` 而非 `ng build`：前案記取的教訓是 `ng build` 會把產物寫入 `angular.json` 設定的 `outputPath`（`../src/main/resources/static/dgrv4/ac4/`，版控中的 production 產物），造成工作區污染。改用 `tsc --noEmit` 後，以 `git status --porcelain --untracked-files=all` 確認工作區只有預期的原始檔異動、無任何建置產物。
- 過程中 IDE 曾回報 `Property 'clearPatientDashboard' does not exist on type 'Lb0018Component'`，經 `tsc` 驗證無誤，判定為 Angular language service 過期快取（與前案同一現象）。
- 本次僅改動 TS、未動 template，故 `tsc --noEmit` 足以涵蓋；**若後續改到 template，需注意 `tsc --noEmit` 不做 Angular 模板型別檢查**，仍須以 `ng build` 驗證（並記得事後還原產物目錄）。

### 尚待人工驗證（建議）

- 啟動 `npm start`，開啟 lb0018 頁面：
  - 停留在 Tab 2、已有病人清單後，把 FHIR Base URL 改成另一個有效 base 並按刷新 → 確認清單以新 base 重查、已選病人與右側 `$everything` 內容一併換掉。
  - 停留在 Tab 2，把 base 改成無效值並按刷新 → 確認出現「找不到 fhir base url」，且下方病人清單／病人摘要／時間軸全部清空。
  - 停留在 Authorization 分頁按刷新（有效 base）→ 切到 Tab 2，確認會以新 base 查詢而非顯示舊資料。
  - 清空 base URL 後按刷新 → 確認 dashboard 清空、且顯示「請先輸入 base url」提示（`hasFhirBase` 為 false）。
  - 確認 `clearSelection()` 的註解位置修正後，程式行為無變化（純註解調整）。

### 已知未處理項目

- **驗證失敗且停留在 Tab 2 時，計數列會顯示「搜尋中」**：`clearPatientDashboard()` 將狀態重設為 `patientsLoaded = false`，而 template 的計數列在 `hasFhirBase && !patientsLoaded` 時顯示 `fhir_viewer.searching`。因使用者要求不自動觸發查詢，此狀態會維持到使用者手動按 Tab 2 的刷新鈕為止。已於回報時告知使用者並提供兩個備案（清空時改設為已載入 0 筆／把「搜尋中」改為僅在請求進行中顯示），待其決定。驗證成功的情況因會立即接一次查詢，不受影響。

## 實作紀錄

### 時間軸

1. **需求 1 提出**：使用者要求「手動刷新找不到資料時，移除原本在 Patient Dashboard 的資料」，未指明哪一顆刷新鈕。
2. **調查**：讀 `clearSelection()` 確認其範圍不含 `patients` 清單；檢查 Tab 2 刷新鈕路徑，發現 `loadPatientPage()` 的 error 分支與 0 筆結果都已會清空，**該路徑無需修改**；判定缺口在 `refreshFhirBaseUrl()` 的失敗路徑。
3. **實作**：新增 `clearPatientDashboard()`（含 `lastPatientListBase = ''` 重設），於驗證失敗與 base 清空時呼叫。
4. **驗證**：`npx tsc --noEmit -p tsconfig.app.json` 通過（exit 0）；`git status` 確認無建置產物污染（相對於前案的教訓）。
5. **回報時主動揭露**：說明 Tab 2 刷新鈕路徑本來就正確所以沒動，並主動指出「驗證失敗且停在 Tab 2 時計數列會顯示搜尋中」這個自己引入的顯示副作用，提供兩個備案。
6. **需求 2 提出（點出對稱缺口）**：使用者實測後回報「有查到資料時 Patient Dashboard 不會更新」——即需求 1 的修法只顧了失敗路徑，成功路徑同樣需要同步。
7. **實作**：成功路徑一律 `clearPatientDashboard()`，並在 `ok && activeTab === 'tab2'` 時 `onSearchPatients()` 立即重查；停在其他分頁時靠 `lastPatientListBase = ''` 於切回時重查，避免在使用者沒看的分頁先打 API。
8. **驗證**：`tsc --noEmit` 再次通過（exit 0）。
9. **建立本紀錄檔時發現自己的疏失**：撰寫本文件前跑 `git diff` 檢視完整異動，發現第 3 步插入 `clearPatientDashboard()` 的位置落在既有註解與 `clearSelection()` 之間，把 `/** 清除已選病人與右側檢視狀態 */` 擠成懸在新方法上方的孤兒註解。已將該註解移回 `clearSelection()` 正上方，並重跑 `tsc --noEmit`（exit 0）。
10. **同一次 `git diff` 中發現非本人的改動**：`bypassHeaders()` 內的 `Authorization: \`Bearer ${this.toolService.getToken()}\`` 被註解掉，此改動**不是本次工作所為**，目前存在於工作區但尚未提交。已保留原狀未還原，並於回報時告知使用者確認去留。
11. **本次工作尚未 commit／push**，待使用者指示。

### 經驗紀錄（給未來類似任務參考）

- **修「找不到資料要清空」這類需求時，記得同時想對稱情境**。本次需求 1 只處理失敗路徑，成功路徑（換了一個有效 base）同樣會殘留舊資料，結果要使用者實測後回報才補上。凡是「狀態 A 要清掉舊資料」的需求，都該一併確認「狀態 B（成功／有資料）是不是也要換掉舊資料」。
- **既有的 clear/reset 方法不一定涵蓋你以為的範圍**。`clearSelection()` 名稱看起來像「清空選取狀態」，實際上不含病人清單；直接沿用會清不乾淨。動手前先讀完該方法實際重設了哪些欄位。
- **重設「上次查詢參數」的快取欄位容易被漏掉**。本次若沒把 `lastPatientListBase` 一併清空，`switchTab()` 會因為 base 比對相同而不重載，使用者切回分頁只會看到空畫面。凡是有「與上次相同就跳過」的最佳化，清空狀態時都要連那個比較基準一起重設。
- **插入新方法時注意別把既有註解擠成孤兒**。以字串比對做編輯時，錨點若選在「註解 + 方法簽章」的交界，很容易把註解留在上一個位置。插入後應檢查 diff 而非只看編譯結果——編譯器不會抓這種問題。
- **`tsc --noEmit -p tsconfig.app.json` 是本專案驗證純 TS 改動的低副作用選項**，不會像 `ng build` 那樣把產物寫進版控中的 `../src/main/resources/static/dgrv4/ac4/`。但它不做 Angular 模板型別檢查，改到 template 時仍需 `ng build`。
- **寫 spec 前先跑一次 `git diff` 通盤檢視**，除了確保紀錄如實，也能抓出自己中途留下的問題（本次的孤兒註解）與工作區裡他人／使用者的未提交改動（本次的 `Authorization` 註解），避免不小心一起提交或誤植為自己的成果。
