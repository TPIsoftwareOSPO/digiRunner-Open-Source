# Angular 13→20 技術債整理 Phase 6：移除全站最後剩餘的 jQuery 用法（其他 toggling/visibility 模式）

延續六階段技術債整理計畫。前一份報告（`2026-07-03_phase6-jquery-required-label-removal.md`）結尾記錄了「全站仍有少量未分類的 jQuery 用法」，本次針對這些剩餘用法逐一處理，並對全站做最終確認掃描。

## 修改內容

### 過程更正：先前的檔案盤點有誤判

上一輪回報「剩餘 jQuery 檔案」時，因為平行執行多個 grep 查詢、比對輸出時發生錯位，誤將 `ac1202.component.ts`／`ac00/ac0015.component.ts` 列為含有 `error_messages`／`switchErrorMessage` 的即時通報。逐一重新核對後更正：這兩個檔案的 `$(` 命中其實都是 `openDialog$(...)`（以 `$` 結尾的 RxJS 慣例方法名稱）誤判，並非 jQuery；真正含有 `switchErrorMessage`／`error_messages` 這個模式的是 `np02/ca-stat/ca-stat.component.ts`，而全站掃描時還額外發現 `np02/np0202/np0202.component.ts` 也有**完全相同的獨立重複實作**（先前的清單漏掉了）。

### 修正一：`ca-stat.component.ts`／`np0202.component.ts` —— 錯誤訊息顯示切換

兩個檔案都已經有 `@ViewChild('error_messages') error_messages!: ElementRef` 宣告，只是切換 class 用 jQuery：

```ts
// 修改前
switchErrorMessage(evt) {
    $(this.error_messages.nativeElement).toggleClass('hide');
}

// 修改後
switchErrorMessage(evt) {
    this.error_messages.nativeElement.classList.toggle('hide');
}
```

原生 `classList.toggle()` 與 jQuery 的 `.toggleClass()` 對單一 class 的行為完全等價，直接替換即可，不需新增任何狀態或改動範本。

### 修正二：`np05/np0511/np0511.component.ts` —— 圖示 icon 依 id 切換 `src`

```ts
// 修改前
$("#keepFlag_" + index).attr("src", flagSrc);
$("#archiveFlag_" + index).attr("src", flagSrc);

// 修改後
document.getElementById("keepFlag_" + index)?.setAttribute("src", flagSrc);
document.getElementById("archiveFlag_" + index)?.setAttribute("src", flagSrc);
```

範本內對應的是普通 `<img id="keepFlag_{{i}}">`／`<img id="archiveFlag_{{i}}">`，用 `document.getElementById` 逐行直譯取代，行為完全相同。

### 修正三：`np01/np0115/np0115.component.ts` —— 網站地圖（sitemap）樹狀結構的展開/收合與行內編輯

此檔案使用第三方套件 `ngx-nestable`（拖曳排序樹狀元件），程式碼透過 jQuery 操作**該套件內部渲染、非本專案範本直接控制**的 DOM 結構（`.collapseBtn`），以及自行實作的「點擊顯示為輸入框」行內編輯效果（`#span_{id}`／`#input_{id}`）：

```ts
// 修改前（plus()：新增節點後，自動展開父節點並進入新節點的編輯狀態）
setTimeout(() => {
    const res = $(itemTemplate).find('.collapseBtn > i').hasClass('fa-angle-down');
    setTimeout(() => {
        if (res) $(itemTemplate).find('.collapseBtn').trigger('click')
        $('#span_' + (maxId + 1)).trigger('click');
    });
});

// 修改後
setTimeout(() => {
    const res = (itemTemplate as HTMLElement)?.querySelector('.collapseBtn > i')?.classList.contains('fa-angle-down');
    setTimeout(() => {
        if (res) (itemTemplate as HTMLElement)?.querySelector<HTMLElement>('.collapseBtn')?.click();
        document.getElementById('span_' + (maxId + 1))?.click();
    });
});
```

