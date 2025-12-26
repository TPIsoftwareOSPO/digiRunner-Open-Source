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
})
export class SidebarTemplateComponent implements OnInit {
  @Input('menu') menu: Menu | undefined;
  @Input('subname') subname!: ElementRef;
  @ViewChild('tsmp_menu') tsmp_menu!: ElementRef;
  @Output('menuClick') menuClick = new EventEmitter<any>();

  isActive: boolean = true;
  firstPageLoad: boolean = true;
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
      if (_id) this._mainId = _id;
    });
  }

  ngAfterViewInit() {
    if (this.tsmp_menu) {
      this.addExpandClass(this.tsmp_menu.nativeElement);
    }
    this.firstPageLoad = false;
  }

  addExpandClass(element: any, _menuMain: string = '') {
    let $li = $(element).children('li').first();

    if (this.firstPageLoad) {
      this.isActive ? $li.find('>ul').slideDown() : $li.find('>ul').slideUp();
    } else {
      $li.find('>ul').is(':hidden')
        ? $li.find('>ul').slideDown()
        : $li.find('>ul').slideUp();

      $li
        .closest('.menu-group')
        .find('li.nested')
        .not($li)
        .find('>ul')
        .slideUp();

      this.service.setMenuMainId(this.menu!.main);
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
