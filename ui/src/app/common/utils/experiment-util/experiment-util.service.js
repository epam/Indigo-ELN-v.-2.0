/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var experimentCompleteModalTemplate = require('../../../experiment/complete-modal/experiment-complete-modal.html');
var experimentSelectSignatureTemplateModal =
    require('../../../experiment/select-signature-template-modal/experiment-select-signature-template-modal.html');

/* @ngInject */
function experimentUtil($state, $uibModal, $q, experimentService, permissionService, signatureTemplatesService,
                        signatureDocumentService, componentsUtil, notifyService) {
    var dlg;

    return {
        versionExperiment: versionExperiment,
        repeatExperiment: repeatExperiment,
        reopenExperiment: reopenExperiment,
        completeExperiment: completeExperiment,
        completeExperimentAndSign: completeExperimentAndSign,
        closeDialog: closeDialog
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
        }, experiment.name).$promise;
    }

    function repeatExperiment(experiment, params) {
        experiment.accessList = permissionService.expandPermission(experiment.accessList);
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
            experiment.accessList = permissionService.expandPermission(experiment.accessList);
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
        closeDialog();
        dlg = $uibModal.open({
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
            controller: 'ExperimentCompleteModalController',
            controllerAs: 'vm'
        });

        return dlg;
    }

    function selectTemplate(componentTemplates, filename, stateParams) {
        return signatureTemplatesService.query({})
            .$promise
            .then(function(result) {
                closeDialog();
                dlg = $uibModal.open({
                    animation: true,
                    template: experimentSelectSignatureTemplateModal,
                    controller: 'ExperimentSelectSignatureTemplateModalController',
                    controllerAs: 'vm',
                    resolve: {
                        result: function() {
                            return result;
                        }
                    }
                });
                return dlg.result
                    .then(function(template) {
                        if (template) {
                            var templates = componentsUtil.getComponentsFromTemplateContent(componentTemplates);

                            return signatureDocumentService.upload(
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

    function closeDialog() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = experimentUtil;
