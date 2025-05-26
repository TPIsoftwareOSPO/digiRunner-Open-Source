import { AA0510Resp } from './../../models/api/UtilService/aa0510.interface';
import { LogoutService } from './logout.service';
import { ResToken } from '../../models/api/TokenService/token.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from "@angular/core";
import { environment } from 'src/environments/environment';
import { of, throwError, Observable } from 'rxjs';
import { GrantType } from 'src/app/models/common.enum';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Injectable()
export class TokenService {

    headers: HttpHeaders = new HttpHeaders();

    moduleName:string = environment.isv4 ? 'dgrv4/': 'tsmpdpaa/';

    public get basePath(): string {

        return this.moduleName + 'tptoken/oauth/token';
    }

    public get testApiPath(): string {
        return environment.isv4 ? `oauth/token` : this.moduleName + `oauth/token`;
    }

    public get ssoPath(): string {
        return this.moduleName + 'ssotoken/oauth/token';
    }

    constructor(
        private http: HttpClient,
        private router: Router,
        private alert: AlertService,
        private ngxService: NgxUiLoaderService,
    ) {
        // this.headers = new HttpHeaders()//.set('Content-Type', 'multipart/form-data');
    }

    dataEncryption(data:string) {
      let formData = new FormData;
      formData.append('data',data)
      let url = `${environment.apiUrl}/dataEncryption`;

      return this.http.post<ResToken>(url, formData).pipe(
        catchError(this.handleError.bind(this))
    )

    }

    auth(username:string , mima: string, grant_type: GrantType = GrantType.mima, appendUrl: string = "")
    {
        try {
            let refresh_token= sessionStorage.getItem('refresh_token')??'';
            if (refresh_token=='' && grant_type == GrantType.refresh_token) {
                // this.router.navigateByUrl('/login');
                // return throwError('no refresh_token in storage');
                console.log('no refresh_token in storage');
                throw new Error("");

                // return throwError(()=>Error('no refresh_token in storage'));
            }

            let body = new FormData;
            this.headers = new HttpHeaders()
            // .set("Authorization", "Basic " + btoa('YWRtaW5Db25zb2xl:dHNtcDEyMw'))

            body.append('grant_type', grant_type);
            // console.log(grant_type);
            switch (grant_type) {

                case GrantType.mima:
                    body.append('username', username);
                    body.append('password', mima);
                    break;
                case GrantType.refresh_token:
                    body.append('refresh_token', sessionStorage.getItem('refresh_token')??'');

                    break;
                case GrantType.client_credentials:
                    let data = username + ":" + mima;
                    this.headers = this.headers.append("Authorization", "Basic " + btoa(data));
                    // this.headers = this.headers.append("Content-Type", "application/x-www-form-urlencoded");
                    break;

            }

            let url = `${environment.apiUrl}/${this.basePath}`;

            if (appendUrl != "") { url = url + "?" + appendUrl; }
            return this.http.post<ResToken>(url, body, { headers: this.headers }).pipe(
                catchError(this.handleError.bind(this))
            )
            // return of({
            //     "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub2RlIjoidTdnbiIsImF1ZCI6WyJZV1J0YVc1QlVFayJdLCJ1c2VyX25hbWUiOiJtYW5hZ2VyIiwic2NvcGUiOlsic2VsZWN0Il0sImV4cCI6MTU0MDE5NzExMiwiYXV0aG9yaXRpZXMiOlsiQURNSU4iXSwianRpIjoiNTUyYzdkNGItMmUyZS00MTdjLWExYjktYjg3MzI3MjNlYzVkIiwiY2xpZW50X2lkIjoiWVdSdGFXNURiMjV6YjJ4bCJ9.j6rxjAwkvVPpgwltn0TKFsCrBVl014XkghJlzifb9W4",
            //     "token_type": "bearer",
            //     "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub2RlIjoidTdnbiIsImF1ZCI6WyJZV1J0YVc1QlVFayJdLCJ1c2VyX25hbWUiOiJtYW5hZ2VyIiwic2NvcGUiOlsic2VsZWN0Il0sImF0aSI6IjU1MmM3ZDRiLTJlMmUtNDE3Yy1hMWI5LWI4NzMyNzIzZWM1ZCIsImV4cCI6MTU0Mjc4ODgxMiwiYXV0aG9yaXRpZXMiOlsiQURNSU4iXSwianRpIjoiZWJhZGYwMGMtMDBjZC00YTQzLWJhMzUtOWVmOTM2MjMxZjEwIiwiY2xpZW50X2lkIjoiWVdSdGFXNURiMjV6YjJ4bCJ9.fP7D9hdaz0G7hoDu6rjVh0t6hwIrXTZxN36bU7mJSwE",
            //     "expires_in": 299,
            //     "scope": "select",
            //     "node": "u7gn",
            //     "jti": "552c7d4b-2e2e-417c-a1b9-b8732723ec5d"
            //   } as ResToken);

        } catch (error) {
            // this.router.navigateByUrl('/login');
            this.logout();
            return throwError('');
        }

    }
    handleError(handleError: HttpErrorResponse) {
        // this.alert.ok(handleError.error['error'],handleError.error['error_description']);
        this.ngxService.stopAll();
        return throwError(()=> handleError);
    }

