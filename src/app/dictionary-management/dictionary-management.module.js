var dictionaryManagementConfig = require('./dictionary-management.config');
var DictionaryManagementController = require('./component/dictionary-management.controller');
var DictionaryManagementDeleteDialogController
    = require('./delete-dialog/dictionary-management-delete-dialog.controller');
var DictionaryManagementDeleteWordDialogController =
    require('./delete-word-dialog/dictionary-management-delete-word-dialog.controller');

module.exports = angular
    .module('indigoeln.dictionaryManagement', [])

    .controller('DictionaryManagementController', DictionaryManagementController)
    .controller('DictionaryManagementDeleteDialogController', DictionaryManagementDeleteDialogController)
    .controller('DictionaryManagementDeleteWordDialogController', DictionaryManagementDeleteWordDialogController)

    .config(dictionaryManagementConfig)

    .name;
