import { Component, OnInit, Output, EventEmitter, Input, ViewEncapsulation, AfterViewInit } from '@angular/core';
import { TreeNode } from 'primeng/api';
import { AA1002List } from 'src/app/models/api/OrgService/aa1002.interface';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ToolService } from '../services/tool.service';
import { Console } from 'console';
import { DynamicDialogConfig,DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class OrganizationComponent implements OnInit, AfterViewInit {

  @Input('orgList') _orgList!: AA1002List[];
  @Input() showCreateBtn = false;
  @Input() showFooterBtn = false;
  // @Input() orgName ="";
  // @Input() dyDialogList:DynamicDialogRef[]=[];
  @Output() doCreate: EventEmitter<boolean> = new EventEmitter();
  @Output('selectedNode') _selectedNode = new EventEmitter();

  form!: FormGroup;
  orgs: TreeNode[] = [];
  currentHighlight: string = '';
  orgNameSuggestions: string[] = [];
  selectedOrgNode?: TreeNode;
  uuid: string = '';

  constructor(
    private fb: FormBuilder,
    private tool: ToolService,
    private config: DynamicDialogConfig,
    public ref: DynamicDialogRef,

  ) { }

  ngOnInit() {
    if (this.config.data) {
      this._orgList = this.config.data.orgList;
      this.showFooterBtn = true;
      // let str = this.config.data.orgName;
      // this.dyDialogList = this.config.data.dyRef
    }else{
      this.showFooterBtn = false;
    }


    this.uuid = this._uuid();
    this.form = this.fb.group({
      orgName: ''
    });
    let orgChart: TreeNode[] = [];
    let orgId: string[] = [];
    // this._orgList.sort((a, b) => { return a.orgCode < b.orgCode ? -1 : 1 })
    this._orgList.map(o => orgId.push(o.orgID));
    // console.log('map orgList', this._orgList)
    this._orgList.map(org => {
      if (orgId.indexOf(org.parentID) == -1) {
        org['master'] = true;
        orgChart.push({ label: org.orgName, expanded: false, data: org, children: [] });
      }
      else {
        orgChart.push({ label: org.orgName, expanded: false, data: org, children: [] });
      }
    });
    for (let item of orgChart) {
      // let temp = Object.assign([], orgChart);
      let temp = [...orgChart];
      for (let tp of temp) {
        if (item.data.orgID == tp.data.parentID) {
          item?.children?.push(tp);
          item.expanded = true;
        }
      }
    }

    this.orgs = orgChart.find(org => org.data.master == true) ? [orgChart.find(org => org.data.master == true)!] : [];

    // if (this.config.data){

    //   if(this.config.data.orgName!==''){
    //     this.selectedOrgNode = this.config.data.orgName;
    //     this.form.get('orgName')?.setValue(this.config.data.orgName);
    //     this.filter();
    //   }

    // }
  }

  ngAfterViewInit() {
    if (window.location.hash == '#/ac00/ac0002') { // 建立使用者時， 依據所屬組織，限制只能點選包含自己以下的組織
      let disabled = document.getElementsByClassName('ui-treenode-selectable');
      window.setTimeout(() => {
        for (let i = 0; i < disabled.length; i++) {
          disabled?.item(i)?.classList.add('ui-cursor');
        }
        let table = document.getElementById((this._orgList?.find(org => org.orgID == this.tool.getOrgId())?.orgName) + '_' + this.uuid);
        do {
          table = table!.parentElement
        }
        while (table!.nodeName != 'TABLE');
        for (let i = 0; i < table!.getElementsByClassName('ui-treenode-selectable').length; i++) {
          table?.getElementsByClassName('ui-treenode-selectable')?.item(i)?.classList.remove('ui-cursor');
        }
      });
    }
  }

  showCreateDialog() {
    this.doCreate.emit(true);
  }
  saveConfirmData(){
    this.ref.close(this.selectedOrgNode);
  }
  cancelOrgDialog(){
    this.ref.close();
  }


  autoComplete(event) {
    let query = event.query;
    this.orgNameSuggestions = this.doSugges(query, this._orgList);
  }

  doSugges(query, orgList: AA1002List[]): string[] {
    let filtered: string[] = [];
    for (let i = 0; i < orgList.length; i++) {
      let org = orgList[i];
      if (org.orgName.toLowerCase().includes(query.toLowerCase())) {
        filtered.push(org.orgName);
      }
    }
    return filtered;
  }

  filter() {
    if (this.form.get('orgName')!.value != this.currentHighlight && this.currentHighlight) {
      document.getElementById(this.currentHighlight)!.parentElement!.parentElement!.parentElement!.parentElement!.style.backgroundColor = '' ;
      // document.getElementById(this.currentHighlight)?.parentElement?.parentElement?.parentElement?.style?.backgroundColor = 'null';
    }
    if (document.getElementById(this.form.get('orgName')!.value + '_' + this.uuid)) {

      this.currentHighlight = this.form.get('orgName')!.value + '_' + this.uuid;

      document.getElementById(this.form.get('orgName')!.value + '_' + this.uuid)!.parentElement!.parentElement!.parentElement!.parentElement!.style.backgroundColor = 'pink' ;

      document.getElementById(this.form.get('orgName')!.value + '_' + this.uuid)?.focus();
      // this.form.reset();
    }
    else {
      if (document.getElementById(this.currentHighlight)) {
        document.getElementById(this.form.get('orgName')!.value + '_' + this.uuid)!.parentElement!.parentElement!.parentElement!.parentElement!.style.backgroundColor = '' ;
        document.getElementById(this.currentHighlight)!.parentElement?.parentElement?.parentElement?.style.backgroundColor ? null : null;
      }

    }
  }

  onNodeSelect(node) {
    this._selectedNode.emit(node.data);
  }

  _uuid() {
    var d = Date.now();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function') {
      d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = (d + Math.random() * 16) % 16 | 0;
      d = Math.floor(d / 16);
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
  }


}
