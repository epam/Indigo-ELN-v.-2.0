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

                    var $$window = $($window);
                    var updateHeight = _.debounce(function () {
                        var height = $$window.height() - $panelBody.offset().top - $footer.outerHeight();
                        $panelBody.css('height', height + 'px');
                    }, 300);

                    angular.element($window).bind('resize', function () {
                        updateHeight();
                    });
                    updateHeight();
                }, false);
            }
        };
    });