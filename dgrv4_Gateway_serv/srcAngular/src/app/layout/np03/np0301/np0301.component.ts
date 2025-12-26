import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';

@Component({
    selector: 'app-np0301',
    templateUrl: './np0301.component.html',
    styleUrls: ['./np0301.component.css']
})
export class Np0301Component extends BaseComponent implements OnInit {

    data?: FormParams;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.data = {
            operate: FormOperate.create
        }
    }

}
