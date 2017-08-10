(function() {
    angular
        .module('indigoeln')
        .controller('PrintModalController', PrintModalController);

    /* @ngInject */
    function PrintModalController($uibModalInstance, entity, Principal, localStorageService, $scope, $timeout) {
        var vm = this;
        var userId;
        var delaySave;
        var storageKey = '-selected-components-for-print';
        vm.dismiss = dismiss;
        vm.confirmCompletion = confirmCompletion;
        console.log('print', entity);

        init();

        function init() {
            var tabs = _.get(entity, 'template.templateContent');
            if (!tabs) {
                if (entity.notebooks) {
                    vm.hasAttachments = true; //project
                } else {
                    vm.askContents = true; //notebooks
                }
                return;
            }
            vm.hasAttachments = false;
            vm.printContent = false;
            vm.components = [];
            _.each(tabs, function(tab) {
                _.each(tab.components, function(comp) {
                    vm.components.push({
                        id: comp.id,
                        value: comp.name
                    });
                    //Should use constant('Components' when ready
                    if (comp.id === 'attachments') {
                        vm.hasAttachments = true;
                    }
                });
            });
            Principal.identity()
                .then(function(user) {
                    userId = user.id;
                    restoreSelection();
                    initEvents();
                });
        }

        function saveSelection() {
            $timeout.cancel(delaySave);
            delaySave = $timeout(function() {
                var lastSelected = localStorageService.get(userId + storageKey) || {};
                _.each(vm.components, function(comp) {
                    lastSelected[comp.id] = comp.isChecked;
                });
                lastSelected.printAttachedPDF = vm.printAttachedPDF;
                lastSelected.printContent = vm.printContent;
                localStorageService.set(userId + storageKey, lastSelected);
            }, 500);
        }

        function restoreSelection() {
            var lastSelected = localStorageService.get(userId + storageKey);
            if (lastSelected) {
                _.each(vm.components, function(comp) {
                    comp.isChecked = lastSelected[comp.id];
                });
                vm.printAttachedPDF = lastSelected.printAttachedPDF;
                vm.printContent = lastSelected.printContent;
            }
        }

        function initEvents() {
            $scope.$watch('vm.components', saveSelection, true);
        }

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmCompletion() {
            var checked = _.filter(vm.components, function(c) {
                return c.isChecked;
            });
            var resultForService = {
                components: _.map(checked, function(c) {
                    return c.id;
                }).join(',')
            };
            if (vm.printAttachedPDF) {
                resultForService.includeAttachments = vm.printAttachedPDF;
            }
            if (vm.printContent) {
                resultForService.withContent = vm.printAttachedPDF;
            }

            $uibModalInstance.close(resultForService);
        }
    }
})();