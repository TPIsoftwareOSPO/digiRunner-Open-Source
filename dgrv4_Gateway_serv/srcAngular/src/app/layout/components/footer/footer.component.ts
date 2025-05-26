import { Router } from '@angular/router';
import { DPB0118Resp } from './../../../models/api/AboutService/dpb0118.interface';
import { AboutService } from './../../../shared/services/api-about.service';
import { Component, OnInit } from '@angular/core';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  coreVer:string = '';

  constructor(
    private toolService:ToolService,
    private aboutService: AboutService
    ) { }

  ngOnInit() {

    if(!this.toolService.getAcConf()) return;

    this.aboutService.getModuleVersionData().subscribe(res=>{
      this.coreVer = res.version;
    });

    // this.aboutService.queryModuleVersion().subscribe(res => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //       // this.versionInfo = res.RespBody;
    //       this.coreVer = res.RespBody.version;
    //       this.toolService.writeToken(res.RespBody.majorVersionNo ,'majorVersionNo');
    //   }

    // });


    //  this.coreVer = this.tool.getAcConf().coreVer;
  }

}
