(function() {
    angular
        .module('indigoeln')
        .directive('indigoReactionDetails', indigoReactionDetails);

    function indigoReactionDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/reaction-details/reaction-details.html',
            controller: indigoReactionDetailsController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '='
            }
        };

        /* @ngInject */
        function indigoReactionDetailsController($state, $q, Principal, Dictionary, Users, notifyService) {
            var vm = this;
            var deferred;

            vm.model = vm.model || {};
            vm.model.reactionDetails = vm.model.reactionDetails || {};

            vm.onLinkedExperimentClick = onLinkedExperimentClick;
            vm.onAddLinkedExperiment = onAddLinkedExperiment;
            vm.getExperiments = getExperiments;

            init();

            function init() {
                Users.get().then(function (dictionary) {
                    vm.users = dictionary.words;

                    vm.model.reactionDetails.experimentCreator = vm.model.reactionDetails.experimentCreator ||
                        _.find(vm.users, {
                            id: Principal.getIdentity().id
                        });
                    vm.model.reactionDetails.experimentCreator = getUser(vm.users, vm.model.reactionDetails.experimentCreator);

                    vm.model.reactionDetails.batchOwner = vm.model.reactionDetails.batchOwner ||
                        _.filter(vm.users, {
                            id: Principal.getIdentity().id
                        });
                    vm.model.reactionDetails.batchOwner = updateUsersById(vm.model.reactionDetails.batchOwner, vm.users);
                    vm.model.reactionDetails.coAuthors = updateUsersById(vm.model.reactionDetails.coAuthors, vm.users);
                });
            }

            function updateUsersById(array, users) {
                return _.map(array, function(user) {
                    return getUser(users, user);
                });
            }

            function getUser(users, user) {
                return _.find(users, function(element) {
                        return element.id === user.id;
                    }) || user;
            }

            function onLinkedExperimentClick(tag) {
                loadExperiments().then(function(experiments) {
                    var experiment = _.find(experiments, function(experiment) {
                        return experiment.name === tag.text;
                    });
                    if (!experiment) {
                        notifyService.error('Can not find a experiment with the name: ' + tag.text);

                        return;
                    }
                    $state.go('entities.experiment-detail', {
                        experimentId: experiment.id,
                        notebookId: experiment.notebookId,
                        projectId: experiment.projectId
                    });
                });
            }

            function onAddLinkedExperiment(tag) {
                var _deferred = $q.defer();
                loadExperiments().then(function(experiments) {
                    _deferred.resolve(_.isObject(_.find(experiments, function(experiment) {
                        return experiment.name === tag.text;
                    })));
                });

                return _deferred.promise;
            }

            function getExperiments(query) {
                var _deferred = $q.defer();
                loadExperiments().then(function(experiments) {
                    var filtered = _.chain(experiments).filter(function(experiment) {
                        return experiment.name.startsWith(query);
                    }).map(function(experiment) {
                        return experiment.name;
                    }).value();
                    _deferred.resolve(filtered);
                });

                return _deferred.promise;
            }

            function loadExperiments() {
                if (deferred) {
                    return deferred.promise;
                }

                deferred = $q.defer();
                Dictionary.get({
                    id: 'experiments'
                }, function(dictionary) {
                    deferred.resolve(dictionary.words);
                });

                return deferred.promise;
            }
        }
    }
})();
