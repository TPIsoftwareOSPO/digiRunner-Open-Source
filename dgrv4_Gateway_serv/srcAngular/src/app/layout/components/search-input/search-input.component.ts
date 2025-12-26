import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-search-input',
  templateUrl: './search-input.component.html',
  styleUrls: ['./search-input.component.scss'],
})

export class SearchInputComponent
{
  @Input() placeholder: string = '';
  @Input() control!: FormControl;
  @Input() searchFn!: () => void;
  @Input() class: string = '';

  onSearch() {
    if (this.searchFn) {
      this.searchFn();
    }
  }

  onKeydown(event: KeyboardEvent) {
    /**
     * Return directly to avoid accidental triggering when IME composes words.
     *  直接 return，避免 IME 組字時誤觸發
     */
    if (event.isComposing) {
      // console.log('is composing')
      return;
    }
    this.onSearch();
  }
}
