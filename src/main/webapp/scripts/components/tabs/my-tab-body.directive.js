/**
 * Created by Stepan_Litvinov on 5/4/2016.
 */
angular.module('indigoeln')
    .directive('myTabContent', function ($timeout, $window) {
        return {
            restrict: 'A',
            link: function (scope, iElement) {
                $timeout(function () {
                    var $element = $(iElement);
                    var $panelBody = $element.find('.panel-body:first');
                    $panelBody.css('overflow-y', 'auto');
                    var $footer = $panelBody.next();

                    function calcHeight() {
                        return $($window).height() - $panelBody.offset().top - $footer.outerHeight();
                    }

                    var unsubscribe = scope.$watch(function () {
                        return calcHeight();
                    }, function (val) {
                        $panelBody.css('height', val + 'px');
                    });
                    scope.$on('$destroy', function () {
                        unsubscribe();
                    });
                    angular.element($window).bind('resize', function () {
                        $panelBody.css('height', calcHeight() + 'px');
                    });

                }, false);
            }
        };
    });