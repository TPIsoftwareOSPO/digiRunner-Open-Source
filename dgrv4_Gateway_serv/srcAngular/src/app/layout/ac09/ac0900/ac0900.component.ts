import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-ac0900',
  templateUrl: './ac0900.component.html',
  styleUrls: ['./ac0900.component.css'],
})
export class Ac0900Component extends BaseComponent implements OnInit {
  reportID: string = '';
  status: boolean = false;
  mask: boolean = true;
  constructor(protected router: ActivatedRoute, tr: TransformMenuNamePipe, private toolService: ToolService) {
    super(router, tr);
    this.route.paramMap.subscribe((res) => {
      this.reportID = res.get('cusfunc')!;
      this.ngOnInit();
    });
  }

  ngOnInit() {
    this.status = false;
    this.mask = true;
    setTimeout(() => {
      this.status = true;
      this.initTitle();
    }, 0);
    // this.router.paramMap.subscribe(res=>{
    //   console.log(res.get("cusfunc"));
    //   // this.reportID = res['id'];
    //   this.reportID = res.get("cusfunc")!
    //   // console.log(this.reportID);
    // })
  }
}
