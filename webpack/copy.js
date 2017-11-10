const path = require('path');

function copy(DIRS) {
    return [
        {
            context: path.join(DIRS.src, 'vendors/ketcher'),
            from: '**/*',
            to: path.join(DIRS.dist, 'vendors/ketcher')
        }
    ];
}
module.exports = copy;
