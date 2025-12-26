import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Pipe({
  name: 'safeUrl' 
})
export class SafeUrlPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {}

  transform(url: string | null | undefined): SafeUrl {    
    if (!url) {
      return '';
    }    
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }
}