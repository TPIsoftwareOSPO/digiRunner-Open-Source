import { Component, HostListener, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0118Resp } from 'src/app/models/api/AboutService/dpb0118.interface';
import { AboutService } from 'src/app/shared/services/api-about.service';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css'],
})
export class AboutComponent implements OnInit {
  versionInfo?: DPB0118Resp;
  title: string = '';
  edition: string = '';
  editionDate: string = '';
  setComp: boolean = false;
  constructor(
    private toolService: ToolService,
    private aboutService: AboutService,
    private translate: TranslateService,
    private ngxSrvice: NgxUiLoaderService
  ) {
    this.translate
      .get('about')
      .subscribe((dict) => (this.title = `${dict} digiRunner`));
  }

  ngOnInit() {
    // this.ngxSrvice.start();
    setTimeout(() => {
      this.aboutService.queryModuleVersion().subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.versionInfo = res.RespBody;
          this.edition = this.toolService.getAcConfEdition();
          this.editionDate = this.toolService.getAcConfExpiryDate();
          this.setDataChecker();
        }
      });
      // this.ngxSrvice.stop();
    }, 500);
  }

  setDataChecker() {
    // console.log('set version data',document.getElementById('center_div'))
    if (document.getElementById('center_div')) {
      let containerWidth = document.getElementById('center_div')!.clientWidth;
      let imgWidth = document.getElementById('logo_img')!.clientWidth;
      let x = containerWidth / 2 - imgWidth / 2;
      document.getElementById('edition_p')!.style.paddingLeft = `${x}px`;
      document.getElementById('version_p')!.style.paddingLeft = `${x}px`;
      document.getElementById('account_p')!.style.paddingLeft = `${x}px`;
      // this.versionInfo?.dataList?.forEach((item,index) => {
      //     document.getElementById(`${index}_p`)!.style.paddingLeft = `${x}px`;

      // });
      this.setComp = true;
    } else {
      setTimeout(() => {
        this.setDataChecker();
      }, 0);
    }
  }

  // ngAfterViewChecked() {
  //     if (document.getElementById('center_div')) {
  //         let containerWidth = document.getElementById('center_div').clientWidth;
  //         let imgWidth = document.getElementById('logo_img').clientWidth;
  //         let x = (containerWidth / 2) - (imgWidth / 2);
  //         this.versionInfo.dataList.map(item => {
  //             document.getElementById(`${item.moduleName}_p`).style.paddingLeft = `${x}px`;
  //             document.getElementById('edition_p').style.paddingLeft = `${x}px`;
  //         });
  //     }
  // }

  @HostListener('window:resize') //Xing20181026a
  onResize(event) {
    this.setDataChecker();
    // window.setTimeout(() => {
    //     let containerWidth = document.getElementById('center_div').clientWidth;
    //     let imgWidth = document.getElementById('logo_img').clientWidth;
    //     let x = (containerWidth / 2) - (imgWidth / 2);
    //     this.versionInfo.dataList.map(item => {
    //         document.getElementById(`${item.moduleName}_p`).style.paddingLeft = `${x}px`;
    //         document.getElementById('edition_p').style.paddingLeft = `${x}px`;
    //     });
    // }, 100);
  }
}