```ts
// 修改前（onClickHandler／onBlurHandler：切換顯示文字 span 與可編輯 input）
onClickHandler(row) {
    if (!this.allDisabled) {
        row.item.focus = true;
        this.allDisabled = true;
        $('#span_' + row.item.id).hide();
        $('#input_' + row.item.id).addClass('d-flex').removeClass('d-none').find('input').first().focus().select();
    }
}
onBlurHandler(row) {
    delete row.item.focus;
    this.allDisabled = false;
    $('#span_' + row.item.id).show();
    $('#input_' + row.item.id).removeClass('d-flex').addClass('d-none');
}

// 修改後
onClickHandler(row) {
    if (!this.allDisabled) {
        row.item.focus = true;
        this.allDisabled = true;
        const span = document.getElementById('span_' + row.item.id);
        if (span) span.style.display = 'none';
        const inputWrapper = document.getElementById('input_' + row.item.id);
        if (inputWrapper) {
            inputWrapper.classList.add('d-flex');
            inputWrapper.classList.remove('d-none');
            const firstInput = inputWrapper.querySelector('input');
            firstInput?.focus();
            firstInput?.select();
        }
    }
}
onBlurHandler(row) {
    delete row.item.focus;
    this.allDisabled = false;
    const span = document.getElementById('span_' + row.item.id);
    if (span) span.style.display = '';
    const inputWrapper = document.getElementById('input_' + row.item.id);
    if (inputWrapper) {
        inputWrapper.classList.remove('d-flex');
        inputWrapper.classList.add('d-none');
    }
}
```

**關鍵對應說明**：
- jQuery `.trigger('click')` 對真實 DOM 元素觸發的是一個會冒泡、能被原生事件監聽器與 Angular `(click)` 綁定同時接收到的合成點擊事件；原生 `HTMLElement.click()` 對按鈕元素效果等價，因此可以直接替換，不需要手動建構 `MouseEvent` 再 `dispatchEvent`。
- jQuery `.hide()`/`.show()` 對沒有既有 class 開關機制的元素，實際上就是設定/清除 inline `display:none`；原生對應為 `element.style.display = 'none'` / `element.style.display = ''`（清空 inline 樣式、還原成 CSS 預設值，與 jQuery `.show()` 行為一致）。
- jQuery `.addClass('d-flex').removeClass('d-none')` 是純 class 開關，`classList.add`/`classList.remove` 逐行對應。
- jQuery `.find('input').first().focus().select()` 對應原生 `wrapper.querySelector('input')`（`querySelector` 本來就回傳文件順序中第一個符合的元素，等價於 `.find().first()`），再呼叫 `.focus()`/`.select()`。

## 驗證方式

### 靜態驗證

- `npm run build`（production）：**成功**，無新增錯誤（`np0202.component.ts` 是後續全站複查時才發現的第 4 個檔案，修正後另外驗證一次同樣成功）。
- 對全站 `src/app` 執行 `$(` 全文掃描，確認**已無任何真實 jQuery 呼叫殘留**。剩餘命中全數為：
  - 已知的既有死代碼／註解（`sidebar.component.ts`、`sidebar-template.component.ts`、`ac0501.component.ts`、`ac0002.component.ts`、`host-input.component.ts`、`lb0013.component.ts` 內的說明性註解），維持原樣不動。
  - `openDialog$(...)` 這種以 `$` 結尾的 RxJS 慣例方法命名（`ac0103`、`ac0015`、`ac0012`、`np0511`、`ac1202`、`ac1002`、`np0401` 等檔案），非 jQuery，不需處理。

### 尚待人工驗證

本環境無可用瀏覽器自動化工具，建議人工測試：
- `np0511`（事件記錄列表）：點擊「保留」／「歸檔」圖示，確認圖示正確切換。
- `ca-stat`／`np0202`：點擊錯誤訊息圖示（🐞），確認錯誤訊息區塊正確顯示/隱藏。
- `np0115`（網站地圖管理）：
  - 新增節點（點擊「+」按鈕），確認父節點自動展開、新節點自動進入「行內編輯」狀態並選取文字，可直接輸入名稱。
  - 點擊既有節點的文字進入編輯狀態，確認顯示文字消失、輸入框出現並自動取得焦點與全選文字；按 Enter 確認、按 Esc 取消、失焦時正確收合回顯示文字。
