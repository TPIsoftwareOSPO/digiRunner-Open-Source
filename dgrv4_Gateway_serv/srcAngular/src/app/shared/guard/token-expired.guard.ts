import { LogoutService } from './../services/logout.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { CanActivate, Router } from '@angular/router';
import { inject, Injectable, Injector } from '@angular/core';
import { tap, map, catchError, concatMap } from 'rxjs/operators';
import { throwError, Observable, of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TokenService } from '../services/api-token.service';
import { GrantType } from 'src/app/models/common.enum';

@Injectable()
export class TokenExpiredGuard implements CanActivate {
  private ngxService = inject(NgxUiLoaderService);

  constructor(
    private injector: Injector
  ) // private ngxService: NgxUiLoaderService,
  {}

  private get logoutService():LogoutService{
     return this.injector.get(LogoutService);
  }

  private get toolService(): ToolService {
      return this.injector.get(ToolService);
  }

  private get tokenService(): TokenService {
      return this.injector.get(TokenService);
  }



  isTokenExpired(token: string = 'access_token'): boolean {
    let jwtStr = sessionStorage.getItem(token) ?? undefined;
    if (jwtStr) {
      return this.toolService.checkTokenExpired(jwtStr); // token expired?
    } else {
      return true; // no token
    }
  }

  refreshToken() {
      return this.tokenService.auth('', '', GrantType.refresh_token).pipe(
        concatMap((r) => {
          this.toolService.setTokenInfo(r);
          this.toolService.writeToken(r.access_token);
          this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()), 'decode_token');
          return of(r);
        })
      );
    }

  canActivate() {
    this.ngxService.stopAll();
    if (this.isTokenExpired()) {
      return this.refreshToken().pipe(
        map((r) => {
          if (r.access_token) return true;
          else return false;
        }),
        catchError(this.handleError.bind(this))
      );
    }
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
