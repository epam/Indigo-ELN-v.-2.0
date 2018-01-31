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