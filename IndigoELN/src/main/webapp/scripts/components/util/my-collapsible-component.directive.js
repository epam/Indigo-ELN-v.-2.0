/**
 * Created by Selector on 09.05.2016.
 */
angular.module('indigoeln')
    .directive('myCollapsibleComponent', function () {
        return {
            restrict: 'A',
            link: function (scope, iElement) {
                var isCollapsed = false;
                // var $panel = $(iElement).find('.panel:first');
                var $heading = $(iElement).find('.panel-heading:first');
                var $collapsible = $heading.next();
                var iconStyle = !isCollapsed ? 'glyphicon-chevron-up' : 'glyphicon-chevron-down';
                var $button = $('<span class="pull-right clickable"><i class="glyphicon ' + iconStyle + '"></i></span>');
                var $icon = $button.find('i');
                $heading.append($button);
                $button.on('click', function () {
                    if (isCollapsed) {
                        // expand the panel
                        $collapsible.slideDown();
                        $icon.removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
                    } else {
                        // collapse the panel
                        $collapsible.slideUp();
                        $icon.removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
                    }
                    isCollapsed = !isCollapsed;
                });
            }
        };
    });