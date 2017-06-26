(function () {
    angular
        .module('indigoeln')
        .directive('indigoSelect', indigoSelect);

    /* @ngInject */
    function indigoSelect(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoItems: '=',
                indigoDictionary: '@',
                indigoMultiple: '=',
                indigoLabelVertical: '=',
                indigoLabelColumnsNum: '=',
                indigoControl: '=',
                indigoPlaceHolder: '@',
                indigoItemProp: '@',
                indigoOrderByProp: '@',
                indigoClasses: '@',
                indigoChange: '&',
                indigoRemove: '&',
                indigoReadonly: '='
            },
            controller: controller,
            compile: compile,
            template: function (tElement, tAttrs) {
                var itemProp = tAttrs.indigoItemProp || 'name';
                var content = _.chain(itemProp.split(','))
                    .map(function (prop) {
                        return '{{ $select.selected.' + prop + '}}';
                    })
                    .reduce(function (memo, num) {
                        return memo + (memo.length > 0 ? ' - ' : '') + num;
                    }, '').value();
                return '<div class="form-group {{indigoClasses}}">' +
                    '<label>{{indigoLabel}}</label>' +
                    // '<div class="col-xs-10">' +
                    '<ui-select ng-model="ctrl.selected" theme="bootstrap" ng-disabled="indigoReadonly" onkeypress="return false;" on-select="indigoChange()" on-remove="indigoRemove()" append-to-body="true">' +
                    '<ui-select-match placeholder="{{indigoPlaceHolder}}" >' + content + '</ui-select-match>' +
                    '<ui-select-choices repeat="item in indigoItems | filter: $select.search">' +
                    '</ui-select-choices>' +
                    '</ui-select>' +
                    // '</div>' +
                    '</div>';
            }
        };

        /* @ngInject */
        function compile(tElement, tAttrs) {
            tAttrs.indigoItemProp = tAttrs.indigoItemProp || 'name';
            tAttrs.indigoOrderByProp = tAttrs.indigoOrderByProp || 'rank';
            if (tAttrs.indigoMultiple) {
                tElement.find('ui-select').attr('multiple', true);
                tElement.find('ui-select-match').html('{{$item.' + tAttrs.indigoItemProp + '}}');
            }
            formUtils.doVertical(tAttrs, tElement);
            var select = tElement.find('ui-select-choices');
            var htmlContent = _.reduce(tAttrs.indigoItemProp.split(','), function (memo, num) {
                return memo + (memo.length > 0 ? " + ' - ' + " : '') + 'item.' + num;
            }, '');
            select.append('<span ng-bind-html="' + htmlContent + ' | highlight: $select.search"></span>');
            var repeat = select.attr('repeat');
            select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.indigoOrderByProp + '"');
            formUtils.clearLabel(tAttrs, tElement);
            formUtils.setLabelColumns(tAttrs, tElement);
            return {
                post: function (scope) {
                    formUtils.addOnChange(scope);
                }
            };
        }

        /* @ngInject */
        function controller($scope, Dictionary) {
            $scope.ctrl = {selected: $scope.indigoModel};

            $scope.control = $scope.indigoControl || {};

            $scope.control.setSelection = function (select) {
                $scope.ctrl.selected = select;
            };

            $scope.control.unSelect = function () {
                $scope.ctrl.selected = {};
            };

            $scope.$watchCollection('ctrl.selected', function (newSelected) {
                $scope.indigoModel = newSelected;
            });
            $scope.$watchCollection('indigoModel', function (indigoModel) {
                $scope.ctrl.selected = indigoModel;
            });
            if ($scope.indigoDictionary) {
                Dictionary.getByName({name: $scope.indigoDictionary}, function (dictionary) {
                    $scope.indigoItems = dictionary.words;
                });
            }
        }
    }
})();
