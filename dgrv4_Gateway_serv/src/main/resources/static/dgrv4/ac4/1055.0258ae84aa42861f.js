"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[1055],{91055:(T,A,n)=>{n.r(A),n.d(A,{Ac1116Module:()=>te});var l=n(69808),p=n(99291),g=n(15861),d=n(93075),b=n(14525),e=n(87587),R=n(56435),s=n(1955),a=n(45682),c=n(92340),u=n(57553),q=n(96614);let f=(()=>{class o{constructor(i){this.api=i,this.api.baseUrl=c.N.dpPath}get basePath(){return c.N.isv4?"dgrv4/11":"tsmpdpaa/11"}addSecurityLevel_before(){let i={ReqHeader:this.api.getReqHeader(u.Nx.addSecurityLevel),ReqBody:{}};return this.api.npPost(`${this.basePath}/AA1101?before`,i)}addSecurityLevel(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.addSecurityLevel),ReqBody:i};return this.api.npPost(`${this.basePath}/AA1101`,t)}updateSecurityLevel_before(){let i={ReqHeader:this.api.getReqHeader(u.Nx.updateSecurityLevel),ReqBody:{}};return this.api.npPost(`${this.basePath}/AA1103?before`,i)}updateSecurityLevel(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.updateSecurityLevel),ReqBody:i};return this.api.npPost(`${this.basePath}/AA1103`,t)}deleteSecurityLevel(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.deleteSecurityLevel),ReqBody:i};return this.api.npPost(`${this.basePath}/AA1104`,t)}querySecurityLevelDetail(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.querySecurityLevelDetail),ReqBody:i};return this.api.npPost(`${this.basePath}/AA1105`,t)}querySecurityLevelList_ignore1298(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.querySecurityLevelList),ReqBody:i};return this.api.excuteNpPost_ignore1298(`${this.basePath}/AA1116`,t)}querySecurityLevelList(i){let t={ReqHeader:this.api.getReqHeader(u.Nx.querySecurityLevelList),ReqBody:i};return this.api.npPost(`${this.basePath}/AA1116`,t)}}return o.\u0275fac=function(i){return new(i||o)(e.LFG(q.K))},o.\u0275prov=e.Yz7({token:o,factory:o.\u0275fac,providedIn:"root"}),o})();var Z=n(59783),x=n(63710),N=n(23099),S=n(40845),P=n(4119),D=n(17773),C=n(51062);function I(o,v){1&o&&e._UZ(0,"col"),2&o&&e.Udp("width",v.$implicit.width)}function B(o,v){if(1&o&&(e.TgZ(0,"colgroup"),e.YNc(1,I,1,2,"col",35),e.qZA(),e.TgZ(2,"colgroup",36),e._uU(3),e.ALo(4,"translate"),e.qZA()),2&o){const i=v.$implicit;e.xp6(1),e.Q6J("ngForOf",i),e.xp6(2),e.Oqu(e.lcZ(4,2,"action"))}}function O(o,v){if(1&o&&(e.TgZ(0,"th",39),e._uU(1),e.qZA()),2&o){const i=v.$implicit;e.xp6(1),e.hij(" ",i.header," ")}}function M(o,v){if(1&o&&(e.TgZ(0,"tr"),e.YNc(1,O,2,1,"th",37),e.TgZ(2,"th",38),e._uU(3),e.ALo(4,"translate"),e.qZA()()),2&o){const i=v.$implicit;e.xp6(1),e.Q6J("ngForOf",i),e.xp6(2),e.Oqu(e.lcZ(4,2,"action"))}}function H(o,v){if(1&o&&(e.TgZ(0,"td")(1,"span"),e._uU(2),e.qZA()()),2&o){const i=v.$implicit,t=e.oxw().$implicit;e.xp6(2),e.Oqu(t[i.field])}}function U(o,v){if(1&o){const i=e.EpF();e.TgZ(0,"tr"),e.YNc(1,H,3,1,"td",40),e.TgZ(2,"td",41)(3,"button",42),e.NdJ("click",function(){const _=e.CHM(i).$implicit;return e.oxw(2).changePage("update",_)}),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"button",43),e.NdJ("click",function(){const _=e.CHM(i).$implicit;return e.oxw(2).changePage("delete",_)}),e.ALo(6,"translate"),e.qZA()()()}if(2&o){const i=v.columns;e.xp6(1),e.Q6J("ngForOf",i),e.xp6(2),e.Q6J("pTooltip",e.lcZ(4,3,"button.update")),e.xp6(2),e.Q6J("pTooltip",e.lcZ(6,5,"button.delete"))}}function w(o,v){if(1&o){const i=e.EpF();e.TgZ(0,"tr")(1,"td",45)(2,"span",46),e._uU(3),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"button",47),e.NdJ("click",function(){return e.CHM(i),e.oxw(3).moreSecurityList()}),e._uU(6),e.ALo(7,"translate"),e._UZ(8,"i",48),e.qZA()()()}if(2&o){const i=e.oxw().$implicit,t=e.oxw(2);e.xp6(1),e.uIk("colspan",i.length+1),e.xp6(2),e.AsE("",e.lcZ(4,4,"row_count"),": ",t.rowcount,""),e.xp6(3),e.hij("",e.lcZ(7,6,"button.more")," ")}}function F(o,v){if(1&o&&e.YNc(0,w,9,8,"tr",44),2&o){const i=e.oxw(2);e.Q6J("ngIf",i.rowcount)}}function $(o,v){if(1&o&&(e.TgZ(0,"tr")(1,"td"),e._uU(2),e.ALo(3,"translate"),e.qZA()()),2&o){const i=v.$implicit;e.xp6(1),e.uIk("colspan",i.length+1),e.xp6(1),e.hij(" ",e.lcZ(3,2,"no_rec")," ")}}const E=function(){return{"word-break":"break-word"}};function J(o,v){if(1&o&&(e.TgZ(0,"p-table",29),e.YNc(1,B,5,4,"ng-template",30),e.YNc(2,M,5,4,"ng-template",31),e.YNc(3,U,7,7,"ng-template",32),e.YNc(4,F,1,1,"ng-template",33),e.YNc(5,$,4,4,"ng-template",34),e.qZA()),2&o){const i=e.oxw();e.Akn(e.DdM(4,E)),e.Q6J("columns",i.cols)("value",i.dataList)}}function k(o,v){if(1&o&&(e.TgZ(0,"div",49)(1,"small",50),e._uU(2),e.qZA(),e.TgZ(3,"small",50),e._uU(4),e.qZA()()),2&o){const i=e.oxw();e.xp6(2),e.Oqu(i.c_securityLevelId.errors.isRequired),e.xp6(2),e.Oqu(i.c_securityLevelId.errors.maxlength)}}function Q(o,v){if(1&o&&(e.TgZ(0,"div",49)(1,"small",50),e._uU(2),e.qZA(),e.TgZ(3,"small",50),e._uU(4),e.qZA()()),2&o){const i=e.oxw();e.xp6(2),e.Oqu(i.c_securityLevelName.errors.isRequired),e.xp6(2),e.Oqu(i.c_securityLevelName.errors.maxlength)}}function Y(o,v){if(1&o&&(e.TgZ(0,"div",49)(1,"small",50),e._uU(2),e.qZA()()),2&o){const i=e.oxw();e.xp6(2),e.Oqu(i.c_securityLevelDesc.errors.maxlength)}}function G(o,v){if(1&o&&(e.TgZ(0,"div",49)(1,"small",50),e._uU(2),e.qZA(),e.TgZ(3,"small",50),e._uU(4),e.qZA()()),2&o){const i=e.oxw();e.xp6(2),e.Oqu(i.u_newSecurityLevelName.errors.isRequired),e.xp6(2),e.Oqu(i.u_newSecurityLevelName.errors.maxlength)}}function K(o,v){if(1&o&&(e.TgZ(0,"div",49)(1,"small",50),e._uU(2),e.qZA()()),2&o){const i=e.oxw();e.xp6(2),e.Oqu(i.u_newSecurityLevelDesc.errors.maxlength)}}const j=function(){return{marginTop:"60px"}},W=[{path:"",component:(()=>{class o extends b.H{constructor(i,t,r,_,y,h,L){super(i,t),this.fb=r,this.roleService=_,this.toolSerive=y,this.securityService=h,this.messageService=L,this.currentTitle=this.title,this.pageNum=1,this.canCreate=!1,this.canUpdate=!1,this.canDelete=!1,this.cols=[],this.dataList=[],this.rowcount=0}ngOnInit(){this.queryForm=this.fb.group({keyword:new d.NI("")}),this.createForm=this.fb.group({securityLevelId:new d.NI(""),securityLevelName:new d.NI(""),securityLevelDesc:new d.NI("")}),this.updateForm=this.fb.group({securityLevelId:new d.NI(""),newSecurityLevelName:new d.NI(""),newSecurityLevelDesc:new d.NI("")}),this.deleteForm=this.fb.group({securityLevelId:new d.NI(""),securityLevelName:new d.NI(""),securityLevelDesc:new d.NI("")}),this.roleService.queryRTMapByUk({txIdList:["AA1101","AA1103","AA1104"]}).subscribe(i=>{var t,r,_,y,h,L;this.toolSerive.checkDpSuccess(i.ResHeader)&&(this.canCreate=null!==(r=null===(t=i.RespBody.dataList.find(m=>"AA1101"===m.txId))||void 0===t?void 0:t.available)&&void 0!==r&&r,this.canUpdate=null!==(y=null===(_=i.RespBody.dataList.find(m=>"AA1103"===m.txId))||void 0===_?void 0:_.available)&&void 0!==y&&y,this.canDelete=null!==(L=null===(h=i.RespBody.dataList.find(m=>"AA1104"===m.txId))||void 0===h?void 0:h.available)&&void 0!==L&&L)}),this.init()}init(){var i=this;return(0,g.Z)(function*(){const r=yield i.toolSerive.getDict(["security_level_id","security_level_name","security_level_desc"]);i.cols=[{field:"securityLevelId",header:r.security_level_id},{field:"securityLevelName",header:r.security_level_name},{field:"securityLevelDesc",header:r.security_level_desc}],i.dataList=[],i.rowcount=i.dataList.length,i.securityService.querySecurityLevelList_ignore1298({keyword:i.q_keyword.value}).subscribe(y=>{i.toolSerive.checkDpSuccess(y.ResHeader)&&(i.dataList=y.RespBody.dataList,i.rowcount=i.dataList.length)})})()}querySecurityList(){this.dataList=[],this.rowcount=this.dataList.length,this.securityService.querySecurityLevelList({keyword:this.q_keyword.value}).subscribe(t=>{this.toolSerive.checkDpSuccess(t.ResHeader)&&(this.dataList=t.RespBody.dataList,this.rowcount=this.dataList.length)})}moreSecurityList(){this.securityService.querySecurityLevelList({securityLevelId:this.dataList[this.dataList.length-1].securityLevelId,keyword:this.q_keyword.value}).subscribe(t=>{this.toolSerive.checkDpSuccess(t.ResHeader)&&(this.dataList=this.dataList.concat(t.RespBody.dataList),this.rowcount=this.dataList.length)})}createSecurityLevel(){var i=this;this.securityService.addSecurityLevel({securityLevelId:this.c_securityLevelId.value,securityLevelName:this.c_securityLevelName.value,securityLevelDesc:this.c_securityLevelDesc.value}).subscribe(function(){var r=(0,g.Z)(function*(_){if(i.toolSerive.checkDpSuccess(_.ResHeader)){const y=["message.create","security_level","message.success"],h=yield i.toolSerive.getDict(y);i.messageService.add({severity:"success",summary:`${h["message.create"]} ${h.security_level}`,detail:`${h["message.create"]} ${h["message.success"]}!`}),i.querySecurityList(),i.changePage("query")}});return function(_){return r.apply(this,arguments)}}())}updateSecurityLevel(){var t,r,i=this;let _={securityLevelId:null===(t=this.securityLevelDetail)||void 0===t?void 0:t.securityLevelId,oriSecurityLevelName:null===(r=this.securityLevelDetail)||void 0===r?void 0:r.securityLevelName,newSecurityLevelName:this.u_newSecurityLevelName.value,newSecurityLevelDesc:this.u_newSecurityLevelDesc.value};this.securityService.updateSecurityLevel(_).subscribe(function(){var y=(0,g.Z)(function*(h){if(i.toolSerive.checkDpSuccess(h.ResHeader)){const L=["message.update","security_level","message.success"],m=yield i.toolSerive.getDict(L);i.messageService.add({severity:"success",summary:`${m["message.update"]} ${m.security_level}`,detail:`${m["message.update"]} ${m["message.success"]}!`}),i.querySecurityList(),i.changePage("query")}});return function(h){return y.apply(this,arguments)}}())}deleteSecurityLevel(){var t,r,i=this;let _={securityLevelId:null===(t=this.securityLevelDetail)||void 0===t?void 0:t.securityLevelId,securityLevelName:null===(r=this.securityLevelDetail)||void 0===r?void 0:r.securityLevelName};this.securityService.deleteSecurityLevel(_).subscribe(function(){var y=(0,g.Z)(function*(h){if(i.toolSerive.checkDpSuccess(h.ResHeader)){const L=["message.delete","security_level","message.success"],m=yield i.toolSerive.getDict(L);i.messageService.add({severity:"success",summary:`${m["message.delete"]} ${m.security_level}`,detail:`${m["message.delete"]} ${m["message.success"]}!`}),i.querySecurityList(),i.changePage("query")}});return function(h){return y.apply(this,arguments)}}())}changePage(i,t){var r=this;return(0,g.Z)(function*(){const y=yield r.toolSerive.getDict(["button.create","button.update","button.delete"]);switch(i){case"query":r.currentTitle=r.title,r.pageNum=1;break;case"create":r.currentTitle=`${r.title} > ${y["button.create"]}`,r.pageNum=2,r.resetFormValidator(r.createForm),r.securityService.addSecurityLevel_before().subscribe(L=>{r.toolSerive.checkDpSuccess(L.ResHeader)&&r.addFormValidator(r.createForm,L.RespBody.constraints)});break;case"update":case"delete":r.securityLevelDetail={},r.securityService.querySecurityLevelDetail({securityLevelId:null==t?void 0:t.securityLevelId,securityLevelName:null==t?void 0:t.securityLevelName}).subscribe(L=>{r.toolSerive.checkDpSuccess(L.ResHeader)&&(r.securityLevelDetail=L.RespBody,"update"==i?(r.currentTitle=`${r.title} > ${y["button.update"]}`,r.pageNum=3,r.resetFormValidator(r.updateForm),r.u_securityLevelId.setValue(r.securityLevelDetail.securityLevelId),r.u_newSecurityLevelName.setValue(r.securityLevelDetail.securityLevelName),r.u_newSecurityLevelDesc.setValue(r.securityLevelDetail.securityLevelDesc),r.securityService.updateSecurityLevel_before().subscribe(m=>{r.toolSerive.checkDpSuccess(m.ResHeader)&&r.addFormValidator(r.updateForm,m.RespBody.constraints)})):(r.currentTitle=`${r.title} > ${y["button.delete"]}`,r.pageNum=4,r.resetFormValidator(r.deleteForm),r.d_securityLevelId.setValue(r.securityLevelDetail.securityLevelId),r.d_securityLevelName.setValue(r.securityLevelDetail.securityLevelName),r.d_securityLevelDesc.setValue(r.securityLevelDetail.securityLevelDesc)))})}})()}headerReturn(){this.changePage("query")}get q_keyword(){return this.queryForm.get("keyword")}get c_securityLevelId(){return this.createForm.get("securityLevelId")}get c_securityLevelName(){return this.createForm.get("securityLevelName")}get c_securityLevelDesc(){return this.createForm.get("securityLevelDesc")}get u_securityLevelId(){return this.updateForm.get("securityLevelId")}get u_newSecurityLevelName(){return this.updateForm.get("newSecurityLevelName")}get u_newSecurityLevelDesc(){return this.updateForm.get("newSecurityLevelDesc")}get d_securityLevelId(){return this.deleteForm.get("securityLevelId")}get d_securityLevelName(){return this.deleteForm.get("securityLevelName")}get d_securityLevelDesc(){return this.deleteForm.get("securityLevelDesc")}}return o.\u0275fac=function(i){return new(i||o)(e.Y36(p.gz),e.Y36(R.W),e.Y36(d.qu),e.Y36(s.N),e.Y36(a.g),e.Y36(f),e.Y36(Z.ez))},o.\u0275cmp=e.Xpm({type:o,selectors:[["app-ac1116"]],features:[e.qOj],decls:104,vars:86,consts:[[3,"title","isDefault","headerReturn"],[3,"hidden"],[3,"formGroup","ngSubmit"],[1,"p-input-icon-right",2,"width","40vw"],[1,"pi","pi-search","tpi-i-search",3,"click"],["type","search","id","keyword","formControlName","keyword",1,"form-control","tpi-i-input",3,"placeholder"],["type","button",1,"btn","tpi-btn","tpi-second","float-right",3,"disabled","click"],["selectionMode","single","styleClass","p-datatable-striped","responsiveLayout","scroll",3,"columns","value","style",4,"ngIf"],[3,"formGroup"],[1,"form-group","row"],[1,"col-6","col-xl-6","col-lg-6"],["id","securityLevelId_label",1,"control-label"],["type","text","formControlName","securityLevelId",1,"form-control"],["class","text-danger",4,"ngIf"],["id","securityLevelName_label",1,"control-label"],["type","text","formControlName","securityLevelName",1,"form-control"],[1,"col-12","col-xl-12","col-lg-12"],["id","securityLevelDesc_label",1,"control-label"],["formControlName","securityLevelDesc",1,"form-control",3,"rows"],["type","button",1,"btn","tpi-btn","tpi-second","float-left","mr-3",3,"disabled","click"],["type","button",1,"btn","tpi-btn","tpi-primary","float-left",3,"click"],[1,"form-control","border-line"],["id","newSecurityLevelName_label",1,"control-label"],["type","text","formControlName","newSecurityLevelName",1,"form-control"],["id","newSecurityLevelDesc_label",1,"control-label"],["formControlName","newSecurityLevelDesc",1,"form-control",3,"rows"],["type","button",1,"btn","tpi-btn","tpi-primary","float-left","mr-3",3,"disabled","click"],["formControlName","securityLevelDesc","readonly","",1,"form-control",3,"rows"],["position","top-left"],["selectionMode","single","styleClass","p-datatable-striped","responsiveLayout","scroll",3,"columns","value"],["pTemplate","colgroup"],["pTemplate","header"],["pTemplate","body"],["pTemplate","footer"],["pTemplate","emptymessage"],[3,"width",4,"ngFor","ngForOf"],[2,"width","120px"],["scope","col",4,"ngFor","ngForOf"],["scope","col",2,"width","120px"],["scope","col"],[4,"ngFor","ngForOf"],[2,"text-align","center","width","120px"],["pButton","","pRipple","","type","button","icon","fa fa-edit","tooltipPosition","top",1,"p-button-rounded","p-button-text","p-button-plain",3,"pTooltip","click"],["pButton","","pRipple","","type","button","icon","fa fa-trash-alt","tooltipPosition","top",1,"p-button-rounded","p-button-text","p-button-plain",3,"pTooltip","click"],[4,"ngIf"],[2,"color","#b7b7b7"],[2,"vertical-align","middle"],["type","button",1,"btn","tpi-header-return",3,"click"],[1,"fas","fa-angle-double-right",2,"margin-left","5px"],[1,"text-danger"],[1,"form-text"]],template:function(i,t){1&i&&(e.TgZ(0,"app-container",0),e.NdJ("headerReturn",function(){return t.headerReturn()}),e.TgZ(1,"div",1)(2,"form",2),e.NdJ("ngSubmit",function(){return t.querySecurityList()}),e.TgZ(3,"span",3)(4,"i",4),e.NdJ("click",function(){return t.querySecurityList()}),e.qZA(),e._UZ(5,"input",5),e.ALo(6,"translate"),e.ALo(7,"translate"),e.ALo(8,"translate"),e.qZA(),e.TgZ(9,"button",6),e.NdJ("click",function(){return t.changePage("create")}),e._uU(10),e.ALo(11,"translate"),e.qZA()(),e._UZ(12,"hr"),e.YNc(13,J,6,5,"p-table",7),e.qZA(),e.TgZ(14,"div",1)(15,"form",8)(16,"div",9)(17,"div",10)(18,"label",11),e._uU(19),e.ALo(20,"translate"),e.qZA(),e._UZ(21,"input",12),e.YNc(22,k,5,2,"div",13),e.qZA(),e.TgZ(23,"div",10)(24,"label",14),e._uU(25),e.ALo(26,"translate"),e.qZA(),e._UZ(27,"input",15),e.YNc(28,Q,5,2,"div",13),e.qZA()(),e.TgZ(29,"div",9)(30,"div",16)(31,"label",17),e._uU(32),e.ALo(33,"translate"),e.qZA(),e._UZ(34,"textarea",18),e.YNc(35,Y,3,1,"div",13),e.qZA()(),e.TgZ(36,"div",9)(37,"div",16)(38,"button",19),e.NdJ("click",function(){return t.createSecurityLevel()}),e._uU(39),e.ALo(40,"translate"),e.qZA(),e.TgZ(41,"button",20),e.NdJ("click",function(){return t.changePage("query")}),e._uU(42),e.ALo(43,"translate"),e.qZA()()()()(),e.TgZ(44,"div",1)(45,"form",8)(46,"div",9)(47,"div",10)(48,"label",11),e._uU(49),e.ALo(50,"translate"),e.qZA(),e.TgZ(51,"label",21),e._uU(52),e.qZA()(),e.TgZ(53,"div",10)(54,"label",22),e._uU(55),e.ALo(56,"translate"),e.qZA(),e._UZ(57,"input",23),e.YNc(58,G,5,2,"div",13),e.qZA()(),e.TgZ(59,"div",9)(60,"div",16)(61,"label",24),e._uU(62),e.ALo(63,"translate"),e.qZA(),e._UZ(64,"textarea",25),e.YNc(65,K,3,1,"div",13),e.qZA()(),e.TgZ(66,"div",9)(67,"div",16)(68,"button",26),e.NdJ("click",function(){return t.updateSecurityLevel()}),e._uU(69),e.ALo(70,"translate"),e.qZA(),e.TgZ(71,"button",20),e.NdJ("click",function(){return t.changePage("query")}),e._uU(72),e.ALo(73,"translate"),e.qZA()()()()(),e.TgZ(74,"div",1)(75,"form",8)(76,"div",9)(77,"div",10)(78,"label",11),e._uU(79),e.ALo(80,"translate"),e.qZA(),e.TgZ(81,"label",21),e._uU(82),e.qZA()(),e.TgZ(83,"div",10)(84,"label",14),e._uU(85),e.ALo(86,"translate"),e.qZA(),e.TgZ(87,"label",21),e._uU(88),e.qZA()()(),e.TgZ(89,"div",9)(90,"div",16)(91,"label",17),e._uU(92),e.ALo(93,"translate"),e.qZA(),e._UZ(94,"textarea",27),e.qZA()(),e.TgZ(95,"div",9)(96,"div",16)(97,"button",26),e.NdJ("click",function(){return t.deleteSecurityLevel()}),e._uU(98),e.ALo(99,"translate"),e.qZA(),e.TgZ(100,"button",20),e.NdJ("click",function(){return t.changePage("query")}),e._uU(101),e.ALo(102,"translate"),e.qZA()()()()()(),e._UZ(103,"p-toast",28)),2&i&&(e.Q6J("title",t.currentTitle)("isDefault",1==t.pageNum),e.xp6(1),e.Q6J("hidden",1!=t.pageNum),e.xp6(1),e.Q6J("formGroup",t.queryForm),e.xp6(3),e.cQ8("placeholder","",e.lcZ(6,47,"security_lev_id"),"\u3001",e.lcZ(7,49,"security_lev_name"),"\u3001",e.lcZ(8,51,"security_level_desc"),""),e.xp6(4),e.Q6J("disabled",0==t.canCreate),e.xp6(1),e.Oqu(e.lcZ(11,53,"button.create")),e.xp6(3),e.Q6J("ngIf",t.cols),e.xp6(1),e.Q6J("hidden",2!=t.pageNum),e.xp6(1),e.Q6J("formGroup",t.createForm),e.xp6(4),e.Oqu(e.lcZ(20,55,"security_level_id")),e.xp6(3),e.Q6J("ngIf",(null==t.c_securityLevelId?null:t.c_securityLevelId.invalid)&&((null==t.c_securityLevelId?null:t.c_securityLevelId.dirty)||(null==t.c_securityLevelId?null:t.c_securityLevelId.touched))),e.xp6(3),e.Oqu(e.lcZ(26,57,"security_level_name")),e.xp6(3),e.Q6J("ngIf",(null==t.c_securityLevelName?null:t.c_securityLevelName.invalid)&&((null==t.c_securityLevelName?null:t.c_securityLevelName.dirty)||(null==t.c_securityLevelName?null:t.c_securityLevelName.touched))),e.xp6(4),e.Oqu(e.lcZ(33,59,"security_level_desc")),e.xp6(2),e.Q6J("rows",5),e.xp6(1),e.Q6J("ngIf",(null==t.c_securityLevelDesc?null:t.c_securityLevelDesc.invalid)&&((null==t.c_securityLevelDesc?null:t.c_securityLevelDesc.dirty)||(null==t.c_securityLevelDesc?null:t.c_securityLevelDesc.touched))),e.xp6(3),e.Q6J("disabled",t.createForm.invalid),e.xp6(1),e.Oqu(e.lcZ(40,61,"button.create")),e.xp6(3),e.Oqu(e.lcZ(43,63,"button.return_to_list")),e.xp6(2),e.Q6J("hidden",3!=t.pageNum),e.xp6(1),e.Q6J("formGroup",t.updateForm),e.xp6(4),e.Oqu(e.lcZ(50,65,"security_level_id")),e.xp6(3),e.Oqu(null==t.u_securityLevelId?null:t.u_securityLevelId.value),e.xp6(3),e.Oqu(e.lcZ(56,67,"security_level_name")),e.xp6(3),e.Q6J("ngIf",(null==t.u_newSecurityLevelName?null:t.u_newSecurityLevelName.invalid)&&((null==t.u_newSecurityLevelName?null:t.u_newSecurityLevelName.dirty)||(null==t.u_newSecurityLevelName?null:t.u_newSecurityLevelName.touched))),e.xp6(4),e.Oqu(e.lcZ(63,69,"security_level_desc")),e.xp6(2),e.Q6J("rows",5),e.xp6(1),e.Q6J("ngIf",(null==t.u_newSecurityLevelDesc?null:t.u_newSecurityLevelDesc.invalid)&&((null==t.u_newSecurityLevelDesc?null:t.u_newSecurityLevelDesc.dirty)||(null==t.u_newSecurityLevelDesc?null:t.u_newSecurityLevelDesc.touched))),e.xp6(3),e.Q6J("disabled",t.updateForm.invalid),e.xp6(1),e.Oqu(e.lcZ(70,71,"button.update")),e.xp6(3),e.Oqu(e.lcZ(73,73,"button.return_to_list")),e.xp6(2),e.Q6J("hidden",4!=t.pageNum),e.xp6(1),e.Q6J("formGroup",t.deleteForm),e.xp6(4),e.Oqu(e.lcZ(80,75,"security_level_id")),e.xp6(3),e.Oqu(t.d_securityLevelId.value),e.xp6(3),e.Oqu(e.lcZ(86,77,"security_level_name")),e.xp6(3),e.Oqu(t.d_securityLevelName.value),e.xp6(4),e.Oqu(e.lcZ(93,79,"security_level_desc")),e.xp6(2),e.Q6J("rows",5),e.xp6(3),e.Q6J("disabled",t.deleteForm.invalid),e.xp6(1),e.Oqu(e.lcZ(99,81,"button.delete")),e.xp6(3),e.Oqu(e.lcZ(102,83,"button.return_to_list")),e.xp6(2),e.Akn(e.DdM(85,j)))},directives:[x.e,d._Y,d.JL,d.sg,d.Fj,d.JJ,d.u,l.O5,N.iA,Z.jx,l.sg,S.Hq,P.u,D.FN],pipes:[C.X$],styles:[".form-group[_ngcontent-%COMP%]   label[_ngcontent-%COMP%]{background:#F6F6F6}"]}),o})()}];let z=(()=>{class o{}return o.\u0275fac=function(i){return new(i||o)},o.\u0275mod=e.oAB({type:o}),o.\u0275inj=e.cJS({imports:[[p.Bz.forChild(W)],p.Bz]}),o})();var V=n(78555),X=n(24783),ee=n(86347);let te=(()=>{class o{}return o.\u0275fac=function(i){return new(i||o)},o.\u0275mod=e.oAB({type:o}),o.\u0275inj=e.cJS({providers:[ee.u6],imports:[[l.ez,z,V.W,X.m,d.UX,d.u5]]}),o})()},63710:(T,A,n)=>{n.d(A,{e:()=>s});var l=n(87587),p=n(69808),g=n(51062);function d(a,c){if(1&a&&(l.TgZ(0,"div")(1,"h3",9),l._uU(2),l.ALo(3,"translate"),l.qZA()()),2&a){const u=l.oxw();l.xp6(2),l.Oqu(l.lcZ(3,1,u.title))}}function b(a,c){if(1&a){const u=l.EpF();l.TgZ(0,"div",10)(1,"button",11),l.NdJ("click",function(){return l.CHM(u),l.oxw().return()}),l._UZ(2,"i",12),l._uU(3),l.ALo(4,"translate"),l.qZA(),l.TgZ(5,"span",13),l._uU(6),l.qZA(),l.TgZ(7,"span",14),l._uU(8),l.qZA()()}if(2&a){const u=l.oxw();l.xp6(3),l.hij(" ",l.lcZ(4,3,"button.return_to_list")," "),l.xp6(3),l.hij("",u.getHead()," /"),l.xp6(2),l.Oqu(u.getTail())}}const e=[[["","center-view","center"]],"*"],R=["[center-view=center]","*"];let s=(()=>{class a{constructor(){this.title="",this.isDefault=!0,this.headerReturn=new l.vpe}ngOnInit(){}return(){this.headerReturn.emit(null)}getHead(){const u=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return u.pop(),u.join(" / ")}getTail(){const u=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return u[u.length-1]}}return a.\u0275fac=function(u){return new(u||a)},a.\u0275cmp=l.Xpm({type:a,selectors:[["app-container"]],inputs:{title:"title",isDefault:"isDefault"},outputs:{headerReturn:"headerReturn"},ngContentSelectors:R,decls:11,vars:2,consts:[[1,"container-fluid","h-100"],[1,"row","position-relative",2,"height","calc(100% - 40px - 0.5rem)"],[1,"col"],[2,"margin-bottom","0.5rem"],[4,"ngIf"],["style","text-align: right",4,"ngIf"],[1,"col","d-flex","justify-content-center"],[1,"my-0","mb-2"],[1,"p-2"],["id","content",1,"page-title"],[2,"text-align","right"],["type","button","icon","",1,"btn","float-left","tpi-header-return",3,"click"],[1,"fas","fa-arrow-left",2,"margin-right","5px"],[1,"mb-0",2,"font-size","0.8rem","color","#666464"],[1,"mb-0",2,"font-size","0.8rem","color","#ff6e38","font-weight","bold"]],template:function(u,q){1&u&&(l.F$t(e),l.TgZ(0,"div",0)(1,"div",1)(2,"div",2)(3,"div",3),l.YNc(4,d,4,3,"div",4),l.YNc(5,b,9,5,"div",5),l.TgZ(6,"div",6),l.Hsn(7),l.qZA()(),l._UZ(8,"hr",7),l.Hsn(9,1),l._UZ(10,"div",8),l.qZA()()()),2&u&&(l.xp6(4),l.Q6J("ngIf",q.isDefault),l.xp6(1),l.Q6J("ngIf",!q.isDefault))},directives:[p.O5],pipes:[g.X$],styles:[".card.card-body[_ngcontent-%COMP%]   h3[_ngcontent-%COMP%]{font-size:20px;font-weight:400;color:#5a5541}"]}),a})()},92718:(T,A,n)=>{n.d(A,{u:()=>c});var l=n(54004),p=n(70262),g=n(62843),d=n(87587),b=n(45682),e=n(89709),R=n(99291),s=n(3937),a=n(60991);let c=(()=>{class u{constructor(f,Z,x,N,S){this.toolService=f,this.tokenService=Z,this.router=x,this.ngxService=N,this.logoutService=S}canActivate(){return this.ngxService.stopAll(),!this.toolService.isTokenExpired()||this.toolService.refreshToken().pipe((0,l.U)(f=>!!f.access_token),(0,p.K)(this.handleError.bind(this)))}handleError(f){return setTimeout(()=>this.logoutService.logout()),(0,g._)(()=>f)}}return u.\u0275fac=function(f){return new(f||u)(d.LFG(b.g),d.LFG(e.B),d.LFG(R.F0),d.LFG(s.LA),d.LFG(a.P))},u.\u0275prov=d.Yz7({token:u,factory:u.\u0275fac}),u})()},86347:(T,A,n)=>{n.d(A,{DL:()=>l.D,u6:()=>g.u});var l=n(81233),g=(n(45240),n(92718));n(45682)},1955:(T,A,n)=>{n.d(A,{N:()=>b});var l=n(92340),p=n(57553),g=n(87587),d=n(96614);let b=(()=>{class e{constructor(s){this.api=s,this.api.baseUrl=l.N.dpPath}get basePath(){return l.N.isv4?"dgrv4/11":"tsmpdpaa/11"}addTRole(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.addTRole),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0011`,a)}updateTRoleFunc(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.updateTRoleFunc),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0013`,a)}deleteTRole(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.deleteTRole),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0014`,a)}addTRoleRoleMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.addTRoleRoleMap),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0016`,a)}deleteTRoleRoleMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.deleteTRoleRoleMap),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0017`,a)}updateTRoleRoleMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.updateTRoleRoleMap),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0018`,a)}queryTRoleList_v3_ignore1298(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryTRoleList_v3),ReqBody:s};return this.api.excuteNpPost_ignore1298(`${this.basePath}/AA0020`,a)}queryTRoleList_v3(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryTRoleList_v3),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0020`,a)}queryTRoleRoleMapDetail(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryTRoleRoleMapDetail),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0021`,a)}queryTRoleRoleMap_ignore1298(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryTRoleList_v3),ReqBody:s};return this.api.excuteNpPost_ignore1298(`${this.basePath}/AA0022`,a)}queryTRoleRoleMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryTRoleList_v3),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0022`,a)}queryRoleRoleList(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryRoleRoleList),ReqBody:s};return this.api.npPost(`${this.basePath}/AA0023`,a)}createRTMap_before(){let s={ReqHeader:this.api.getReqHeader(p.Nx.createRTMap),ReqBody:{}};return this.api.npPost(`${this.basePath}/DPB0110?before`,s)}createRTMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.createRTMap),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0110`,a)}queryRTMapList_ignore1298(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryRTMapList),ReqBody:s};return this.api.excuteNpPost_ignore1298(`${this.basePath}/DPB0111`,a)}queryRTMapList(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryRTMapList),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0111`,a)}queryRTMapByPk(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryRTMapByPk),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0112`,a)}updateRTMap_before(){let s={ReqHeader:this.api.getReqHeader(p.Nx.updateRTMap),ReqBody:{}};return this.api.npPost(`${this.basePath}/DPB0113?before`,s)}updateRTMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.updateRTMap),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0113`,a)}deleteRTMap(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.deleteRTMap),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0114`,a)}queryRTMapByUk(s){let a={ReqHeader:this.api.getReqHeader(p.Nx.queryRTMapByUk),ReqBody:s};return this.api.npPost(`${this.basePath}/DPB0115`,a)}}return e.\u0275fac=function(s){return new(s||e)(g.LFG(d.K))},e.\u0275prov=g.Yz7({token:e,factory:e.\u0275fac,providedIn:"root"}),e})()}}]);