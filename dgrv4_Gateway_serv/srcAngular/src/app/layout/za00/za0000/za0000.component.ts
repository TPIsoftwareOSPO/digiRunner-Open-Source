import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  AfterViewInit,
  OnChanges,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { filter } from 'rxjs';
import { FrameComponent } from '../../components/frame/frame.component';

@Component({
  selector: 'app-za0000',
  templateUrl: './za0000.component.html',
  styleUrls: ['./za0000.component.css'],
})
export class za0000Component extends BaseComponent implements OnInit {
  @ViewChild('frameRef', { read: ViewContainerRef, static: true })
  frameRef!: ViewContainerRef;

  reportID: string = '';
  // status: boolean = false;

  constructor(tr: TransformMenuNamePipe, route: ActivatedRoute) {
    super(route, tr);
    // 因此路由共用元件
    this.route.paramMap.subscribe((res) => {
      this.ngOnInit();
    });
  }

  ngOnInit() {

    this.initTitle();
    if (this.frameRef) {
      if (this.route.snapshot.params['cusfunc']) {
        this.reportID = this.route.snapshot.params['cusfunc'];
      } else {
        this.route.data.subscribe((res) => {
          this.reportID = res['id'];
        });
      }
      this.frameRef.clear();
      let _frameRef = this.frameRef.createComponent(FrameComponent);
      _frameRef.instance.reportID = this.reportID;
    }

    // this.status = false;
    // setTimeout(() => {
    //   this.status = true;
    //   this.initTitle();
    //   if (this.route.snapshot.params['cusfunc']) {
    //     this.reportID = this.route.snapshot.params['cusfunc'];
    //   } else {
    //     this.route.data.subscribe((res) => {
    //       this.reportID = res['id'];
    //     });
    //   }
    // }, 0);
  }

}
