import { AlertService } from 'src/app/shared/services/alert.service';
import { ResHeader } from 'src/app/models/api/base.header.interface';
import { FuncService } from 'src/app/shared/services/api-func.service';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { Observable, tap } from 'rxjs';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { HttpClient } from '@angular/common/http';
import { ToolService } from './../../shared/services/tool.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-idpsso',
  templateUrl: './idpsso.component.html',
  styleUrls: ['./idpsso.component.scss']
})
export class IdpssoComponent implements OnInit {
  msg: string = 'Loading...';
  btnDisabled:boolean = true;
  processShow:boolean = true;
  constructor(
    private route: ActivatedRoute,
    private toolService: ToolService,
    private router: Router,
    private http:HttpClient,
    private signBlockService: SignBlockService,
    private userService: UserService,
    private util: UtilService,
    private funcService: FuncService,
    private alertService: AlertService
  ) { }

  ngOnInit(): void {
    // console.log('udpsso login...')

    this.route.queryParams.subscribe((value) => {
      if (value['msg'] && value['dgRcode']== undefined) {
        this.msg = this.toolService.Base64Decoder(value['msg'])
        this.processShow = false;
      }

      if(value['dgRcode'])
      {
         this.getToken(value['dgRcode']);
      }
    })

    setTimeout(() => {
      this.btnDisabled = false;
    }, 1000);
  }

  returnToLogin() {
    // console.log('go login')
     this.router.navigate(['/login']).then(()=>{
      window.location.reload();
     })

  }

  getToken(dgrCode:string){
    // https://10.20.30.88:18442/dgrv4/ssotoken/acidp/oauth/token?dgRcode=Mini123
    // const tarUrl = `https://10.20.30.88:18442/dgrv4/ssotoken/acidp/oauth/token?dgRcode=${dgrCode}`
    const tarUrl = `${location.protocol}//${location.host}/dgrv4/ssotoken/acidp/oauth/token?dgRcode=${dgrCode}`
    this.http.get<ResToken>(tarUrl).subscribe( (r:ResToken)=>{
      if (r && r.access_token) {
        this.toolService.setTokenInfo(r);
        this.toolService.writeToken(r.access_token);
        this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()), "decode_token");

        this.signBlockService.getSignBlock().subscribe(resSB => {
          if (this.toolService.checkSuccess(resSB.ResHeader)) {
            this.toolService.writeSignBlock(resSB.Res_getSignBlock.signBlock);
            this.util.getAcConf().subscribe(res => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.toolService.writeAcConf(JSON.stringify(res.RespBody));
                //check由按鈕超連結的頁面是否有權限
                let checks = ['AC0004', 'AC0005', 'AC0102', 'AC0203', 'AC0204', 'AC0205', 'AC0221', 'AC0223', 'AC0224', 'AC0225', 'AC0226', 'AC0302', 'AC0304', 'AC0305', 'AC0318', 'AC0505'];
                this.auth(checks).subscribe(r => {
                  this.toolService.setHyperLinkAuth(r);
                })
                this.userService.queryUserDataByLoginUser().subscribe(r => {
                  if (this.toolService.checkDpSuccess(r.ResHeader)) {
                    this.toolService.setUserID(r.RespBody.userID);
                    if (r.RespBody.userAlias) {
                      this.toolService.setUserAlias(r.RespBody.userAlias);
                    }
                    else {
                      this.toolService.setUserAlias('');
                    }

                    if(r.RespBody.idTokenJwtstr) sessionStorage.setItem('idTokenJwtstr', r.RespBody.idTokenJwtstr);

                    this.setFuncList().pipe(
                      tap(r => {
                        if (r) this.router.navigateByUrl('/dashboard');
                      })
                    ).subscribe()
                  }
                });
              }
            });
          }
        });

      }
      else {
        let header = r['ResHeader'] as ResHeader;
        this.alertService.ok(header.rtnCode, header.rtnMsg);
      }
    })
  }

  /**
* 判斷使用者有無權限執行相關動作
*/
public auth(funCodes: string[]): Observable<{ funCode: string, canExecute: boolean }[]> {
  // let result: boolean = false;
  let result: { funCode: string, canExecute: boolean }[] = [];
  return new Observable(obser => {
    this.userService.queryFuncByLoginUser().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.toolService.writeRoleFuncCodeList(res.RespBody.funcCodeList);
        for (let j = 0; j < funCodes.length; j++) {
          const funCode = funCodes[j];
          let idxUp = res.RespBody.funcCodeList.findIndex((f:string) => f === funCode);
          // 權限做聯集
          if (result.findIndex(obj => obj.funCode == funCode) < 0) {
            result.push({
              funCode: funCode,
              canExecute: (idxUp >= 0)
            });
          }
          else {
            if (idxUp >= 0) {
              result[result.findIndex(obj => obj.funCode == funCode)].canExecute = true;
            }
          }
        };
        obser.next(result);
      }
    });
  });
}

setFuncList(): Observable<boolean> {
  return new Observable(obser => {
    this.funcService.queryAllFunc().subscribe(r => {
      if (this.toolService.checkDpSuccess(r.ResHeader)) {
        this.toolService.setFuncList(r.RespBody.funcList);
        obser.next(true);
      }
    });
  });
};
}
