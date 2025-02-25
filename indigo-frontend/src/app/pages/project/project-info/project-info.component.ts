import { AttachmentComponent } from '@/core/components/common/attachment/attachment.component';
import { AvatarComponent } from '@/core/components/common/avatar/avatar.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { CopyComponent } from '@/core/components/common/copy/copy.component';
import { CounterComponent } from '@/core/components/common/counter/counter.component';
import { DropdownMenuComponent } from '@/core/components/common/dropdown-menu/dropdown-menu.component';
import { Attachment } from '@/core/types/attachment.i';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-project-info',
  standalone: true,
  imports: [
    CommonModule,
    ButtonComponent,
    ChipComponent,
    CounterComponent,
    AvatarComponent,
    AttachmentComponent,
    DropdownMenuComponent,
    CopyComponent,
  ],

  templateUrl: './project-info.component.html',
})
export class ProjectInfoComponent {
  @Input() project = {
    name: 'Test Project',
    keywords: ['Chips Text', 'Chips Text', 'Chips Text', 'Chips Text'],
    description:
      'The aim of this project is to investigate the best way of aspirin synthesis strategies.',
    details:
      'The synthesis of aspirin may be achieved in one simple step, O-acetylation of salicylic acid, which is incorporated into many synthetic chemistry laboratory courses. An additional step may be added to the synthesis of aspirin: conversion of oil of wintergreen (methylsalicylate) to salicylic acid.',
    attachments: [
      {
        name: 'Test File Name.docx',
        type: 'docx',
        size: '16KB',
        date: 'Jan 22, 2025 18:51:00 CET',
        author: 'Cameron W.',
      },
      {
        name: 'Test File Name.xlsx',
        type: 'xlsx',
        size: '16KB',
        date: 'Jan 22, 2025 18:51:00 CET',
        author: 'Cameron W.',
      },
      {
        name: 'Test File Name.png',
        type: 'png',
        size: '16KB',
        date: 'Jan 22, 2025 18:51:00 CET',
        author: 'Cameron W.',
      },
    ] as Attachment[],
    team: [
      {
        name: 'Kristin Watson',
        email: 'oliver.tresk@indigo.com',
        role: 'Admin',
        avatar: 'assets/avatar2.png',
      },
      {
        name: 'Floyd Miles',
        email: 'oliver.tresk@indigo.com',
        role: 'Member',
        avatar: 'assets/avatar3.png',
      },
      // ... other team members
    ],
  };
}
