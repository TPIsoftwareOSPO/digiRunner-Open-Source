import { FormsModule } from '@angular/forms';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { TokenInterceptor } from './shared/Interceptors/token-interceptor';
import { PrimengModule } from './shared/primeng.module';
import { AutoLoginGuard } from './shared/guard/auto-login.guard';
import { SharedModule } from './shared/shared.module';
import { HttpClient, HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app.routing';
import { AppComponent } from './app.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { NgxUiLoaderModule, SPINNER } from 'ngx-ui-loader';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// import { HttpClientModule } from '@angular/common/http';
import { JwtModule } from '@auth0/angular-jwt';


@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    SharedModule.forRoot(),
    PrimengModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (createTranslateLoader),
        deps: [HttpClient]
      }
    }),

    JwtModule.forRoot({
      config: {
        tokenGetter: jwtTokenGetter,
        allowedDomains: ['localhost:4200' , 'localhost:8080'],
        disallowedRoutes: ['/oauth/token'],
        // whitelistedDomains: ['localhost:4200']
        headerName:"TPI"
      }
    }),
    NgxUiLoaderModule.forRoot({ fgsType: SPINNER.ballSpinClockwise }),

  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    AutoLoginGuard,
    TokenService, ToolService, UserService, SignBlockService, UtilService,
    // MyCustomHttpClient
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

export function jwtTokenGetter() {
  return sessionStorage.getItem('access_token');
}

