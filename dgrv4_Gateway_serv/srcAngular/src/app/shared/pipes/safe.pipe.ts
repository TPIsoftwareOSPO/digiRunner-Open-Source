import { PipeTransform, Pipe } from '@angular/core';
import { DomSanitizer } from "@angular/platform-browser";

@Pipe({ name: 'safe' })
export class SafePipe implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) { }
    transform(url:string) {
        if (!url) return null;
        return this.sanitizer.bypassSecurityTrustHtml(url);
    }
}
