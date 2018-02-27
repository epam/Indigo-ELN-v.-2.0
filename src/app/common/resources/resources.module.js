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

var dictionaryService = require('./dictionary-service/dictionary.service');
var notebookService = require('./notebook/notebook.service');
var projectService = require('./project/project.service');
var registrationService = require('./registration-service/registration.service');
var roleService = require('./role-service/role.service');
var searchService = require('./search-service/search.service');
var signatureDocumentService = require('./signature-document/signature-document.service');
var signatureTemplatesService = require('./signature-templates/signature-templates.service');
var templateService = require('./template/template.service');
var userReagentsService = require('./user-reagents/user-reagents.service');
var userService = require('./user-service/user.service');
var userPasswordValidationService = require('./user-service/user-password-validation.service');
var dashboardService = require('./dashboard.service');
var experimentService = require('./experiment.service');
var notebookSummaryExperimentsService = require('./notebook-summary-experiments.service');
var notebooksForSubCreationService = require('./notebooks-for-sub-creation.service');
var projectsForSubCreationService = require('./projects-for-sub-creation.service');

module.exports = angular
    .module('indigoeln.common.resources', [])

    .factory('dictionaryService', dictionaryService)
    .factory('notebookService', notebookService)
    .factory('projectService', projectService)
    .factory('registrationService', registrationService)
    .factory('roleService', roleService)
    .factory('searchService', searchService)
    .factory('signatureDocumentService', signatureDocumentService)
    .factory('signatureTemplatesService', signatureTemplatesService)
    .factory('templateService', templateService)
    .factory('userReagentsService', userReagentsService)
    .factory('userService', userService)
    .factory('userPasswordValidationService', userPasswordValidationService)
    .factory('dashboardService', dashboardService)
    .factory('experimentService', experimentService)
    .factory('notebookSummaryExperimentsService', notebookSummaryExperimentsService)
    .factory('notebooksForSubCreationService', notebooksForSubCreationService)
    .factory('projectsForSubCreationService', projectsForSubCreationService)

    .name;