- `ac0301`（API 管理）：觸發原本呼叫 `queryGroupApiList_ajax` 的流程（依 `keyword`/`moduleName`/`apiKey` 查詢 API 群組清單的情境），確認清單正確載入、頁面捲動行為（`document.querySelector('section')?.scrollTo(0, 0)`）與錯誤訊息提示（`rtnCode != '1298'` 時彈窗）皆正常。
- `ac0311`（匯入 OpenAPI）：透過「URL」方式匯入 OpenAPI 文件（而非直接上傳檔案），確認能正確抓取遠端 JSON 文件並完成後續上傳流程。

## 追加修正：`$.ajax()`／`$.get()` 兩處靜態方法呼叫（先前所有掃描皆遺漏）

在完成上述 4 個檔案並準備移除 jQuery 全域依賴時，執行 `npm run build` 意外發現另外 2 處真實 jQuery 呼叫。**先前 Phase 6 全程使用的掃描關鍵字都是 `\$\(`（要求 `$` 後面緊接左括號），完全沒有涵蓋 `$.ajax(...)`／`$.get(...)` 這種「jQuery 靜態方法」呼叫形式**（`$` 與 `(` 之間隔了 `.ajax`／`.get`），因此這兩處在今天所有輪次的掃描中都被遺漏，直到移除 `tsconfig` 的 `"jquery"` types 設定、讓型別檢查真正生效後，才透過編譯錯誤浮現。

### `src/app/shared/services/api-api.service.ts`：`queryGroupApiList_ajax()` —— 重複造輪子且繞過 `ApiBaseService`

```ts
// 修改前：手動組 SignCode、Authorization，直接用 $.ajax() 呼叫後端
queryGroupApiList_ajax(ReqBody: AA0320Req) {
    ...
    let signCode = shajs('sha256').update(prefix + bodyJosn).digest('hex');
    return new Promise<ResAA0320>((resolve, reject) => {
      $.ajax({ url: path, type: 'POST', ..., headers: { SignCode: signCode, Authorization: `Bearer ${token}` } })
        .done((res) => resolve(res))
        .fail((res) => reject(res));
    });
}
```

這個方法呼叫的後端交易碼（`AA0320`）與同檔案內**緊鄰在上面**的 `queryGroupApiList()` 完全相同，差別只在於 `queryGroupApiList()` 是透過 `ApiBaseService.npPost()`（正確做法，簽章/Token 由 `ApiBaseService` 統一處理），而 `_ajax` 版本手動重新實作了一次簽章計算與 header 組裝，直接用 jQuery 發送——明顯違反 `.claude/RULES/api-pattern.md`「所有 HTTP 請求必須透過 `ApiBaseService`」的規則，屬於歷史上重複造輪子的殘留程式碼。

全站 grep 確認 `queryGroupApiList_ajax` 只有唯一一處呼叫端（`ac0301.component.ts:2034`），且該呼叫端的後續處理邏輯（`checkDpSuccess`／`RespBody.dataList`）與另外 3 處呼叫 `queryGroupApiList(...).subscribe(...)` 的既有寫法完全一致。**修正方式不是把 jQuery 換成原生 fetch/XHR，而是直接改呼叫既有的正確方法**：

```ts
// 修改前
this.apiService.queryGroupApiList_ajax(ReqBody).then((res) => { ... });
// 修改後
this.apiService.queryGroupApiList(ReqBody).subscribe((res) => { ... });
```

`queryGroupApiList_ajax()` 方法本體隨即刪除（連同其專用的 `shajs` import，確認全檔案已無其他地方使用後一併移除；`environment` import 仍被同檔案其他方法使用，予以保留）。

### `src/app/layout/ac03/ac0311/ac0311.component.ts`：`getUploadFile()` —— 抓取外部 URL 的 JSON 檔案

```ts
// 修改前
getUploadFile(url) {
    return new Promise<File>((resolve, reject) => {
      $.get(url, (data) => {
        let filename = this.getFilename(url);
        let jsonse = JSON.stringify(data);
        let blob = new Blob([jsonse], { type: 'application/json' });
        let file = new File([blob], filename);
        resolve(file);
      });
    });
}

// 修改後
getUploadFile(url) {
    return fetch(url)
      .then((res) => res.json())
      .then((data) => {
        let filename = this.getFilename(url);
        let jsonse = JSON.stringify(data);
        let blob = new Blob([jsonse], { type: 'application/json' });
        return new File([blob], filename);
      });
}
```

