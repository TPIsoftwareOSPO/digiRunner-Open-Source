# Angular 13→20 技術債整理 Phase 6 子任務：移除 jQuery — Sidebar Active State / 手風琴展開收合

延續六階段技術債整理計畫，接續 file-input、欄位 enable/disable、chart canvas 三個子任務，本次處理 Phase 6 的第四種 jQuery 模式：側邊導覽選單（sidebar）的 active 高亮與手風琴式展開/收合。

## 修改內容

### 範圍

- `src/app/layout/components/sidebar/sidebar.service.ts`
- `src/app/layout/components/sidebar/sidebar-template/sidebar-template.component.ts`／`.html`
- `src/styles/layout/_sidebar.scss`
- `src/app/layout/components/sidebar/sidebar.component.ts`／`.html`：**已確認不需修改**——其 `addExpandClass` 方法整個被註解掉、範本內對應的 `menu1`/`menu2`/`menu3` 範例區塊也整段包在 HTML 註解內，從未渲染（實際選單是透過 `*ngFor` 動態產生的 `<app-sidebar-template>`）。

### 發現零（部署後回報修正）：`activeId` 大小寫不一致，導致子選單點選後高亮消失

**使用者回報**：選單點選後，被 hover 或被點選到的項目顏色效果消失了。

**根因**：`sidebar-template.component.html` 子選單連結的高亮判斷式是 `[ngClass]="{'active' : sub.name.toLowerCase() === this.service.activeId}"`，要求 `service.activeId` 必須是小寫才能比對成功。`activeId` 有兩個賦值來源：
- 建構子內 `this.activeId = urlSegments[urlSegments.length - 1];`（來自路由網址片段，本來就是小寫，例如 `ac0101`）。
- `siderbarEventEmitter` 訂閱內 `if (id) this.activeId = id;`，其中 `id` 是點擊子選單時傳入的 `sub.name`（來自 `funcCode`，是大寫，例如 `AC0101`）——**這裡沒有轉小寫**。

點擊子選單後 `activeId` 被設成大寫的 `AC0101`，與範本 `sub.name.toLowerCase()`（小寫 `ac0101`）比對必然為 `false`，`.active` class 無法套用。這個大小寫不一致的 bug 原本就存在，但**被移除前的 jQuery `$(actived).addClass(this.ACTIVE)` 蓋過去了**——jQuery 是直接對「被點擊的那個 DOM 節點」加上 class，不經過字串比對，因此不受這個大小寫問題影響，視覺上一直是正常的。這次移除 jQuery、改用 `[ngClass]` 字串比對後，這個潛藏的既有 bug 才真正浮現。

**修正**：

```ts
// 修改前
if (id) this.activeId = id;
// 修改後
if (id) this.activeId = id.toLowerCase();
```

已確認 `activeId` 的另一個消費端（`sidebar-template.component.ts` 的 `this.menu.subs.map(s => s.name.toLowerCase()).includes(this.service.activeId)`）本來就假設 `activeId` 是小寫，修正後兩處消費端的假設一致。

### 發現一：`sidebar.service.ts` 的 active class jQuery 是純冗餘

```ts
// 修改前
this.siderbarEventEmitter.subscribe(({ menu, actived, id }) => {
    this.clear();
    this.actived = actived;
    if (id) this.activeId = id;
    $(actived).addClass(this.ACTIVE)
});
...
clear() {
    $(this.actived).removeClass(this.ACTIVE)
}
```

比對 `sidebar-template.component.html` 確認：主選單圖示的 active class 早就是 `[ngClass]="{'active': (_mainId == menu.main)}"`、子選單連結是 `[ngClass]="{'active' : sub.name.toLowerCase() === this.service.activeId}"`——兩者都已經是 Angular 響應式綁定，直接讀取 `activeId`／`_mainId`。這段 jQuery 操作的是同一批 DOM 元素，純粹是重複、且會跟 Angular 的 class 綁定互相打架的殘留程式碼。

**修正**：刪除 `$(actived).addClass(this.ACTIVE)` 呼叫與死欄位 `actived`、`ACTIVE`，只保留 `if (id) this.activeId = id;`（這才是真正驅動 `[ngClass]` 的關鍵狀態）。

### 發現二：`clear()` 並非死代碼，`header.component.ts` 仍在呼叫

移除 jQuery 內容後原打算直接刪除 `clear()` 方法，但 build 立即報錯：`header.component.ts:279` 的 `click($event)`（綁定在點擊 logo 導向 `/dashboard` 的連結上）呼叫 `this.siderbar.clear()`，意圖是「導回 dashboard 時重設側邊欄的 active／展開視覺狀態」。

**修正**：改寫 `clear()` 為新架構下對等的重設邏輯，而非刪除：

```ts
/**
 * 重設 sidebar 目前的 active／展開狀態（例如點擊 logo 返回 dashboard 時）
 */
clear() {
    this.activeId = '';
    this.setMenuMainId('');
}
```

### 發現三：手風琴展開/收合是真正需要重新設計的部分

