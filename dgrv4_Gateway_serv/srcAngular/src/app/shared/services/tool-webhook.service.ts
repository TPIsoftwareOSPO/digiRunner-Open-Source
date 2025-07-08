import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ToolServiceWebhoook {
  constructor(){}
  dgrProtocol_match(testString: string = '') {
    const match = testString.match(/^(dgr)((\+[-a-zA-Z]+)+)##(.+)/);

    if (match) {
      const base = match[1]; // dgr
      const plusParts = match[2].match(/\+[-a-zA-Z]+/g) || []; // 所有 +xxx
      const url = match[4]; // ## 後面的內容
      return [base, ...plusParts, url];
    }
    return [];
  }
}
