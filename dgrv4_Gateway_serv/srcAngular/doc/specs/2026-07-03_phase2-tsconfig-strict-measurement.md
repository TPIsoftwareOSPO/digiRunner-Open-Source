# Angular 13→20 技術債整理 Phase 2：tsconfig 型別檢查嚴格化量測

延續 `doc/specs/2026-07-03_phase1-tech-debt-cleanup.md` 所述的六階段技術債整理計畫，本次執行 **Phase 2：tsconfig 型別檢查嚴格化（先量測、後決定範圍）**。本 Phase **不涉及任何程式碼或設定的永久異動**，純粹是一次性量測並決策，因此沒有對應的 commit。

## 背景

`tsconfig.json` 現況存在矛盾：宣告 `"strict": true`，但同時：

```jsonc
"strictInjectionParameters": true,
"strictInputAccessModifiers": true,
"strictTemplates": false,
"fullTemplateTypeCheck": false,
```

`strictTemplates`/`fullTemplateTypeCheck` 皆為 `false`，代表 Angular 模板（`.component.html`）內的型別檢查實質上沒有真正啟用，型別安全網有名無實。Phase 1 分析階段已規劃：不直接打開這兩個開關並批次修正，而是先量測「打開後會出現多少錯誤、分佈在哪裡」，再依錯誤規模決定是否值得投入修正。

## 執行方式

1. 在 `tsconfig.json` 暫時將 `strictTemplates`、`fullTemplateTypeCheck` 由 `false` 改為 `true`。
2. 執行 `npm run build`（production），完整擷取編譯錯誤輸出。
3. 統計錯誤數量、依錯誤代碼分類、統計受影響檔案數與最集中的檔案。
4. **量測完成後立即將 `tsconfig.json` 還原為原本的 `false`/`false`**，並用 `git status`/`git diff` 確認完全無殘留變更。
5. 量測用的 `npm run build` 會連帶重新產生本儲存庫內被追蹤的建置產物目錄（`../src/main/resources/static/dgrv4/ac4/`），比照 Phase 1 的做法用 `git checkout --` + `git clean -fd` 還原，避免污染工作目錄。

## 量測結果

### 總體數字

- 開啟 `strictTemplates`/`fullTemplateTypeCheck` 後，`ng build` 產生 **3043 個編譯錯誤**。
- 錯誤分佈在 **110 個檔案**（絕大多數是 `.component.html` 模板檔）。

### 錯誤代碼分佈

| 錯誤代碼 | 數量 | 佔比 | 說明 |
|---|---:|---:|---|
| `TS2531` | 1418 | 46.6% | Object is possibly `null` |
| `TS4111` | 917 | 30.1% | 屬性來自 index signature，需改用 `['x']` 存取 |
| `TS2532` | 241 | 7.9% | Object is possibly `undefined` |
| `TS2533` | 239 | 7.9% | Object is possibly `null` or `undefined` |
| `TS2322` | 86 | 2.8% | 型別不可指派 |
| `TS2339` | 42 | 1.4% | 屬性不存在於該型別上 |
| `TS2345` | 37 | 1.2% | 參數型別不可指派 |
| `TS2551` | 10 | 0.3% | 屬性不存在，並提示相近名稱 |
| `TS2341` | 9 | 0.3% | 存取 private 屬性 |
| `TS2554` | 7 | 0.2% | 參數數量不符 |
| `TS18048` | 4 | 0.1% | 可能為 `undefined` |
| `TS2739` | 3 | 0.1% | 缺少必要屬性 |

**前四類（`TS2531`/`TS4111`/`TS2532`/`TS2533`）合計 2815 個，佔全部錯誤的 92.5%。**

### 錯誤最集中的檔案（前 10）

| 檔案 | 錯誤數 |
|---|---:|
| `src/app/layout/ac02/ac0202/ac0202.component.html` | 318 |
| `src/app/layout/ac07/ac0706/ac0706.component.html` | 234 |
| `src/app/layout/ac02/ac0212/ac0212.component.html` | 163 |
| `src/app/layout/ac03/ac0301/ac0301.component.html` | 162 |
| `src/app/layout/ac02/ac0222/ac0222.component.html` | 159 |
| `src/app/layout/ac07/ac0702/ac0702.component.html` | 114 |
| `src/app/layout/labs/tsmpdpfile/tsmpdpfile.component.html` | 93 |
| `src/app/layout/ac03/ac0311/ac0311.component.html` | 82 |
| `src/app/layout/ac03/ac0301/api-detail/api-detail.component.html` | 63 |
| `src/app/layout/ac11/ac1107/ac1107.component.html` | 61 |

