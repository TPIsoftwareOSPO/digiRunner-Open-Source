import { LogoutService } from './../services/logout.service';
import { TokenService } from './../services/api-token.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { CanActivate, Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { GrantType } from 'src/app/models/common.enum';
import { tap, map, catchError } from 'rxjs/operators';
import { throwError, Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Injectable()
export class TokenExpiredGuard implements CanActivate {
    constructor(
        private toolService: ToolService,
        private tokenService: TokenService,
        private router: Router,
        private ngxService: NgxUiLoaderService,
        private logoutService: LogoutService
    ) {

    }
    canActivate() {
      this.ngxService.stopAll();
      if (this.toolService.isTokenExpired()) {
          return this.toolService.refreshToken().pipe(
              map((r) => {
                  if (r.access_token)
                      return true
                  else
                      return false;
              }),
              catchError( this.handleError.bind(this) )
          )
      };
      return true;
    }
  // hanldeError(error: HttpErrorResponse) {
  //     if (error.status == 401) {
  //         setTimeout(() => this.router.navigate(['/login']));
  //         throwError('token expired,redirect to login page');
  //     }
  // }

  handleError(err: HttpErrorResponse): Observable<never> {
    // console.log(err.error)
    // if (err.status == 401) {
    //           setTimeout(() => this.logoutService.logout());
    //           // throwError('token expired,redirect to login page');
    //           return throwError(() => err);
    // }
    setTimeout(() => this.logoutService.logout());

    return throwError(() => err);
  }



}
