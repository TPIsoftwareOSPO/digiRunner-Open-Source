# Angular 元件撰寫規範

## Standalone vs NgModule

- 依 `AGENTS.md`，**新元件優先使用 standalone**（`standalone: true`），透過 `imports` 陣列引入所需模組（`CommonModule`、`PrimengModule`、`SharedModule` 等），並以 `loadComponent` 註冊路由。
- 既有的大量功能模組（`ac00`–`ac13`、`np01`–`np05`）仍是 `standalone: false` 搭配 `xxNNNN.module.ts`／`xxNNNN-routing.module.ts`。**修改既有 NgModule 元件時維持原本的 NgModule 寫法**，不要中途改成 standalone（會牽動整個模組結構與路由註冊方式）。
- 兩者的 `@Component` decorator 都固定包含 `selector`、`templateUrl`、`styleUrls`；standalone 額外加 `standalone: true` 與 `imports`，需要元件層級 service 時加 `providers`（例：`providers: [ConfirmationService, TokenExpiredGuard]`）。

```ts
// NgModule 元件（既有大宗寫法）
@Component({
  selector: 'app-ac0101',
  templateUrl: './ac0101.component.html',
  styleUrls: ['./ac0101.component.css'],
  standalone: false,
})

// Standalone 元件（新功能優先採用）
@Component({
  selector: 'app-lb0014',
  templateUrl: './lb0014.component.html',
  styleUrls: ['./lb0014.component.css'],
  providers: [ConfirmationService, TokenExpiredGuard],
  imports: [CommonModule, PrimengModule, SharedModule, SharedPipesModule, ReactiveFormsModule, FormsModule],
  standalone: true,
})
```

## Template 控制流程（`@if` / `@for` / `@switch`）

- **新元件與新增的 template 區塊一律使用 Angular 內建控制流程語法**（`@if`、`@for`、`@switch`），不要再寫 `*ngIf`／`*ngFor`／`*ngSwitch`。
- 修改既有元件時，若該檔已全面使用 `*ngIf` 等舊語法，只在**該次任務明確要求改寫**時才轉換；同一份 template 內避免新舊語法混用（新增區塊時比照該檔現況）。
- 原本 `<ng-container *ngIf="...">` 純粹作為條件包裹用途者，改寫時直接以 `@if (...) { ... }` 包住內容，**移除多餘的 `ng-container`**；原本掛在有意義元素（如 `<div class="...">`）上的 `*ngIf`，改寫時保留該元素、將條件移到外層 `@if`，不要改變 DOM 結構。
- `@for` 必須依 Angular 規定提供 `track`（例：`@for (item of list; track item.id) { ... }`；無穩定 key 時用 `track $index`）。
- 改用新語法後，若 template 已不再使用 `CommonModule` 提供的指令，仍要先確認是否還有 `[ngStyle]`、`[ngClass]`、pipe（`async`、`date` 等）等其他用途，**不要貿然移除 `CommonModule` import**。

```html
<!-- ✓ 條件顯示 -->
@if (baseUrlNotFound) {
  <small class="text-danger">{{ 'fhir_viewer.base_url_not_found' | translate }}</small>
}

<!-- ✓ 互斥條件用 @else if / @else，不要寫兩個獨立的相反條件 -->
@if (activeTab === 'authorization') {
  <div>...</div>
} @else {
  <div>...</div>
}

<!-- ❌ 新程式碼不要再用結構型指令 -->
<small *ngIf="baseUrlNotFound">...</small>
```

## 類別內部順序

依既有元件（`ac0101.component.ts`、`lb0014.component.ts`）觀察到的一致順序：

1. 欄位（狀態、表單物件、清單資料、旗標）
2. Constructor（依賴注入 + 表單初始化）
3. 生命週期方法（`ngOnInit`、`ngOnDestroy`）
4. 業務邏輯方法
5. 表單控制項 getter（放在檔案最後）

```ts
export class Ac0101Component extends BaseComponent implements OnInit {
  // ===== 欄位 =====
  form: UntypedFormGroup;
  funcInfoList: Array<AA0103List> = [];
  canUpdate: boolean = false;

  // ===== Constructor =====
  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private funcService: FuncService,
    private fb: UntypedFormBuilder,
    // ...其他注入
  ) {
    super(route, tr);
    this.form = this.fb.group({ /* ... */ });
  }

  // ===== 生命週期 =====
  ngOnInit() { /* ... */ }

  // ===== 業務方法 =====
  queryFuncList() { /* ... */ }
  updateFunc() { /* ... */ }

  // ===== 表單 getter（檔案最後）=====
  public get keyword() { return this.form.get('keyword'); }
}
```

- 依賴注入目前仍以**建構子注入**為主（既有程式碼慣例）；依 `AGENTS.md`，新元件可優先評估使用 `inject()`，但**修改既有元件時維持原本的建構子注入寫法**，不要在同一檔案混用兩種 DI 方式。

## `BaseComponent` 共用邏輯

