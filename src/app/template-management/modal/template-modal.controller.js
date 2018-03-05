/* @ngInject */
function TemplateModalController($scope, $stateParams, templateService, notifyService, $state, dragulaService,
                                 typeOfComponents, pageInfo, entitiesBrowserService, tabKeyService, $interval) {
    var vm = this;

    vm.components = _.values(typeOfComponents);
    vm.template = pageInfo.entity || {};
    vm.template.templateContent = vm.template.templateContent || [];

    vm.save = save;
    vm.close = close;
    vm.addTab = addTab;
    vm.removeTab = removeTab;
    vm.removeComponent = removeComponent;
    vm.checkName = checkName;

    init();

    function init() {
        if (!vm.template.templateContent.length) {
            vm.addTab();
        }

        dragulaService.options($scope, 'tabs', {
            moves: function(el, container, handle) {
                return !handle.classList.contains('draggable-component') && !handle.classList.contains('no-draggable');
            }
        });

        // dragula autoscroller
        var lastIndex;
        var up = true;
        var interval;
        $scope.$on('components.out', function(e, el) {
            var $el = angular.element(el);
            var $cont = $el.parents('[scroller]').eq(0);
            var top = $cont.scrollTop();

            interval = $interval(function() {
                top += up ? -3 : 3;
                $cont.scrollTop(top).attr('scrollTop', top);
            }, 10);
        });

        $scope.$on('$destroy', cancelInterval);
        $scope.$on('components.over', cancelInterval);
        $scope.$on('components.dragend', cancelInterval);

        $scope.$on('components.shadow', function(e, el) {
            var $el = angular.element(el);
            var index = $el.index();
            if (!lastIndex) {
                lastIndex = index;
            } else {
                up = lastIndex > index;
                lastIndex = index;
            }
        });

        sortComponents();

        function cancelInterval() {
            $interval.cancel(interval);
        }
    }

    function save() {
        vm.isSaving = true;
        if (vm.template.id) {
            templateService.update(vm.template, onSaveSuccess, onSaveError);
        } else {
            templateService.save(vm.template, onSaveSuccess, onSaveError);
        }
    }

    function close() {
        if (!vm.template.id) {
            var tabName = $state.$current.data.tab.name;
            entitiesBrowserService.close(tabKeyService.getTabKeyFromName(tabName));
        } else {
            entitiesBrowserService.close(tabKeyService.getTabKeyFromParams($stateParams));
        }
    }

    function addTab() {
        vm.template.templateContent.push({
            name: 'Tab ' + (vm.template.templateContent.length + 1),
            components: []
        });
    }

    function removeTab(tab) {
        vm.template.templateContent = _.without(vm.template.templateContent, tab);
    }

    function removeComponent(tab, component) {
        tab.components = _.without(tab.components, component);
        vm.components.push(component);
        sortComponents();
    }

    function sortComponents() {
        vm.components.sort(function(b, a) {
            /* eslint no-nested-ternary: "off"*/
            return (a.name < b.name) ? 1 : (a.name > b.name) ? -1 : 0;
        });
    }

    function onSaveSuccess() {
        vm.isSaving = false;
        vm.close();
    }

    function onSaveError(result) {
        vm.isSaving = false;
        var mess = result.status === 400
            ? 'Error saving, template name already exists.'
            : 'Template is not saved due to server error!';
        notifyService.error(mess);
    }

    function checkName(name) {
        if (name.trim() === '') {
            return 'Tab name shouldn\'t be empty';
        }
    }
}

module.exports = TemplateModalController;
