'use strict';

angular.module('indigoeln').factory('experimentPdfCreator',
    function (PdfService) {

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

        return {
            createPDF: function (actionToApply) {
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
                            actionToApply(response);
                        });
                        $iframe.remove();
                    };
                    prepareIframe($iframe, callback);
                });
                $iframe.appendTo('body');
            }
        };
    });
