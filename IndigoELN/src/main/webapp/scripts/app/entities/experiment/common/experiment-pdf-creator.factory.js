angular.module('indigoeln').factory('experimentPdfCreator',
    function (PdfService) {

        var preparedPrintForm = function () {
            var $printFormClone = $('#print-form').clone();
            $printFormClone.find('.print-header').remove();
            var desc = $printFormClone.find('.simditor-body').html();

            $printFormClone.find('.simditor').children().remove().html(desc);
            $printFormClone.find('div.print-component').each(function () {
                var $this = $(this);
                var $checkbox = $this.find('.need-to-print');
                if (!$checkbox.find('.checked').length) {
                    $this.remove();
                } else if ($checkbox.length) {
                    $checkbox.remove();
                }
                $checkbox.remove();
                var  $tr =  $this.find('.print-table').children().children();
                $tr.each(function() {
                    var $tb = $('<table>').appendTo($this).append($(this))
                    var $td = $tb.find('td,th')
                    $td.css('width', Math.round(1/$td.length * 100) + '%')
                    $tb.wrap('<div class="pba"><div>');
                })

            });
            //$printFormClone.find('td').wrapInner('<div class="pba" />')
            
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
                    var data = '.for-print-only img {width: 100% } .for-print-only {font-size: 13px; color: #000; font-family: arial; } .print-component {page-break-before: auto; page-break-inside: avoid; margin: 20px 0; } .print-component.first{  margin-top: 0 } .for-print-only table {width: 100%; border-spacing: 0; table-layout:fixed;   border-collapse: collapse ; } table { page-break-after:auto } tr    { page-break-inside:avoid; } .pba    { page-break-inside:avoid;} thead { display:table-header-group } tfoot { display:table-footer-group } .for-print-only table td, .for-print-only table th {border: 1px solid #ccc; font-size: 13px; padding: 4px;     word-wrap: break-word; page-break-inside:avoid; }';

                    //BODY ZOOM FOR LINUX VERSION ONLY! SHOULD BE MOVED TO SERVER     
                    data = 'body { zoom: 0.75; } ' + data;

                    var html = '<style>'  + data + '</style> <div class="for-print-only">' +  $form.html() + '</div>';
                    var $origHeader = $('#print-form').find('.print-header').parent().html();
                    var header = '<style>'  + data + '</style> <div class="for-print-only">' +  $origHeader + '</div>';
                    //console.warn('header',  header)
                    PdfService.create({
                        html: '<!DOCTYPE html>' + html + '</html>',
                        header: header,
                        fileName: fileName,
                        headerHeight : '70mm'
                    }).$promise.then(function (response) {
                        actionToApply(response);
                    });
                    var w = window.open('', 'wnd');
                    w.document.body.innerHTML = header + html;
                //});
            }
        };
    });