    public authBytestApi(clientid: string, clientmima: string, username?: string, mima?: string, grant_type: GrantType = GrantType.mima) {
        try {
            let body = new FormData;
            // this.headers = new HttpHeaders()
            let data = clientid + ":" + clientmima;
            // let testHeader = new  HttpHeaders();
            // testHeader = testHeader.append("Authorization_chad", "Basic " + btoa(data));
            // testHeader = testHeader.append("Authorization", "Basic " + btoa(data));

            const testHeader = new  HttpHeaders({
              'Authorization': `Basic ${btoa(data)}`,
              'digiRunner' : 'Test Process' //api測試區標記，在token intercepter內辨識流程用，用來區分是由api測試區打出的api
            });


            body.append('grant_type', grant_type);
            switch (grant_type) {
                case GrantType.mima:
                    body.append('username', username??'');
                    body.append('password', mima??'');
                    break;
                case GrantType.refresh_token:
                    break;
                case GrantType.client_credentials:
                    break;

            }

            // this.toolService.writeToken('access_token_b', this.toolService.getToken() )
            // this.toolService.writeToken('access_token', `Basic ${btoa(data)}`)

            return this.http.post<ResToken>(`${environment.apiUrl}/${this.testApiPath}`, body, { headers: testHeader}, ).pipe(
                catchError(this.handleError.bind(this))
            )

        } catch (error) {
            // this.router.navigateByUrl('/login');

            return throwError(()=> new Error('Auth by test api Error'));
        }
    }


    getSsotoken(grant_type: GrantType = GrantType.mima, username?: string, codeVerifier?: string, userMail?:string , refresh_token ?: string ) {
        try {
            let body = new FormData;
            this.headers = new HttpHeaders();

            body.append('grant_type', grant_type);

            switch (grant_type) {
                case GrantType.mima:
                    body.append('username', username??'');
                    body.append('codeVerifier', codeVerifier??'');
                    body.append('userMail', userMail??'');
                    break;
                case GrantType.refresh_token:
                    body.append('refresh_token', sessionStorage.getItem('refresh_token')??'');
                    break;

            }

            let url = `${environment.apiUrl}/${this.ssoPath}`;

            return this.http.post<ResToken>(url, body, { headers: this.headers }).pipe(
                catchError(this.handleError.bind(this))
            )
        }
        catch (error)
        {
            // this.router.navigateByUrl('/login');
            this.logout();
            return throwError;
        }
    }

    logout(){
      const logoutUrl: string | undefined = sessionStorage.getItem("AcConf") ? (JSON.parse(sessionStorage.getItem("AcConf")!) as AA0510Resp).logoutUrl : undefined;
      sessionStorage.clear();
      if (logoutUrl && logoutUrl != '') {
          window.location.href = logoutUrl;
      }
      else {
          this.router.navigate(['/login'])
          // .then(()=>{
          //   window.location.reload();
          // });
      }
      this.ngxService.stopAll();
  }

}

