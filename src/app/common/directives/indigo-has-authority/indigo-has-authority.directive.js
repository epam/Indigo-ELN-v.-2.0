/* @ngInject */
function indigoHasAuthority(principalService, ngIfDirective) {
    var ngIf = ngIfDirective[0];

    return {
        restrict: 'A',
        priority: ngIf.priority,
        terminal: ngIf.terminal,
        transclude: ngIf.transclude,
        link: {
            pre: function($scope, $element, $attrs) {
                var authorityAttr = $attrs.indigoHasAuthority;
                var authority = $scope.$eval(authorityAttr);
                var isAuthorized = true;
                var initialNgIf = $attrs.ngIf;
                var ifEvaluator;

                isAuthorized = principalService.hasAuthority(authority);

                if (initialNgIf) {
                    // If element has another ng-if directive it should be evaluated
                    ifEvaluator = function() {
                        return $scope.$eval(initialNgIf) && isAuthorized;
                    };
                } else {
                    ifEvaluator = function() {
                        return isAuthorized;
                    };
                }

                $attrs.ngIf = ifEvaluator;
                ngIf.link.apply(ngIf, arguments);
            }
        }
    };
}

module.exports = indigoHasAuthority;