此處是抓取**使用者提供的外部 OpenAPI/Swagger 文件 URL**（非本專案自己簽章協定的後端 API），因此不透過 `ApiBaseService`，改用瀏覽器原生 `fetch()` API 是最直接對應的寫法；`fetch(url).then(res => res.json())` 與 jQuery `$.get()` 預設依 response content-type 自動解析 JSON 的行為一致。

## 移除 jQuery 全域依賴與設定

確認全站已無任何真實 jQuery 呼叫殘留後，一併移除相關依賴與設定：

- `angular.json`：`architect.build.options.scripts` 移除 `"node_modules/jquery/dist/jquery.min.js"`。
- `package.json`：移除 `dependencies` 的 `jquery`、`dependencies`/`devDependencies` 內重複出現兩次的 `@types/jquery`，並執行 `npm install` 同步 `package-lock.json`（移除 3 個套件）。
- `tsconfig.app.json`：`types` 由 `["jquery"]` 還原為 `[]`（Phase 1 為了修復當時既有的 build 錯誤而暫時加上，見 `2026-07-03_phase1-tech-debt-cleanup.md`）。
- `tsconfig.spec.json`：`types` 由 `["jasmine", "jquery"]` 還原為 `["jasmine"]`。

`npm run build` 於移除依賴、重新 `npm install` 後仍成功通過，證實全站真的已無任何程式碼依賴 jQuery。

## Phase 6 總結

至此，全站 jQuery 的**實際程式碼呼叫已全數移除**，涵蓋七種模式：file-input 觸發/清空、欄位 enable/disable、chart canvas 重建、sidebar active state、required-label 標記、其他 toggling/visibility（錯誤訊息切換／圖示切換／樹狀結構操作）、以及本次追加發現的 AJAX 靜態方法呼叫。`angular.json` 的全域 script、`package.json` 的 `jquery`／`@types/jquery` 依賴、`tsconfig` 的 `jquery` types 皆已一併移除，這個專案的程式碼與建置設定**已完全不依賴 jQuery**。

## 實作紀錄

### 時間軸

1. 使用者詢問「現在系統內還有 jQuery 的使用嗎」，進行全站 `$(` 掃描盤點，回報 4 個疑似仍有真實 jQuery 用法的檔案（`np0511`、`ac1202`、`ac0015`、`np0115`）。
2. 使用者要求繼續移除，逐檔重新核對時發現：先前平行查詢的輸出在對應檔名時發生錯位，`ac1202`／`ac0015` 其實是 `openDialog$(` 誤判，真正的 `error_messages` 切換模式屬於 `ca-stat.component.ts`（先前清單漏列）。已就此更正並向使用者說明。
3. 依序修正 `np0511`（簡單 id 屬性替換）、`ca-stat`（既有 ViewChild，直接替換 API）、`np0115`（較複雜，涉及第三方 `ngx-nestable` 套件的內部 DOM 結構與行內編輯顯示切換，逐一比對 jQuery 各方法的原生對應寫法後替換）。
4. `npm run build` 驗證通過後，對全站 `src/app` 做最終複查掃描，意外發現 `np0202.component.ts` 也有跟 `ca-stat.component.ts` 完全相同的獨立重複實作（先前兩輪清單都未列出，屬於本次複查才發現的遺漏），一併修正。
5. 再次 `npm run build` 驗證通過，並對全站做最後一次確認掃描，確認所有殘留命中皆為死代碼註解或 `openDialog$` 命名誤判，無真實 jQuery 呼叫殘留。
6. 使用者確認要一併移除 jQuery 全域依賴，開始移除 `angular.json` 的 script、`package.json` 的 `jquery`/`@types/jquery`、`tsconfig.app.json`/`tsconfig.spec.json` 的 `jquery` types。
7. 移除 `tsconfig` 的 `jquery` types 並重跑 `npm install` + `npm run build` 後，型別檢查真正對 `$` 生效，才發現先前所有輪次的 `\$\(` 掃描完全遺漏了 `$.ajax(...)`／`$.get(...)` 這種 jQuery 靜態方法呼叫形式（`api-api.service.ts`、`ac0311.component.ts`）。
8. 追查 `api-api.service.ts` 的 `queryGroupApiList_ajax()`，發現它與同檔案內既有的 `queryGroupApiList()`（透過 `ApiBaseService` 正確實作）呼叫同一個後端交易碼，屬於重複造輪子且繞過簽章機制的歷史殘留；確認唯一呼叫端（`ac0301.component.ts`）的後續處理邏輯與正確版本一致後，直接改呼叫既有方法並刪除重複實作，而非把 jQuery 逐行翻譯成 fetch/XHR。
9. `ac0311.component.ts` 的 `getUploadFile()` 是抓取外部 URL（非本專案簽章協定的後端 API），改用原生 `fetch()` 直譯即可。
10. 最終 `npm run build` 驗證通過，確認建置設定與程式碼均已完全不依賴 jQuery。

