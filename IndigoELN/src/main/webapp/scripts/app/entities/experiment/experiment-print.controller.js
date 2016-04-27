'use strict';

angular.module('indigoeln').controller('ExperimentPrintController',
    function ($scope, $rootScope, $stateParams, $state, $compile, $window, Experiment, PdfService, pageInfo,
              experimentPdfCreator) {

        $scope.fullPrint = false;

        $scope.experiment = pageInfo.experiment;
        $scope.notebook = pageInfo.notebook;
        $scope.project = pageInfo.project;

        $scope.batchDetails = $scope.experiment.components.productBatchDetails;
        $scope.batchSummary = $scope.experiment.components.productBatchSummary;
        $scope.conceptDetails = $scope.experiment.components.conceptDetails;
        $scope.reactionDetails = $scope.experiment.components.reactionDetails;
        $scope.reaction = $scope.experiment.components.reaction;
        $scope.molecule = $scope.experiment.components.molecule;
        $scope.experimentDescription = $scope.experiment.components.experimentDescription;
        $scope.stoichTable = $scope.experiment.components.stoichTable;

        $scope.currentDate = Date.now();

        var downloadReport = function (response) {
            var hiddenFrameId = 'hiddenDownloader';
            var hiddenDownloader = $('#' + hiddenFrameId);
            if (!hiddenDownloader.length) {
                hiddenDownloader = $('<iframe id="' + hiddenFrameId + '" style="display:none"/>');
                $('body').append(hiddenDownloader);
            }
            hiddenDownloader.attr('src', 'api/print?fileName=' + response.fileName);
            $scope.isPrinting = false;
        };

        $scope.print = function () {
            $scope.isPrinting = true;
            var fileName = $scope.notebook.name + '-' + $scope.experiment.name;
            experimentPdfCreator.createPDF(fileName, downloadReport);
        };

    });
