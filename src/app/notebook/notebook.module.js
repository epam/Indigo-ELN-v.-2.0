var notebookConfig = require('./notebook.config');
var NotebookDetailController = require('./detail/notebook-detail.controller');
var NotebookSelectParentController = require('./select-parent/notebook-select-parent.controller');

module.exports = angular
    .module('indigoeln.notebook', [])

    .controller('NotebookDetailController', NotebookDetailController)
    .controller('NotebookSelectParentController', NotebookSelectParentController)

    .config(notebookConfig)

    .name;
