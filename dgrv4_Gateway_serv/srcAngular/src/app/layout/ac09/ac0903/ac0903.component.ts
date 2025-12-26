import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
    selector: 'app-ac0903',
    templateUrl: './ac0903.component.html',
    styleUrls: ['./ac0903.component.css']
})
export class Ac0903Component extends BaseComponent implements OnInit {

    constructor(
        protected router: ActivatedRoute,
        tr: TransformMenuNamePipe
    ) {
        super(router, tr);
    }

    ngOnInit() { }

}
