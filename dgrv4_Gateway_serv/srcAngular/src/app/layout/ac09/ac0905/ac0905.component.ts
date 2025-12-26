import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
    selector: 'app-ac0905',
    templateUrl: './ac0905.component.html',
    styleUrls: ['./ac0905.component.css']
})
export class Ac0905Component extends BaseComponent implements OnInit {

    constructor(
        protected router: ActivatedRoute,
         tr: TransformMenuNamePipe
    ) {
        super(router, tr);
    }

    ngOnInit() { }

}
