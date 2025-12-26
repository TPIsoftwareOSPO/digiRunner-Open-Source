import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-ca-card',
  templateUrl: './ca-card.component.html',
  styleUrls: ['./ca-card.component.css']
})
export class CaCardComponent implements OnInit {
  @Input() cards?:any[];

  constructor() { }

  ngOnInit() {
  }

}
