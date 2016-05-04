/**
 * Created by Stepan_Litvinov on 5/4/2016.
 */
angular.module('indigoeln')
    .directive('myTabContent', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs) {
                $timeout(function () {
                    var $element = $(iElement);
                    var $panelBody = $element.find('.panel-body:first');
                    $panelBody.css('overflow-y', 'auto');
                    var $footer = $panelBody.next();
                    scope.$watch(function () {
                        return $(window).height() - $panelBody.offset().top - $footer.outerHeight();
                    }, function (val) {
                        $panelBody.css('height', val + 'px');
                    });
                });
            }
        };
    });