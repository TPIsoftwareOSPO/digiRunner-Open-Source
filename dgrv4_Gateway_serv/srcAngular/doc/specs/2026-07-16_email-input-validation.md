# 統一專案 Email 輸入格式驗證

## 修改內容

### 問題描述

使用者指出 `ac0016` 的 User Email 欄位沒有檢查輸入內容，導致中文或不符合 email 格式的文字仍可送出，並要求：

1. 先盤點專案內相關 email 輸入欄位並提出 plan。
2. 依確認後的 plan 實作共用驗證與各頁面欄位檢核。
3. 建立本次工作日誌，在 `npm run build` 成功後提交並上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/shared/validator-functions.ts` | 修正單一 email validator，新增逗號分隔多 email validator |
| `src/app/shared/validator-functions.spec.ts` | 新增單一與多組 email 的 5 個 Jasmine 測試案例 |
| `src/app/layout/ac00/ac0016/ac0016.component.ts` | 為新增／修改 IdP User email 套用共用 validator，並在動態 constraints 後補回 validator |
| `src/app/layout/ac00/ac0016/ac0016.component.html` | 改用 `type="email"` 並顯示 `validation.mailFormat` |
| `src/app/layout/ac00/ac0002/ac0002.component.ts` | 為使用者建立／修改 email 套用共用 validator |
| `src/app/layout/ac00/ac0002/ac0002.component.html` | 新增 email 格式錯誤訊息 |
| `src/app/layout/ac00/ac0006/ac0006.component.ts` | 為個人資料 email 套用共用 validator |
| `src/app/layout/ac00/ac0006/ac0006.component.html` | 改用 `type="email"` 並新增格式錯誤訊息 |
| `src/app/layout/login/login.component.ts` | 為忘記密碼 email 套用共用 validator |
| `src/app/layout/login/login.component.html` | 改用 `type="email"` 並新增格式錯誤訊息 |
| `src/app/layout/ac02/ac0202/ac0202.component.ts` | 為 Client 的 `emails`／`newEmails` 套用多 email validator |
| `src/app/layout/ac02/ac0202/ac0202.component.html` | 新增多 email 格式錯誤訊息 |
| `src/app/layout/ac00/ac0018/*` | 為 LDAP Approval Result Mail 套用多 email validator 與錯誤訊息 |
| `src/app/layout/ac00/ac0019/*` | 為 MLDAP Approval Result Mail 套用多 email validator 與錯誤訊息 |
| `src/app/layout/ac00/ac0020/*` | 為 API Approval Result Mail 套用多 email validator 與錯誤訊息 |

### 詳細程式碼異動

#### 1. 修正單一 email validator

修改前的 regex 沒有 `^` 開頭錨點，且 TLD 只允許 2–3 字元：

```ts
const regex = new RegExp(/[a-zA-Z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/g);
```

修改後要求整個輸入都是 ASCII email，domain 至少包含一個 `.`，並不再限制 TLD 最多 3 字元：

```ts
const regex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$/;
```

空字串、`null`、`undefined` 仍回傳 `null`，是否必填交由既有 required validator 處理。

#### 2. 新增多組 email 驗證

```ts
export function multipleMailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string | null | undefined;
    if (value === '' || value === null || value === undefined) return null;

    const mails = value.split(',').map((mail) => mail.trim());
    return mails.length > 0 && mails.every(isValidMail)
      ? null
      : { mailFormat: 'validation.mailFormat' };
  };
}
```

允許逗號兩側有空白，但任何一段不合法或為空段落時，整個欄位回傳 `mailFormat`。

#### 3. 避免後端動態 constraints 覆蓋 email validator

`BaseComponent.addFormValidator()` 會對 constraint 中的欄位執行 `setValidators()`，因此只在 form 初始化時設定 email validator 並不足夠。相關頁面在呼叫 `addFormValidator()` 後補上：

```ts
this.userEmail.addValidators(ValidatorFns.mailValidator());
this.userEmail.updateValueAndValidity();
```

多 email 欄位則補 `multipleMailValidator()`。這樣保留後端的 required、maxLength、pattern 等 constraints，同時確保 email 格式驗證不被覆蓋。

#### 4. 顯示既有多語系錯誤訊息

四份語系檔原本都已有 `validation.mailFormat`，因此不新增 i18n key：

```html
<small *ngIf="userEmail.errors?.mailFormat" class="form-text">
  {{userEmail.errors?.mailFormat | translate}}
</small>
```

#### 5. 驗證 Approval Result Mail 的多收件人語意

後端 `AcIdPHelper.getApprovalResultMails()` 會將 `approvalResultMail` 原值交給 `TsmpMailEventBuilder.setRecipients()`；`MailHelper` 最終使用：

```java
InternetAddress[] recipients = InternetAddress.parse(event.getRecipients());
```

`InternetAddress.parse()` 支援逗號分隔收件者，因此 `ac0018`、`ac0019`、`ac0020` 採用多 email validator，而不是只允許單一地址。

## 預計修改方式

### 需求 1：盤點缺少 email 格式驗證的欄位

**Context** — 使用者以 `ac0016` User Email 為例，反映中文或錯誤格式可以輸入並要求先列 plan。

**分析** — 搜尋 `src/app` 內含 `email`／`mail` 的 form control、template 與 validator，確認專案已有 `ValidatorFns.mailValidator()`，且 `ac1002` 的 `conMail` 是唯一明確使用案例。進一步區分真正的 email 地址與非地址欄位：`idtEmail` 是 ID Token 欄位名稱、mail template／mail log 是設定或顯示資料，均不應套用地址 validator。確認需處理 `ac0016`、`ac0002`、`ac0006`、login、`ac0202` 與三個 Approval Result Mail 頁面。

**修正方案** — 優先修正共用 validator，再依單一／多組地址語意套用，避免每頁自行維護不同 regex。

### 需求 2：阻擋中文與不合法 email

**Context** — 使用者確認 plan 後同意實作。

**分析** — 現有 regex 缺少開頭錨點，所以可能只匹配 `中文abc@test.com` 尾端的合法片段；`[a-z]{2,3}` 也會誤擋 `.info`、`.technology` 等合法長 TLD。專案的動態 constraints 透過 `setValidators()` 設定欄位規則，會覆蓋 form 初始化時已有的 validator。

**修正方案** — 建立私有 `isValidMail()` 作為單一判斷來源；`mailValidator()` 與 `multipleMailValidator()` 共用它。所有動態 constraints 頁面在 `addFormValidator()` 後使用 `addValidators()` 補回 email 規則。

### 需求 3：建立工作日誌、build 成功後上傳

**Context** — 使用者要求建立工作日誌，並指定 `npm run build` 成功才可上傳。

**分析** — `angular.json` 的 `outputPath` 是後端 tracked 目錄 `../src/main/resources/static/dgrv4/ac4/`，因此每次 build 都會替換 hash 檔名。最初將 build 視為純驗證而清理產物；使用者後續明確說明，只要要求「`npm run build` 且上傳」，build 輸出就是交付內容，必須一併提交。Git 工作樹另有使用者既有的 `.claude/settings.local.json`，不屬於本次修改。

**修正方案** — 執行 build 並確認 exit code 0；來源碼、測試與本文件先行提交。收到使用者對交付語意的更正後重新 build，將 `static/dgrv4/ac4/` 內更新的 `index.html`、新 hash bundles 與被取代的舊 bundles 另行補充提交並推送。

## 驗證方式

### 靜態驗證

- `npx ng test --watch=false --browsers=ChromeHeadless --include='**/validator-functions.spec.ts'`
  - 結果：`TOTAL: 5 SUCCESS`，exit code 0。
  - 涵蓋合法 email、長 TLD、中文、前置空白、不完整 domain、空值、合法多組地址、不合法子項與尾端空段落。
