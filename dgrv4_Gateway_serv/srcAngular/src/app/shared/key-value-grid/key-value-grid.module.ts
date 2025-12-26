
import { KeyValueGridDetailComponent } from './key-value--grid-detail/key-value-grid-detail.component';
import { KeyValueGridComponent } from './key-value-grid.component';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

export {KeyValueGridComponent}

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
  ],
  declarations: [KeyValueGridComponent],
  exports:[KeyValueGridComponent],
  providers:[TranslateService]
})
export class KeyValueGridModule { }
