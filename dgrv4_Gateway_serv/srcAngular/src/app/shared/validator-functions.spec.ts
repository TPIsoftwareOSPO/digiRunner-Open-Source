import { UntypedFormControl } from '@angular/forms';
import { mailValidator, multipleMailValidator } from './validator-functions';

describe('email validators', () => {
  describe('mailValidator', () => {
    const validator = mailValidator();

    it('應接受合法 email 與長 TLD', () => {
      expect(validator(new UntypedFormControl('user@example.com'))).toBeNull();
      expect(validator(new UntypedFormControl('user@example.technology'))).toBeNull();
    });

    it('應拒絕中文、空白與不完整格式', () => {
      expect(validator(new UntypedFormControl('中文user@example.com'))).toEqual({ mailFormat: 'validation.mailFormat' });
      expect(validator(new UntypedFormControl(' user@example.com'))).toEqual({ mailFormat: 'validation.mailFormat' });
      expect(validator(new UntypedFormControl('user@example'))).toEqual({ mailFormat: 'validation.mailFormat' });
    });

    it('應讓空值交由 required validator 處理', () => {
      expect(validator(new UntypedFormControl(''))).toBeNull();
    });
  });

  describe('multipleMailValidator', () => {
    const validator = multipleMailValidator();

    it('應接受以逗號分隔的多組 email', () => {
      expect(validator(new UntypedFormControl('first@example.com, second@example.info'))).toBeNull();
    });

    it('任一 email 不合法或為空段落時應拒絕', () => {
      expect(validator(new UntypedFormControl('first@example.com,中文@example.com'))).toEqual({ mailFormat: 'validation.mailFormat' });
      expect(validator(new UntypedFormControl('first@example.com,'))).toEqual({ mailFormat: 'validation.mailFormat' });
    });
  });
});
