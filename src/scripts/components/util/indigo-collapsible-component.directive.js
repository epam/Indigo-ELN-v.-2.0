(function() {
    angular
        .module('indigoeln')
        .directive('indigoCollapsibleComponent', indigoCollapsibleComponent);

    /* @ngInject */
    function indigoCollapsibleComponent($state, localStorageService, Principal) {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, iElement) {
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


            Principal.identity()
                .then(function(user) {
                    var isCollapsed = false;
                    var $element = $(iElement);
                    var $heading = $element.find('.panel-heading:first');
                    var componentId = $element.parents('.my-component:first').attr('indigo-component-id');
                    var collapsedComponents = JSON.parse(localStorageService.get(user.id + '.collapsed-components'));
                    var entityId = compactIds($state.params);
                    if (collapsedComponents && collapsedComponents[entityId]) {
                        isCollapsed = collapsedComponents[entityId][componentId];
                    }
                    var $collapsible = $heading.next();
                    var iconStyle = !isCollapsed ? 'glyphicon-chevron-up' : 'glyphicon-chevron-down';
                    var $button = $('<span class="pull-right clickable"><i class="glyphicon ' + iconStyle + '"></i></span>');
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
                        collapsedComponents = JSON.parse(localStorageService.get(user.id + '.collapsed-components'));
                        collapsedComponents = collapsedComponents || {};
                        collapsedComponents[entityId] = collapsedComponents[entityId] || {};
                        collapsedComponents[entityId][componentId] = isCollapsed;
                        localStorageService.set(user.id + '.collapsed-components', JSON.stringify(collapsedComponents));
                    });
                });
        }
    }
})();
