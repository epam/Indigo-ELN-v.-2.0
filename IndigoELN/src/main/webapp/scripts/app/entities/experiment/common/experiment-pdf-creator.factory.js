angular.module('indigoeln').factory('experimentPdfCreator',
    function (PdfService) {

        var preparedPrintForm = function () {
            var $printFormClone = $('#print-form').clone();
            $printFormClone.find('.print-header').remove();
            $printFormClone.find('div.print-component').each(function () {
                var $this = $(this);
                var $checkbox = $this.find('.need-to-print');
                $checkbox.remove();
                //$this.find('div.col-xs-11').removeClass('col-xs-11').addClass('col-xs-12');
                // if (!$checkbox.find('.checked').length) {
                //     $this.remove();
                // } else if ($checkbox.length) {
                //     $checkbox.remove();
                // }

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
            createPDF: function (fileName, actionToApply) {
                var $form = preparedPrintForm();
               // $.get('/assets/print.css', function (data) {
                   // var html = '<style>'  + data + '</style>'+ $form.html() + $form.html() + '<!--ADD_PAGE--><h1 style="page-break-before: always">Numbers</h1>' + $form.html() + '<div class="pb"> </div>' + $form.html() + '<div class="pb"> </div>' + $form.html();
                    var data = '   .for-print-only img {width: 100% } .for-print-only {font-size: 13px; color: #000; font-family: arial; } .print-component {page-break-before: auto; page-break-inside: avoid; margin: 20px 0; } .print-component.first{  margin-top: 0 } .for-print-only table {width: 100%; border-spacing: 0;    border-collapse: collapse ; } table { page-break-after:auto } tr    { page-break-inside:avoid; page-break-after:auto } td, .pba    { page-break-inside:avoid; page-break-after:auto } thead { display:table-header-group } tfoot { display:table-footer-group } .for-print-only table td, .for-print-only table th {border: 1px solid #ccc; font-size: 13px; padding: 4px; }';
                    var html = '<style>'  + data + '</style> <div class="for-print-only">' +  $form.html() + '</div>';
                    console.warn('html', html)
                    var $origHeader = $('#print-form').find('.print-header').parent().html();
                    header = '<style>'  + data + '</style> <div class="for-print-only">' +  $origHeader + '</div>';
                    console.warn('header',  header)
                    PdfService.create({
                        html: '<!DOCTYPE html>' + html + '</html>',
                        header: header,
                        fileName: fileName,
                        headerHeight : '85mm'
                    }).$promise.then(function (response) {
                        actionToApply(response);
                    });
                    var w = window.open('', 'wnd');
                    w.document.body.innerHTML = header + html;
                //});
            }
        };
    });
