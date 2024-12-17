"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[6071],{16071:(F,h,o)=>{o.r(h),o.d(h,{IdpssoModule:()=>D});var S=o(18505),p=o(68306),e=o(87587),g=o(99291),A=o(45682),C=o(40520),y=o(4924),R=o(24300),B=o(27896),I=o(55679),b=o(40704),v=o(69808),f=o(48185),T=o(51062);function k(t,a){1&t&&e._UZ(0,"p-progressBar",11)}const U=[{path:"",component:(()=>{class t{constructor(s,i,c,r,d,u,n,l,x){this.route=s,this.toolService=i,this.router=c,this.http=r,this.signBlockService=d,this.userService=u,this.util=n,this.funcService=l,this.alertService=x,this.msg="Loading...",this.btnDisabled=!0,this.processShow=!0}ngOnInit(){this.route.queryParams.subscribe(s=>{s.msg&&null==s.dgRcode&&(this.msg=this.toolService.Base64Decoder(s.msg),this.processShow=!1),s.dgRcode&&this.getToken(s.dgRcode)}),setTimeout(()=>{this.btnDisabled=!1},1e3)}returnToLogin(){this.router.navigate(["/login"]).then(()=>{window.location.reload()})}getToken(s){const i=`${location.protocol}//${location.host}/dgrv4/ssotoken/acidp/oauth/token?dgRcode=${s}`;this.http.get(i).subscribe(c=>{if(c&&c.access_token)this.toolService.setTokenInfo(c),this.toolService.writeToken(c.access_token),this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()),"decode_token"),this.signBlockService.getSignBlock().subscribe(r=>{this.toolService.checkSuccess(r.ResHeader)&&(this.toolService.writeSignBlock(r.Res_getSignBlock.signBlock),this.util.getAcConf().subscribe(d=>{this.toolService.checkDpSuccess(d.ResHeader)&&(this.toolService.writeAcConf(JSON.stringify(d.RespBody)),this.auth(["AC0004","AC0005","AC0102","AC0203","AC0204","AC0205","AC0221","AC0223","AC0224","AC0225","AC0226","AC0302","AC0304","AC0305","AC0318","AC0505"]).subscribe(n=>{this.toolService.setHyperLinkAuth(n)}),this.userService.queryUserDataByLoginUser().subscribe(n=>{this.toolService.checkDpSuccess(n.ResHeader)&&(this.toolService.setUserID(n.RespBody.userID),this.toolService.setUserAlias(n.RespBody.userAlias?n.RespBody.userAlias:""),n.RespBody.idTokenJwtstr&&sessionStorage.setItem("idTokenJwtstr",n.RespBody.idTokenJwtstr),this.setFuncList().pipe((0,S.b)(l=>{l&&this.router.navigateByUrl("/dashboard")})).subscribe())}))}))});else{let r=c.ResHeader;this.alertService.ok(r.rtnCode,r.rtnMsg)}})}auth(s){let i=[];return new p.y(c=>{this.userService.queryFuncByLoginUser().subscribe(r=>{if(this.toolService.checkDpSuccess(r.ResHeader)){this.toolService.writeRoleFuncCodeList(r.RespBody.funcCodeList);for(let d=0;d<s.length;d++){const u=s[d];let n=r.RespBody.funcCodeList.findIndex(l=>l===u);i.findIndex(l=>l.funCode==u)<0?i.push({funCode:u,canExecute:n>=0}):n>=0&&(i[i.findIndex(l=>l.funCode==u)].canExecute=!0)}c.next(i)}})})}setFuncList(){return new p.y(s=>{this.funcService.queryAllFunc().subscribe(i=>{this.toolService.checkDpSuccess(i.ResHeader)&&(this.toolService.setFuncList(i.RespBody.funcList),s.next(!0))})})}}return t.\u0275fac=function(s){return new(s||t)(e.Y36(g.gz),e.Y36(A.g),e.Y36(g.F0),e.Y36(C.eN),e.Y36(y.T),e.Y36(R.K),e.Y36(B.f),e.Y36(I.B),e.Y36(b.c))},t.\u0275cmp=e.Xpm({type:t,selectors:[["app-idpsso"]],decls:17,vars:6,consts:[[1,"login-page"],[1,"image-panel"],["src","assets/images/img_login_visual.png",1,"img-building"],[1,"loading-panel"],[2,"width","380px"],[1,"loading-section"],["src","assets/images/DigiFusion_digiRunner_logo_horizontal.png","alt","","title","v202102251030-v3",1,"img-logo"],[1,"line"],["mode","indeterminate",4,"ngIf"],["type","button",1,"btn","btn-login",3,"disabled","click"],[1,"login-footer"],["mode","indeterminate"]],template:function(s,i){1&s&&(e.TgZ(0,"div",0)(1,"div",1),e._UZ(2,"img",2),e.qZA(),e.TgZ(3,"div",3)(4,"div",4)(5,"section",5),e._UZ(6,"img",6),e.TgZ(7,"div",7)(8,"p"),e._uU(9),e.qZA(),e.YNc(10,k,1,0,"p-progressBar",8),e.qZA(),e.TgZ(11,"button",9),e.NdJ("click",function(){return i.returnToLogin()}),e._uU(12),e.ALo(13,"translate"),e.qZA()()()(),e.TgZ(14,"footer",10)(15,"span"),e._uU(16,"Copyright\xa9 TPIsoftware. All Rights Reserved."),e.qZA()()()),2&s&&(e.xp6(9),e.Oqu(i.msg),e.xp6(1),e.Q6J("ngIf",i.processShow),e.xp6(1),e.Q6J("disabled",i.btnDisabled),e.xp6(1),e.hij(" ",e.lcZ(13,4,"button.confirm")," "))},directives:[v.O5,f.k],pipes:[T.X$],styles:[""]}),t})()}];let L=(()=>{class t{}return t.\u0275fac=function(s){return new(s||t)},t.\u0275mod=e.oAB({type:t}),t.\u0275inj=e.cJS({imports:[[g.Bz.forChild(U)],g.Bz]}),t})();var Z=o(24783),m=o(93075);let D=(()=>{class t{}return t.\u0275fac=function(s){return new(s||t)},t.\u0275mod=e.oAB({type:t}),t.\u0275inj=e.cJS({imports:[[v.ez,L,Z.m,m.UX,m.u5,f.q]]}),t})()}}]);