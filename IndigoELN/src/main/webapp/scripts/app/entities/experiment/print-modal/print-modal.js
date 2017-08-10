angular.module('indigoeln')
    .constant('PrintModal', {
        url: '/print',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'Experiment', 'Notebook', 'Project', '$window',
            function($rootScope, $stateParams, $state, $uibModal, Experiment, Notebook, Project, $window) {
                var entity;
                if ($stateParams.experimentId >= 0) {
                    entity = Experiment;
                } else if ($stateParams.notebookId >= 0) {
                    entity = Notebook;
                } else if ($stateParams.projectId >= 0) {
                    entity = Project;
                }
                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/print-modal/print-modal.html',
                    resolve: {
                        entity: entity.get($stateParams).$promise
                    },
                    controller: 'PrintModalController',
                    controllerAs: 'vm'
                }).result.then(function(result) {
                    //this won't work while response is not an object
                    // entity.print(angular.extend($stateParams, result)).$promise.then(function(data) {
                    //     //window.open("data:application/pdf," + escape(data)); 
                    // });
                   
                    var qs = serialize(result);
                    var url = 'http://ecse00100843.epam.com/api/print/project/' + $stateParams.projectId;
                    if ($stateParams.notebookId >= 0) {
                        url += '/notebook/' + $stateParams.notebookId;
                    } 
                    if ($stateParams.experimentId >= 0) {
                        url += '/experiment/' + $stateParams.experimentId;
                    }
                    url += qs;
                    $window.open(url);
                    $state.go('^');
                    function serialize(obj) {
                        return '?' + Object.keys(obj).reduce(function(a, k) {
                            a.push(k + '=' + encodeURIComponent(obj[k])); 
                            return a;
                        }, []).join('&');
                    }
                }, function() {
                    $state.go('^');
                });
            }
        ]
    });