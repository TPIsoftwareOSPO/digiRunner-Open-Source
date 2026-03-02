import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KeyValueComponent } from './key-value.component';
import { KeyValueDetailComponent } from './key-value-detail/key-value-detail.component';
export {KeyValueComponent};
@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
  ],
  declarations: [KeyValueComponent],
  exports:[KeyValueComponent],
  providers:[TranslateService]
})
export class KeyValueModule { }
