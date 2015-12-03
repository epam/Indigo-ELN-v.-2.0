module.exports = function (grunt) {
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-html2js');
    grunt.loadNpmTasks('grunt-usemin');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-cache-bust');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-text-replace');
    
    grunt.registerTask('bower-install', 'install frontend dependencies', function() {
        var exec = require('child_process').exec;
        var cb = this.async();
        exec('bower install', {}, function(err, stdout, stderr) {
            console.log(stdout);
            cb();
        });
    });    

    // Builds developer version. Bower components should be already installed.
    grunt.registerTask('dev', ['clean', 'copy:index']);

    // Builds production version.
    grunt.registerTask('release', ['bower-install', 'clean', 'html2js', 'copy:index', /*'copy:images',*/ 'useminPrepare', 'cssmin', 'concat:generated', 'uglify', 'usemin', 'replace']);

    grunt.initConfig({
        distdir: 'dist',
        distdirjs: '<%= distdir %>/js',
        distdircss: '<%= distdir %>/css',
        pkg: grunt.file.readJSON('package.json'),
        src: {
            css_dir: 'css',
            js: ['js/**/*.js'],
            css: '<%= src.css_dir %>/*.css',
            html: 'views/**/*.html',
            index: ['index.html']
        },
        clean: ['<%= distdir %>/*'],
        copy: {
            index: {
                files: [{
                    	dest: '', src: ['index.html'], expand: true, filter: 'isFile', flatten: true,
                        rename: function(dest, src) {
                            return dest + src.replace('index','_index');
                        }
                    }]
            }/*,
            images : {
                files: [{
                        dest: 'dist/images', src: ['/images/*.*'], expand: true, filter: 'isFile', flatten: true
                    }]
            }*/
        },
        replace : {
             index: {
                src : '_index.html',
                dest : '_index.html',
                replacements : [{
                    from : '<!--APPTEMPLATES-->',
                    to : '<script src="dist/js/templates.js"></script>'
                }]
             }
        },
        html2js: {
            main: {
                src: '<%= src.html %>',
                dest: '<%= distdirjs %>/templates.js',
                module: 'indigoeln',
                options : {
                    base: '',
                    singleModule: true,
                    existingModule: true
                }
            }
        },
        useminPrepare: {
            html: '<%= src.index %>',
            options: {
                dest: '.',
                flow: {
                    steps: {
                        js: ['uglifyjs'],
                        css: ['concat'],
                        jsConcat: ['concat']
                    },
                    post: {}
                }
            }
        },
        usemin: {
            html: '_<%= src.index %>',
            options: {
                blockReplacements: {
                    css: function (block) {
                        return '<link rel="stylesheet" type="text/css" href="' + block.dest + '" />';
                    },
                    js: function (block) {
                        return '<script src="' + block.dest + '"></script>';
                    },
                    jsConcat: function (block) {
                        return '<script type="text/javascript" src="' + block.dest + '"></script>';
                    }
                }
            }
        },
        cssmin: {
            min: {
                cwd: '<%= distdircss %>',
                src: '*.css',
                dest: '<%= distdircss %>',
                expand: true
            }
        },
        uglify: {
            options: {
                mangle: true,
                compress: {
                    warnings: false
                }
            }
        }
    });
}

