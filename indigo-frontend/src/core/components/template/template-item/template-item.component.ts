import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@core/components/common/card/card.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AvatarComponent } from '@core/components/common/avatar/avatar.component';
import { ItemTemplate, RootTemplate } from '@core/types/entities/template.i';


@Component({
  selector: 'eln-template-item',
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent,
  ],
  templateUrl: './template-item.component.html',
})
export class TemplateItemComponent implements OnInit {

  ngOnInit(){
  }
  mock_users = {
    link: 'assets/avatar1.png'
  }

  @Input() template: ItemTemplate;
  @Input() variant: 'grid' | 'list' = 'grid';
}
