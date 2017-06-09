"use strict";
var page = require('webpage').create(),
     fs = require('fs');
if (arguments.length < 4) {
    console.log('Usage: rasterizeHeader.js content header filename headerHeight');
} else {
    var content = arguments[0];
    var header = arguments[1];
    var output = arguments[2];
    var headerHeight = arguments[3] || '5cm';

    console.log('headerHeight',headerHeight );
    page.viewportSize = { width: 600, height: 600 };
    page.paperSize = {
        format: 'A4',
       margin: '1cm',
        header: {
            height: headerHeight,
            contents: phantom.callback(function(pageNum, numPages) {
                return header.replace('#pageNum', pageNum).replace('#numPages', numPages);
            })
        }
    };
    page.onLoadFinished = function(status) {
        page.onLoadFinished = null;
        if (status !== 'success') {
            console.log('Unable to load the content!');
        } else {
            console.log('Content loaded.');
            console.log('Start rendering.');
            page.render(output);
            console.log('Content successfully rendered to: ' + output);
            console.log('Create done file.');
            fs.write(output + '.done', "", 'w');
            console.log('Done file was created successfully.');
        }
        page.close();
    };
    console.log('Load content.');
    page.setContent(content, "localhost");
}
