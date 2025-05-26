import { Component, OnInit, ViewChild, ElementRef, TemplateRef } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { SitemapService } from '../../../shared/services/api-sitemap.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AlertType, FormOperate } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { DetailComponent } from './detail/detail.component';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPF0011Node } from 'src/app/models/api/SiteMapService/dpf0011.interface';
import { of, Observable } from 'rxjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { FormBuilder } from '@angular/forms';
import { DPB0033Req } from 'src/app/models/api/SiteMapService/dpb0033.interface';

@Component({
    selector: 'app-np0115',
    templateUrl: './np0115.component.html',
    styleUrls: ['./np0115.component.css'],
    providers: [MessageService, SitemapService, ConfirmationService]
})
export class Np0115Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    formOperate = FormOperate;
    dialogTitle: string = "";
    curSiteId: number = 0;
    siteMaps?: DPF0011Node;
    editMode: boolean = false;
    operate = 'OPERATE';
    public options = {
        fixedDepth: false,
        disableDrag: true
    } as NestableSettings;
    list: any;
    allDisabled: boolean = false;

    constructor(
         route: ActivatedRoute,
         tr: TransformMenuNamePipe,
        private _sitemap: SitemapService,
        private _ngxService: NgxUiLoaderService,
        private _messageService: MessageService,
        private _tool: ToolService,
        private alert: AlertService,
        private fb: FormBuilder,
        private confirmationService:ConfirmationService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this._sitemap.querySiteMap_ignore298().subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.siteMaps = res.RespBody;
                const newList = this.siteMaps.nodes.map(siteMap => {
                    const obj = {
                        id: siteMap.siteId,
                        pid: siteMap.siteParentId,
                        name: siteMap.siteDesc,
                        url: siteMap.siteUrl,
                        expanded: true,
                        children: []
                    };
                    return this.recusiveTransform(siteMap, obj);
                });

                if (newList.length == 0) {
                    this.procDefaultData();
                }
                else {
                    this.list = newList;
                }
            }
            else {
                // no result set init data
                this.procRootData();
            }
        });
    }
    /**
     * 將api的樹狀json轉為ngx-nestable認得的格式
     * @param source 原有節點格式
     * @param obj 新的節點
     * @returns
     */
    recusiveTransform(source: DPF0011Node, obj) {
        if (source.nodes && source.nodes.length) {
            source.nodes.forEach(sr_node => {
                const child = {
                    id: sr_node.siteId,
                    pid: sr_node.siteParentId,
                    name: sr_node.siteDesc,
                    url: sr_node.siteUrl,
                    expanded: true,
                    children: []
                };
                obj.children.push(child)
                this.recusiveTransform(sr_node, child)
            });
        }
        return obj;
    }

    public loadData(): void {
        this._sitemap.querySiteMap().subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.siteMaps = res.RespBody;
                const newList = this.siteMaps.nodes.map(siteMap => {
                    const obj = {
                        id: siteMap.siteId,
                        name: siteMap.siteDesc,
                        url: siteMap.siteUrl,
                        $$expanded: true,
                        children: []
                    };
                    return this.recusiveTransform(siteMap, obj);
                });

                if (newList.length == 0) {
                    this.procDefaultData();
                }
                else {
                    this.list = newList;
                }

            }
        });
    }

    /**
     * 沒資料的時候塞入第一筆root資料
     * site_parent_id設為0
     * site_id 為自動產生
     */
    setDefaultRootData() {
        return new Promise((resolve, reject) => {
            let ReqBody = {
                siteParentId: 0,
                siteDesc: 'TSMP入口網',
                siteUrl: '/'
            } as DPB0033Req;

            this._sitemap.addNode(ReqBody).subscribe(res => {
                if (this._tool.checkDpSuccess(res.ResHeader)) {
                    resolve(this._tool.checkDpSuccess(res.ResHeader));
                }
                else {
                    reject();
                }
            });

        })
    }

    /**
     * 當有第一筆root資料後還要再寫入一筆預設資料
     * site_paent_id為第一筆root資料的site_id
     */
    setDefaultData() {
        return new Promise((resolve, reject) => {
            let ReqBody = {
                siteParentId: this.siteMaps?.siteId,
                siteDesc: '公告區',
                siteUrl: '/'
            } as DPB0033Req;
            this._sitemap.addNode(ReqBody).subscribe(res => {
                if (this._tool.checkDpSuccess(res.ResHeader)) {
                    resolve(this._tool.checkDpSuccess(res.ResHeader));
                }
                else {
                    reject();
                }
            });
        })
    }

    /*
     * TSMP_DP_SITE_MAP 若資料為空時，無法建立資料
     * 因此若發生資料為空的狀況，塞入預設root層的資料2筆，後續即可正常操作
     */
    async procRootData() {
        await this.setDefaultRootData();
        this.loadData();
    }
    /*
     * TSMP_DP_SITE_MAP 若資料只有一筆root data時
     * 塞入預設資料1筆，後續即可正常操作
     */
    async procDefaultData() {
        await this.setDefaultData();
        this.loadData();
    }

    public async showDialog(node: DPF0011Node, operate: FormOperate): Promise<void> {
        const codes = ['add_sitemap_node', 'edit_name', 'cfm_del_sitemap', 'sitmap_name'];
        const dict = await this._tool.getDict(codes);
        switch (operate) {
            case FormOperate.create:
                this.dialogTitle = dict['add_sitemap_node'];
                this._dialog.open(DetailComponent, {
                    data: node, operate: operate, afterCloseCallback: (r) => {
                        this.loadData();
                    }
                });
                break;
            case FormOperate.update:
                this.dialogTitle = dict['edit_name'];
                this._dialog.open(DetailComponent, {
                    data: node, operate: operate, afterCloseCallback: (r) => {
                        this.loadData();
                    }
                });
                break;
            case FormOperate.delete:
                this.curSiteId = node.siteId;
                this._messageService.clear();
                // this._messageService.add({
                //     key: 'delete', sticky: true, severity: 'error',
                //     summary: dict['cfm_del_sitemap'], detail: `${dict['sitmap_name']} : ${node.siteDesc}`
                // });

                this.confirmationService.confirm({
                  header: dict['cfm_del_sitemap'],
                  message: `${dict['sitmap_name']} : ${node.siteDesc}`,
                  accept: () => {
                      this.delete();
                  }
                });
                break;
        }
    }

    public async delete(): Promise<void> {
        const codes = ['message.delete', 'message.sitemap', 'message.success'];
        const dict = await this._tool.getDict(codes);
        this._messageService.clear();
        // this._ngxService.start();
        this._sitemap.deleteNodeById({ siteId: this.curSiteId }).subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.loadData();
                // this._ngxService.stop();
                this._messageService.add({ key: 'confirm', severity: 'success', summary: `${dict['message.delete']} ${dict['message.sitemap']}`, detail: `${dict['message.delete']} ${dict['message.success']}!` });
            }
        });
    }

    public cancel(): void {
        this._messageService.clear();
    }

    public checkNodes(nodes: Array<DPF0011Node>): boolean {
        return nodes.length != 0;
    }
    plus(row, itemTemplate: any) {
        const nums = this.list.map(l => {
            let tmp = [];
            return this.recursiveId(l, tmp);
        }).reduce((a, b) => a.concat(b)) // [1,2,3,4,5]
        const maxId = Math.max.apply(null, nums) // 5

        if (row) {
            const item = this.list.map(tree => this.getNodeById(tree, row.item.id, 'children')).find(n => n != false) // parent item
            item.$$expanded = true;
            item.children.push({
                id: maxId + 1,
                name: 'New Item',
                url: 'New Url',
                pid: row.item.id,
                children: []
            });

        } else {
            const newList = JSON.parse(JSON.stringify(this.list));
            this.list = [];
            const newParent = {
                id: maxId + 1,
                name: 'New Item',
                url: 'New Url',
                pid: this.siteMaps?.siteId,
                children: []
            }
            newList.push(newParent);
            this.list = newList;
        }
        setTimeout(() => {
            const res = $(itemTemplate).find('.collapseBtn > i').hasClass('fa-angle-down');

            setTimeout(() => {
                if (res) $(itemTemplate).find('.collapseBtn').trigger('click')
                $('#span_' + (maxId + 1)).trigger('click');
            });

        });

    }
    RemoveNode(node, id) {
        if (node.id === id) {
            const pnode = this.list.map(tree => this.getNodeById(tree, node.pid, 'children')).find(f => f != false);
            if (!pnode) {
                this.list.splice(this.list.findIndex(x => x.id === id), 1);
            } else {
                pnode.children.splice(pnode.children.findIndex(c => c.id === id), 1);
            }
        } else {
            node.children.forEach(c => this.RemoveNode(c, id))
        }

    };
    recursiveId(item, tmp) {
        tmp.push(item.id);
        item.children.forEach(child => {
            return this.recursiveId(child, tmp);
        });
        return tmp;
    }

    onClickHandler(row) {
        if (!this.allDisabled) {
            row.item.focus = true;
            this.allDisabled = true;
            $('#span_' + row.item.id).hide();
            $('#input_' + row.item.id).addClass('d-flex').removeClass('d-none').find('input').first().focus().select();
        }
    }
    onBlurHandler(row) {
        delete row.item.focus;
        this.allDisabled = false;
        $('#span_' + row.item.id).show();
        $('#input_' + row.item.id).removeClass('d-flex').addClass('d-none');
    }
    onEscapeHandler(row) {
        this.cancelEdit(row);
    }
    getNodeById(tree, id, key) {
        let tmp;
        if (tree.siteId === id || tree.id === id) {
            return tree;
        } else {
            return tree[key] && tree[key].some(node => tmp = this.getNodeById(node, id, key)) && tmp;
        }
    }

    /**
     * 由id到原始資料裡查找，若無資料表示新增，有資料表示更新
     * @param row
     * @returns
     */
    detectOperate(row): FormOperate {
        const findNode = this.getNodeById(this.siteMaps, row.item.id, 'nodes');
        let operate = findNode ? FormOperate.update : FormOperate.create;
        return operate;
    }

    async confirm(row, inputName, inputUrl) {
        let operate;
        operate = this.detectOperate(row);
        const cmp = new DetailComponent(this.fb, this._sitemap, this.alert, this._tool);
        cmp.operate = operate;
        const node = this.siteMaps?.nodes.find(n => n.siteId == row.item.id);
        cmp.data = {
            data: node, operate: operate, afterCloseCallback: (r) => {

            }
        };
        const code = ['button.create', 'button.update', 'sitemap', 'message.success'];
        const dict = await this._tool.getDict(code);
        switch (operate) {
            case FormOperate.create:
                let res = await cmp.executeSend({
                    siteId: row.item.pid,
                    siteDesc: inputName.value,
                    siteUrl: inputUrl.value
                });
                if (res) {
                    this.loadData();
                    this._messageService.add({ key: "confirm", severity: 'success', summary: `${dict['button.create']} ${dict['sitemap']}`, detail: `${dict['button.create']} ${dict['sitemap']} ${dict['message.success']}!` });
                }
                break;
            case FormOperate.update:
                res = await cmp.executeSend({
                    siteId: row.item.id,
                    siteDesc: inputName.value,
                    siteUrl: inputUrl.value
                });
                if (res) {
                    this.loadData();
                    this._messageService.add({ key: "confirm", severity: 'success', summary: `${dict['button.update']} ${dict['sitemap']}`, detail: `${dict['button.update']} ${dict['sitemap']} ${dict['message.success']}!` });
                }
                break;
            default:
                break;
        }
        this.onBlurHandler(row);
    }
    async remove(row) {
        this.curSiteId = row.item.id;
        this._messageService.clear();
        const codes = ['add_sitemap_node', 'edit_name', 'cfm_del_sitemap', 'sitmap_name'];
        const dict = await this._tool.getDict(codes);
        // this._messageService.add({
        //     key: 'delete', sticky: true, severity: 'error',
        //     summary: dict['cfm_del_sitemap'], detail: `${dict['sitmap_name']} : ${row.item.name}`
        // });

        this.confirmationService.confirm({
          header: dict['cfm_del_sitemap'],
          message: `${dict['sitmap_name']} : ${row.item.name}`,
          accept: () => {
              this.delete();
          }
        });
    }
    cancelEdit(row) {
        const operate = this.detectOperate(row);
        if (operate === FormOperate.create) {
            this.list.forEach(t => this.RemoveNode(t, row.item.id));
        }
        this.onBlurHandler(row);
    }
    collapseAll(e) {
        this.list.forEach(n => n.$$expanded = false);
        this.list = [...this.list];

    }
    expandAll(e) {
        this.list.forEach(n => n.$$expanded = true);
        this.list = [...this.list];
    }
}
