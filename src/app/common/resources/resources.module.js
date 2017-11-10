var dictionaryService = require('./dictionary-service/dictionary.service');
var notebookService = require('./notebook/notebook.service');
var projectService = require('./project/project.service');
var registrationService = require('./registration-service/registration.service');
var roleService = require('./role-service/role.service');
var searchService = require('./search-service/search.service');
var signatureDocument = require('./signature-document/signature-document.service');
var signatureTemplates = require('./signature-templates/signature-templates.service');
var templateService = require('./template/template.service');
var userReagents = require('./user-reagents/user-reagents.service');
var userService = require('./user-service/user.service');
var dashboardService = require('./dashboard.service');
var experimentService = require('./experiment.service');
var notebookSummaryExperiments = require('./notebook-summary-experiments.service');
var notebooksForSubCreation = require('./notebooks-for-sub-creation.service');
var projectsForSubCreation = require('./projects-for-sub-creation.service');

module.exports = angular
    .module('indigoeln.common.resources', [])

    .factory('dictionaryService', dictionaryService)
    .factory('notebookService', notebookService)
    .factory('projectService', projectService)
    .factory('registrationService', registrationService)
    .factory('roleService', roleService)
    .factory('searchService', searchService)
    .factory('signatureDocument', signatureDocument)
    .factory('signatureTemplates', signatureTemplates)
    .factory('templateService', templateService)
    .factory('userReagents', userReagents)
    .factory('userService', userService)
    .factory('dashboardService', dashboardService)
    .factory('experimentService', experimentService)
    .factory('notebookSummaryExperiments', notebookSummaryExperiments)
    .factory('notebooksForSubCreation', notebooksForSubCreation)
    .factory('projectsForSubCreation', projectsForSubCreation)

    .name;