## 分析與判斷

- 前四類錯誤（92.5%）性質幾乎都是「模板第一次套用 strict null check（`strictNullChecks`）與 index signature 存取限制（`noPropertyAccessFromIndexSignature`）」所產生的**系統性**錯誤，而非個別邏輯錯誤。這兩項限制其實在 `tsconfig.json` 的 `"strict": true`／`noPropertyAccessFromIndexSignature: true` 早已對 `.ts` 檔案生效，只是過去因為 `strictTemplates: false`，同一套規則從未真正套用到 `.html` 模板表達式（例如 `{{ obj?.errors.pattern }}` 這類需要先做 null 檢查或改用 `['pattern']` 索引存取的寫法）。
- 剩餘 7.5%（`TS2322`/`TS2339`/`TS2345`/`TS2551`/`TS2341`/`TS2554`/`TS2739`）則較可能反映真實的型別不一致問題，但數量分散在 110 個檔案中，難以快速判斷是否為真正的潛在 bug 還是模板寫法過於寬鬆但實際執行無礙。
- 3043 個錯誤、110 個檔案的規模，若要一次性修正並打開這兩個開關，等同於對全站三分之一以上的功能模組做大規模模板修改，明顯超出「一次改動、風險可控」的範圍，且與當前無具體業務需求的技術債整理任務不成比例。

## 決策

**暫不開啟 `strictTemplates`/`fullTemplateTypeCheck`**，`tsconfig.json` 維持原狀（已還原，無異動）。將此量測結果記錄為已知技術債：

- 若日後某個功能模組因其他原因（新增功能、修 bug）被大幅重構，可以評估是否**針對該檔案**練習性地修正其 strict template 錯誤，而不必等待全站一次性打開開關。
- `noImplicitAny` 依 Phase 1 規劃的決策，本次同樣不調整。

## 驗證方式

- `git status`/`git diff -- tsconfig.json`：確認量測後 `tsconfig.json` 與量測前完全一致，無殘留變更。
- `git status`：確認量測用的 `npm run build` 產生的建置產物目錄變動已用 `git checkout --`/`git clean -fd` 還原，工作目錄除本次工作階段開始前就存在的無關變更外皆為乾淨狀態。

## 實作紀錄

### 時間軸

1. 使用者要求檢查 Phase 1 尚未執行的項目，確認接下來要執行 Phase 2。
2. 依 Phase 1 規劃的方法論，在 `tsconfig.json` 暫時開啟 `strictTemplates`/`fullTemplateTypeCheck`，執行一次 production build 擷取完整錯誤輸出。
3. 錯誤輸出含 ANSI 顏色控制碼，直接用 `error TS[0-9]+` 之類的正則比對會零命中；改用 `sed` 先清除 ANSI 控制碼，再統計錯誤代碼分佈與逐檔錯誤數，才取得正確的統計數字。
4. 統計完成後立即將 `tsconfig.json` 還原，並確認建置產物目錄已比照 Phase 1 的做法清理乾淨。
5. 依錯誤規模（3043 個、110 檔案，92.5% 為系統性 null-check／index-signature 錯誤）判斷不值得在本次任務中批次修正，決策為暫緩並記錄技術債，符合 Phase 1 規劃時預先設定的決策邏輯（「錯誤數量大 → 記錄為已知技術債，暫不開啟」）。

### 經驗紀錄（給未來類似任務參考）

- **量測型任務要確保「觀察」不留下副作用**：本次修改 `tsconfig.json` 只是為了取得錯誤數量，並非要真的變更設定，因此在統計完成後立刻還原，且额外確認了因跑 build 而連帶弄髒的已追蹤建置產物目錄也一併還原，讓這次任務對儲存庫的淨影響是「零程式碼異動、僅產出一份量測報告」。
- **終端機色碼會讓正則比對静默失敗**：`ng build`/webpack 的錯誤輸出預設帶 ANSI 顏色碼，直接用簡單正則（如 `error TS[0-9]+`）擷取代碼會因為顏色碼插在字元中間而完全比對不到，需要先用 `sed -E 's/\x1b\[[0-9;]*m//g'` 之類的方式去除顏色碼，再進行文字統計，否則會誤以為「沒有這類錯誤」。
