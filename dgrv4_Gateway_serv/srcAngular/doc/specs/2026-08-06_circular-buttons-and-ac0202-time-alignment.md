# 修正專案圓形按鈕與 AC0202 服務時間對齊

## 修改內容

### 問題描述

1. 使用者以 AC0301 編輯畫面為例，指出子元件中的 `fa-minus`、`fa-plus` 按鈕不是完整圓形，要求先排查並提出修改計畫。
2. 使用者要求先不執行原計畫第 6 項，其餘項目照計畫處理。
3. 使用者追加要求盤點整個專案的圓形按鈕是否同樣變形，並修正列舉項目，但 Header 內按鈕暫不處理。
4. 使用者要求執行 `npm run build`，保留 Build 產物，之後連同原始碼提交與上傳。
5. 使用者指出 AC0202 編輯項目的服務時間欄位中，`:` 與 `～` 未上下左右置中，要求調整。
6. 第一次調整後，使用者回報建立與更新畫面的 `～` 垂直位置仍不自然，要求重新排查並修正。
7. 使用者最後要求 Build 成功後建立工作日誌，並將修改及 Build 產物上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/ac00/ac0019/ldap-data-list{,-detail}/*.component.html` | LDAP 資料新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac01/ac0105/locale-func-{form,detail}/*.component.html` | 多語系功能新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac02/ac0202/host-input{,-detail}/*.component.html` | Host 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac02/ac0228/key-value-{form,field}/*.component.html` | Key/Value 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac03/ac0301/mock-headers-input{,-detail}/*.component.html` | Mock Header 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac03/ac0301/src-url-input{,-detail}/*.component.html` | 來源 URL 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac03/ac0311/hostname-config{,-form}/*.component.html` | Hostname 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac03/ac0311/src-url-reg{,-detail}/*.component.html` | URL 註冊新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac03/ac0316/api-test/api-test.component.html` | Form 欄位新增按鈕改用既有 `btn-icon`，保留原本底部間距。 |
| `src/app/layout/ac03/ac0319/api-url-setting{,-detail}/*.component.html` | API URL 設定新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/components/fields/fields-form{,-detail}/*.component.html` | 共用欄位新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/components/source-ip/source-ip-form{,-detail}/*.component.html` | Source IP 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/labs/lb0010/white-list-{form,detail}/*.component.html` | 白名單新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/labs/lb0011/key-value-{form,field}/*.component.html` | Labs Key/Value 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/labs/lb0011/mapping-url-{form,field}/*.component.html` | Mapping URL 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/labs/lb0013/diversion-list{,-row}/*.component.html` | 分流設定新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/labs/lb0018/lb0018.component.html` | Form 欄位新增按鈕改用既有 `btn-icon`，保留原本底部間距。 |
| `src/app/layout/labs/website-proxy/website-setting{,-row}/*.component.html` | Website Proxy 新增／刪除圓形按鈕改用既有 `btn-icon`。 |
| `src/app/layout/ac02/ac0202/ac0202.component.html` | 建立及更新的服務時間容器加入垂直置中。 |
| `src/app/layout/components/timepicker/timepicker.component.html` | 時間元件移除底部外距，並置中 `:`。 |
| `../src/main/resources/static/dgrv4/ac4/` | `npm run build` 產生的 `index.html`、新 hash bundles 與被取代的舊 bundles。 |

Header 相關元件未納入修改，符合使用者「header 內的先不處理」要求。

### 詳細程式碼異動

#### 圓形加減按鈕統一採用既有樣式

修改前：

```html
<button type="button" class="btn tpi-second" style="border-radius: 50%;">
  <i class="fa fa-plus"></i>
</button>
```

修改後：

```html
<button type="button" class="btn btn-icon tpi-second flex-shrink-0">
  <i class="fa fa-plus"></i>
</button>
```

專案既有 `.btn-icon` 明確設定 `width: 30px`、`height: 30px`、`border-radius: 50%` 與 `padding: 0`；`flex-shrink-0` 避免按鈕在 Flex 容器空間不足時被壓縮成橢圓。

#### AC0202 建立與更新的服務時間分隔符

修改前：

```html
<div class="d-flex">
  <app-timepicker formControlName="svcST"></app-timepicker>
  <span class="px-1 form-field-affix">～</span>
  <app-timepicker formControlName="svcET"></app-timepicker>
</div>
```

修改後：

```html
<div class="d-flex align-items-center">
  <app-timepicker formControlName="svcST"></app-timepicker>
  <span class="px-1 form-field-affix">～</span>
  <app-timepicker formControlName="svcET"></app-timepicker>
</div>
```

建立用的 `svcST`／`svcET` 與更新用的 `newSvcST`／`newSvcET` 均採相同結構。

#### 共用 Timepicker 的冒號與高度基準

修改前：

```html
<form class="d-flex form-group" [formGroup]="form">
  <input class="form-control text-center">
  <div class="form-field-affix">：</div>
  <input class="form-control text-center">
</form>
```

修改後：

```html
<form class="d-flex align-items-center form-group mb-0" [formGroup]="form">
  <input class="form-control text-center">
  <div class="d-flex align-items-center justify-content-center form-field-affix">：</div>
  <input class="form-control text-center">
</form>
```

`mb-0` 覆寫全域 `.form-group { margin-bottom: 0.5rem; }`，使外層 Flex 依輸入框本身高度對齊 `～`，而不是依包含額外底部空間的高度置中。

## 預計修改方式

### AC0301 圓形按鈕變形

**Context** — AC0301 編輯畫面的子元件加減按鈕使用 `border-radius: 50%`，但未固定等寬等高，畫面呈現不是完整圓形。

**分析** — 搜尋專案後確認 `src/styles/custom/_button.scss` 已有 `.btn-icon`，固定寬高為 `30px` 並清除 padding。專案內多個動態表單按鈕仍只使用 inline `border-radius: 50%`。

**修正方案** — 移除 inline 圓角，改用 `btn-icon`，並加入 `flex-shrink-0` 防止 Flex 壓縮；不新增重複 CSS。

### 全專案同型圓形按鈕

**Context** — 使用者要求擴大盤點並調整列舉項目，但指定 Header 暫不處理。

**分析** — 透過 `rg` 搜尋 `fa-plus`、`fa-minus`、`border-radius: 50%` 與按鈕 class，確認同型問題分布於 AC00、AC01、AC02、AC03、共用元件及 Labs。

**修正方案** — 僅修改盤點到的同型加減按鈕，保留原事件、disabled/hidden 條件、顏色與必要間距；Header 相關檔案排除。

### AC0202 服務時間 `:` 與 `～` 對齊

**Context** — 使用者先指出編輯項目未置中，第一次修改後又回報建立與更新的 `～` 垂直位置不自然。

**分析** — AC0202 建立與更新外層原本只有 `d-flex`。共用 `app-timepicker` 的 `<form>` 使用 `.form-group`，而全域樣式為其加入 `margin-bottom: 0.5rem`；第一次僅在更新區把 `～` 設成巢狀 Flex，仍會依包含底部間距的元件盒置中，因此視覺位置偏移。

**修正方案** — 建立與更新外層統一使用 `align-items-center`；`～` 不再自行建立巢狀 Flex；共用 Timepicker 加入 `mb-0`，並在元件內置中 `:`。

## 驗證方式

### 靜態驗證

1. 多次執行 `git diff --check`，結果均為 exit code `0`。
2. 執行 `npm run build` 成功：
   - 指令：`ng build --optimization=true`
   - 最終 Build Hash：`4105f3110b21cbf8`
   - 最終 Build 時間：2026-08-06 15:10（Asia/Taipei；工具輸出為 `2026-08-06T07:10:58.703Z`）
   - Exit code：`0`
3. 最終 AC0202 lazy chunks 包含 `4324.f2feed8fc68ef0bb.js` 與 `7191.f2b0f211fd63916a.js`。
4. Build 產物保留於 `../src/main/resources/static/dgrv4/ac4/`，未依一般驗證流程清除，因使用者明確要求一併上傳。

### 尚待人工驗證（建議）

1. 開啟 AC0202 建立與更新畫面，確認 `HH：MM ～ HH：MM` 的 `:`、`～` 均與輸入框垂直置中。
2. 抽查 AC0301、AC0311、AC0202、LB0011 等動態表單，在一般寬度與窄版寬度確認加減按鈕維持 `30px × 30px` 圓形。
3. 確認加減按鈕的 click、disabled 與 hidden 行為未受 class 調整影響。
4. 本次嘗試連接瀏覽器進行視覺驗證時，環境回覆 `No browser is available`，因此未完成自動化畫面驗證。

## 實作紀錄

### 時間軸

1. 使用者指出 AC0301 編輯子元件的 `fa-minus`、`fa-plus` 按鈕變形，要求先排查與列計畫。
2. 使用者要求略過原計畫第 6 項，其餘照計畫執行。
3. 依既有 `.btn-icon` 樣式修正 AC0301 相關按鈕。
4. 使用者要求盤點整個專案；搜尋後列出其他同型圓形按鈕。
5. 使用者同意調整列舉項目，但要求 Header 暫不處理；因此將 34 個非 Header template 的 inline 圓角改為 `btn-icon`，並加入 `flex-shrink-0`。
6. 使用者要求執行 Build 並保留產物，Build 產物自此保留供後續提交。
7. 使用者提出 AC0202 編輯服務時間的 `:`、`～` 未置中。第一次修正共用 Timepicker 的 `:`，並在更新區對 `～` 加入巢狀 Flex 置中。
8. 使用者回報建立與更新的 `～` 垂直位置仍不自然。重新檢查後確認建立區尚未同步，且 `.form-group` 的 `margin-bottom: 0.5rem` 改變了外層 Flex 的高度基準。
9. 最終將建立與更新外層統一為 `d-flex align-items-center`，移除 `～` 的巢狀 Flex，並以 `mb-0` 清除 Timepicker 底部外距。
10. `npm run build` 最終成功，Hash 為 `4105f3110b21cbf8`，Build 產物保留。
11. 依使用者要求建立本工作日誌；建立當下尚未進行 commit/push，後續提交與上傳結果應以 Git 操作實際結果為準。

### 經驗紀錄（給未來類似任務參考）

1. `border-radius: 50%` 只會依元素現有長寬產生圓角，不能保證圓形；應使用專案既有、固定等寬等高的 `.btn-icon`。
2. 圓形按鈕位於 Flex 容器時應防止 shrink，否則即使設定等寬等高仍可能在空間不足時變形。
3. 排查 Flex 垂直置中不能只看父層 `align-items-center`，也要檢查子元件內部 margin 是否改變外部尺寸基準。
4. 相同欄位若同時存在建立與更新兩套 template，修正時必須同步檢查，避免只修其中一處。
5. Build 輸出位於 tracked 後端 static 目錄；使用者明確要求上傳時，必須連同新舊 hash bundles 與 `index.html` 一起處理。
