import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

export interface StreamResponse {
  status: number;
  statusText: string;
  headers: Record<string, string>;
  chunk: string;
}

interface ReadResult {
  done: boolean;
  value?: Uint8Array;
}

@Injectable({
  providedIn: 'root',
})
export class StreamApiService {
  constructor(private zone: NgZone) {}

  postStream(
    method: string,
    url: string,
    body: any,
    headers: any,
  ): Observable<StreamResponse> {
    return new Observable((observer) => {
      fetch(url, {
        method: method,
        body: body,
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
      })
        .then(async (response) => {
          const reader = response.body?.getReader();
          if (!reader) {
            observer.complete();
            return;
          }

          const decoder = new TextDecoder();

          // 先解析 headers
          const headers: Record<string, string> = {};
          response.headers.forEach((value, key) => {
            headers[key] = value;
          });

          const readChunk = ({ done, value }: ReadResult) => {
            if (done) {
              observer.complete();
              return;
            }

            const chunkText = decoder.decode(value, { stream: true });

            observer.next({
              status: response.status,
              statusText: response.statusText,
              headers,
              chunk: chunkText,
            });

            reader.read().then(readChunk);
          };

          reader.read().then(readChunk);
        })
        .catch((error) => {
          observer.error(error);
        });
    });
  }

  getStreamMessages(
    method: string,
    url: string,
    body: any,
    headers: any,
  ): Observable<any> {
    return new Observable((observer) => {
      const decoder = new TextDecoder();
      let buffer = '';

      fetch(url, {
        method: method,
        body: body,
        headers: {
          ...headers,
        },
      })
        .then(async (response) => {
          if (!response.ok) {
            observer.error(`伺服器錯誤: ${response.status}`);
            return;
          }

          const reader = response.body?.getReader();
          if (!reader) {
            observer.complete();
            return;
          }

          // 持續讀取串流
          while (true) {
            const { done, value } = await reader.read();

            if (done) {
              // 當串流結束，處理最後殘留在 buffer 裡的資料
              if (buffer.trim()) {
                this.processText(buffer, observer);
              }
              break;
            }

            // 讀取新的片段並串接到緩衝區
            buffer += decoder.decode(value, { stream: true });

            // 只要 buffer 裡還有換行符，就持續處理
            while (buffer.includes('\n')) {
              const index = buffer.indexOf('\n');
              const line = buffer.slice(0, index); // 取出第一行
              buffer = buffer.slice(index + 1); // 剩下的留回 buffer

              this.processSingleLine(line, observer);
            }
          }
          observer.complete();
        })
        .catch((error) => {
          this.zone.run(() => observer.error(error));
        });
    });
  }
  /**
   * 處理單行資料，確保解析 data: 欄位
   */
  private processSingleLine(line: string, observer: any) {
    const trimmed = line.trim();
    if (!trimmed || !trimmed.startsWith('data:')) return;

    try {
      const jsonStr = trimmed.replace(/^data:\s*/, '');
      const data = JSON.parse(jsonStr);

      // 回到 Angular 的 Zone 以更新 UI
      this.zone.run(() => observer.next(data));
    } catch (e) {
      console.warn('JSON 解析失敗，可能該行不完整:', trimmed);
    }
  }

  /**
   * 輔助處理最後殘留 buffer (針對沒有以 \n 結尾的最後一筆資料)
   */
  private processText(text: string, observer: any) {
    const lines = text.split('\n');
    lines.forEach((line) => this.processSingleLine(line, observer));
  }
}
