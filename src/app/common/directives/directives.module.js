var additionalEditorValue = require('./additional-editor/additional-editor.directive');
var customScroll = require('./custom-scroll/custom-scroll.directive');
var iTranslate = require('./i-translate/i-translate.directive');
var indigoCollapsibleComponent = require('./indigo-collapsible-component/indigo-collapsible-component.directive');
var indigoFileReader = require('./indigo-file-reader/indigo-file-reader.directive');
var indigoFocusOnCreate = require('./indigo-focus-on-create/indigo-focus-on-create.directive');
var indigoHasAnyAuthority = require('./indigo-has-any-authority/indigo-has-any-authority.directive');
var indigoHasAuthority = require('./indigo-has-authority/indigo-has-authority.directive');
var indigoIndelnScroll = require('./indigo-indeln-scroll/indigo-indeln-scroll.directive');
var indigoParsersFormatters = require('./indigo-parsers-formatters/indigo-parsers-formatters.directive');
var indigoResizable = require('./indigo-resizable/indigo-resizable.directive');
var indigoScroller = require('./indigo-scroller/indigo-scroller.directive');
var indigoSort = require('./indigo-sorting/indigo-sort.directive');
var indigoSortBy = require('./indigo-sorting/indigo-sort-by.directive');
var indigoTabScroller = require('./indigo-tab-scroller/indigo-tab-scroller.directive');
var indigoToggleClassChild = require('./indigo-toggle-class-child/indigo-toggle-class-child.directive');
var nestedOutsideClick = require('./nested-outside-click/nested-outside-click.directive');
var pressEnter = require('./press-enter/press-enter.directive');
var simpleRadio = require('./simple-radio/simple-radio.directive');
var detectRendered = require('./detect-rendered/detect-rendered.directive');
var simpleInput = require('./simple-input/simple-input.directive');
var linkedExperiments = require('./linked-experiments/linked-experiments.directive');
var unitSelect = require('./unit-select/unit-select.directive');
var autocomplete = require('./autocomplete/autocomplete.directive');
var scrollSpy = require('./autocomplete/scroll-spy/scroll-spy.directive');
var dynamicValidators = require('./dynamic-validators/dynamic-validators');
var dynamicAsyncValidators = require('./dynamic-async-validators/dynamic-async-validators');

var entityTree = require('./entity-tree/entity-tree.module');
var indigoFormElements = require('./indigo-form-elements/indigo-form-elements.module');

var dependencies = [
    entityTree,
    indigoFormElements
];

module.exports = angular
    .module('indigoeln.common.directives', dependencies)

    .directive('additionalEditorValue', additionalEditorValue)
    .directive('customScroll', customScroll)
    .directive('iTranslate', iTranslate)
    .directive('indigoCollapsibleComponent', indigoCollapsibleComponent)
    .directive('indigoFileReader', indigoFileReader)
    .directive('indigoFocusOnCreate', indigoFocusOnCreate)
    .directive('indigoHasAnyAuthority', indigoHasAnyAuthority)
    .directive('indigoHasAuthority', indigoHasAuthority)
    .directive('indigoIndelnScroll', indigoIndelnScroll)
    .directive('indigoParsersFormatters', indigoParsersFormatters)
    .directive('indigoResizable', indigoResizable)
    .directive('indigoScroller', indigoScroller)
    .directive('indigoSort', indigoSort)
    .directive('indigoSortBy', indigoSortBy)
    .directive('indigoTabScroller', indigoTabScroller)
    .directive('indigoToggleClassChild', indigoToggleClassChild)
    .directive('nestedOutsideClick', nestedOutsideClick)
    .directive('pressEnter', pressEnter)
    .directive('simpleRadio', simpleRadio)
    .directive('detectRendered', detectRendered)
    .directive('simpleInput', simpleInput)
    .directive('linkedExperiments', linkedExperiments)
    .directive('unitSelect', unitSelect)
    .directive('autocomplete', autocomplete)
    .directive('scrollSpy', scrollSpy)
    .directive('dynamicValidators', dynamicValidators)
    .directive('dynamicAsyncValidators', dynamicAsyncValidators)

    .name;
