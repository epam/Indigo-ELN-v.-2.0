var notebookConfig = require('./notebook.config');
var NotebookDialogController = require('./notebook-dialog.controller');
var NotebookSelectParentController = require('./notebook-select-parent.controller');

module.exports = angular
    .module('indigoeln.entities.notebook', [])

    .controller('NotebookDialogController', NotebookDialogController)
    .controller('NotebookSelectParentController', NotebookSelectParentController)

    .config(notebookConfig)

    .name;
