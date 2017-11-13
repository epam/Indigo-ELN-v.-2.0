var dictionaryManagementConfig = require('./dictionary-management.config');
var DictionaryManagementController = require('./component/dictionary-management.controller');
var DictionaryManagementDeleteController = require('./delete-dialog/dictionary-management-delete-dialog.controller');
var DictionaryManagementDeleteWordController =
    require('./delete-word-dialog/dictionary-management-delete-word-dialog.controller');

module.exports = angular
    .module('indigoeln.dictionaryManagement', [])

    .controller('DictionaryManagementController', DictionaryManagementController)
    .controller('DictionaryManagementDeleteController', DictionaryManagementDeleteController)
    .controller('DictionaryManagementDeleteWordController', DictionaryManagementDeleteWordController)

    .config(dictionaryManagementConfig)

    .name;
