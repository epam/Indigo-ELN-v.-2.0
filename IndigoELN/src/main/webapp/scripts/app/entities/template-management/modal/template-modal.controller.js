(function() {
    angular
        .module('indigoeln')
        .controller('TemplateModalController', TemplateModalController);

    /* @ngInject */
    function TemplateModalController($scope, $stateParams, Template, notifyService, $state, dragulaService,
                                     Components, pageInfo, EntitiesBrowser, TabKeyUtils) {
        var vm = this;

        vm.components = _.values(Components);
        vm.template = pageInfo.entity || {};
        vm.template.templateContent = vm.template.templateContent || [];

        vm.save = save;
        vm.close = close;
        vm.addTab = addTab;
        vm.removeTab = removeTab;
        vm.removeComponent = removeComponent;

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
                var $el = $(el),
                    $cont = $el.parents('[scroller]').eq(0),
                    top = $cont.scrollTop();
                interval = setInterval(function() {
                    top += up ? -3 : 3;
                    $cont.scrollTop(top).attr('scrollTop', top);
                }, 10);
            });

            $scope.$on('components.over', function() {
                clearInterval(interval);
            });

            $scope.$on('components.dragend', function() {
                clearInterval(interval);
            });

            $scope.$on('components.shadow', function(e, el) {
                var $el = $(el),
                    index = $el.index();
                if (!lastIndex) {
                    lastIndex = index;
                } else {
                    up = lastIndex > index;
                    lastIndex = index;
                }
            });

            sortComponents();
        }

        function save() {
            vm.isSaving = true;
            if (vm.template.id) {
                Template.update(vm.template, onSaveSuccess, onSaveError);
            } else {
                Template.save(vm.template, onSaveSuccess, onSaveError).$promise;
            }
        }

        function close() {
            if (!vm.template.id) {
                var tabName = $state.$current.data.tab.name;
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromName(tabName));
            } else {
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));
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

        function removeComponent() {
            tab.components = _.without(tab.components, component);
            vm.components.push(component);
            sortComponents();
        }

        function sortComponents() {
            vm.components.sort(function(b, a) {
                return (a.name < b.name) ? 1 : (a.name > b.name) ? -1 : 0;
            });
        }

        function onSaveSuccess() {
            vm.isSaving = false;
            vm.close();
        }

        function onSaveError(result) {
            vm.isSaving = false;
            var mess = result.status === 400 ? 'Error saving, template name already exists.' : 'Template is not saved due to server error!';
            notifyService.error(mess);
        }
    }
})();