- 第一次在 sandbox 執行 targeted test 時，Node 因無權限 `lstat C:\Users\atirl` 回傳 `EPERM`，尚未進入 Angular 編譯；改以核准的非 sandbox 環境執行後通過。
- `npm run build`：實作完成後第一次執行成功，exit code 0，build hash `d162bab3c3c966f2`。
- 依使用者上傳前要求再次執行 `npm run build`：成功，exit code 0，build hash同為 `d162bab3c3c966f2`。
- `git diff --check`：無 whitespace error。
- 最終補正 build 成功，exit code 0；其 static hash bundles 與 `index.html` 納入補充提交。

### 尚待人工驗證（建議）

- 在 `ac0016` 新增與修改頁輸入中文、缺少 `@`、缺少 domain 點號，確認按鈕停用並顯示 Mail 格式錯誤。
- 在 `ac0002`、`ac0006` 與忘記密碼頁確認同樣行為，且合法長 TLD 可以送出。
- 在 `ac0202` 與三個 Approval Result Mail 頁輸入兩個逗號分隔合法地址，確認可送出；其中任一地址不合法或尾端多逗號時應阻擋。

## 實作紀錄

### 時間軸

1. 使用者反映 `ac0016` User Email 未檢核中文或 email 格式，要求先盤點並列 plan。
2. 讀取專案 Angular 規範，搜尋所有 email/mail form controls、templates 與 validators。
3. 確認 `ac1002` 已使用共用 `mailValidator()`，但 validator 本身缺少 `^` 且只接受 2–3 字元 TLD，不能直接沿用現況。
4. 排除 `idtEmail` 等欄位映射名稱與 mail template／log，列出單一 email、多 email 與待確認的 Approval Result Mail 項目。
5. 使用者同意 plan 後，修正共用 validator、新增多 email validator 及 5 個測試案例。
6. 為 `ac0016`、`ac0002`、`ac0006`、login、`ac0202` 套用相符 validator 與錯誤訊息，並處理動態 constraints 覆蓋問題。
7. 搜尋後端 `approvalResultMail` 使用方式，確認最終交由 `InternetAddress.parse()` 解析逗號分隔收件者，因而將 `ac0018`、`ac0019`、`ac0020` 納入多 email 驗證。
8. 第一次 targeted test 在 sandbox 因 Node `EPERM` 中止；核准後重新執行，5 項測試全部成功。
9. 第一次 `npm run build` 成功。因 outputPath 指向 tracked static 目錄，build 產生 hash 檔變更。
10. 第一次清理 static 時，將 Git 回傳的相對 `srcAngular` 路徑誤以 repository root 解析，安全檢查主動中止，沒有刪除非預期檔案；改由目前工作目錄解析並確認目標位於 static 目錄後完成清理。
11. 使用者要求建立工作日誌並在 build 成功後上傳；再次執行 `npm run build` 成功，當時仍依「純驗證」理解清理 build 產物並提交來源碼與本文件。
12. 使用者更正交付規則：明確要求「`npm run build` 且上傳」時，build 內容必須併入同次上傳。依此重新執行 build，保留並補充提交 `static/dgrv4/ac4/` 產物，同時修正本文件的錯誤記錄。

### 經驗紀錄（給未來類似任務參考）

- Email regex 必須驗證完整輸入，而不是只確認尾端存在合法片段；開頭／結尾錨點不可缺少。
- TLD 不應限制為 2–3 字元，否則會拒絕現代合法網域。
- 共用 validator 應讓空值交由 required validator 處理，保持單一責任並支援 optional email 欄位。
- 使用後端動態 constraints 的頁面要注意 `setValidators()` 會覆蓋既有規則；需要保留的前端規則應在其後用 `addValidators()` 補回。
- 多 email 欄位不能直接套用單一 email regex，必須按系統實際分隔格式逐項驗證。
- Angular build 的 outputPath 若指向 tracked 部署目錄，是否保留產物取決於使用者語意：只要求 build 驗證時可清理；明確要求「build 且上傳」時必須將產物納入同次交付。
