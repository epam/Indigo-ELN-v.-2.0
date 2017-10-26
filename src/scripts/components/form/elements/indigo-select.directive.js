(function() {
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
            controllerAs: 'vm',
            bindToController: true,
            compile: compile,
            template: function(tElement, tAttrs) {
                var itemProp = tAttrs.indigoItemProp || 'name';
                var content = _.chain(itemProp.split(','))
                    .map(function(prop) {
                        return '{{ $select.selected.' + prop + '}}';
                    })
                    .reduce(function(memo, num) {
                        return memo + (memo.length > 0 ? ' - ' : '') + num;
                    }, '').value();

                return '<div class="form-group {{vm.indigoClasses}}">' +
                    '<label ng-bind="vm.indigoLabel"></label>' +
                    // '<div class="col-xs-10">' +
                    '<ui-select ng-model="vm.ctrl.selected" theme="bootstrap" ng-disabled="vm.indigoReadonly"' +
                    ' onkeypress="return false;" on-select="vm.indigoChange()" on-remove="vm.indigoRemove()"' +
                    ' append-to-body="true">' +
                    '<ui-select-match placeholder="{{vm.indigoPlaceHolder}}" >' + content + '</ui-select-match>' +
                    '<ui-select-choices repeat="item in vm.indigoItems | filter: $select.search">' +
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
            var htmlContent = _.reduce(tAttrs.indigoItemProp.split(','), function(memo, num) {
                return memo + (memo.length > 0 ? ' + \' - \' + ' : '') + 'item.' + num;
            }, '');
            select.append('<span ng-bind-html="' + htmlContent + ' | highlight: $select.search"></span>');
            var repeat = select.attr('repeat');
            select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.indigoOrderByProp + '"');
            formUtils.clearLabel(tAttrs, tElement);
            formUtils.setLabelColumns(tAttrs, tElement);

            return {
                post: function(scope) {
                    formUtils.addOnChange(scope);
                }
            };
        }

        /* @ngInject */
        function controller($scope, Dictionary) {
            var vm = this;

            vm.ctrl = {selected: vm.indigoModel};
            vm.control = vm.indigoControl || {};
            vm.control.setSelection = setSelection;
            vm.control.unSelect = unSelect;

            init();

            function setSelection(select) {
                vm.ctrl.selected = select;
            }

            function unSelect() {
                vm.ctrl.selected = {};
            }

            function init() {
                $scope.$watchCollection('vm.ctrl.selected', function(newSelected) {
                    vm.indigoModel = newSelected;
                });

                $scope.$watchCollection('vm.indigoModel', function(indigoModel) {
                    vm.ctrl.selected = indigoModel;
                });

                if (vm.indigoDictionary) {
                    Dictionary.getByName({
                        name: vm.indigoDictionary
                    }, function(dictionary) {
                        vm.indigoItems = dictionary.words;
                    });
                }
            }
        }
    }
})();
