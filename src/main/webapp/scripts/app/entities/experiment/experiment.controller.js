(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentController', ExperimentController);

    /* @ngInject */
    function ExperimentController($scope, Dashboard, CONFIG, $filter, $timeout, Experiment) {
        var self = this,
            openExperiments,
            waitingExperiments,
            submittedExperiments,
            etimeout;

        self.experiments = [];
        self.dView = 'open';
        self.itemsPerPage = 20;
        self.signatureServiceUrl = CONFIG['indigoeln.client.signatureservice.url'];

        self.loadAll            = loadAll;
        self.onViewChange       = onViewChange;
        self.sort               = sort;
        self.doPage             = doPage;
        self.refresh            = refresh;
        self.getIdleWorkdays    = getIdleWorkdays;
        self.experimentEnter    = experimentEnter;
        self.clear              = clear;

        self.loadAll();

        function loadAll() {
            self.loading = Dashboard.get({}, function (result) {
                openExperiments = result.openAndCompletedExp.filter(function (e) {
                    return e.status === 'Open';
                });
                waitingExperiments = result.waitingSignatureExp;
                submittedExperiments = result.submittedAndSigningExp;
                self.openExperimentsLength = openExperiments.length;
                self.waitingExperimentsLength = waitingExperiments.length;
                self.submittedExperimentsLength = submittedExperiments.length;
                self.onViewChange();
            }).$promise;
        }

        function onViewChange() {
            if (self.dView === 'open') {
                self.curExperiments = openExperiments;
            } else if (self.dView === 'wait') {
                self.curExperiments = waitingExperiments;
            } else if (self.dView === 'submitted') {
                self.curExperiments = submittedExperiments;
            }
            self.totalItems = self.curExperiments.length;
            self.sort('lastEditDate', true, true);
        }

        function sort(predicate, reverse, noDigest) {
            self.curExperiments = $filter('orderBy')(self.curExperiments, predicate, reverse);
            self.doPage(1);
            if (!noDigest) {
                $scope.$apply();
            }
        }

        function doPage(page) {
            if (page) {
                self.page = page;
            }
            else {
                page = self.page;
            }

            var ind = (page - 1) * self.itemsPerPage;
            self.curExperimentsPaged = self.curExperiments.slice(ind, ind + self.itemsPerPage);
        }

        function refresh() {
            self.loadAll();
            self.clear();
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

            etimeout = $timeout(function () {
                Experiment.get({
                    experimentId: experiment.experimentId,
                    notebookId: experiment.notebookId,
                    projectId: experiment.projectId
                }).$promise.then(function (data) {
                    if (data.components.reaction) {
                        experiment.reactionImage = data.components.reaction.image;
                    }
                });
            }, 500);
        }

        function clear() {
            self.experiment = {
                name: null,
                experimentNumber: null,
                templateId: null,
                id: null
            };
        }
    }
})();
