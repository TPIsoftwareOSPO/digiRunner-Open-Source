import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.css']
})
export class RoleListComponent implements OnInit {
  @Input() data : any;
  @Input() close:Function;
  roles: any;

  constructor() { }

  ngOnInit() {
    // console.log(this.data);
    this.roles = this.data.data.roleNameList.map(role => {
      return {label : role , value : role}
    });
  }
  click(){
    this.close();
  }

}