```ts
// 修改前
addExpandClass(element: any, _menuMain: string = '') {
    let $li = $(element).children('li').first();
    if (this.firstPageLoad) {
      this.isActive ? $li.find('>ul').slideDown() : $li.find('>ul').slideUp();
    } else {
      $li.find('>ul').is(':hidden') ? $li.find('>ul').slideDown() : $li.find('>ul').slideUp();
      $li.closest('.menu-group').find('li.nested').not($li).find('>ul').slideUp();
      this.service.setMenuMainId(this.menu!.main);
    }
}
```

**根因**：`_sidebar.scss` 原本 `ul.submenu { display: block; ... }` 是寫死的預設值（恆為可見），完全沒有 Angular 狀態控制展開/收合，全靠 jQuery 用 inline style 動態覆蓋 `display` 並做滑動動畫；「一次只展開一個」的手風琴效果則是靠 jQuery `.closest().find().not()` 手動走訪 DOM、逐一收合其他區塊來達成。

**關鍵發現**：`_mainId`（透過 `SidebarService.setMenuMainId()`/`getMenuMainId()` 廣播、每個 `SidebarTemplateComponent` 實例各自訂閱同步）本來就已經是「目前應該展開的是哪個主選單」的正確共用狀態來源——因為它本來就是驅動主選單圖示 active 高亮的同一個值。因此只要把既有的 `_mainId === menu.main` 判斷式改綁到子選單的顯示/隱藏，就能免費獲得「一次只展開一個」的手風琴效果，完全不需要新增 service 狀態，也不需要用 jQuery 手動走訪、收合其他區塊。

**修正**：

```ts
// 修改後（sidebar-template.component.ts）
toggleExpand() {
  if (this._mainId === this.menu!.main) {
    this.service.setMenuMainId(''); // 再次點擊已展開的項目 → 收合（保留原本 toggle 行為）
  } else {
    this.service.setMenuMainId(this.menu!.main); // 展開這個、自動收合其他（沿用既有廣播機制）
  }
}
```

移除 `ngAfterViewInit()`（原本只是呼叫 `addExpandClass` 做初始展開判斷，現在展開狀態完全由 `_mainId === menu.main` 這個既有運算式衍生，不需要任何指令式 DOM 操作）與 `firstPageLoad` 欄位（僅 `addExpandClass` 用到）。

**修正過程中發現的既有邏輯缺陷**：`ngOnInit()` 內原本訂閱 `getMenuMainId()` 時寫的是 `if (_id) this._mainId = _id;`——這個 truthy 判斷會把 `setMenuMainId('')`（收合訊號）過濾掉，導致「收合」永遠無法透過廣播機制生效。因為新設計明確需要 `''` 代表「無展開項目」並正確傳播，已一併修正為 `this._mainId = _id;`（移除 truthy 過濾）。

```html
<!-- sidebar-template.component.html 修改前 -->
<a class="menu-group-item" (click)="addExpandClass(tsmp_menu,menu.main)" ...>
<ul class="submenu" *ngIf="menu.subs && menu.subs.length">

<!-- 修改後 -->
<a class="menu-group-item" (click)="toggleExpand()" ...>
<ul class="submenu" [class.active]="_mainId == menu.main" *ngIf="menu.subs && menu.subs.length">
```

### `_sidebar.scss`：`ul.submenu` 改用 `max-height` transition 取代 jQuery 滑動動畫

`display` 屬性不支援 CSS transition 動畫，因此改用 `max-height` + `overflow:hidden` 的常見替代技巧模擬展開/收合的滑動視覺效果：

```scss
// 修改前
ul.submenu {
  display: block;
  ...
}
.active {
  display: block !important;
}

// 修改後
ul.submenu {
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.3s ease;
  ...
  &.active {
    max-height: 1000px; // 足夠容納選單項目數量的上限值
  }
}
```

原本位於 `.nested-menu` 底下、與 `.nested`/`ul.submenu` 同層級的通用 `.active { display: block !important; }` 規則已移除，改為明確只針對 `ul.submenu.active` 定義，避免影響同一層級其他可能也用到 `.active` class 的元素。

## 驗證方式

### 靜態驗證

- `npm run build`（production）：**成功**，無新增錯誤（過程中曾因 `header.component.ts` 依賴 `clear()` 而出現一次編譯錯誤，已修正）。
- grep 確認 `src/app/layout/components/sidebar/` 目錄下已無殘留 `$(` 呼叫（僅剩 2 處與本次修改無關的既有死註解，維持原樣未動）。

### 尚待人工驗證

本環境無可用瀏覽器自動化工具，建議人工測試：
- 登入後檢查左側選單，確認初始載入時「目前所在頁面」對應的主選單自動展開，其餘收合。
- 點擊不同主選單項目，確認手風琴效果正確（展開新的、收合其他的），且有平滑過渡動畫。
- 點擊「已展開」的主選單項目本身，確認會收合（toggle 關閉）。
- 點擊 header 的 logo 回到 dashboard，確認側邊欄的 active 高亮與展開狀態正確重設為「無」。
- 切換頁面路由後，確認主選單/子選單的 active 高亮仍正確反映目前路由。

