/**
 * Created by Selector on 09.05.2016.
 */
angular.module('indigoeln')
    .directive('myCollapsibleComponent', function ($state, EntitiesBrowser, localStorageService) {
        return {
            restrict: 'A',
            link: function (scope, iElement) {
                var isCollapsed = false;
                var $element = $(iElement);
                var $heading = $element.find('.panel-heading:first');
                var componentId = $element.parents('.my-component:first').attr('my-component');
                var collapsedComponents = JSON.parse(localStorageService.get('collapsed-components'));
                var entityId = EntitiesBrowser.compactIds($state.params);
                if (collapsedComponents && collapsedComponents[entityId]) {
                    isCollapsed = collapsedComponents[entityId][componentId];
                }
                var $collapsible = $heading.next();
                var iconStyle = !isCollapsed ? 'glyphicon-chevron-up' : 'glyphicon-chevron-down';
                var $button = $('<span class="pull-right clickable"><i class="glyphicon ' + iconStyle + '"></i></span>');
                var $icon = $button.find('i');
                $heading.append($button);
                if (isCollapsed) {
                    $collapsible.hide();
                }
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
                    collapsedComponents = JSON.parse(localStorageService.get('collapsed-components'));
                    collapsedComponents = collapsedComponents || {};
                    collapsedComponents[entityId] = collapsedComponents[entityId] || {};
                    collapsedComponents[entityId][componentId] = isCollapsed;
                    localStorageService.set('collapsed-components', JSON.stringify(collapsedComponents));
                });
            }
        };
    });