(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponents', indigoComponents);

    /* @ngInject */
    function indigoComponents($timeout) {
        var scrollCache = {};

        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoTemplate: '=',
                indigoReadonly: '=',
                model: '=indigoModel',
                indigoExperiment: '=',
                indigoExperimentForm: '=',
                indigoSaveExperimentFn: '&'
            },
            link: link,
            templateUrl: 'scripts/components/entities/template/components/components-templete/components.html',
            controllerAs: 'vm',
            controller: indigoComponentsController
        };

        /* @ngInject */
        function link(scope, element) {
            if (!scope.indigoExperiment) {
                return;
            }
            var id = scope.indigoExperiment.fullId;
            var tc;
            var preventFirstScroll;
            $timeout(function() {
                tc = element.find('.tab-content');
                if (scrollCache[id]) {
                    setTimeout(function() {
                        nostore = true;
                        tc[0].scrollTop = scrollCache[id];
                    }, 100);
                }
                var stimeout,
                    nostore;
                tc.on('scroll', function(e) {
                    // will close some dropdowns EPMLSOPELN-437
                    element.trigger('click');
                    if (nostore) {
                        nostore = false;

                        return;
                    }
                    if (!preventFirstScroll) {
                        scrollCache[id] = this.scrollTop;
                    } else {
                        nostore = true;
                        tc[0].scrollTop = scrollCache[id] || 0;
                    }
                    clearTimeout(stimeout);
                    stimeout = setTimeout(function() {
                        preventFirstScroll = true;
                    }, 300);
                    preventFirstScroll = false;
                    nostore = false;
                });
            }, 100);
        }

        /* @ngInject */
        function indigoComponentsController($scope, $rootScope, Components, ProductBatchSummaryOperations) {
            var vm = this;

            init();

            function init() {
                // for communication between components
                vm.share = {};

                vm.batches = null;
                vm.selectedBatch = null;
                vm.selectedBatchTrigger = 0;

                vm.compoundSummarySelectedRow = compoundSummarySelectedRow;
                vm.onSelectBatch = onSelectBatch;
                vm.onRemoveBatches = onRemoveBatches;

                bindEvents();
            }

            function updateBatches() {
                // TODO: implement find default bath if in productBatch summry hasn't any batches
                vm.batches = _.get($scope.model, 'productBatchSummary.batches') ||
                    _.get($scope.model, _.find(Components, {name: 'preferCompoundDetails'})) || [];
                if (vm.batches.length && !vm.selectedBatch) {
                    updateSelectedBatch();
                }
            }

            function bindEvents() {
                $scope.$on('batch-summary-row-selected', function(event, data) {
                    onSelectBatch(data.row);
                });
                $scope.$on('batch-summary-row-deselected', function() {
                    onSelectBatch();
                });
                $scope.$watch('model', updateBatches);
            }

            function updateSelectedBatch() {
                onSelectBatch(_.first(vm.batches) || null);
            }
            
            function changeBatchSelected(batch, isSelected) {
                if (batch) {
                    batch.$$selected = isSelected;
                }
            }

            function onRemoveBatches(batches) {
                var ind = vm.batches.indexOf(vm.selectedBatch) - 1;
                var deleted = ProductBatchSummaryOperations.deleteBatches();
                if (deleted > 0) {
                    if (ind < 0) {
                        ind = 0;
                    }
                    onSelectBatch(vm.batches[ind]);
                }
            }
            function onSelectBatch(batch) {
                // TODO: should update id of batch from all batches! Now wrong work
                if (vm.selectedBatch) {
                    changeBatchSelected(vm.selectedBatch, false);
                }
                vm.selectedBatch = batch;
                changeBatchSelected(vm.selectedBatch, true);
                vm.selectedBatch = batch;
                vm.selectedBatchTrigger++;

                // TODO: remove if when share will be removed
                _.set(vm.share, 'selectedRow', vm.selectedBatch);
            }

            function compoundSummarySelectedRow(row) {
                vm.share.selectedRow = row || null;
                if (row) {
                    var data = {
                        row: row
                    };
                    $rootScope.$broadcast('batch-summary-row-selected', data);
                } else {
                    $rootScope.$broadcast('batch-summary-row-deselected');
                }
            }
        }
    }
})();
