import { HttpErrorResponse } from '@angular/common/http';
import { classifySmartError, SMART_MISSING_STATE } from './smart-error';

describe('classifySmartError', () => {
  it('缺少 state 的哨兵錯誤應歸類為 no-state', () => {
    expect(classifySmartError(new Error(SMART_MISSING_STATE))).toBe('no-state');
  });

  it('非 HttpErrorResponse（如 setRequestHeader 同步 throw）應歸類為 server', () => {
    expect(classifySmartError(new TypeError('invalid header value'))).toBe('server');
    expect(classifySmartError('unexpected')).toBe('server');
  });

  it('status 0（離線／連線中斷）應歸類為 network', () => {
    expect(classifySmartError(new HttpErrorResponse({ status: 0 }))).toBe('network');
  });

  it('consent-info 的 400 invalid_state 應歸類為 expired', () => {
    const err = new HttpErrorResponse({ status: 400, error: { error: 'invalid_state' } });
    expect(classifySmartError(err)).toBe('expired');
  });

  it('proxy 的 401 invalid_token 應歸類為 expired', () => {
    const err = new HttpErrorResponse({ status: 401, error: { error: 'invalid_token' } });
    expect(classifySmartError(err)).toBe('expired');
  });

  it('無可辨識 body 的 401 應後備歸類為 expired', () => {
    expect(classifySmartError(new HttpErrorResponse({ status: 401 }))).toBe('expired');
  });

  it('上游 FHIR server 的 OperationOutcome 不論 status 都不可誤判為 expired', () => {
    const err400 = new HttpErrorResponse({ status: 400, error: { resourceType: 'OperationOutcome' } });
    const err401 = new HttpErrorResponse({ status: 401, error: { resourceType: 'OperationOutcome' } });
    expect(classifySmartError(err400)).toBe('server');
    expect(classifySmartError(err401)).toBe('server');
  });

  it('403 不應歸類為 expired（安全檢查也會回 403）', () => {
    expect(classifySmartError(new HttpErrorResponse({ status: 403 }))).toBe('server');
  });

  it('404 與 5xx 應歸類為 server', () => {
    expect(classifySmartError(new HttpErrorResponse({ status: 404 }))).toBe('server');
    expect(classifySmartError(new HttpErrorResponse({ status: 502 }))).toBe('server');
  });

  it('body 為字串時不應誤判，應落到 status 判斷', () => {
    const err = new HttpErrorResponse({ status: 500, error: 'Internal Server Error' });
    expect(classifySmartError(err)).toBe('server');
  });
});