### 經驗紀錄（給未來類似任務參考）

- **多個平行 grep 查詢的輸出容易在腦內對應錯檔名**：當一次發出多個平行搜尋、每個都回傳「檔名 + 前後文」時，若沒有非常小心核對輸出順序與請求順序是否一致，很容易把 A 檔案的內容誤記成 B 檔案的（這次就把 `ca-stat.component.ts` 的內容誤記成 `ac1202.component.ts`）。之後遇到需要精確比對「哪個檔案有什麼內容」的情境，寧可拆成一個個獨立查詢逐一確認，或至少在回報前針對關鍵發現重新單獨驗證一次，不要只信任腦內對平行查詢結果的排列組合。
- **同一個視覺/行為模式，在全站可能有超過一處獨立重複實作**：這是本次 Phase 6 系列第三次遇到「修完一個地方後，全站複查才發現還有其他檔案獨立複製了同一段邏輯」的情況（先前是 `key-value-grid`/`key-value` 的 enable/disable、`base-component.ts` 之外的 required-label 重複實作）。每次「移除某個 jQuery 模式」的任務，除了修目標檔案，都應該額外用「這個 jQuery 呼叫的具體特徵字串」（例如這次的 `error_messages`／`toggleClass('hide')`）反向搜尋全站一次，才能確保真的移除乾淨，而不是修完第一個發現的案例就結案。
- **第三方元件（如 `ngx-nestable`）內部的 DOM 結構，用原生 `querySelector`/`.click()` 取代 jQuery 一樣可行**：不需要因為操作對象是第三方套件渲染的內部結構，就假設一定要繼續依賴 jQuery 或改用更複雜的方案——只要目標選擇器與觸發事件的語意單純（class 名稱比對、模擬點擊），原生 DOM API 就足以取代，不需要額外抽象層。
- **搜尋關鍵字要涵蓋目標 API 的「所有呼叫形式」，不能只涵蓋最常見的那一種**：這次全程用來掃描 jQuery 殘留的正則 `\$\(` 只涵蓋「`$` 後面緊接左括號」的選擇器呼叫形式（`$('#id')`、`$(el)`），完全沒涵蓋 `$.ajax(...)`／`$.get(...)` 這種「`$` 物件本身的靜態方法」呼叫形式。這兩處因此在今天所有輪次的掃描中都被遺漏，直到移除型別宣告、讓 TypeScript 編譯器強制報錯才浮現。之後要「確認某個全域符號/API 完全沒被使用」時，應該同時搜尋該符號的多種呼叫形態（成員存取 `$.xxx`、函式呼叫 `$(...)`、單純參考 `$`），或乾脆用「移除型別宣告後跑一次完整編譯」當作最終、最可靠的確認手段，而不是只依賴文字模式比對。
- **移除疑似重複的程式碼前，先確認它是否真的是「重複」，並找出正確版本再合併**：`queryGroupApiList_ajax()` 表面上只是「用 jQuery 實作的 API 呼叫」，但深入比對後發現它其實是繞過 `ApiBaseService`、手動重算簽章的重複實作，且旁邊就有正確版本存在。這種情況下，正確的移除方式不是逐行把 jQuery／XHR 部分翻譯成原生寫法，而是先確認「是否已經有做同一件事、寫法正確的既有方法」，有的話直接合併呼叫端、刪除重複實作，而不是保留兩份邏輯只是把底層 API 換掉。
