import { LogoutService } from './../services/logout.service';
import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Router } from '@angular/router';

@Injectable()
export class AuthGuard implements CanActivate {
    constructor(private router: Router,  private logoutService: LogoutService) {}

    canActivate() {
      console.log('isLoggedin',sessionStorage.getItem('isLoggedin'))
      // console.log(sessionStorage.getItem('isLoggedin')!.toString() == 'true')
        if (sessionStorage.getItem('isLoggedin') && sessionStorage.getItem('isLoggedin')?.toString() == 'true') {
            return true;
        }

        // this.router.navigate(['/login']);
        this.logoutService.logout();
        return false;
    }
}
