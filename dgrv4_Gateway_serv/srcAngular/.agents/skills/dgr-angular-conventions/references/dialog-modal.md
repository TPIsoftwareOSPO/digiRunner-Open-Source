# Dialog / Modal 元件規範

本專案有三種彈窗模式，依情境選用，不要每次都重新刻一套 `p-dialog` 結構。

## 模式一：PrimeNG `DialogService`（動態掛載獨立元件）

適合情境：需要開啟**獨立元件**、傳入資料、以非同步方式回傳結果（例如挑選清單、多步驟表單）。

### 開啟端（父元件）

```ts
// src/app/layout/ac02/ac0202/ac0202.component.ts
import { DialogService } from 'primeng/dynamicdialog';
import { ManagerGroupListComponent } from 'src/app/shared/manager-group-list/manager-group-list.component';

constructor(private dialogService: DialogService) {}

async addGroupToList() {
  const dict = await this.toolService.getDict(['button.manager_oauth_group']);
  const refDialog = this.dialogService.open(ManagerGroupListComponent, {
    header: dict['button.manager_oauth_group'],
    modal: true,
    width: '90vw',
    // data: { ... } // 需要傳入資料時使用
  });

  refDialog!.onClose.subscribe((res) => {
    if (res) {
      // 處理回傳結果
    }
  });
}
```

### 被開啟端（子元件）

```ts
// src/app/shared/manager-group-list/manager-group-list.component.ts
import { DynamicDialogRef } from 'primeng/dynamicdialog';

constructor(private ref: DynamicDialogRef) {}

chooseGroup() {
  this.ref.close(this.selectedGroups); // 回傳結果給開啟端的 onClose
}

cancelGroup() {
  this.ref.close(null);
}
```

- 需要接收開啟端傳入的資料時，注入 `DynamicDialogConfig`，透過 `this.config.data.xxx` 取得（見 `manager-group-list.component.ts` 中被註解的 `config` 用法）。
- 子元件必須在需要關閉時明確呼叫 `this.ref.close(結果)`，取消時傳 `null` 或不傳，由開啟端自行判斷 `if (res)`。
- 參考案例：`src/app/layout/ac02/ac0202/ac0202.component.ts`（開啟端）、`src/app/shared/manager-group-list/manager-group-list.component.ts`（被開啟端）。

## 模式二：`p-confirmDialog` + `ConfirmationService`（確認提示）

適合情境：刪除確認、動作前的簡單二次確認，不需要獨立元件與複雜表單。

### Template（放在頁面最外層，通常在檔案底部）

```html
<p-confirmDialog #cd [style]="{width: '30vw'}" icon="pi pi-exclamation-triangle">
  <ng-template pTemplate="footer">
    <button pButton type="button" label="Confirm" (click)="cd.onAccept()"></button>
    <button pButton type="button" label="Cancel" (click)="cd.onReject()"></button>
  </ng-template>
</p-confirmDialog>
```

### 呼叫端

```ts
async deleteClientData() {
  this.confirmationService.confirm({
    header: this.translate.instant('cfm_del_client'),
    message: `${this.translate.instant('client_id')}:${this.clientDetail!.clientID}`,
    accept: () => {
      this.onDeleteClientConfirm();
    },
  });
}
```

- `header`/`message` 一律透過 `this.translate.instant(...)` 取得多語系文字，不要在程式碼中硬編碼中文字串。
- `accept` callback 呼叫實際執行動作的方法，不要把業務邏輯直接寫在 `confirm({...})` 內。
- 參考案例：`src/app/layout/ac02/ac0202/ac0202.component.ts`（多處 `this.confirmationService.confirm(...)`）。

## 模式三：專案自訂 `app-dialog`（動態掛載，較舊）

`src/app/shared/dialog/dialog.component.ts` 是專案自建的輕量彈窗容器，透過 `ViewContainerRef.createComponent()` 動態掛載任意元件：

```ts
// 使用端
@ViewChild(DialogComponent) dialog!: DialogComponent;

openXxxDialog() {
  this.dialog.open(XxxComponent, { someData: 'value', afterCloseCallback: (result) => { ... } });
}
```

- 被掛載元件需自行接收 `data` 輸入屬性，並呼叫外部傳入的 `close` 方法（由 `dialog.component.ts` 綁定）關閉。
- 這是較早期的實作方式，**新功能優先使用模式一（PrimeNG `DialogService`）**，僅在維護既有使用此元件的畫面時才沿用此模式，不要在新畫面引入新的 `app-dialog` 用法。

## 選擇原則

| 情境 | 選擇 |
|------|------|
| 需要獨立元件、可能有表單、需回傳資料 | **模式一：`DialogService`** |
| 刪除確認、簡單二次確認 | **模式二：`p-confirmDialog`** |
| 維護既有使用 `app-dialog` 的畫面 | **模式三：沿用既有寫法，不新增新用法** |

## 常見錯誤

```ts
// ❌ 錯誤：在 confirm() 的 accept 內直接寫一大段業務邏輯
this.confirmationService.confirm({ accept: () => { /* 50 行邏輯 */ } });

// ✓ 正確：accept 只呼叫獨立方法
this.confirmationService.confirm({ accept: () => this.onDeleteClientConfirm() });

// ❌ 錯誤：DialogService 開啟的子元件忘記呼叫 ref.close()，導致 onClose 永不觸發
// ✓ 正確：確定/取消都要呼叫 this.ref.close(...)
```
