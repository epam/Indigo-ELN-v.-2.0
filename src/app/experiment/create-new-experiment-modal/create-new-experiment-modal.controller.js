/* @ngInject */
function CreateNewExperimentModalController(componentsUtil, $uibModalInstance, experimentService,
                                            principalService, $q, simpleLocalCache, fullNotebookId,
                                            notebooksForSubCreationService, templateService, entityTreeService) {
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
        entityTreeService.addExperiment(result);
        $uibModalInstance.close(experiment);
    }

    function onSaveError() {
        vm.isSaving = false;
    }
}

module.exports = CreateNewExperimentModalController;
