angular
    .module('indigoeln')
    .factory('experimentPdfCreator', experimentPdfCreator);

/* @ngInject */
function experimentPdfCreator(PdfService) {
    return {
        createPDF: createPDF
    };

    function createPDF(fileName, actionToApply) {
        var $form = preparedPrintForm();
        var cssForPrint = $('#css-for-print').html();

        //BODY ZOOM FOR LINUX VERSION ONLY! SHOULD BE MOVED TO SERVER
        cssForPrint = 'body { zoom: 0.75; } ' + cssForPrint;

        var html = '<style>' + cssForPrint + '</style> <div class="for-print-only">' + $form.html() + '</div>';
        var $origHeader = $('#print-form').find('.print-header').parent().html();
        var header = '<style>' + cssForPrint + '</style> <div class="for-print-only">' + $origHeader + '</div>';
        //console.warn('header',  header)
        PdfService.create({
            html: '<!DOCTYPE html>' + html + '</html>',
            header: header,
            fileName: fileName,
            headerHeight: '85mm'
        }).$promise.then(function (response) {
            actionToApply(response);
        });
        var w = window.open('', 'wnd');
        w.document.body.innerHTML = header + html;
    }


    function preparedPrintForm() {
        var $printFormClone = $('#print-form').clone();
        $printFormClone.find('#print-img-logo').appendTo($printFormClone).hide();

        $printFormClone.find('.print-header').remove();
        var desc = $printFormClone.find('.simditor-body').html();
        $printFormClone.find('.simditor').children().remove();
        $printFormClone.find('.simditor').html(desc);
        $printFormClone.find('div.print-component').each(function () {
            var $this = $(this);
            var $checkbox = $this.find('.need-to-print');
            if (!$checkbox.find('.checked').length) {
                $this.remove();
            } else if ($checkbox.length) {
                $checkbox.remove();
            }
            $checkbox.remove();
            $this.find('.print-table').each(function () {
                var $table = $(this);
                var $tr = $table.children().children();
                $tr.each(function () {
                    var $tb = $('<table>').insertBefore($table).append($(this));
                    $tb.wrap('<div class="pba"><div>');
                });
            });

        });
        //$printFormClone.find('td').wrapInner('<div class="pba" />')

        return $printFormClone;
    }
}