## 實作紀錄

### 時間軸

1. 使用者選擇 Phase 6 剩餘模式清單中的「Sidebar active state」繼續處理。發現此模式涉及 jQuery 滑動動畫與手風琴互動邏輯，複雜度明顯高於前三個子任務，改用 Plan 模式先研究再執行。
2. 研究過程中發現 `sidebar.service.ts` 的 active class jQuery 其實與範本既有的 `[ngClass]` 綁定完全重複，屬於純冗餘可直接刪除；而 `sidebar-template.component.ts` 的手風琴展開/收合才是真正需要重新設計的部分，且發現既有的 `_mainId` 共用狀態剛好可以直接拿來驅動展開/收合，不需要新增任何 service 狀態。
3. 曾嘗試詢問使用者「toggle 行為是否完整保留」「動畫效果是否保留」兩個問題，逾時未獲回應，依風險最小化原則採用「完整保留」的預設方案並於計畫中明確記錄。
4. 依計畫逐步實作：`sidebar.service.ts` 清理 → `sidebar-template.component.ts`/`.html` 改用宣告式綁定 → `_sidebar.scss` 改用 `max-height` transition。
5. 執行 `npm run build` 時發現 `header.component.ts` 仍呼叫 `SidebarService.clear()`（先前規劃時只搜尋了 `sidebar` 目錄內部，沒有搜尋全站呼叫端），導致編譯錯誤。追查後確認 `clear()` 有實際用途（點擊 header logo 返回 dashboard 時重設側邊欄狀態），改寫其實作內容（改用 `activeId`/`setMenuMainId` 重設）而非直接刪除。
6. 改寫 `toggleExpand()` 時發現 `ngOnInit()` 內既有的 `if (_id) this._mainId = _id;` 這行 truthy 判斷會擋掉「收合」（空字串）訊號的傳播，属于配合新設計必須一併修正的既有邏輯缺陷，已修正為無條件賦值。
7. `npm run build` 再次驗證通過，比照先前慣例清理驗證動作弄髒的已追蹤建置產物目錄。
8. **部署後使用者回報**：選單點選後，被 hover 或被點選到的項目顏色效果消失了。追查發現 `sidebar.service.ts` 內 `if (id) this.activeId = id;` 沒有轉小寫，與範本 `sub.name.toLowerCase() === this.service.activeId` 的比對假設不一致——這是被移除前的 jQuery `$(actived).addClass(this.ACTIVE)`（直接對 DOM 節點加 class，不經字串比對）長期蓋過去的既有 bug，拿掉 jQuery 後才浮現。修正為 `id.toLowerCase()`，再次 `npm run build` 驗證通過。

### 經驗紀錄（給未來類似任務參考）

- **搜尋呼叫端時，範圍要涵蓋全站，不能只搜尋「看起來相關」的目錄**：一開始只在 `sidebar/` 目錄內搜尋 `clear()`/`actived`/`ACTIVE` 的使用情況，沒有發現 `header.component.ts` 這個站在完全不同目錄、語意上不直覺（「點 logo 回首頁」跟「側邊欄狀態」關聯性不明顯）的呼叫端。這類共用 service 的方法，删除或大幅修改前務必用 grep 搜尋整個 `src/`，而不是憑目錄結構猜測呼叫範圍。
- **移除「看似冗餘」的程式碼前，要先驗證它是否真的冗餘，而不是預設它是**：一開始差點把整個 `clear()` 方法直接砍掉（因為它原本內容全是 jQuery），但它的「方法意圖」（重設 active 狀態）仍然成立，只是原本的「實作方式」用了 jQuery。正確做法是保留方法、只換掉實作方式，而不是連同呼叫端的需求一起消滅。
- **既有的共用 state（如 `_mainId`）可能已經隱含著你需要的另一種狀態語意**：這次最大的簡化來自於發現「驅動主選單高亮」跟「決定哪個手風琴展開」其實是同一個問題，同一個既有變數 `_mainId` 就能兩用，省下了大量原本設計中「額外走訪 DOM 收合其他區塊」的複雜邏輯。動手改寫前，值得多花時間釐清現有狀態變數的真正語意範圍，而不是急著加新的 flag。
- **jQuery 直接操作 DOM 節點，天生會蓋過字串/狀態比對邏輯裡的既有 bug**：這是本次 Phase 6 系列任務中第二次遇到「移除 jQuery 後才浮現既有 bug」的情況（另一次是 `lb0013.component.ts` 的重複 id 問題）。原因都類似：jQuery 版本是直接抓「當下這個 DOM 節點」操作，不經過任何字串比對或狀態推導；改成 Angular 宣告式綁定後，change detection 是依賴狀態值（字串、id、旗標）比對來決定要不要套用某個 class／樣式，任何既有的大小寫、型別、命名不一致問題都會被放大成「看得到的視覺 bug」。這提示未來做類似的 jQuery → Angular 綁定轉換時，遇到「表面上邏輯只是抄過去」的情況，也要花點時間交叉確認實際跑起來的字串/值是否真的如預期一致，不能只看程式碼結構對稱就假設行為對等。
