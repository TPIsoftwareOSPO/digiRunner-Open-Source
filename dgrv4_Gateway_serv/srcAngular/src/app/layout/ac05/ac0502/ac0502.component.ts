import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit} from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
@Component({
  selector: 'app-ac0502',
  templateUrl: './ac0502.component.html',
  styleUrls: ['./ac0502.component.css']
})
export class Ac0502Component extends BaseComponent implements OnInit {

  constructor(
    protected router: ActivatedRoute,
     tr: TransformMenuNamePipe
  ) {
    super(router, tr);
  }
  ngOnInit() {}
}
