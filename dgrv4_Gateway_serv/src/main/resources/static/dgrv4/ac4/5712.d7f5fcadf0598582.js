"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[5712],{5712:(x,u,o)=>{o.r(u),o.d(u,{AboutModule:()=>D});var e=o(69808),h=o(99291),l=o(86347),t=o(87587),m=o(45682),A=o(64386),f=o(51062),d=o(63710),s=o(93075);function a(r,p){if(1&r&&(t.TgZ(0,"form")(1,"div",2)(2,"div",3)(3,"div",4),t._UZ(4,"img",5)(5,"br"),t.TgZ(6,"h1",6),t._uU(7),t.qZA(),t.TgZ(8,"p",7),t._uU(9),t.qZA(),t.TgZ(10,"p",8),t._uU(11),t.qZA(),t.TgZ(12,"p",9),t._uU(13),t.qZA()()()()()),2&r){const n=t.oxw();t.xp6(6),t.Q6J("hidden",!n.setComp),t.xp6(1),t.Oqu(n.versionInfo.majorVersionNo),t.xp6(1),t.Q6J("hidden",!n.setComp),t.xp6(1),t.AsE(" ",n.edition," Exp\uff1a",n.editionDate,""),t.xp6(1),t.Q6J("hidden",!n.setComp),t.xp6(1),t.hij(" Version\uff1a",n.versionInfo.version,""),t.xp6(1),t.Q6J("hidden",!n.setComp||null==n.versionInfo.account||""==(null==n.versionInfo?null:n.versionInfo.account)),t.xp6(1),t.hij(" Account\uff1a",n.versionInfo.account,"")}}const g=[{path:"",component:(()=>{class r{constructor(n,v,_){this.toolService=n,this.aboutService=v,this.translate=_,this.title="",this.edition="",this.editionDate="",this.setComp=!1,this.translate.get("about").subscribe(T=>this.title=`${T} digiRunner`)}ngOnInit(){this.aboutService.queryModuleVersion().subscribe(n=>{this.toolService.checkDpSuccess(n.ResHeader)&&(this.versionInfo=n.RespBody,this.edition=this.toolService.getAcConfEdition(),this.editionDate=this.toolService.getAcConfExpiryDate(),this.setDataChecker())})}setDataChecker(){if(document.getElementById("center_div")){let _=document.getElementById("center_div").clientWidth/2-document.getElementById("logo_img").clientWidth/2;document.getElementById("edition_p").style.paddingLeft=`${_}px`,document.getElementById("version_p").style.paddingLeft=`${_}px`,document.getElementById("account_p").style.paddingLeft=`${_}px`,this.setComp=!0}else setTimeout(()=>{this.setDataChecker()},0)}onResize(n){this.setDataChecker()}}return r.\u0275fac=function(n){return new(n||r)(t.Y36(m.g),t.Y36(A.G),t.Y36(f.sK))},r.\u0275cmp=t.Xpm({type:r,selectors:[["app-about"]],hostBindings:function(n,v){1&n&&t.NdJ("resize",function(){return v.onResize()},!1,t.Jf7)},decls:2,vars:2,consts:[[3,"title"],[4,"ngIf"],[1,"form-group","row"],[1,"col-12","col-lg-12"],["id","center_div",1,"text-center"],["id","logo_img","src","assets/images/DigiFusion_digiRunner_logo.png","width","250px","alt",""],[3,"hidden"],["id","edition_p",1,"text-left",3,"hidden"],["id","version_p",1,"text-left",3,"hidden"],["id","account_p",1,"text-left",3,"hidden"]],template:function(n,v){1&n&&(t.TgZ(0,"app-container",0),t.YNc(1,a,14,9,"form",1),t.qZA()),2&n&&(t.Q6J("title",v.title),t.xp6(1),t.Q6J("ngIf",v.versionInfo))},directives:[d.e,e.O5,s._Y,s.JL,s.F],styles:[""]}),r})(),canActivate:[l.u6]}];let c=(()=>{class r{}return r.\u0275fac=function(n){return new(n||r)},r.\u0275mod=t.oAB({type:r}),r.\u0275inj=t.cJS({imports:[[h.Bz.forChild(g)],h.Bz]}),r})();var C=o(88893),E=o(24783);let D=(()=>{class r{}return r.\u0275fac=function(n){return new(n||r)},r.\u0275mod=t.oAB({type:r}),r.\u0275inj=t.cJS({providers:[l.u6],imports:[[e.ez,c,C.W,E.m,l.DL,s.UX,s.u5]]}),r})()},63710:(x,u,o)=>{o.d(u,{e:()=>d});var e=o(87587),h=o(69808),l=o(51062);function t(s,a){if(1&s&&(e.TgZ(0,"div",9)(1,"h3",10),e._uU(2),e.ALo(3,"translate"),e.qZA()()),2&s){const i=e.oxw();e.xp6(2),e.Oqu(e.lcZ(3,1,i.title))}}function m(s,a){if(1&s){const i=e.EpF();e.TgZ(0,"div",11)(1,"button",12),e.NdJ("click",function(){return e.CHM(i),e.oxw().return()}),e._UZ(2,"i",13),e._uU(3),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"span",14),e._uU(6),e.qZA(),e.TgZ(7,"span",15),e._uU(8),e.qZA()()}if(2&s){const i=e.oxw();e.xp6(3),e.hij(" ",e.lcZ(4,3,"button.return_to_list")," "),e.xp6(3),e.hij("",i.getHead()," /"),e.xp6(2),e.Oqu(i.getTail())}}const A=[[["","center-view","center"]],"*"],f=["[center-view=center]","*"];let d=(()=>{class s{constructor(){this.title="",this.isDefault=!0,this.headerReturn=new e.vpe}ngOnInit(){}return(){this.headerReturn.emit(null)}getHead(){const i=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return i.pop(),i.join(" / ")}getTail(){const i=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return i[i.length-1]}}return s.\u0275fac=function(i){return new(i||s)},s.\u0275cmp=e.Xpm({type:s,selectors:[["app-container"]],inputs:{title:"title",isDefault:"isDefault"},outputs:{headerReturn:"headerReturn"},ngContentSelectors:f,decls:11,vars:2,consts:[[1,"h-100"],[1,"container-fluid","h-100",2,"padding-left","10px","margin-top","10px"],[1,"row","h-100","position-relative"],[1,"col","pb-5"],[1,"card-title","row"],["class","col-12 col-md-12",4,"ngIf"],["class","col-12 col-md-12","style","text-align: right;",4,"ngIf"],[1,"col","d-flex","justify-content-center"],[1,"my-0","mb-2"],[1,"col-12","col-md-12"],["id","content",1,"bd-title","mb-0"],[1,"col-12","col-md-12",2,"text-align","right"],["type","button","icon","",1,"btn","float-left","tpi-header-return",3,"click"],[1,"fas","fa-arrow-left",2,"margin-right","5px"],[1,"bd-title","mb-0",2,"color","#666464"],[1,"bd-title","mb-0",2,"color","#FF6E38","font-weight","bold"]],template:function(i,g){1&i&&(e.F$t(A),e.TgZ(0,"div",0)(1,"div",1)(2,"div",2)(3,"div",3)(4,"div",4),e.YNc(5,t,4,3,"div",5),e.YNc(6,m,9,5,"div",6),e.TgZ(7,"div",7),e.Hsn(8),e.qZA()(),e._UZ(9,"hr",8),e.Hsn(10,1),e.qZA()()()()),2&i&&(e.xp6(5),e.Q6J("ngIf",g.isDefault),e.xp6(1),e.Q6J("ngIf",!g.isDefault))},directives:[h.O5],pipes:[l.X$],styles:[".card.card-body[_ngcontent-%COMP%]   h3[_ngcontent-%COMP%]{font-size:20px;font-weight:400;color:#5a5541}"]}),s})()},92718:(x,u,o)=>{o.d(u,{u:()=>a});var e=o(54004),h=o(70262),l=o(62843),t=o(87587),m=o(45682),A=o(89709),f=o(99291),d=o(3937),s=o(60991);let a=(()=>{class i{constructor(c,C,E,D,r){this.toolService=c,this.tokenService=C,this.router=E,this.ngxService=D,this.logoutService=r}canActivate(){return this.ngxService.stopAll(),!this.toolService.isTokenExpired()||this.toolService.refreshToken().pipe((0,e.U)(c=>!!c.access_token),(0,h.K)(this.handleError.bind(this)))}handleError(c){return setTimeout(()=>this.logoutService.logout()),(0,l._)(()=>c)}}return i.\u0275fac=function(c){return new(c||i)(t.LFG(m.g),t.LFG(A.B),t.LFG(f.F0),t.LFG(d.LA),t.LFG(s.P))},i.\u0275prov=t.Yz7({token:i,factory:i.\u0275fac}),i})()},86347:(x,u,o)=>{o.d(u,{DL:()=>e.D,u6:()=>l.u});var e=o(81233),l=(o(45240),o(92718));o(45682)}}]);