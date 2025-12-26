import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Router } from '@angular/router';
import { ToolService } from '../services/tool.service';
import { UserService } from '../services/api-user.service';
import { Observable } from 'rxjs';

@Injectable()
export class Ac0505Guard implements CanActivate {
    constructor(
        private router: Router,
        private toolService: ToolService,
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
          return this.toolService.getHyperLinkAuth()?.find(r => r.funCode === 'AC0505')!.canExecute;

    }


}
