(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentController', ExperimentController);

    /* @ngInject */
    function ExperimentController($scope, Dashboard, CONFIG, $filter, $timeout, Experiment) {
        var vm = this;
        var openExperiments;
        var waitingExperiments;
        var submittedExperiments;
        var etimeout;

        vm.experiments = [];
        vm.dView = 'open';
        vm.itemsPerPage = 20;
        vm.signatureServiceUrl = CONFIG['indigoeln.client.signatureservice.url'];

        vm.loadAll = loadAll;
        vm.onViewChange = onViewChange;
        vm.sort = sort;
        vm.doPage = doPage;
        vm.refresh = refresh;
        vm.getIdleWorkdays = getIdleWorkdays;
        vm.experimentEnter = experimentEnter;
        vm.clear = clear;

        vm.loadAll();

        function loadAll() {
            vm.loading = Dashboard.get({}, function(result) {
                openExperiments = result.openAndCompletedExp.filter(function(e) {
                    return e.status === 'Open';
                });
                waitingExperiments = result.waitingSignatureExp;
                submittedExperiments = result.submittedAndSigningExp;
                vm.openExperimentsLength = openExperiments.length;
                vm.waitingExperimentsLength = waitingExperiments.length;
                vm.submittedExperimentsLength = submittedExperiments.length;
                vm.onViewChange();
            }).$promise;
        }

        function onViewChange() {
            if (vm.dView === 'open') {
                vm.curExperiments = openExperiments;
            } else if (vm.dView === 'wait') {
                vm.curExperiments = waitingExperiments;
            } else if (vm.dView === 'submitted') {
                vm.curExperiments = submittedExperiments;
            }
            vm.totalItems = vm.curExperiments.length;
            vm.sort('lastEditDate', true, true);
        }

        function sort(predicate, reverse, noDigest) {
            vm.curExperiments = $filter('orderBy')(vm.curExperiments, predicate, reverse);
            vm.doPage(1);
            if (!noDigest) {
                $scope.$apply();
            }
        }

        function doPage(page) {
            if (page) {
                vm.page = page;
            } else {
                page = vm.page;
            }

            var ind = (page - 1) * vm.itemsPerPage;
            vm.curExperimentsPaged = vm.curExperiments.slice(ind, ind + vm.itemsPerPage);
        }

        function refresh() {
            vm.loadAll();
            vm.clear();
        }

        function getIdleWorkdays(creationDate) {
            return Math.round((new Date() - Date.parse(creationDate)) / (1000 * 60 * 60 * 24));
        }

        function experimentEnter(experiment) {
            if (etimeout) {
                $timeout.cancel(etimeout);
            }

            if (experiment.components) {
                return;
            }

            etimeout = $timeout(function() {
                Experiment.get({
                    experimentId: experiment.experimentId,
                    notebookId: experiment.notebookId,
                    projectId: experiment.projectId
                }).$promise.then(function(data) {
                    if (data.components.reaction) {
                        experiment.reactionImage = data.components.reaction.image;
                    }
                });
            }, 500);
        }

        function clear() {
            vm.experiment = {
                name: null,
                experimentNumber: null,
                templateId: null,
                id: null
            };
        }
    }
})();
