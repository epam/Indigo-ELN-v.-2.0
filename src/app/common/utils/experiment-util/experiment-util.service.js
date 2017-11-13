var experimentCompleteModalTemplate = require('../../../experiment/complete-modal/experiment-complete-modal.html');
var experimentSelectSignatureTemplateModal =
    require('../../../experiment/select-signature-template-modal/experiment-select-signature-template-modal.html');

/* @ngInject */
function experimentUtil($state, $uibModal, $q, experimentService, permissionManagementService, signatureTemplates,
                        signatureDocument, componentsUtils, notifyService) {
    return {
        versionExperiment: versionExperiment,
        repeatExperiment: repeatExperiment,
        reopenExperiment: reopenExperiment,
        completeExperiment: completeExperiment,
        completeExperimentAndSign: completeExperimentAndSign
    };

    function goToExperimentDetail(result, params) {
        $state.go('entities.experiment-detail', {
            experimentId: result.id,
            notebookId: params.notebookId,
            projectId: params.projectId
        });
    }

    function versionExperiment(experiment, params) {
        return experimentService.version({
            projectId: params.projectId,
            notebookId: params.notebookId
        }, experiment.name, function(result) {
            goToExperimentDetail(result, params);
        }).$promise;
    }

    function repeatExperiment(experiment, params) {
        experiment.accessList = permissionManagementService.expandPermission(experiment.accessList);
        var ec = experiment.components;
        var components = {
            reactionDetails: ec.reactionDetails,
            conceptDetails: ec.conceptDetails,
            reaction: ec.reaction,
            stoichTable: ec.stoichTable,
            experimentDescription: ec.experimentDescription
        };

        var experimentForSave = {
            accessList: experiment.accessList,
            components: components,
            name: experiment.name,
            status: 'Open',
            template: experiment.template
        };

        return experimentService.save({
            projectId: params.projectId,
            notebookId: params.notebookId
        }, experimentForSave, function(result) {
            goToExperimentDetail(result, params);
        }).$promise;
    }

    function reopenExperiment(experiment, params) {
        return experimentService.reopen({
            projectId: params.projectId,
            notebookId: params.notebookId,
            experimentId: params.experimentId
        }, experiment.version)
            .$promise
            .catch(function(error) {
                notifyService.error(error.data.message);
            });
    }

    function completeExperiment(experiment, params, notebookName) {
        return openCompleteConfirmationModal(experiment, notebookName).result.then(function() {
            experiment.accessList = permissionManagementService.expandPermission(experiment.accessList);
            var experimentForSave = _.extend({}, experiment, {
                status: 'Completed'
            });

            return experimentService.update({
                projectId: params.projectId,
                notebookId: params.notebookId
            }, experimentForSave).$promise;
        });
    }

    function completeExperimentAndSign(experiment, params, notebookName, experimentTitle) {
        return openCompleteConfirmationModal(experiment, notebookName).result.then(function() {
            return selectTemplate(experiment.template.templateContent, experimentTitle, params);
        });
    }

    function openCompleteConfirmationModal(experiment, notebookName) {
        return $uibModal.open({
            animation: true,
            template: experimentCompleteModalTemplate,
            resolve: {
                fullExperimentName: function() {
                    var fullName = notebookName + '-' + experiment.name;
                    if (experiment.experimentVersion > 1 || !experiment.lastVersion) {
                        fullName += ' v' + experiment.experimentVersion;
                    }

                    return fullName;
                }
            },
            conoller: 'ExperimentCompleteModalController',
            controllerAs: 'vm'
        });
    }

    function selectTemplate(componentTemplates, filename, stateParams) {
        return signatureTemplates.query({})
            .$promise
            .then(function(result) {
                return $uibModal
                    .open({
                        animation: true,
                        template: experimentSelectSignatureTemplateModal,
                        controller: 'ExperimentSelectSignatureTemplateModalController',
                        controllerAs: 'vm',
                        resolve: {
                            result: function() {
                                return result;
                            }
                        }
                    })
                    .result
                    .then(function(template) {
                        if (template) {
                            var templates = componentsUtils.getComponentsFromTemplateContent(componentTemplates);

                            return signatureDocument.upload(
                                {
                                    fileName: filename + '.pdf',
                                    components: getComponentsForPrint(templates),
                                    templateId: template.id,
                                    experimentId: stateParams.experimentId,
                                    notebookId: stateParams.notebookId,
                                    projectId: stateParams.projectId
                                }, {}).$promise;
                        }

                        return $q.reject();
                    });
            });
    }

    function getComponentsForPrint(componentTemplates) {
        return _.map(componentTemplates, 'field').join().replace('attachments', 'attachments&includeAttachments=true');
    }
}

module.exports = experimentUtil;
