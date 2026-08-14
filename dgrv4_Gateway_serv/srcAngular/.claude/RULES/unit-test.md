# 單元測試規範（Karma / Jasmine）

> 本文件如實反映現況：本專案目前**沒有**成熟的單元測試文化或基礎設施（無 mock 慣例、無 coverage 門檻）。以下是務實建議，**不是**強制門檻，待專案累積測試基礎設施（例如導入 HttpClientTestingModule 的標準 mock 慣例）後可再收緊規則。

## 現況

- 全專案共有 44 個 `*.spec.ts`（`ng generate` 產生），絕大多數僅有 Angular CLI 預設的樣板斷言 `expect(component).toBeTruthy()`，沒有針對業務邏輯、API 呼叫分支、表單驗證等撰寫實質測試案例。
- `karma.conf.js` 已設定 coverage 報表輸出（`./coverage/src-angular/`，HTML + text-summary），但**未設定 coverage 門檻**，測試覆蓋率低不會導致 `npm test` 失敗。
- 目前**沒有** MSW 或等效的 API mock 基礎設施，`HttpClient` 相關測試仍需透過 Angular 官方的 `HttpClientTestingModule`/`HttpTestingController` 手動 mock。

## 指令

```bash
npm test                                              # ng test，執行全部 spec（Karma/Chrome）
ng test --include='**/ac0101.component.spec.ts'        # 只跑單一測試檔
ng test --code-coverage                                 # 產出 coverage 報表
```

## 何時該補測試

- **不強制**在每次修改元件/服務時都補齊完整測試（現況基礎設施尚未支援大量 mock 慣例，貿然要求會拖慢開發且測試品質難以維持一致）。
- 若使用者**明確要求**新增或修改測試，或本次任務屬於修 bug（適合先寫一個能重現 bug 的測試再修復），依下列建議結構撰寫。

## 建議結構

### 元件測試（TestBed）

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { XxxComponent } from './xxx.component';

describe('XxxComponent', () => {
  let component: XxxComponent;
  let fixture: ComponentFixture<XxxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [XxxComponent], // standalone 元件則改用 imports: [XxxComponent]
      imports: [HttpClientTestingModule, /* 其他必要模組 */],
      providers: [/* mock service，見下 */],
    }).compileComponents();

    fixture = TestBed.createComponent(XxxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('查詢成功時應更新清單', () => {
    // Given / When / Then 風格註解，說明測試情境
  });
});
```

### Mock `api-*.service.ts` / `ApiBaseService`

由於所有 API 呼叫都經過 `ApiBaseService`（見 `.claude/RULES/api-pattern.md`），測試元件時優先 mock 元件直接注入的 `api-*.service.ts`（用 `jasmine.createSpyObj`），而不是往下 mock 到 `HttpClient` 層：

```ts
const funcServiceSpy = jasmine.createSpyObj('FuncService', ['queryFuncList']);
funcServiceSpy.queryFuncList.and.returnValue(of({ ResHeader: { rtnCode: '0000' }, RespBody: { list: [] } }));

TestBed.configureTestingModule({
  providers: [{ provide: FuncService, useValue: funcServiceSpy }],
});
```

### 需涵蓋的基本情境（若被要求補測試）

1. **正常路徑**：元件建立成功、核心方法在合法輸入下正確更新狀態
2. **API 失敗分支**：`rtnCode !== '0000'` 時元件不應誤判為成功
3. **表單驗證**：`ValidatorFns.*` 驗證器在邊界值（空值、超長字串）下的行為

## 提交前

修改到的檔案若已有對應測試，執行 `ng test --include='**/<檔名>.spec.ts'` 確認綠燈；若沒有測試基礎設施可用（例如需要 mock 的相依過於複雜），如實告知使用者現況，不要為了讓測試通過而弱化斷言或刪除既有測試案例。