`src/app/layout/base-component.ts` 提供功能模組共用的基礎邏輯：自動依路由 `data.id`／`:cusfunc` 參數取得頁面標題（`initTitle`）、`FormArray` 取值（`getControls`）、動態組表單驗證器（`addFormValidator`/`resetFormValidator`）。`layout/` 底下需要頁面標題或這類共用表單邏輯的元件，繼承 `BaseComponent` 並在 constructor 呼叫 `super(route, tr)`，不要重複實作相同邏輯。

```ts
export class Ac0101Component extends BaseComponent implements OnInit {
  constructor(route: ActivatedRoute, tr: TransformMenuNamePipe, /* ... */) {
    super(route, tr);
  }
}
```

## Reactive Forms

一律使用 Reactive Forms + `UntypedFormBuilder`（歷史遺留的 `Untyped*` 前綴，沿用即可，不需要改寫成強型別 `FormGroup<T>` 除非該次任務明確要求）。自訂驗證器統一從 `src/app/shared/validator-functions.ts`（`ValidatorFns.*`）引入：

```ts
this.form = this.fb.group({
  keyword: new UntypedFormControl(''),
  newFuncName: new UntypedFormControl('', ValidatorFns.stringSpaceAliasValidator(50)),
});
```

## API 呼叫與 Loading 狀態

本專案**沒有**自動化的 loading 攔截機制，呼叫 API 時需手動控制 `NgxUiLoaderService`（`ngxService`）的 `start()`/`stop()`：

```ts
exportTsmpFunc() {
  this.ngxService.start();
  this.serverService.exportTsmpFunc({ funcType: 'xxx' }).subscribe((res) => {
    // 處理結果
    this.ngxService.stop();
  });
}
```

呼叫回傳的 `ResHeader` 需透過 `ToolService.checkDpSuccess(res.ResHeader)`（或 `checkSuccess`）判斷是否成功，再決定後續流程；API 呼叫規則詳見 `.claude/RULES/api-pattern.md`。

## Toast（`MessageService`）通知

元件若注入 PrimeNG 的 `MessageService` 並呼叫 `this.messageService.add(...)` 顯示成功／失敗提示，**template 內必須同時放一個 `<p-toast>` 元件**，否則 `add()` 呼叫不會有任何畫面效果（`MessageService` 只是把訊息塞進共用佇列，需要 `<p-toast>` 訂閱該佇列才會渲染，且不會有編譯期錯誤提醒漏放）。

標準放法：緊接在 `</app-container>` 之後、作為同層級的 sibling 元素：

```html
</app-container>
<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>
```

`PrimengModule` 已 export `ToastModule`，只要元件 `imports`（或所屬 NgModule）已引入 `PrimengModule`，不需要再額外 import `ToastModule`。

`summary`/`detail` 文字組法沿用既有 dict 慣例：`summary` 用動作/情境對應的 i18n key，`detail` 為同一個 key 再接 `message.success`：

```ts
const code = ['export', 'message.success'];
const dict = await this.toolService.getDict(code);
this.messageService.add({
  severity: 'success',
  summary: dict['export'],
  detail: `${dict['export']} ${dict['message.success']}!`,
});
```

參考實作：`src/app/layout/labs/lb0014/lb0014.component.html`、`src/app/layout/labs/lb0017/lb0017.component.html`。

## RxJS Subscription 清理

較新的元件（如 `lb0014.component.ts`）使用 `Subject<void>` + `takeUntil` 管理需要在元件銷毀時取消的訂閱（例如輪詢）：

```ts
destroy$ = new Subject<void>();

ngOnInit() {
  timer(0, 1000).pipe(
    switchMap(() => this.serverService.dbSyncNodeInfo_ignore1298({})),
    takeUntil(this.destroy$),
  ).subscribe(/* ... */);
}

ngOnDestroy(): void {
  this.destroy$.next();
  this.destroy$.complete();
}
```

較舊的元件多半直接 `.subscribe()` 而未做清理，**新增輪詢或長生命週期訂閱時一律採用 `takeUntil` 模式**；一次性的請求（如按鈕觸發的查詢）沿用既有直接 `.subscribe()` 寫法即可，不必為每個 subscription 都加上清理機制。

## 命名慣例

- 業務方法：動詞開頭，直接描述動作，例：`queryFuncList()`、`updateFunc()`、`exportTsmpFunc()`
- 事件處理：無強制前綴慣例（`headerReturn()`、`openFileBrowser()`、`fileChange(event)` 皆有出現），依鄰近程式碼風格命名即可
- Boolean 旗標：目前**不一致**（`canUpdate`/`canDetail`/`canQuery` 等完整字詞，也有 `status`/`mask`/`btnStatus` 等縮寫），新增旗標時優先使用完整、語意清楚的命名（如 `canXxx`、`isXxx`、`xxxLoading`），不要為了統一而回頭修改既有欄位命名。

## 註解

依 `AGENTS.md`，複雜業務邏輯、演算法或新增功能必須有清晰簡潔的註解；程式碼中的說明性註解以中文為主（既有程式碼慣例），識別字、方法名維持英文。
