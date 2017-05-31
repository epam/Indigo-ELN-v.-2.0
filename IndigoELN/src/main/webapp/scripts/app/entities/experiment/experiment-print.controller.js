angular.module('indigoeln').controller('ExperimentPrintController',
    function ($scope, pageInfo, experimentPdfCreator) {

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
        $scope.preferredCompounds = $scope.experiment.components.preferredCompoundSummary;
        if ($scope.batchSummary)
            $scope.registrationSummary = {batches : $scope.batchSummary.batches.filter(function(b) { return b.conversationalBatchNumber}) }

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
            if ($scope.experiment.experimentVersion > 1 || !$scope.experiment.lastVersion) {
                fileName += ' v' + $scope.experiment.experimentVersion;
            }
            experimentPdfCreator.createPDF(fileName, downloadReport);
        };
        $scope.toDigits = function(o) {
            if (!o || o.value == 0) return '';
            var val = o.value ? Number(o.value) : Number(o);
            if (!val) return '';
            var unit = o.unit ? ' ' + o.unit : ''; 
            return val.toFixed(3).replace(/0+$/,'') + unit;
        }
        $scope.joinArr = function(users, fi) {
            return users ? users.map(function(u) { return fi ? u[fi] : u.name }).join(', ') : null;
        }
        console.warn($scope.experiment)

    });
