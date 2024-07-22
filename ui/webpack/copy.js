var path = require('path');

function copy(DIRS) {
    return [
        {
            context: path.join(DIRS.src, 'vendors/ketcher'),
            from: '**/*',
            to: path.join(DIRS.dist, 'vendors/ketcher')
        },
        {
            from: path.join(DIRS.src, 'assets/images/favicon.ico'),
            to: path.join(DIRS.dist, 'assets/images')
        },
        {
            from: path.join(DIRS.src, 'user-guide/Indigo ELN v2.0 User Guide.docx'),
            to: path.join(DIRS.dist, 'user-guide')
        }
    ];
}
module.exports = copy;
