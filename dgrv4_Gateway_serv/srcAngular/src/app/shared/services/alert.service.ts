import { Injectable } from '@angular/core';
import { AlertType } from 'src/app/models/common.enum';
import { Observable, from, zip } from 'rxjs';
import { Router } from '@angular/router';
import { DialogService } from 'primeng/dynamicdialog';
import { CustomAlertComponent } from '../custom-alert/custom-alert.component';

@Injectable()
export class AlertService {
  // opened : boolean;

  constructor(private router: Router, private dialogService: DialogService) {
    // this.opened = false;
  }
  ok(
    title: string,
    text: string,
    type: AlertType = AlertType.warning,
    html?: string
  ) {
    // if (!this.opened){
    // Swal.fire({
    //     title: title,
    //     text: text,
    //     icon: type,
    //     html: html,
    //     // target: document.getElementById('form-modal')??'',
    //     // showCancelButton: true,
    //     // confirmButtonText: 'Yes, delete it!',
    //     // cancelButtonText: 'No, keep it'
    // }).then((result) => {
    //     // this.opened = false;
    // })
    // this.opened = true;
    // }
    const ref = this.dialogService.open(CustomAlertComponent, {
      data: {
        title: title,
        text: text,
        type: type,
        html: html,
      },
      showHeader: false,
    });
    // ref.onClose.subscribe((res) => {console.log(res)});
  }

  // confirm(title: string, text: string, confirmObservable?: Observable<any>, cancelObservable?: Observable<any>) {

  //     // if (!this.opened){
  //     Swal.fire({
  //         title: title,
  //         text: text,
  //         icon: AlertType.question,
  //         showConfirmButton: true,
  //         showCancelButton: true,

  //     }).then((result) => {
  //         // this.opened = false;
  //         if (result.value) {
  //             if (confirmObservable) {
  //                 confirmObservable.subscribe();
  //             }
  //         } else {
  //             if (cancelObservable) cancelObservable.subscribe();
  //         }
  //     })
  //     // this.opened = true;
  //     // }
  // }

  error(title: string, text: string, type: AlertType = AlertType.error) {
    // Swal.fire({
    //   title: title,
    //   text: text,
    //   icon: type,
    // }).then((result) => {});
    const ref = this.dialogService.open(CustomAlertComponent, {
      data: {
        title: title,
        text: text,
        type: type,
      },
      showHeader: false,
    });
  }

  logout(title: string, text: string, type: AlertType = AlertType.success) {
    // console.log(document.getElementById('form-modal'))
    // Swal.fire({
    //   title: title,
    //   text: text,
    //   icon: type,
    // }).then((result) => {
    //   this.router.navigateByUrl('/login');
    // });
    const ref = this.dialogService.open(CustomAlertComponent, {
      data: {
        title: title,
        text: text,
        type: type,
      },
      showHeader: false,
    });
     ref.onClose.subscribe((res) => {
        this.router.navigateByUrl('/login');
     });
  }
}
