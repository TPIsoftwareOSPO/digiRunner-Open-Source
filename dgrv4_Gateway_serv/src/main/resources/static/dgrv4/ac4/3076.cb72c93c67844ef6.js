"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[3076],{13076:(z,v,s)=>{s.r(v),s.d(v,{LoginModule:()=>O});var U=s(24783),m=s(69808),d=s(99291),k=s(96614),f=s(55679),A=s(24300),L=s(27896),C=s(4924),Z=s(40704),S=s(45682),b=s(89709),l=s(93075),I=s(18505),y=s(68306),x=s(92340),_=s(51753),e=s(87587),B=s(3937),J=s(40520),T=s(51062);const R=["username"];function P(n,u){if(1&n){const t=e.EpF();e.TgZ(0,"button",23),e.NdJ("click",function(){e.CHM(t);const o=e.oxw().$implicit;return e.oxw().goCusPage(o.cusLoginUrl)}),e._uU(1),e.qZA()}if(2&n){const t=e.oxw().$implicit;e.xp6(1),e.hij(" ",t.acIdpInfoCusName," ")}}function N(n,u){if(1&n&&(e.ynx(0),e.YNc(1,P,2,1,"button",29),e.BQk()),2&n){const t=e.oxw();e.xp6(1),e.Q6J("ngIf",t.cusIdpLoginList.length>0)}}function Y(n,u){if(1&n){const t=e.EpF();e.ynx(0,30),e.TgZ(1,"button",23),e.NdJ("click",function(){return e.CHM(t),e.oxw().goCusPage()}),e._uU(2," CUS "),e.qZA(),e.BQk()}}const F=[{path:"",component:(()=>{class n{constructor(t,i,o,r,a,g,p,c,h,w,D,q,Q,H){this.fb=t,this.router=i,this.ngxService=o,this.tokenService=r,this.toolService=a,this.alertService=g,this.signBlockService=p,this.util=c,this.userService=h,this.funcService=w,this.route=D,this.httpClient=q,this.translate=Q,this.sanitizerService=H,this.isReady=!1,this.relogin=!1,this.cusIdpLoginList=[],this.form=this.fb.group({uname:new l.NI(""),pwd:new l.NI("")})}ngOnInit(){this.route.queryParams.subscribe(t=>{this.relogin=null==t.re}),setTimeout(()=>{this.isReady=!0,setTimeout(()=>{this.username.nativeElement.focus()},0)},1e3),this.getCusLoginUrl()}ngAfterViewInit(){this.username.nativeElement.focus()}getCusLoginUrl(){let t="";t="localhost"==location.hostname||"127.0.0.1"==location.hostname?x.N.apiUrl:`${location.protocol}//${location.hostname}:${location.port}`,t+="/dgrv4/ssotoken/acCusIdp/login/getCusLoginUrl",this.httpClient.get(t,{responseType:"text"}).subscribe(i=>{i&&(this.cusIdpLoginList=JSON.parse(i).map(o=>o))})}goCusPage(t){this.cusIdpLoginList.some(o=>o.cusLoginUrl===t)?this.sanitizerService.navigateUrl(t):this.translate.get("noCusInfo").subscribe(o=>{this.alertService.ok(o,"")})}submitForm(){var t,i,o;""!=(null===(t=this.form.get("uname"))||void 0===t?void 0:t.value)?(this.ngxService.start(),this.tokenService.auth(null===(i=this.form.get("uname"))||void 0===i?void 0:i.value,this.toolService.Base64Encoder(null===(o=this.form.get("pwd"))||void 0===o?void 0:o.value)).subscribe(r=>{if(r&&r.access_token)this.toolService.setTokenInfo(r),this.toolService.writeToken(r.access_token),this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()),"decode_token"),this.signBlockService.getSignBlock().subscribe(a=>{this.toolService.checkSuccess(a.ResHeader)&&(this.toolService.writeSignBlock(a.Res_getSignBlock.signBlock),this.util.getAcConf().subscribe(g=>{this.toolService.checkDpSuccess(g.ResHeader)&&(this.toolService.writeAcConf(JSON.stringify(g.RespBody)),this.auth(["AC0004","AC0005","AC0102","AC0203","AC0204","AC0205","AC0221","AC0223","AC0224","AC0225","AC0226","AC0302","AC0304","AC0305","AC0318","AC0505"]).subscribe(c=>{this.toolService.setHyperLinkAuth(c)}),this.userService.queryUserDataByLoginUser().subscribe(c=>{this.toolService.checkDpSuccess(c.ResHeader)&&(this.toolService.setUserID(c.RespBody.userID),this.toolService.setUserAlias(c.RespBody.userAlias?c.RespBody.userAlias:""),this.setFuncList().pipe((0,I.b)(h=>{h&&this.router.navigateByUrl("/dashboard")})).subscribe())}))}))});else{let a=r.ResHeader;this.alertService.ok(a.rtnCode,a.rtnMsg)}this.ngxService.stopAll()})):this.translate.get("user_name_required").subscribe(r=>{this.alertService.ok(r,"")})}setFuncList(){return new y.y(t=>{this.funcService.queryAllFunc().subscribe(i=>{this.toolService.checkDpSuccess(i.ResHeader)&&(this.toolService.setFuncList(i.RespBody.funcList),t.next(!0))})})}auth(t){let i=[];return new y.y(o=>{this.userService.queryFuncByLoginUser().subscribe(r=>{if(this.toolService.checkDpSuccess(r.ResHeader)){this.toolService.writeRoleFuncCodeList(r.RespBody.funcCodeList);for(let a=0;a<t.length;a++){const g=t[a];let p=r.RespBody.funcCodeList.findIndex(c=>c===g);i.findIndex(c=>c.funCode==g)<0?i.push({funCode:g,canExecute:p>=0}):p>=0&&(i[i.findIndex(c=>c.funCode==g)].canExecute=!0)}o.next(i)}})})}ssologin(t){window.location.href=`${location.protocol}//${location.host}/dgrv4/ssotoken/acidp/${t}/acIdPAuth`}goLdapPage(){this.router.navigate(["/ldap"],{queryParams:{type:"LDAP"}})}goMLdapPage(){this.router.navigate(["/ldap"],{queryParams:{type:"MLDAP"}})}goAPIPage(){this.router.navigate(["/ldap"],{queryParams:{type:"API"}})}}return n.\u0275fac=function(t){return new(t||n)(e.Y36(l.qu),e.Y36(d.F0),e.Y36(B.LA),e.Y36(b.B),e.Y36(S.g),e.Y36(Z.c),e.Y36(C.T),e.Y36(L.f),e.Y36(A.K),e.Y36(f.B),e.Y36(d.gz),e.Y36(J.eN),e.Y36(T.sK),e.Y36(_.Y))},n.\u0275cmp=e.Xpm({type:n,selectors:[["app-login"]],viewQuery:function(t,i){if(1&t&&e.Gf(R,7),2&t){let o;e.iGM(o=e.CRH())&&(i.username=o.first)}},features:[e._Bn([b.B,Z.c,S.g,C.T,L.f,A.K,f.B,k.K,_.Y])],decls:63,vars:27,consts:[[1,"login-page",3,"hidden"],[1,"image-panel"],["src","assets/images/img_login_visual.png",1,"img-building"],[1,"form-panel"],[3,"formGroup","ngSubmit"],[1,"login_section"],["src","assets/images/DigiFusion_digiRunner_logo_horizontal.png","alt","","title","v202102251030-v3",1,"img-logo"],[1,"line"],[3,"hidden"],[1,"form-wrapper"],[1,"form-area"],[1,"input-line"],["for","uname"],[1,"input-sign-wrapper"],[1,"sign"],["src","assets/images/login/i_user.png","alt","number"],["type","text","formControlName","uname",1,"form-control",3,"placeholder"],["username",""],["src","assets/images/login/i_pwd.png","alt","pass"],["type","password","formControlName","pwd",1,"form-control",3,"placeholder"],[1,"sign-btn-grid"],["type","submit",1,"btn","btn-login"],[1,"sign-btn-grid",2,"--grid-columns","1fr 1fr"],["type","button",1,"btn","btn-login",3,"click"],[1,"pi","pi-google"],[1,"pi","pi-microsoft"],[4,"ngFor","ngForOf"],["class","sign_row",4,"ngIf"],[1,"login-footer"],["type","button","class","btn btn-login",3,"click",4,"ngIf"],[1,"sign_row"]],template:function(t,i){1&t&&(e.TgZ(0,"div",0)(1,"div",1),e._UZ(2,"img",2),e.qZA(),e.TgZ(3,"div",3)(4,"form",4),e.NdJ("ngSubmit",function(){return i.submitForm()}),e.TgZ(5,"section",5),e._UZ(6,"img",6),e.TgZ(7,"div",7)(8,"p",8),e._uU(9,"Welcome Back!"),e.qZA(),e.TgZ(10,"div",8)(11,"span"),e._uU(12),e.ALo(13,"translate"),e.qZA(),e.TgZ(14,"span"),e._uU(15),e.ALo(16,"translate"),e.qZA()()(),e.TgZ(17,"div",9)(18,"div",10)(19,"div",11)(20,"label",12),e._uU(21),e.ALo(22,"translate"),e.qZA(),e.TgZ(23,"div",13)(24,"span",14),e._UZ(25,"img",15),e.qZA(),e._UZ(26,"input",16,17),e.ALo(28,"translate"),e.qZA()(),e.TgZ(29,"div",11)(30,"label",12),e._uU(31),e.ALo(32,"translate"),e.qZA(),e.TgZ(33,"div",13)(34,"span",14),e._UZ(35,"img",18),e.qZA(),e._UZ(36,"input",19),e.ALo(37,"translate"),e.qZA()()(),e.TgZ(38,"div",20)(39,"button",21),e._uU(40),e.ALo(41,"translate"),e.qZA()(),e.TgZ(42,"div",22)(43,"button",23),e.NdJ("click",function(){return i.ssologin("GOOGLE")}),e._UZ(44,"i",24),e.qZA(),e.TgZ(45,"button",23),e.NdJ("click",function(){return i.ssologin("MS")}),e._UZ(46,"i",25),e.qZA()(),e.TgZ(47,"div",20)(48,"button",23),e.NdJ("click",function(){return i.ssologin("OIDC")}),e._uU(49," OIDC "),e.qZA()(),e.TgZ(50,"div",22)(51,"button",23),e.NdJ("click",function(){return i.goLdapPage()}),e._uU(52," LDAP "),e.qZA(),e.TgZ(53,"button",23),e.NdJ("click",function(){return i.goMLdapPage()}),e._uU(54," MLDAP "),e.qZA()(),e.TgZ(55,"div",20)(56,"button",23),e.NdJ("click",function(){return i.goAPIPage()}),e._uU(57," API "),e.qZA(),e.YNc(58,N,2,1,"ng-container",26),e.YNc(59,Y,3,0,"ng-container",27),e.qZA()()()()(),e.TgZ(60,"footer",28)(61,"span"),e._uU(62,"Copyright\xa9 TPIsoftware. All Rights Reserved."),e.qZA()()()),2&t&&(e.Q6J("hidden",!i.isReady),e.xp6(4),e.Q6J("formGroup",i.form),e.xp6(4),e.Q6J("hidden",!i.relogin),e.xp6(2),e.Q6J("hidden",i.relogin),e.xp6(2),e.Oqu(e.lcZ(13,13,"logoutBySystem")),e.xp6(3),e.Oqu(e.lcZ(16,15,"plz_login_again")),e.xp6(6),e.Oqu(e.lcZ(22,17,"user_name")),e.xp6(5),e.Q6J("placeholder",e.lcZ(28,19,"user_name")),e.xp6(5),e.Oqu(e.lcZ(32,21,"user_password")),e.xp6(5),e.Q6J("placeholder",e.lcZ(37,23,"user_password")),e.xp6(4),e.hij(" ",e.lcZ(41,25,"login")," "),e.xp6(18),e.Q6J("ngForOf",i.cusIdpLoginList),e.xp6(1),e.Q6J("ngIf",0==i.cusIdpLoginList.length))},directives:[l._Y,l.JL,l.sg,l.Fj,l.JJ,l.u,m.sg,m.O5],pipes:[T.X$],styles:[""]}),n})()}];let M=(()=>{class n{}return n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=e.oAB({type:n}),n.\u0275inj=e.cJS({imports:[[d.Bz.forChild(F)],d.Bz]}),n})(),O=(()=>{class n{}return n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=e.oAB({type:n}),n.\u0275inj=e.cJS({imports:[[m.ez,M,l.u5,l.UX,U.m]]}),n})()}}]);