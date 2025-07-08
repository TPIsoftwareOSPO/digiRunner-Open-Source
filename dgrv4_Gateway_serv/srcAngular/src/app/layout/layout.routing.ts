import { AboutComponent } from './about/about.component';
import { LayoutComponent } from './layout.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { za0000Component } from './za00/za0000/za0000.component';



const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'about', loadChildren: () => import('./about/about.module').then(m => m.AboutModule) },
      { path: 'profile', loadChildren: () => import('./profile/profile.module').then(m => m.ProfileModule) },
      // { path: 'dashboard', matcher: EdtionMatcher, loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule) },
      // { path: '', redirectTo: 'dashboard', pathMatch: 'prefix' },
      // { path: 'page1', loadChildren: () => import('./components/page1/page1.module').then(m => m.Page1Module)},
      // { path: 'page2', loadChildren: () => import('./components/page2/page2.module').then(m => m.Page2Module)},

      { path: 'ac00/ac0002', loadChildren: () => import('./ac00/ac0002/ac0002.module').then(m => m.Ac0002Module), data: { id: 'ac0002' } },
      { path: 'ac00/ac0006', loadChildren: () => import('./ac00/ac0006/ac0006.module').then(m => m.Ac0006Module), data: { id: 'ac0006' } },
      { path: 'ac00/ac0012', loadChildren: () => import('./ac00/ac0012/ac0012.module').then(m => m.Ac0012Module), data: { id: 'ac0012' } },
      { path: 'ac00/ac0015', loadChildren: () => import('./ac00/ac0015/ac0015.module').then(m => m.Ac0015Module), data: { id: 'ac0015' } },
      { path: 'ac00/ac0016', loadChildren: () => import('./ac00/ac0016/ac0016.module').then(m => m.Ac0016Module), data: { id: 'ac0016' } },
      { path: 'ac00/ac0017', loadChildren: () => import('./ac00/ac0017/ac0017.module').then(m => m.Ac0017Module), data: { id: 'ac0017' } },
      { path: 'ac00/ac0018', loadChildren: () => import('./ac00/ac0018/ac0018.module').then(m => m.Ac0018Module), data: { id: 'ac0018' } },
      { path: 'ac00/ac0019', loadChildren: () => import('./ac00/ac0019/ac0019.module').then(m => m.Ac0019Module), data: { id: 'ac0019' } },
      { path: 'ac00/ac0020', loadChildren: () => import('./ac00/ac0020/ac0020.module').then(m => m.Ac0020Module), data: { id: 'ac0020' } },
      { path: 'ac00/ac0021', loadChildren: () => import('./ac00/ac0021/ac0021.module').then(m => m.Ac0021Module), data: { id: 'ac0021' } },

      { path: 'ac01/ac0101', loadChildren: () => import('./ac01/ac0101/ac0101.module').then(m => m.Ac0101Module), data: { id: 'ac0101' } },
      { path: 'ac01/ac0103', loadChildren: () => import('./ac01/ac0103/ac0103.module').then(m => m.Ac0103Module), data: { id: 'ac0103' } },
      { path: 'ac01/ac0104', loadChildren: () => import('./ac01/ac0104/ac0104.module').then(m => m.Ac0104Module), data: { id: 'ac0104' } },
      { path: 'ac01/ac0105', loadChildren: () => import('./ac01/ac0105/ac0105.module').then(m => m.Ac0105Module), data: { id: 'ac0105' } },

      { path: 'ac02/ac0202', loadChildren: () => import('./ac02/ac0202/ac0202.module').then(m => m.Ac0202Module), data: { id: 'ac0202' } },
      { path: 'ac02/ac0212', loadChildren: () => import('./ac02/ac0212/ac0212.module').then(m => m.Ac0212Module), data: { id: 'ac0212' } },
      { path: 'ac02/ac0222', loadChildren: () => import('./ac02/ac0222/ac0222.module').then(m => m.Ac0222Module), data: { id: 'ac0222' } },
      { path: 'ac02/ac0226', loadChildren: () => import('./ac02/ac0226/ac0226.module').then(m => m.Ac0226Module), data: { id: 'ac0226' } },
      { path: 'ac02/ac0227', loadChildren: () => import('./ac02/ac0227/ac0227.module').then(m => m.Ac0227Module), data: { id: 'ac0227' } },
      { path: 'ac02/ac0228', loadChildren: () => import('./ac02/ac0228/ac0228.module').then(m => m.Ac0228Module), data: { id: 'ac0228' } },
      { path: 'ac02/ac0229', loadChildren: () => import('./ac02/ac0229/ac0229.module').then(m => m.Ac0229Module), data: { id: 'ac0229' } },
      { path: 'ac02/ac0230', loadChildren: () => import('./ac02/ac0230/ac0230.module').then(m => m.Ac0230Module), data: { id: 'ac0230' } },
      { path: 'ac02/ac0231', loadChildren: () => import('./ac02/ac0231/ac0231.module').then(m => m.Ac0231Module), data: { id: 'ac0231' } },

      { path: 'ac03/ac0301', loadChildren: () => import('./ac03/ac0301/ac0301.module').then(m => m.Ac0301Module), data: { id: 'ac0301' } },
      { path: 'ac03/ac0311', loadChildren: () => import('./ac03/ac0311/ac0311.module').then(m => m.Ac0311Module), data: { id: 'ac0311' } },
      { path: 'ac03/ac0315', loadChildren: () => import('./ac03/ac0315/ac0315.module').then(m => m.Ac0315Module), data: { id: 'ac0315' } },
      { path: 'ac03/ac0316', loadChildren: () => import('./ac03/ac0316/ac0316.module').then(m => m.Ac0316Module), data: { id: 'ac0316' } },
      { path: 'ac03/ac0318', loadChildren: () => import('./ac03/ac0318/ac0318.module').then(m => m.Ac0318Module), data: { id: 'ac0318' } },
      { path: 'ac03/ac0319', loadChildren: () => import('./ac03/ac0319/ac0319.module').then(m => m.Ac0319Module), data: { id: 'ac0319' } },
      { path: 'ac03/ac0320', loadChildren: () => import('./ac03/ac0320/ac0320.module').then(m => m.Ac0320Module), data: { id: 'ac0320' } },

      // { path: 'ac04/ac0402', loadChildren: () => import('./ac04/ac0402/ac0402.module').then(m => m.Ac0402Module), data: { id: 'ac0402' } },
      // { path: 'ac04/ac0412', loadChildren: () => import('./ac04/ac0412/ac0412.module').then(m => m.Ac0412Module), data: { id: 'ac0412' } },

      { path: 'ac05/ac0501', loadChildren: () => import('./ac05/ac0501/ac0501.module').then(m => m.Ac0501Module), data: { id: 'ac0501' } },
      { path: 'ac05/ac0502', loadChildren: () => import('./ac05/ac0502/ac0502.module').then(m => m.Ac0502Module), data: { id: 'ac0502' } },
      { path: 'ac05/ac0503', loadChildren: () => import('./ac05/ac0503/ac0503.module').then(m => m.Ac0503Module), data: { id: 'ac0503' } },
      // { path: 'ac05/ac0504', loadChildren: () => import('./ac05/ac0504/ac0504.module').then(m => m.Ac0504Module), data: { id: 'ac0504' } },
      // { path: 'ac05/ac0507', loadChildren: () => import('./ac05/ac0507/ac0507.module').then(m => m.Ac0507Module), data: { id: 'ac0507' } },
      { path: 'ac05/ac0508', loadChildren: () => import('./ac05/ac0508/ac0508.module').then(m => m.Ac0508Module), data: { id: 'ac0508' } },
      { path: 'ac05/ac0509', loadChildren: () => import('./ac05/ac0509/ac0509.module').then(m => m.Ac0509Module), data: { id: 'ac0509' } },
      { path: 'ac05/ac0510', loadChildren: () => import('./ac05/ac0510/ac0510.module').then(m => m.Ac0510Module), data: { id: 'ac0510' } },
      // { path: 'ac05/ac0521', loadChildren: () => import('./ac05/ac0521/ac0521.module').then(m => m.Ac0521Module), data: { id: 'ac0521' } },

      { path: 'ac07/ac0702', loadChildren: () => import('./ac07/ac0702/ac0702.module').then(m => m.Ac0702Module), data: { id: 'ac0702' } },
      { path: 'ac07/ac0706', loadChildren: () => import('./ac07/ac0706/ac0706.module').then(m => m.Ac0706Module), data: { id: 'ac0706' } },

      // { path: 'ac09/ac0901', loadChildren: () => import('./ac09/ac0901/ac0901.module').then(m => m.Ac0901Module), data: { id: 'ac0901' } },
      // { path: 'ac09/ac0902', loadChildren: () => import('./ac09/ac0902/ac0902.module').then(m => m.Ac0902Module), data: { id: 'ac0902' } },
      // { path: 'ac09/ac0903', loadChildren: () => import('./ac09/ac0903/ac0903.module').then(m => m.Ac0903Module), data: { id: 'ac0903' } },
      // { path: 'ac09/ac0904', loadChildren: () => import('./ac09/ac0904/ac0904.module').then(m => m.Ac0904Module), data: { id: 'ac0904' } },
      // { path: 'ac09/ac0905', loadChildren: () => import('./ac09/ac0905/ac0905.module').then(m => m.Ac0905Module), data: { id: 'ac0905' } },
      // { path: 'ac09/ac0906', loadChildren: () => import('./ac09/ac0906/ac0906.module').then(m => m.Ac0906Module), data: { id: 'ac0906' } },
      // { path: 'ac09/ac0907', loadChildren: () => import('./ac09/ac0907/ac0907.module').then(m => m.Ac0907Module), data: { id: 'ac0907' } },
      // { path: 'ac09/ac0908', loadChildren: () => import('./ac09/ac0908/ac0908.module').then(m => m.Ac0908Module), data: { id: 'ac0908' } },
      // { path: 'ac09/ac0909', loadChildren: () => import('./ac09/ac0909/ac0909.module').then(m => m.Ac0909Module), data: { id: 'ac0909' } },
      // { path: 'ac09/ac0910', loadChildren: () => import('./ac09/ac0910/ac0910.module').then(m => m.Ac0910Module), data: { id: 'ac0910' } },

      { path: 'ac10/ac1002', loadChildren: () => import('./ac10/ac1002/ac1002.module').then(m => m.Ac1002Module), data: { id: 'ac1002' } },

      { path: 'ac11/ac1107', loadChildren: () => import('./ac11/ac1107/ac1107.module').then(m => m.Ac1107Module), data: { id: 'ac1107' } },
      { path: 'ac11/ac1116', loadChildren: () => import('./ac11/ac1116/ac1116.module').then(m => m.Ac1116Module), data: { id: 'ac1116' } },

      { path: 'ac12/ac1202', loadChildren: () => import('./ac12/ac1202/ac1202.module').then(m => m.Ac1202Module), data: { id: 'ac1202' } },

      { path: 'ac13/ac1301', loadChildren: () => import('./ac13/ac1301/ac1301.module').then(m => m.Ac1301Module), data: { id: 'ac1301' } },
      { path: 'ac13/ac1302', loadChildren: () => import('./ac13/ac1302/ac1302.module').then(m => m.Ac1302Module), data: { id: 'ac1302' } },
      { path: 'ac13/ac1303', loadChildren: () => import('./ac13/ac1303/ac1303.module').then(m => m.Ac1303Module), data: { id: 'ac1303' } },
      { path: 'ac13/ac1304', loadChildren: () => import('./ac13/ac1304/ac1304.module').then(m => m.Ac1304Module), data: { id: 'ac1304' } },
      { path: 'ac13/ac1305', loadChildren: () => import('./ac13/ac1305/ac1305.module').then(m => m.Ac1305Module), data: { id: 'ac1305' } },

      // { path: 'np01/np0105', loadChildren: () => import('./np01/np0105/np0105.module').then(m => m.Np0105Module), data: { id: 'np0105' } },
      // { path: 'np01/np0113', loadChildren: () => import('./np01/np0113/np0113.module').then(m => m.Np0113Module), data: { id: 'np0113' } },
      // { path: 'np01/np0114', loadChildren: () => import('./np01/np0114/np0114.module').then(m => m.Np0114Module), data: { id: 'np0114' } },
      // { path: 'np01/np0115', loadChildren: () => import('./np01/np0115/np0115.module').then(m => m.Np0115Module), data: { id: 'np0115' } },
      // { path: 'np01/np0116', loadChildren: () => import('./np01/np0116/np0116.module').then(m => m.Np0116Module), data: { id: 'np0116' } },
      // { path: 'np12/np1201', loadChildren: () => import('./np01/np1201/np1201.module').then(m => m.Np1201Module), data: { id: 'np1201' } }, //20240918 移除
      { path: 'np12/np1202', loadChildren: () => import('./np01/np1202/np1202.module').then(m => m.Np1202Module), data: { id: 'np1202' } },

      { path: 'np02/np0201', loadChildren: () => import('./np02/np0201/np0201.module').then(m => m.Np0201Module), data: { id: 'np0201' } },
      { path: 'np02/np0202', loadChildren: () => import('./np02/np0202/np0202.module').then(m => m.Np0202Module), data: { id: 'np0202' } },
      { path: 'np02/np0203', loadChildren: () => import('./np02/np0203/np0203.module').then(m => m.Np0203Module), data: { id: 'np0203' } },
      { path: 'np02/np0204', loadChildren: () => import('./np02/np0204/np0204.module').then(m => m.Np0204Module), data: { id: 'np0204' } },
      // { path: 'np02/np0205', loadChildren: () => import('./np02/np0205/np0205.module').then(m => m.Np0205Module), data: { id: 'np0205' } },

      // { path: 'np03/np0301', loadChildren: () => import('./np03/np0301/np0301.module').then(m => m.Np0301Module), data: { id: 'np0301' } },
      // { path: 'np03/np0302', loadChildren: () => import('./np03/np0302/np0302.module').then(m => m.Np0302Module), data: { id: 'np0302' } },
      // { path: 'np03/np0303', loadChildren: () => import('./np03/np0303/np0303.module').then(m => m.Np0303Module), data: { id: 'np0303' } },
      { path: 'np03/np0304', loadChildren: () => import('./np03/np0304/np0304.module').then(m => m.Np0304Module), data: { id: 'np0304' } },

      { path: 'np04/np0401', loadChildren: () => import('./np04/np0401/np0401.module').then(m => m.Np0401Module), data: { id: 'np0401' } },
      { path: 'np04/np0402', loadChildren: () => import('./np04/np0402/np0402.module').then(m => m.Np0402Module), data: { id: 'np0402' } },

      { path: 'np05/np0504', loadChildren: () => import('./np05/np0504/np0504.module').then(m => m.Np0504Module), data: { id: 'np0504' } },
      // { path: 'np05/np0511', loadChildren: () => import('./np05/np0511/np0511.module').then(m => m.Np0511Module), data: { id: 'np0511' } },
      { path: 'np05/np0512', loadChildren: () => import('./np05/np0512/np0512.module').then(m => m.Np0512Module), data: { id: 'np0512' } },
      { path: 'np05/np0513', loadChildren: () => import('./np05/np0513/np0513.module').then(m => m.Np0513Module), data: { id: 'np0513' } },
      { path: 'np05/np0514', loadChildren: () => import('./np05/np0514/np0514.module').then(m => m.Np0514Module), data: { id: 'np0514' } },
      { path: 'np05/np0516', loadChildren: () => import('./np05/np0516/np0516.module').then(m => m.Np0516Module), data: { id: 'np0516' } },

      //labs
      { path: 'labs/tsmpdpfile', loadChildren: () => import('./labs/tsmpdpfile/tsmpdpfile.module').then(m => m.TsmpdpFileModule), data: { id: 'tsmpdpfile' } },
      { path: 'labs/tsmpdpitems', loadChildren: () => import('./labs/tsmpdpitems/tsmpdpitems.module').then(m => m.TsmpdpitemsModule), data: { id: 'tsmpdpitems' } },
      { path: 'labs/tsmpsetting', loadChildren: () => import('./labs/tsmpsetting/tsmpsetting.module').then(m => m.TsmpsettingModule), data: { id: 'tsmpsetting' } },
      { path: 'labs/cussetting', loadChildren: () => import('./labs/cussetting/cussetting.module').then(m => m.CussettingModule), data: { id: 'cussetting' } },

      //console
      { path: 'console/onlineConsole', loadChildren: () => import('./console/online-console/online-console.module').then(m => m.OnlineConsoleModule), data: { id: 'onlineConsole' } },

      { path: 'lb00/lb0001', loadChildren: () => import('./labs/tsmpsetting/tsmpsetting.module').then(m => m.TsmpsettingModule), data: { id: 'lb0001' } },
      { path: 'lb00/lb0002', loadChildren: () => import('./labs/tsmpdpitems/tsmpdpitems.module').then(m => m.TsmpdpitemsModule), data: { id: 'lb0002' } },
      { path: 'lb00/lb0003', loadChildren: () => import('./labs/tsmpdpfile/tsmpdpfile.module').then(m => m.TsmpdpFileModule), data: { id: 'lb0003' } },
      { path: 'lb00/lb0004', loadChildren: () => import('./labs/cussetting/cussetting.module').then(m => m.CussettingModule), data: { id: 'lb0004' } },
      { path: 'lb00/lb0005', loadChildren: () => import('./console/online-console/online-console.module').then(m => m.OnlineConsoleModule), data: { id: 'lb0005' } },
      { path: 'lb00/lb0006', loadChildren: () => import('./labs/websocket-proxy/websocket-proxy.module').then(m => m.WebsocketProxyModule), data: { id: 'lb0006' } },
      { path: 'lb00/lb0007', loadChildren: () => import('./labs/website-proxy/website-proxy.module').then(m => m.WebsiteProxyModule), data: { id: 'lb0007' } },
      { path: 'lb00/lb0008', loadChildren: () => import('./labs/rdb-connection/rdb-connection.module').then(m => m.RdbConnectionModule), data: { id: 'lb0008' } },
      { path: 'lb00/lb0009', loadChildren: () => import('./labs/mail-template-io/mail-template-io.module').then(m => m.MailTemplateIoModule), data: { id: 'lb0009' } },
      { path: 'lb00/lb0010', loadChildren: () => import('./labs/lb0010/lb0010.module').then(m => m.Lb0010Module), data: { id: 'lb0010' } }, //Bot Detection
      { path: 'lb00/lb0011', loadChildren: () => import('./labs/lb0011/lb0011.module').then(m => m.Lb0011Module), data: { id: 'lb0011' } }, //webhook
      { path: 'lb00/lb0012', loadChildren: () => import('./labs/lb0012/lb0012.module').then(m => m.Lb0012Module), data: { id: 'lb0012' } }, //webhook logs

      //註冊自定義報表容器
      { path: 'ac09/:cusfunc', loadChildren: () => import('./ac09/ac0900/ac0900.module').then(m => m.Ac0900Module) },
      //註冊客製包容器
      { path: ':cus/:cusfunc', loadChildren: () => import('./za00/za0000/za0000.module').then(m => m.Za0000Module) },

    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LayoutRoutingModule { }
