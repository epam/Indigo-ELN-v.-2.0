'use strict';

angular.module('indigoeln').controller('ExperimentPrintController',
    function ($scope, $rootScope, $stateParams, $state, $compile, $window, Experiment, PdfService, pageInfo) {

        $scope.experiment = pageInfo.experiment;
        $scope.notebook = pageInfo.notebook;
        $scope.project = pageInfo.project;

        $scope.batchDetails = $scope.experiment.components.productBatchDetails;
        $scope.batchSummary = $scope.experiment.components.productBatchSummary;
        $scope.conceptDetails = $scope.experiment.components.conceptDetails;
        $scope.reactionDetails = $scope.experiment.components.reactionDetails;
        $scope.reaction = $scope.experiment.components.reaction;
        $scope.molecule = $scope.experiment.components.molecule;

        $scope.currentDate = Date.now();

        var preparedPrintForm = function () {
            var $printFormClone = $('#print-form').clone();
            $printFormClone.find('div.print-component').each(function () {
                var $this = $(this);
                var $input = $this.find('input');
                $this.find('div.col-xs-11').removeClass('col-xs-11').addClass('col-xs-12');
                if ($input.length && $input.get(0).checked !== true) {
                    $this.remove();
                } else if ($input.length) {
                    $this.find('div.need-to-print').remove();
                }
            });
            return $printFormClone;
        };

        var prepareIframe = function ($iframe, callback) {
            var $iframeContents = $iframe.contents();
            var promises = [];
            $('link')
                .clone()
                .map(function (index, value) {
                    promises[index] = $.get(value.href, function (data) {
                        $('<style type="text/css">' + data + '</style>').appendTo($iframeContents.find('head'));
                    });
                });
            $.when.apply($, promises).then(callback.bind(null, $iframeContents));
            var iframeBody = $iframeContents.find('body');
            iframeBody.addClass('main-container');
            iframeBody.css({
                'min-width': '960px',
                'background-color': '#fff'
            });
            iframeBody.css('overflow', 'auto');
            var $preparedPrintForm = preparedPrintForm();
            iframeBody.append($preparedPrintForm);
        };

        var downloadReport = function (response) {
            var hiddenFrameId = 'hiddenDownloader';
            var hiddenDownloader = $('#' + hiddenFrameId);
            if (!hiddenDownloader.length) {
                hiddenDownloader = $('<iframe id="' + hiddenFrameId + '" style="display:none"/>');
                $('body').append(hiddenDownloader);
            }
            hiddenDownloader.attr('src', 'api/print?fileName=' + response.fileName);
            $scope.isPrinting = false;
        };

        $scope.print = function () {
            $scope.isPrinting = true;
            var $iframe = $('<iframe />', {
                name: 'reportHolder',
                id: 'reportHolder'
            });
            var iframeEl = $iframe.get(0);
            iframeEl.className = 'html2canvas-container';
            iframeEl.style.visibility = 'hidden';
            iframeEl.style.position = 'fixed';
            iframeEl.style.left = '-10000px';
            iframeEl.style.top = '0px';
            iframeEl.style.border = '0';
            $iframe.load(function () {
                var callback = function ($iframeContents) {
                    PdfService.create({
                        html: '<!DOCTYPE html>' + $iframeContents.find('html').html() + '</html>',
                        header: $iframeContents.find('.print-header').html()
                    }).$promise.then(function (response) {
                        downloadReport(response);
                    });
                    $iframe.remove();
                };
                prepareIframe($iframe, callback);
            });
            $iframe.appendTo('body');
        };

    });
