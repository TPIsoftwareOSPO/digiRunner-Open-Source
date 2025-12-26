import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-api-status',
  templateUrl: './api-status.component.html',
  styleUrls: ['./api-status.component.css']
})
export class ApiStatusComponent implements OnInit {

  @Input() type:string = '';
  @Input() rowData:any;
  @Input() readOnly:boolean = false;


  apiStateLabel:string  = '-';

  constructor(
    private toolService: ToolService,
    private translateService:TranslateService
  ) { }

async ngOnInit() {
    if(this.rowData[this.type] == 0){

      this.apiStateLabel = '-';
    }
    else{
      let dateDiff = (this.rowData[this.type]) - new Date().getTime();
      if (dateDiff > 0) {
        let day = Math.ceil(dateDiff / (1000 * 60 * 60 * 24));
        let date = this.toolService.setformate(this.rowData[this.type], "YYYY-MM-DD");
        this.translateService.get('apiStatus.daysLater',{day:day}).subscribe(res=>{
          this.apiStateLabel = `${date}, ${res}`;
        })
      }
      else{
        this.apiStateLabel = '-';
      }
    }
  }
}
