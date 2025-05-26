import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
    selector: 'app-ac0521',
    templateUrl: './ac0521.component.html',
    styleUrls: ['./ac0521.component.css']
})
export class Ac0521Component extends BaseComponent implements OnInit {

    constructor(
        protected router: ActivatedRoute,
        tr: TransformMenuNamePipe
    ) {
        super(router, tr);
    }

    ngOnInit() {
    }

}
