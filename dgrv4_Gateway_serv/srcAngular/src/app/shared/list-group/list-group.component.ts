import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
} from '@angular/core';
import { Menu } from 'src/app/models/menu.model';

@Component({
  selector: 'app-list-group',
  templateUrl: './list-group.component.html',
  styleUrls: ['./list-group.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListGroupComponent implements OnInit {
  /**
   * 處理功能清單 - 用於角色設定功能項目 & Menu
   */

  @Input() height = '300px';
  @Input() disableCheckbox: boolean = false;
  @Input() showCheckbox: boolean = true;
  @Input() showButton = true;
  @Input('menu') menus;
  @Input() selected: any;
  @Output('updateHandler') updateHandler: EventEmitter<any> =
    new EventEmitter();
  form: FormGroup;
  newMenus: Menu[] = [];

  constructor(protected fb: FormBuilder) {
    this.form = this.fb.group([]);
  }

  ngOnInit() {
    if (this.menus) {
      //移除沒有建立子選單的項目
      this.newMenus = JSON.parse(JSON.stringify(this.menus)).filter(menu=> menu.subs && menu.subs.length>0);
      this.newMenus?.forEach((newMenu) => {
        this.form.addControl(
          newMenu.main,
          new FormControl({ value: false, disabled: this.disableCheckbox })
        );
        newMenu.subs?.forEach((subMenu) => {
          let defaultChecked = this.selected.includes(subMenu.name);
          this.form.addControl(
            subMenu.name,
            new FormControl({
              value: defaultChecked,
              disabled: this.disableCheckbox,
            })
          );
        });
      });

      Object.keys(this.form.controls)
        .filter((ctl) => ctl.length == 4)
        .forEach((m) => {
          let defMainChecked = this.checkedGroup(m);
          this.form.controls[m].setValue(defMainChecked);
        });
      // this.form.valueChanges.subscribe(r => console.log(r));
    }
  }

  reset() {
    Object.keys(this.form.controls).forEach((key) => {
      this.form.controls[key].setValue(false);
    });
  }

  menuChange(ev, name) {
    if (name.length == 4) {
      Object.keys(this.form.controls)
        .filter((ctl) => {
          switch (name) {
            default:
              return ctl.indexOf(name) >= 0;

            case 'AC00':
              return (
                ctl.indexOf(name) >= 0 ||
                ctl.indexOf('AC10') >= 0 ||
                ctl.indexOf('AC12') >= 0
              );

            case 'AC02':
              return ctl.indexOf(name) >= 0 || ctl.indexOf('AC11') >= 0;

            case 'AC03':
              return (
                ctl.indexOf(name) >= 0 ||
                ctl.indexOf('AC04') >= 0 ||
                ctl.indexOf('AC08') >= 0
              );

            case 'AC05':
              return ctl.indexOf(name) >= 0 || ctl.indexOf('AC07') >= 0;

            case 'NP01':
              return ctl.indexOf(name) >= 0 || ctl.indexOf('NP12') >= 0;

            case 'NP03':
              return ctl.indexOf(name) >= 0 || ctl.indexOf('NP04') >= 0;
            case 'NP05':
                return ctl.indexOf(name) >= 0 || ctl.indexOf('NP1202') >= 0;
          }
        })
        .forEach((key) => {
          this.form.controls[key].setValue(ev.target.checked);
        });
    } else {
      switch (name.substr(0, 4)) {
        default:
          this.form.controls[name.substr(0, 4)].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'AC10':
        case 'AC12':
          this.form.controls['AC00'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'AC11':
          this.form.controls['AC02'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'AC04':
        case 'AC08':
          this.form.controls['AC03'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'AC07':
          this.form.controls['AC05'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'NP12':
          this.form.controls['NP01'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'NP04':
          this.form.controls['NP03'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
        case 'LAB0':
          this.form.controls['LABS'].setValue(
            this.checkedGroup(name, ev.target.value)
          );
          break;
      }
    }
    if (!this.showButton) this.updateHandler.emit(this.form.value);
  }

  checkedGroup(name: string, checked?: boolean) {
    let groupChecked: boolean = true;
    if (name.length > 4) {
      if (checked) {
        Object.keys(this.form.controls)
          .filter((ctl) => {
            switch (name.substr(0, 4)) {
              // case 'AC10':
              //     return ctl.length > 4 && (ctl.substr(0, 4) == 'AC00' || ctl.substr(0, 4) == 'AC10');
              case 'AC00':
              case 'AC10':
              case 'AC12':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'AC00' ||
                    ctl.substr(0, 4) == 'AC12' ||
                    ctl.substr(0, 4) == 'AC10')
                );

              case 'AC02':
              case 'AC11':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'AC02' || ctl.substr(0, 4) == 'AC11')
                );

              case 'AC04':
              case 'AC08':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'AC03' ||
                    ctl.substr(0, 4) == 'AC04' ||
                    ctl.substr(0, 4) == 'AC08')
                );

              case 'AC05':
              case 'AC07':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'AC05' || ctl.substr(0, 4) == 'AC07')
                );

              case 'NP01':
              case 'NP12':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'NP01' || ctl.substr(0, 4) == 'NP12')
                );

              case 'NP03':
              case 'NP04':
                return (
                  ctl.length > 4 &&
                  (ctl.substr(0, 4) == 'NP03' || ctl.substr(0, 4) == 'NP04')
                );
              default:
                return ctl.length > 4 && ctl.substr(0, 4) == name.substr(0, 4);
            }
          })
          .forEach((key) => {
            let val = this.form.controls[key].value;
            if (!val) groupChecked = val;
          });
      } else {
        groupChecked = checked ? checked : false;
      }
      return groupChecked;
    } else {
      let tmp_menu = Object.keys(this.form.controls).filter((ctl) => {
        switch (name) {
          default:
            return ctl.indexOf(name) >= 0 && ctl != name;
          case 'AC00':
            return (
              (ctl.indexOf(name) >= 0 ||
                ctl.indexOf('AC10') >= 0 ||
                ctl.indexOf('AC12') >= 0) &&
              ctl != name
            );
          case 'AC02':
            return (
              (ctl.indexOf(name) >= 0 || ctl.indexOf('AC11') >= 0) &&
              ctl != name
            );
          case 'AC03':
            return (
              (ctl.indexOf(name) >= 0 ||
                ctl.indexOf('AC04') >= 0 ||
                ctl.indexOf('AC08') >= 0) &&
              ctl != name
            );
          case 'AC05':
            return (
              (ctl.indexOf(name) >= 0 || ctl.indexOf('AC07') >= 0) &&
              ctl != name
            );
          case 'NP01':
            return (
              (ctl.indexOf(name) >= 0 || ctl.indexOf('NP12') >= 0) &&
              ctl != name
            );
          case 'NP03':
            return (
              (ctl.indexOf(name) >= 0 || ctl.indexOf('NP04') >= 0) &&
              ctl != name
            );
        }
      });

      if (tmp_menu.length == 0) {
        groupChecked = false;
      } else {
        tmp_menu.forEach((sub) => {
          let val = this.form.controls[sub].value;
          if (!val) groupChecked = val;
        });
      }

      return groupChecked;
    }
  }

  click() {
    this.updateHandler.emit(this.form.value);
  }
}
