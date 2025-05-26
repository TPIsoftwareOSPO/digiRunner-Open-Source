import { TranslateService } from '@ngx-translate/core';
import { UserService } from 'src/app/shared/services/api-user.service';
import { Component, OnInit } from '@angular/core';
import { AA0003Resp, AA0003Req } from 'src/app/models/api/UserService/aa0003.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as base64 from 'js-base64';
import * as dayjs from 'dayjs';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

    title: string = '';
    user?: AA0003Resp;

    idTokenJwtstr:string= sessionStorage.getItem('idTokenJwtstr')?? '';
    tokenData:{[key:string]:any}[] = [];
    isidTokenForm:boolean = false;

    constructor(
        private userService: UserService,
        private toolService: ToolService,
        private translate: TranslateService
    ) {
        this.translate.get('profile').subscribe(dict => this.title = dict)
    }

    ngOnInit() {
        let ReqBody = {
            userID: this.toolService.getUserID(),
            userName: this.toolService.getUserName()
        } as AA0003Req;
        this.userService.queryTUserDetail(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.user = res.RespBody;
            }
        });


        if(this.idTokenJwtstr!='')
        {
          const tmpTokenPool:string[] =  this.idTokenJwtstr.split('.');
          // console.log(this.tool.base64_decode(tmpTokenPool[1]))
          const strToken:string = tmpTokenPool.length>1 ? base64.Base64.decode(tmpTokenPool[1]) : '';

          if(strToken!='')
          {
            const jsonToken = JSON.parse(strToken);
            if(jsonToken){
              this.isidTokenForm = true;
              Object.keys(jsonToken).map(key => {
                this.tokenData.push({'label': key , 'value':jsonToken[key] } )
              })
              console.log(this.tokenData)
              console.log(new Date().getTime())
            }
          }
        }
    }

    formateDate(date:Date){
      const procDate = Number(date)*1000;
      return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss'): '';
    }

}
