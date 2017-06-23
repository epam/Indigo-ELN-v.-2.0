(function () {
    angular
        .module('indigoeln')
        .controller('TemplateModalController', TemplateModalController);

    function TemplateModalController($scope, $stateParams, Template, Alert, $state, dragulaService,
                                     Components, pageInfo, EntitiesBrowser, TabKeyUtils) {
        var self = this;

        self.components = Components.map(function (c) {
            return c;
        });
        self.template = pageInfo.entity || {};
        self.template.templateContent = self.template.templateContent || [];

        self.save               = save;
        self.close              = close;
        self.addTab             = addTab;
        self.removeTab          = removeTab;
        self.removeComponent    = removeComponent;

        sortComponents();

        //TODO: remove whole object or remove multiple properties
        var drag = dragulaService.options($scope, 'components', {
            //removeOnSpill: true,
            copy: function (el, source) {
                return source.classList.contains('palette');
            },
            accepts: function () {
                return true;
            },
            moves: function (el, container, handle) {
                return !handle.classList.contains('no-draggable');
            }//,
            //copy: false
        });

        dragulaService.options($scope, 'tabs', {
            //removeOnSpill: true,
            moves: function (el, container, handle) {
                return !handle.classList.contains('draggable-component') && !handle.classList.contains('no-draggable');
            }
        });

        if (!self.template.templateContent.length) {
            self.addTab();
        }

        //dragula autoscroller
        var lastIndex, up = true,
            interval;
        $scope.$on('components.out', function (e, el) {
            var $el = $(el),
                $cont = $el.parents('[scroller]').eq(0),
                top = $cont.scrollTop();
            interval = setInterval(function () {
                $cont.scrollTop(top += up ? -3 : 3).attr('scrollTop', top);
            }, 10);
        });

        $scope.$on('components.over', function () {
            clearInterval(interval);
        });

        $scope.$on('components.dragend', function () {
            clearInterval(interval);
        });

        $scope.$on('components.shadow', function (e, el) {
            var $el = $(el),
                index = $el.index();
            if (!lastIndex) {
                lastIndex = index;
            } else {
                up = lastIndex > index;
                lastIndex = index;
            }
        });

        function save() {
            self.isSaving = true;
            if (self.template.id) {
                Template.update(self.template, onSaveSuccess, onSaveError);
            } else {
                Template.save(self.template, onSaveSuccess, onSaveError).$promise;
            }
        }

        function close() {
            if (!self.template.id) {
                var tabName = $state.$current.data.tab.name;
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromName(tabName));
            } else {
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));
            }
        }

        function addTab() {
            self.template.templateContent.push({
                name: 'Tab ' + (self.template.templateContent.length + 1),
                components: []
            });
        }

        function removeTab(tab) {
            self.template.templateContent = _.without(self.template.templateContent, tab);
        }

        function removeComponent() {
            tab.components = _.without(tab.components, component);
            self.components.push(component);
            sortComponents();
        }

        function sortComponents() {
            self.components.sort(function (b, a) {
                return (a.name < b.name) ? 1 : (a.name > b.name) ? -1 : 0;
            });
        }

        function onSaveSuccess() {
            self.isSaving = false;
            self.close();
        }

        function onSaveError(result) {
            self.isSaving = false;
            var mess = result.status === 400 ? 'Error saving, template name already exists.' : 'Template is not saved due to server error!';
            Alert.error(mess);
        }
    }
})();