/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function indigoCollapsibleComponent($state, simpleLocalCache, principalService) {
    return {
        restrict: 'A',
        link: link
    };

    /* @ngInject */
    function link(scope, $element) {
        var extractParams = function(obj) {
            return {
                projectId: obj.projectId,
                notebookId: obj.notebookId,
                experimentId: obj.experimentId
            };
        };

        function compactIds(params) {
            params = extractParams(params);
            var paramsArr = [];
            if (params.projectId) {
                paramsArr.push(params.projectId);
            }
            if (params.notebookId) {
                paramsArr.push(params.notebookId);
            }
            if (params.experimentId) {
                paramsArr.push(params.experimentId);
            }

            return paramsArr.join('-');
        }

        principalService.checkIdentity()
            .then(function(user) {
                var isCollapsed = false;
                var $heading = $element.find('.panel-heading:first');
                var componentId = $element.parents('.my-component:first').attr('indigo-component-id');
                var collapsedComponents = simpleLocalCache.getByKey(user.id + '.collapsed-components');
                var entityId = compactIds($state.params);
                if (collapsedComponents && collapsedComponents[entityId]) {
                    isCollapsed = collapsedComponents[entityId][componentId];
                }
                var $collapsible = $heading.next();
                var iconStyle = !isCollapsed ? 'glyphicon-chevron-up' : 'glyphicon-chevron-down';
                var $button = angular.element(
                    '<span class="pull-right clickable"><i class="glyphicon ' + iconStyle + '"></i></span>'
                );
                var $icon = $button.find('i');
                $heading.prepend($button);
                if (isCollapsed) {
                    $collapsible.hide();
                }
                $heading.on('click', function() {
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
                    collapsedComponents = simpleLocalCache.getByKey(user.id + '.collapsed-components');
                    collapsedComponents = collapsedComponents || {};
                    collapsedComponents[entityId] = collapsedComponents[entityId] || {};
                    collapsedComponents[entityId][componentId] = isCollapsed;
                    simpleLocalCache.putByKey(user.id + '.collapsed-components', collapsedComponents);
                });
            });
    }
}

module.exports = indigoCollapsibleComponent;

