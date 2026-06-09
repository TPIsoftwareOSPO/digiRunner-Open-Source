import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { PrimengModule } from '../primeng.module';
import { SharedModule } from 'primeng/api';

@Component({
  selector: 'app-mima-input',
  templateUrl: './mima-input.component.html',
  styleUrls: ['./mima-input.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    PrimengModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModule
  ],
})
export class MimaInputComponent {

  mask:boolean = true;
  @Input() placeholder: string = '';
  @Input() control!: UntypedFormControl;
  @Input() class: string = 'form-control';

  constructor() { }

  toggleMask(){
    this.mask = !this.mask;
  }

}
