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
