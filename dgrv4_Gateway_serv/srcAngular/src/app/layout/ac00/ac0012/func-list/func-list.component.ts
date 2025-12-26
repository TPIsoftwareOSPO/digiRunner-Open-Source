import { FuncService } from './../../../../shared/services/api-func.service';
import { Component, OnInit, Input } from '@angular/core';
import { FormParams } from '../../../../models/api/form-params.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { SidebarService } from 'src/app/layout/components/sidebar/sidebar.service';
import { Menu } from 'src/app/models/menu.model';

@Component({
    selector: 'app-func-list',
    templateUrl: './func-list.component.html',
    styleUrls: ['./func-list.component.css'],
    providers: [FuncService]
})
export class FuncListComponent implements OnInit {

    @Input() data?: FormParams;
    @Input() close?: Function;
    @Input() closeWithPram?: Function;
    @Input() disableCheckbox: boolean = false;

    selectedCities: string[] = [];
    selecteds: { name: string; code: string; }[] = [];
    funcs: { name: string; code: string; }[] = [];
    menus: Menu[] =[];

    constructor(
        private toolService: ToolService,
        private sidebarService: SidebarService

    ) { }

    ngOnInit() {
        let tFuncsDetails = this.toolService.getFuncList();
        let menus = this.sidebarService.transform(tFuncsDetails);
        this.menus = menus;
    }

}
