import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
@Component({
    selector: 'app-ac0910',
    templateUrl: './ac0910.component.html',
    styleUrls: ['./ac0910.component.css']
})
export class Ac0910Component extends BaseComponent implements OnInit {

    constructor(
        protected router: ActivatedRoute,
        tr: TransformMenuNamePipe
    ) {
        super(router, tr);
    }
    ngOnInit() { }
}
