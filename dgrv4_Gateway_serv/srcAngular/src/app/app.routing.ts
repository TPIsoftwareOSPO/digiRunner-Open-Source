import { AutoLoginGuard } from './shared/guard/auto-login.guard';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [


  { path: 'login', loadChildren: () => import('./layout/login/login.module').then(m => m.LoginModule) ,canActivate:[AutoLoginGuard]},
  { path: 'login2', loadChildren: () => import('./layout/login/redirect/redirect.module').then(m => m.RedirectModule) },
  { path: 'idpsso/errMsg', loadChildren: () => import('./layout/idpsso/idpsso.module').then(m => m.IdpssoModule)},
  { path: 'idpsso/accallback', loadChildren: () => import('./layout/idpsso/idpsso.module').then(m => m.IdpssoModule)},
  { path: 'ldap', loadChildren: () => import('./layout/login/ldap/ldap.module').then(m => m.LdapModule)},
  { path: 'gtwidp/:type/:action', loadChildren: () => import('./layout/login/gtwidp/gtwidp.module').then(m => m.GtwidpModule)},
  { path: 'gtwidp/:type', loadChildren: () => import('./layout/login/gtwidp/gtwidp.module').then(m => m.GtwidpModule)},
  { path: '', loadChildren: () => import('./layout/layout.module').then(m => m.LayoutModule)},
  { path: '**', redirectTo: '/'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
