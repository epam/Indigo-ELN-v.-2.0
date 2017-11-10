var notebookConfig = require('./notebook.config');
var NotebookDialogController = require('./dialog/notebook-dialog.controller');
var NotebookSelectParentController = require('./select-parent/notebook-select-parent.controller');

module.exports = angular
    .module('indigoeln.notebook', [])

    .controller('NotebookDialogController', NotebookDialogController)
    .controller('NotebookSelectParentController', NotebookSelectParentController)

    .config(notebookConfig)

    .name;
