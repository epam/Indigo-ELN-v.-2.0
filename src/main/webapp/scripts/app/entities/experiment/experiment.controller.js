angular.module('indigoeln')
    .controller('ExperimentController', function($rootScope, $scope, $state, Dashboard, CONFIG, $filter, $timeout, Experiment) {
        var vm = this;

        vm.experimentsStatisticsText = 'experiments statistics';
        vm.openText = 'Open';
        vm.waitingAuthorsSignatureText = 'Waiting author’s signature';
        vm.submittedByAuthorText = 'Submitted by author';
        vm.experimentsLCText = 'experiments';
        vm.experimentsUCText = 'Experiments';
        vm.experimentsCommentText = 'created during the last month';
        vm.openExperimentsText = 'Open experiments';
        vm.experimentIDText = 'Experiment ID';
        vm.titleText = 'Title';
        vm.authorText = 'Author';
        vm.projectText = 'Project';
        vm.creationDateText = 'Creation Date';
        vm.lastEditDateText = 'Last Edit Date';
        vm.idleWorkdaysText = 'Idle Workdays';
        vm.statusText = 'Status';
        vm.submitterText = 'Submitter';
        vm.witnessText = 'Witness';
        vm.lastActionDateText = 'Last Action Date';
        vm.commentsText = 'Comments';
        vm.experimentsWaitingAuthorsSignatureText = 'Experiments Waiting Author’s Signature';
        vm.experimentsSubmittedByAuthorText = 'Experiments Submitted by Author';

        $scope.experiments = [];
        $scope.dView = 'open';
        $scope.itemsPerPage = 20;
        var openExperiments, waitingExperiments, submittedExperiments
        $scope.loadAll = function() {
            $scope.loading = Dashboard.get({}, function(result) {
                openExperiments = result.openAndCompletedExp.filter(function(e) {
                    return e.status == 'Open'
                })
                waitingExperiments = result.waitingSignatureExp;
                submittedExperiments = result.submittedAndSigningExp;
                $scope.openExperimentsLength = openExperiments.length
                $scope.waitingExperimentsLength = waitingExperiments.length
                $scope.submittedExperimentsLength = submittedExperiments.length
                $scope.onViewChange()
            }).$promise;
        };
        $scope.onViewChange = function(val) {
            if ($scope.dView == 'open') {
                $scope.curExperiments = openExperiments;
            } else if ($scope.dView == 'wait') {
                $scope.curExperiments = waitingExperiments;
            } else if ($scope.dView == 'submitted') {
                $scope.curExperiments = submittedExperiments;
            }
            $scope.totalItems = $scope.curExperiments.length;
            $scope.sort('lastEditDate', true, true)
        }
        $scope.loadAll();
        $scope.sort = function(predicate, reverse, noDigest) {
            $scope.curExperiments = $filter('orderBy')($scope.curExperiments, predicate, reverse)
            $scope.doPage(1)
            if (!noDigest)
                $scope.$apply();
        }
        $scope.doPage = function(page) {
            if (page)
                $scope.page = page;
            else
                page = $scope.page;
            var ind = (page - 1) * $scope.itemsPerPage;
            $scope.curExperimentsPaged = $scope.curExperiments.slice(ind, ind + $scope.itemsPerPage);
        }
        $scope.signatureServiceUrl = CONFIG['indigoeln.client.signatureservice.url'];
        $scope.refresh = function() {
            $scope.loadAll();
            $scope.clear();
        };
        $scope.getIdleWorkdays = function(creationDate, lastEditDate) {
            return Math.round((new Date() - Date.parse(creationDate)) / (1000 * 60 * 60 * 24));
        }
        var etimeout;
        $scope.experimentEnter = function(experiment) {
            if (etimeout) $timeout.cancel(etimeout)
            if (experiment.components) return;
            etimeout = $timeout(function() {
                Experiment.get({
                    experimentId: experiment.experimentId,
                    notebookId: experiment.notebookId,
                    projectId: experiment.projectId
                }).$promise.then(function(data) {
                    if (data.components.reaction) {
                        experiment.reactionImage =  data.components.reaction.image;
                    }
                })
            }, 500);
        };

        $scope.clear = function() {
            $scope.experiment = {
                name: null,
                experimentNumber: null,
                templateId: null,
                id: null
            };
        };
    });
