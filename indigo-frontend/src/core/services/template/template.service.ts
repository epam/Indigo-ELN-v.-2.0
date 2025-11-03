import { Injectable } from '@angular/core';
import {
  ItemTemplate,
  RootTemplate,
  Template,
} from '@core/types/entities/template.i';
import { ApiService } from '@core/services/api.service';
import { PagedRequest } from '@core/types/request/paged-request.i';

@Injectable({ providedIn: 'root' })
export class TemplateService {
  constructor(private api: ApiService<RootTemplate>) {}

  getTemplates(pager: PagedRequest, search?: string) {
    return this.api.getPaged('templates', pager, { search });
  }

  createTemplate(body: Template) {
    return this.api.create('templates', body);
  }

  updateTemplate(body: ItemTemplate) {
    return this.api.update(`templates/${body.id}`, body);
  }

  getTemplateDictionary() {
    return this.api.getDictionary('templates/dictionary');
  }

  deleteTemplate(templateId: string) {
    return this.api.delete(`templates/`, templateId);
  }
}
