'use strict';

angular.module('indigoeln').controller('ExperimentSubmitController',
    function ($scope, $rootScope, $stateParams, $state, $compile, $window, Experiment, PdfService, pageInfo,
              experimentPdfCreator, SignatureTemplates, $uibModal, PermissionManagement, SignatureDocument) {

        $scope.fullPrint = true;

        $scope.experiment = pageInfo.experiment;
        $scope.notebook = pageInfo.notebook;
        $scope.project = pageInfo.project;

        $scope.batchDetails = $scope.experiment.components.productBatchDetails;
        $scope.batchSummary = $scope.experiment.components.productBatchSummary;
        $scope.conceptDetails = $scope.experiment.components.conceptDetails;
        $scope.reactionDetails = $scope.experiment.components.reactionDetails;
        $scope.reaction = $scope.experiment.components.reaction;
        $scope.molecule = $scope.experiment.components.molecule;

        $scope.currentDate = Date.now();

        var onCompleteSuccess = function () {
            $rootScope.$broadcast('experiment-status-changed',
                {projectId: $stateParams.projectId, notebookId: $stateParams.notebookId, id: $scope.experiment.id});
        };

        var selectTemplate = function (filename) {
            SignatureTemplates.query({}, function (result) {

                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/experiment-select-signature-template-modal.html',
                    controller: function ($scope, $uibModalInstance) {
                        $scope.items = result.Templates;
                        $scope.dismiss = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                        $scope.selectSignatureTemplateModal = function () {
                            $uibModalInstance.close($scope.selectedTemplate);
                        };
                    }
                }).result.then(function (template) {
                    if (template) {
                        SignatureDocument.upload(
                            {
                                fileName: filename.fileName,
                                templateId: template.id,
                                experimentId: $scope.experiment.id,
                                notebookId: $scope.notebook.id,
                                projectId: $scope.project.id
                            }, {},
                            function () {
                                onCompleteSuccess();
                                $state.go('entities.experiment-detail', {
                                    statusChanged: true,
                                    experimentId: $scope.experiment.id,
                                    notebookId: $scope.notebook.id,
                                    projectId: $scope.project.id
                                });
                            });
                    }
                });
            });
        };

        $scope.submit = function () {
            var fileName = $scope.notebook.name + '-' + $scope.experiment.name;
            experimentPdfCreator.createPDF(fileName, selectTemplate);
        };

    });
