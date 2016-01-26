'use strict';

angular.module('indigoeln')
    .controller('NewNotebookController', function ($scope, $stateParams, notebookService, notebook) {
        $scope.notebook = notebook;
        $scope.description = '';
    });
