angular.module('indigoeln').factory('experimentPdfCreator',
    function (PdfService) {

        var preparedPrintForm = function () {
            var $printFormClone = $('#print-form').clone();
            $printFormClone.find('div.print-component').each(function () {
                var $this = $(this);
                var $checkbox = $this.find('.need-to-print');
                $this.find('div.col-xs-11').removeClass('col-xs-11').addClass('col-xs-12');
                if (!$checkbox.find('.checked').length) {
                    $this.remove();
                } else if ($checkbox.length) {
                    $checkbox.remove();
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
            createPDF: function (fileName, actionToApply) {
                var $form = preparedPrintForm();
               // $.get('/assets/print.css', function (data) {
                   // var html = '<style>'  + data + '</style>'+ $form.html() + $form.html() + '<!--ADD_PAGE--><h1 style="page-break-before: always">Numbers</h1>' + $form.html() + '<div class="pb"> </div>' + $form.html() + '<div class="pb"> </div>' + $form.html();
                    var data = '.for-print-only {font-size: 10px; width: 570px; color: #000; } .for-print-only table {width: 100% } .for-print-only table td, .for-print-only table th {border: 1px solid #ccc; font-size: 10px } .pb {page-break-before: always } ';
                    $form.find('img').remove();
                    var html = '<style>'  + data + '</style> <div class="for-print-only">' +  $form.html() + '</div>';
                    var pdf = new jsPDF('p', 'pt', 'a4');
                    var canvas = pdf.canvas;
                    canvas.height = 72 * 11;
                    canvas.width = 72 * 8.5;;

                    console.warn('start', pdf, html)

                    html2pdf(html, pdf, function(_pdf) {
                        _pdf.output('dataurlnewwindow');
                        pdf.save(fileName);
                        console.warn('completed pdf', pdf)
                    });
                    var w = window.open('', 'wnd');
                    w.document.body.innerHTML = html;
                //});

                // return;
                // var $iframe = $('<iframe />', {
                //     name: 'reportHolder',
                //     id: 'reportHolder'
                // });
                // var iframeEl = $iframe.get(0);
                // iframeEl.className = 'html2canvas-container';
                // iframeEl.style.visibility = 'hidden';
                // iframeEl.style.position = 'fixed';
                // iframeEl.style.left = '-10000px';
                // iframeEl.style.top = '0px';
                // iframeEl.style.border = '0';
                // $iframe.load(function () {
                //     var callback = function ($iframeContents) {
                //         $iframeContents.find('body').css({
                //             'min-width': '960px',
                //             'background-color': '#fff'
                //         });
                //         console.warn($iframeContents.find('body'))
                //         var html = $iframeContents.find('html').html();
                //         PdfService.create({
                //             html: '<!DOCTYPE html>' + html + '</html>',
                //             header: $iframeContents.find('.print-header').html(),
                //             fileName: fileName
                //         }).$promise.then(function (response) {
                //             actionToApply(response);
                //         });
                //         $iframe.remove();
                //     };
                //     prepareIframe($iframe, callback);
                // });
                // $iframe.appendTo('body');
            }
        };
    });
