import { Component, Input, OnInit, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { EmApptJobType } from 'src/app/models/common.enum';
@Component({
  selector: 'app-ca-stat',
  templateUrl: './ca-stat.component.html',
  styleUrls: ['./ca-stat.component.css']
})
export class CaStatComponent implements OnInit {
  @Input() showType:boolean = true;
  @Input() type?: EmApptJobType;
  @Input() apptJobId?: number;
  @Input() statusName?: string;
  @Input() stackTrace?: string;
  @Input() cgRespBody?: string;
  @Output() restart: EventEmitter<any> = new EventEmitter();
  @ViewChild('error_messages') error_messages!: ElementRef;

  constructor() { }

  ngOnInit() {
  }
  restartHandler(evt) {
    this.restart.emit({});
  }
  switchErrorMessage(evt) {
    $(this.error_messages.nativeElement).toggleClass('hide');
  }
}
