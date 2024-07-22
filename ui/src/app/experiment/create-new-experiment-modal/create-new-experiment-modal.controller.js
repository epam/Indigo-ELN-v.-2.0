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

/* @ngInject */
function CreateNewExperimentModalController(componentsUtil, $uibModalInstance, experimentService,
                                            principalService, $q, simpleLocalCache, fullNotebookId,
                                            notebooksForSubCreationService, templateService) {
    var vm = this;
    var lastSelectedTemplateIdKey = '.lastSelectedTemplateId';
    var lastSelectedNotebookIdKey = '.lastSelectedNotebookId';

    init();

    function init() {
        vm.fullNotebookId = fullNotebookId;
        vm.selectedNotebook = '';
        vm.selectedTemplate = '';
        vm.experiment = {
            name: null,
            experimentNumber: null,
            template: null,
            id: null,
            components: {}
        };

        vm.ok = save;
        vm.cancel = cancelPressed;
        vm.onTemplateSelect = onTemplateSelect;
        vm.onNotebookSelect = onNotebookSelect;

        $q.all([
            notebooksForSubCreationService.query().$promise,
            templateService.query({size: 100000}).$promise
        ]).then(function(responses) {
            vm.notebooks = responses[0];
            vm.templates = responses[1];
            selectNotebookById();
            selectTemplateById();
        });
    }

    function getKey(suffix) {
        return principalService.getIdentity().id + suffix;
    }

    function onTemplateSelect() {
        onSelect(getKey(lastSelectedTemplateIdKey), vm.selectedTemplate.id);
    }

    function onNotebookSelect() {
        onSelect(getKey(lastSelectedNotebookIdKey), vm.selectedNotebook.fullId);
    }

    function onSelect(key, id) {
        simpleLocalCache.putByKey(key, id);
    }

    function selectNotebookById() {
        var lastNotebookId = vm.fullNotebookId
            || simpleLocalCache.getByKey(getKey(lastSelectedNotebookIdKey));

        if (lastNotebookId) {
            vm.selectedNotebook = _.find(vm.notebooks, {fullId: lastNotebookId});
        }
    }

    function selectTemplateById() {
        var lastSelectedTemplateId = simpleLocalCache.getByKey(getKey(lastSelectedTemplateIdKey));

        if (lastSelectedTemplateId) {
            vm.selectedTemplate = _.find(vm.templates, {id: lastSelectedTemplateId});
        }
    }

    function save() {
        vm.isSaving = true;
        vm.experiment = _.extend(vm.experiment, {
            template: vm.selectedTemplate
        });

        initComponents(vm.experiment);

        experimentService.save({
            notebookId: vm.selectedNotebook.id,
            projectId: vm.selectedNotebook.parentId
        }, vm.experiment, onSaveSuccess, onSaveError);
    }

    function initComponents(experiment) {
        componentsUtil.initComponents(
            experiment.components,
            experiment.template.templateContent
        );
    }

    function cancelPressed() {
        $uibModalInstance.dismiss();
    }

    function onSaveSuccess(result) {
        var experiment = {
            projectId: vm.selectedNotebook.parentId,
            notebookId: vm.selectedNotebook.id,
            id: result.id
        };
        onSaveSuccess.isSaving = false;
        $uibModalInstance.close(experiment);
    }

    function onSaveError() {
        vm.isSaving = false;
    }
}

module.exports = CreateNewExperimentModalController;
