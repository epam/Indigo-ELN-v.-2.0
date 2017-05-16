angular.module('indigoeln')
    .factory('ExperimentUtil', function ($rootScope, $state, $uibModal, $q, Experiment, PermissionManagement) {
        var versionExperiment = function (experiment, params) {
            return Experiment.version({
                projectId: params.projectId,
                notebookId: params.notebookId
            }, experiment.name, function (result) {
                $state.go('entities.experiment-detail', {
                    experimentId: result.id,
                    notebookId: params.notebookId,
                    projectId: params.projectId
                });
                $rootScope.$broadcast('experiment-created', {
                    projectId: params.projectId,
                    notebookId: params.notebookId,
                    id: result.id
                });
                $rootScope.$broadcast('experiment-version-created', {
                    projectId: params.projectId,
                    notebookId: params.notebookId,
                    name: result.name
                });
                $rootScope.$broadcast('experiment-updated', experiment)
            }).$promise;
        };

        var repeatExperiment = function (experiment, params) {
            experiment.accessList = PermissionManagement.expandPermission(experiment.accessList);
            console.log('repeat', experiment)
            var ec = experiment.components;
            var components = {
                reactionDetails : ec.reactionDetails,
                conceptDetails : ec.conceptDetails,
                reaction : ec.reaction,
                stoichTable : ec.stoichTable,
                experimentDescription : ec.experimentDescription
            }
            
            var experimentForSave = {
                accessList: experiment.accessList,
                components: components,
                name: experiment.name,
                status: 'Open',
                template: experiment.template
            };
            var productBatchSummary = experimentForSave.components.productBatchSummary;
            if (productBatchSummary) {
                productBatchSummary.batches = [];
            }
            return Experiment.save({
                projectId: params.projectId,
                notebookId: params.notebookId
            }, experimentForSave, function (result) {
                $state.go('entities.experiment-detail', {
                    experimentId: result.id,
                    notebookId: params.notebookId,
                    projectId: params.projectId
                });
                $rootScope.$broadcast('experiment-created', {
                    projectId: params.projectId,
                    notebookId: params.notebookId,
                    id: result.id
                });
            }).$promise;
        };

        var onChangeStatusSuccess = function (result, status) {
            var statuses = {};
            statuses[result.fullId] = status;
            $rootScope.$broadcast('experiment-status-changed', statuses);
        };

        var openCompleteConfirmationModal = function (experiment, notebookName) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/experiment-complete-modal.html',
                resolve: {
                    fullExperimentName: function () {
                        var fullName = notebookName + '-' + experiment.name;
                        if (experiment.experimentVersion > 1 || !experiment.lastVersion) {
                            fullName += ' v' + experiment.experimentVersion;
                        }
                        return fullName;
                    }
                },
                controller: function ($scope, $uibModalInstance, fullExperimentName) {
                    $scope.fullExperimentName = fullExperimentName;
                    $scope.dismiss = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.confirmCompletion = function () {
                        $uibModalInstance.close(true);
                    };
                }
            });
        };

        var reopenExperiment = function (experiment, params) {
            experiment.accessList = PermissionManagement.expandPermission(experiment.accessList);
            var experimentForSave = _.extend({}, experiment, {status: 'Open'});
            return Experiment.update({
                projectId: params.projectId,
                notebookId: params.notebookId
            }, experimentForSave, function (result) {
                onChangeStatusSuccess(result, 'Open');
            }).$promise;
        };

        var completeExperimentAndSign = function (experiment, params, notebookName) {
            openCompleteConfirmationModal(experiment, notebookName).result.then(function () {
                // show PDF preview
                $state.go('experiment-preview-submit', {
                    experimentId: params.experimentId,
                    notebookId: params.notebookId,
                    projectId: params.projectId
                });
            });
        };

        var completeExperiment = function (experiment, params, notebookName) {
            var defer = $q.defer();
            openCompleteConfirmationModal(experiment, notebookName).result.then(function () {
                experiment.accessList = PermissionManagement.expandPermission(experiment.accessList);
                var experimentForSave = _.extend({}, experiment, {status: 'Completed'});
                Experiment.update({
                    projectId: params.projectId,
                    notebookId: params.notebookId
                }, experimentForSave, function (result) {
                    onChangeStatusSuccess(result, 'Completed');
                    defer.resolve();
                });
            });
            return defer.promise;
        };

        var printExperiment = function (params) {
            $state.go('experiment-print', {
                experimentId: params.experimentId,
                notebookId: params.notebookId,
                projectId: params.projectId
            });
        };

        return {
            printExperiment: printExperiment,
            versionExperiment: versionExperiment,
            repeatExperiment: repeatExperiment,
            reopenExperiment: reopenExperiment,
            completeExperiment: completeExperiment,
            completeExperimentAndSign: completeExperimentAndSign
        };
    });
