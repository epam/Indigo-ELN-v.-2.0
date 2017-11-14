var notebookConfig = require('./notebook.config');
var NotebookDialogController = require('./detail/notebook-detail.controller');
var NotebookSelectParentController = require('./select-parent/notebook-select-parent.controller');

module.exports = angular
    .module('indigoeln.notebook', [])

    .controller('NotebookDialogController', NotebookDialogController)
    .controller('NotebookSelectParentController', NotebookSelectParentController)

    .config(notebookConfig)

    .name;
