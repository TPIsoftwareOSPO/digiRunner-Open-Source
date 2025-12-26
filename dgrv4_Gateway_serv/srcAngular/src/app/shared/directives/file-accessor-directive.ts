import { Directive, HostListener, ElementRef, Renderer2 } from "@angular/core";
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from "@angular/forms";

@Directive({
    selector: 'input[type=file]',
    providers: [
      {
        provide: NG_VALUE_ACCESSOR,
        useExisting: FileAccessorDirective,
        multi: true
      }
    ]
  })
  export class FileAccessorDirective implements ControlValueAccessor {
    onChange;
  
    @HostListener('change', ['$event.target']) _handleInput(event) {
      this.onChange(event.files[0]);
    }
  
    constructor(private element: ElementRef, private render: Renderer2) {  }
  
    writeValue(value: any) {
      const normalizedValue = value == null ? '' : value;
      this.render.setProperty(this.element.nativeElement, 'value', normalizedValue);
    }
  
    registerOnChange(fn) {
        this.onChange = fn;
    }
  
    registerOnTouched(fn: any) {  }
  
    nOnDestroy() {  }
  }