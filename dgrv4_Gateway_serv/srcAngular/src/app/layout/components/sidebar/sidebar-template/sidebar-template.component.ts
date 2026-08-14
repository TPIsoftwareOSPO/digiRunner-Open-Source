import { TransformMenuNamePipe } from './../../../../shared/pipes/transform-menu-name.pipe';
import { Menu, SubMenu } from 'src/app/models/menu.model';
import { SidebarService } from './../sidebar.service';
import {
  Component,
  OnInit,
  ViewChild,
  Input,
  ElementRef,
  EventEmitter,
  Output,
} from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-sidebar-template',
    templateUrl: './sidebar-template.component.html',
    styleUrls: ['./sidebar-template.component.scss'],
    standalone: false
})
export class SidebarTemplateComponent implements OnInit {
  @Input('menu') menu: Menu | undefined;
  @Input('subname') subname!: ElementRef;
  @ViewChild('tsmp_menu') tsmp_menu!: ElementRef;
  @Output('menuClick') menuClick = new EventEmitter<any>();

  isActive: boolean = true;
  allEnabled: boolean = true;

  _mainId: string = '';

  constructor(private service: SidebarService, private router: Router) {}

  ngOnInit(): void {
    if (this.menu?.subs) {
      this.isActive = this.menu.subs
        .map((s: SubMenu) => s.name.toLowerCase())
        .includes(this.service.activeId);

      if (this.isActive) this._mainId = this.menu.main;
      this.menu?.subs.forEach((sub: SubMenu) => {
        if (sub.enabled) this.allEnabled = sub.enabled;
      });
    }

    this.service.getMenuMainId().subscribe((_id) => {
      this._mainId = _id;
    });
  }

  toggleExpand() {
    if (this._mainId === this.menu!.main) {
      this.service.setMenuMainId(''); // 再次點擊已展開的項目 → 收合
    } else {
      this.service.setMenuMainId(this.menu!.main); // 展開這個、自動收合其他（沿用既有廣播機制）
    }
  }

  click(actived: any, sub: SubMenu) {
    // $(subname).addClass('active');
    this.service.setMenuMainId(this.menu!.main);
    this.service.siderbarEventEmitter.emit({
      menu: this.tsmp_menu,
      actived: actived,
      id: sub.name,
    });
    if (sub.funcURL) {
      if (sub.funcURL.includes('tsmpdpbe')) {
        this.router.navigate([sub.path, { funcURL: sub.funcURL }]);
      }
    }
  }

  mouseenter(img, id) {
    img.setAttribute(
      'src',
      'assets/images/menu/' + (id.indexOf('ZA') > -1 ? 'AC13' : id) + '_h.png'
    );
  }

  mouseout(img, id) {
    img.setAttribute(
      'src',
      'assets/images/menu/' + (id.indexOf('ZA') > -1 ? 'AC13' : id) + '.png'
    );
  }
}
