import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-container',
  templateUrl: './container.component.html',
  styleUrls: ['./container.component.css']
})
export class ContainerComponent implements OnInit {
  @Input('title') title: string= '';

  /*
  *  true  畫面排版預設   大字體 無返回按鈕
  *  false 畫面進分頁     小字體且靠右 有返回按鈕
  */
  @Input('isDefault') isDefault:boolean = true;
  @Output() headerReturn = new EventEmitter();

  constructor() { }

  ngOnInit() {

  }

  return(){
    this.headerReturn.emit(null);
  }

  getHead() {

    const titleArr = this.title.indexOf('>')>-1 ? this.title.split('>') : [this.title];
    titleArr.pop();
    return titleArr.join(' / ')
  }

  getTail() {
    const titleArr = this.title.indexOf('>')>-1 ? this.title.split('>') : [this.title];

    return titleArr[titleArr.length-1];
  }

}
