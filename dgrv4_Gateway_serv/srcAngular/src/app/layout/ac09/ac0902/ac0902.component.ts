import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
    selector: 'app-ac0902',
    templateUrl: './ac0902.component.html',
    styleUrls: ['./ac0902.component.css']
})
export class Ac0902Component extends BaseComponent implements OnInit {

    constructor(
        protected router: ActivatedRoute,
        tr: TransformMenuNamePipe
    ) {
        super(router, tr);
    }

    ngOnInit() { }

}
