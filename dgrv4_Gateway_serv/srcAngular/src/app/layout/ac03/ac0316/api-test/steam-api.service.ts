import { Injectable } from '@angular/core';
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
  postStream(method:string ,url: string, body: any, headers: any): Observable<StreamResponse> {
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
}
