import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';

@Component({
    selector: 'app-np0302',
    templateUrl: './np0302.component.html',
    styleUrls: ['./np0302.component.css']
})
export class Np0302Component extends BaseComponent implements OnInit {

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
