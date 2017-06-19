angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, Template, Alert, $state, dragulaService, Components, pageInfo, EntitiesBrowser, TabKeyUtils) {
        $scope.components = Components.map(function(c) {
            return c;
        });
        $scope.template = pageInfo.entity || {};
        $scope.template.templateContent = $scope.template.templateContent || [];

        var sortComponents = function() {
            $scope.components.sort(function(b, a) {
                return (a.name < b.name) ? 1 : (a.name > b.name) ? -1 : 0;
            })
        }

        sortComponents();

        var onSaveSuccess = function () {
            $scope.isSaving = false;
            $scope.close();
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
            var mess = result.status == 400 ? 'Error saving, template name already exists.' : 'Template is not saved due to server error!'
            Alert.error(mess);
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.template.id) {
                Template.update($scope.template, onSaveSuccess, onSaveError);
            } else {
                Template.save($scope.template, onSaveSuccess, onSaveError).$promise;
            }
        };

        $scope.close = function () {
            if (!$scope.template.id) {
                var tabName = $state.$current.data.tab.name;
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromName(tabName));
            } else {
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));

            }
        };

        var hasComponent = function (id) {
            var component = _.chain($scope.template.templateContent).map(function (tc) {
                return tc.components;
            }).flatten().find(function (c) {
                return c.id === id;
            }).value();
            return !_.isUndefined(component);
        };

        var drag = dragulaService.options($scope, 'components', {
            //removeOnSpill: true,
            copy: function (el, source) {
                return source.classList.contains('palette');
            },
            accepts: function (el, target) {
                return true;
            },
            moves: function (el, container, handle) {
                return !handle.classList.contains('no-draggable');
            }
//            ,copy: false
        });

        dragulaService.options($scope, 'tabs', {
            //removeOnSpill: true,
            moves: function (el, container, handle) {
                return !handle.classList.contains('draggable-component') && !handle.classList.contains('no-draggable');
            }
        });

        $scope.addTab = function () {
            $scope.template.templateContent.push({
                name: 'Tab ' + ($scope.template.templateContent.length + 1),
                components: []
            });
        };
        if (!$scope.template.templateContent.length) {
            $scope.addTab();
        }

        $scope.removeTab = function (tab) {
            $scope.template.templateContent = _.without($scope.template.templateContent, tab);
        };

        $scope.removeComponent = function (tab, component) {
            tab.components = _.without(tab.components, component);
            $scope.components.push(component);
            sortComponents();
        };

        //dragula autoscroller
        var lastIndex, up = true,
            interval;
        $scope.$on('components.out', function(e, el, cont, source) {
            var $el = $(el),
                $cont = $el.parents('[scroller]').eq(0),
                top = $cont.scrollTop();
            interval = setInterval(function() {
                $cont.scrollTop(top += up ? -3 : 3).attr('scrollTop', top);
            }, 10)
        })

        $scope.$on('components.over', function() {
            clearInterval(interval);
        })

        $scope.$on('components.dragend', function() {
            clearInterval(interval);
        })
        
        $scope.$on('components.shadow', function(e, el, cont, source) {
            var $el = $(el),
                index = $el.index();
            if (!lastIndex) {
                lastIndex = index;
            } else {
                up = lastIndex > index;
                lastIndex = index;
            }
        })
    });

angular.module('indigoeln').directive('focusOnCreate', function () {
    // focus node on create
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var $this = $(element)
            var $cont = $this.parents('[scroller]').eq(0);
            var top = $this.position().top + $this.outerHeight(true);
            $cont.animate({ scrollTop: top }, 500);
            console.log(top)
        }
    };
})
