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
                    //var $td = $tb.find('td,th')
                    //$td.css('width', Math.round(1/$td.length * 100) + '%')
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
                var cssForPrint =  $('#css-for-print').html()
                
                //BODY ZOOM FOR LINUX VERSION ONLY! SHOULD BE MOVED TO SERVER     
                cssForPrint = 'body { zoom: 0.75; } ' + cssForPrint;

                var html = '<style>'  + cssForPrint + '</style> <div class="for-print-only">' +  $form.html() + '</div>';
                var $origHeader = $('#print-form').find('.print-header').parent().html();
                var header = '<style>'  + cssForPrint + '</style> <div class="for-print-only">' +  $origHeader + '</div>';
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
            }
        };
    });
