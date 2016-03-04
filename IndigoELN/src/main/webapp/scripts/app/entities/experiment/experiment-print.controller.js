'use strict';

angular.module('indigoeln').controller('ExperimentPrintController',
    function ($scope, $rootScope, $stateParams, $state, $compile, $window, Experiment, PdfService, experiment) {

        $scope.experiment = experiment;

        //var onPrintSuccess = function (result) {
        //    $scope.isPrinting = false;
        //};
        //var onPrintError = function (result) {
        //    $scope.isPrinting = false;
        //};

        $scope.toModel = function toModel(components) {
            if (_.isArray(components)) {
                return _.object(_.map(components, function (component) {
                    return [component.name, component.content];
                }));
            } else {
                return components;
            }
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
            iframeBody.append($('#printForm').clone());
            iframeBody.css('overflow', 'auto');
        };

        var downloadReport = function (response) {
            var hiddenFrameId = 'hiddenDownloader';
            var hiddenDownloader = $('#' + hiddenFrameId);
            if (!hiddenDownloader.length) {
                hiddenDownloader = $('<iframe id="' + hiddenFrameId + '" style="display:none"/>');
                $('body').append( hiddenDownloader);
            }
            hiddenDownloader.attr('src', 'api/print?fileName=' + response.fileName);
        };

        $scope.print = function () {
            //$scope.isPrinting = true;
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
                    PdfService.create({html: '<!DOCTYPE html><html>' + $iframeContents.find('html').html() + '</html>'})
                        .$promise.then(function(response) {
                            downloadReport(response);
                    });
                    $iframe.remove();
                };
                prepareIframe($iframe, callback);
            });
            $iframe.appendTo('body');


            //$window.open('/pdf', '_blank');
            //$window.location.href = ('/pdf');
            //var htmlToPrint = $compile($('#printForm'));
            //var head = $('<head>').append($('head')).html();
            //var body = $('<body>').append($('#printForm')).html();
            //var html = '<!DOCTYPE html><html>' + $iframeContents.find('html').html() + '</html>';
            //Printer.print({html: html});
        };


    });
