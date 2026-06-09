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
import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app.routing';
import { AppComponent } from './app.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { NgxUiLoaderModule, SPINNER } from 'ngx-ui-loader';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { definePreset } from '@primeng/themes';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

export const MyPreset = definePreset(Aura, {
    semantic: {
        primary: {
            50: '{orange.50}',
            100: '{orange.100}',
            200: '{orange.200}',
            300: '{orange.300}',
            400: '{orange.400}',
            500: '{orange.500}',
            600: '{orange.600}',
            700: '{orange.700}',
            800: '{orange.800}',
            900: '{orange.900}',
            950: '{orange.950}'
        }
    }
});

@NgModule({
  declarations: [AppComponent],
  bootstrap: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    AppRoutingModule,
    SharedModule.forRoot(),
    PrimengModule,
    NgxUiLoaderModule.forRoot({ fgsType: SPINNER.ballSpinClockwise }),
    TranslateModule.forRoot(),
  ],
  providers: [
    providePrimeNG({
      theme: {
        preset: MyPreset,
        options: {
          darkModeSelector: false,
          cssLayer: true,
        },
      },
    }),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,
    },
    AutoLoginGuard,
    TokenService,
    ToolService,
    UserService,
    SignBlockService,
    UtilService,
    provideHttpClient(withInterceptorsFromDi()),
    provideTranslateHttpLoader({
            prefix: './assets/i18n/',
            suffix: '.json'
        }),
  ],
})
export class AppModule {}
