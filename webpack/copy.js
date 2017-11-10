const path = require('path');

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
        }
    ];
}
module.exports = copy;
