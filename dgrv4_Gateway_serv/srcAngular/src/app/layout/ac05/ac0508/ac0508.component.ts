import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { BaseComponent } from '../../base-component';

@Component({
  selector: 'app-ac0508',
  templateUrl: './ac0508.component.html',
  styleUrls: ['./ac0508.component.css']
})
export class Ac0508Component extends BaseComponent implements OnInit {

  constructor(
    protected router: ActivatedRoute,
    tr: TransformMenuNamePipe
  ) {
    super(router, tr);
  }
  ngOnInit() {}

}
