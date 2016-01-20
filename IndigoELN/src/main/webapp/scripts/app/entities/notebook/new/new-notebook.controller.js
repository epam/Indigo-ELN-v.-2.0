'use strict';

angular.module('indigoeln')
    .controller('NewNotebookController', function ($stateParams, notebookService, notebook) {
        var vm = this;
        vm.notebook = notebook;
        vm.description = "";
    });
