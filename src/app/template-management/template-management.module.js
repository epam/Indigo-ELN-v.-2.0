/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var templateManagementConfig = require('./template-management.config');
var TemplateManagementController = require('./component/template-management.controller');
var TemplateDeleteController = require('./delete-dialog/template-delete-dialog.controller');
var TemplateDetailController = require('./detail/template-detail.controller');
var TemplateModalController = require('./modal/template-modal.controller');

module.exports = angular
    .module('indigoeln.templateManagement', [])

    .controller('TemplateManagementController', TemplateManagementController)
    .controller('TemplateDeleteController', TemplateDeleteController)
    .controller('TemplateDetailController', TemplateDetailController)
    .controller('TemplateModalController', TemplateModalController)

    .config(templateManagementConfig)

    .name;
