'use strict';

// usemin custom step
var useminAutoprefixer = {
    name: 'autoprefixer',
    createConfig: function(context, block) {
        if (block.src.length === 0) {
            return {};
        }

        // Reuse cssmins createConfig
        return require('grunt-usemin/lib/config/cssmin').createConfig(context, block);
    }
};

module.exports = function(grunt) {
    require('load-grunt-tasks')(grunt);
    require('time-grunt')(grunt);
    var serveStatic = require('serve-static');
    grunt.loadNpmTasks('grunt-ng-constant');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.initConfig({
        connect: {
            server: {
                options: {
                    port: 9000,
                    keepalive: true,
                    middleware: function(connect) {
                        return [
                            serveStatic('src'),
                            connect().use(
                                '/bower_components',
                                serveStatic('./bower_components')
                            )
                        ];
                    }
                }
            }
        },
        yeoman: {
            // configurable paths
            app: require('./bower.json').appPath || 'app',
            dist: 'dist'
        },
        watch: {
            options: {spawn: false},
            bower: {
                files: ['bower.json'],
                tasks: ['wiredep']
            },
            less: {
                files: ['<%= yeoman.app %>/assets/less/*.less', '<%= yeoman.app %>/scripts/**/*.less'],
                tasks: ['less:dev']
            }
        },
        ngconstant: {
            options: {
                name: 'config',
                dest: 'src/scripts/app/config.js',
                constants: {
                    apiUrl: grunt.option('apiUrl') || 'api/'
                }
            },
            build: {
            }
        },
        autoprefixer: {},
        wiredep: {app: {src: ['src/index.html']}},
        browserSync: {
            dev: {
                bsFiles: {
                    src: [
                        'src/**/*.html',
                        'src/**/*.json',
                        'src/assets/styles/**/*.css',
                        'src/scripts/**/*.{js,html}',
                        'src/assets/images/**/*.{png,jpg,jpeg,gif,webp,svg}',
                        'tmp/**/*.{css,js}'
                    ]
                }
            },
            options: {
                watchTask: true,
                proxy: 'localhost:8080'
            }
        },
        complexity: {
            generic: {
                src: ['src/scripts/**/*.js'],
                exclude: [],
                options: {pmdXML: './pmd.xml'}
            }
        },
        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },
        concat: {
            // src and dest is configured in a subtask called "generated" by usemin
        },
        uglifyjs: {
            // src and dest is configured in a subtask called "generated" by usemin
        },
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= yeoman.dist %>/scripts/**/*.js',
                        '<%= yeoman.dist %>/assets/styles/**/*.css'
                    ]
                }
            }
        },
        useminPrepare: {
            html: 'src/index.html',
            options: {
                dest: '<%= yeoman.dist %>',
                flow: {
                    html: {
                        steps: {
                            js: ['concat', 'uglifyjs'],
                            // Let cssmin concat files so it corrects relative paths to fonts and images
                            css: ['cssmin', useminAutoprefixer]
                        },
                        post: {}
                    }
                }
            }
        },
        usemin: {
            html: ['<%= yeoman.dist %>/**/index.html'],
            css: ['<%= yeoman.dist %>/assets/styles/**/*.css'],
            js: ['<%= yeoman.dist %>/scripts/**/*.js'],
            options: {
                assetsDirs: ['<%= yeoman.dist %>', '<%= yeoman.dist %>/assets/styles', '<%= yeoman.dist %>/assets/images', '<%= yeoman.dist %>/assets/fonts'],
                patterns: {
                    js: [
                        [/(assets\/images\/.*?\.(?:gif|jpeg|jpg|png|webp|svg))/gm, 'Update the JS to reference our revved images']
                    ]
                },
                dirs: ['<%= yeoman.dist %>']
            }
        },
        imagemin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: 'src/assets/images',
                    // we don't optimize PNG files as it doesn't work on Linux. If you are not on Linux, feel free to use '**/*.{png,jpg,jpeg}'
                    src: '**/*.{jpg,jpeg}',
                    dest: '<%= yeoman.dist %>/assets/images'
                }]
            }
        },
        svgmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: 'src/assets/images',
                    src: '**/*.svg',
                    dest: '<%= yeoman.dist %>/assets/images'
                }]
            }
        },
        cssmin: {
            // src and dest is configured in a subtask called "generated" by usemin
        },
        ngtemplates: {
            dist: {
                cwd: 'src',
                src: ['scripts/**/*.html'],
                dest: '.tmp/templates/templates.js',
                options: {
                    module: 'indigoeln',
                    usemin: 'scripts/app.js',
                    htmlmin: '<%= htmlmin.dist.options %>'
                }
            }
        },
        htmlmin: {
            dist: {
                options: {
                    removeCommentsFromCDATA: true,
                    // https://github.com/yeoman/grunt-usemin/issues/44
                    collapseWhitespace: true,
                    collapseBooleanAttributes: true,
                    conservativeCollapse: true,
                    removeAttributeQuotes: true,
                    removeRedundantAttributes: true,
                    useShortDoctype: true,
                    removeEmptyAttributes: true,
                    keepClosingSlash: true
                },
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.dist %>',
                    src: ['*.html'],
                    dest: '<%= yeoman.dist %>'
                }]
            }
        },
        // Put files not handled in other tasks here
        copy: {
            fonts: {
                files: [{
                    expand: true,
                    dot: true,
                    flatten: true,
                    cwd: 'src',
                    dest: '<%= yeoman.dist %>/assets/fonts',
                    src: [
                        'bower_components/bootstrap/fonts/*.*',
                        'bower_components/font-awesome-bower/fonts/*.*'
                    ]
                }]
            },
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: 'src',
                    dest: '<%= yeoman.dist %>',
                    src: [
                        '*.html',
                        // 'scripts/**/*.html',
                        'assets/images/**/*.{png,gif,webp,jpg,jpeg,svg}',
                        'assets/fonts/**/*.{ttf,otf,woff,woff2,eot,svg}',
                        'assets/data/**/*.json',
                        'vendors/**/*'
                    ]
                }, {
                    expand: true,
                    cwd: '.tmp/assets/images',
                    dest: '<%= yeoman.dist %>/assets/images',
                    src: [
                        'generated/*'
                    ]
                }]
            }
        },
        ngAnnotate: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '.tmp/concat/scripts',
                    src: '*.js',
                    dest: '.tmp/concat/scripts'
                }]
            }
        },
        less: {
            dev: {
                options: {
                    sourceMap: true,
                    outputSourceFiles: true
                },
                files: {
                    '<%= yeoman.app %>/assets/styles/main.css': '<%= yeoman.app %>/assets/less/main.less',
                    '<%= yeoman.app %>/assets/styles/indigo-bootstrap.css':
                        '<%= yeoman.app %>/assets/less/indigo-bootstrap.less'
                }
            },
            prod: {
                files: {
                    '<%= yeoman.app %>/assets/styles/main.css': '<%= yeoman.app %>/assets/less/main.less',
                    '<%= yeoman.app %>/assets/styles/indigo-bootstrap.css':
                        '<%= yeoman.app %>/assets/less/indigo-bootstrap.less'
                }
            }
        }
    });

    grunt.registerTask('start', [
        // 'clean:server',
        'wiredep',
        'ngconstant',
        'connect',
        'watch'
    ]);

    // grunt.registerTask('server', function(target) {
    //     grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    //     grunt.task.run([target ? ('serve:' + target) : 'serve']);
    // });

    grunt.registerTask('build', [
        'clean:dist',
        'wiredep:app',
        'ngconstant',
        'useminPrepare',
        'ngtemplates',
        'less:prod',
        'imagemin',
        'svgmin',
        'concat',
        'copy:fonts',
        'copy:dist',
        'ngAnnotate',
        'cssmin',
        'autoprefixer',
        'uglify',
        'rev',
        'usemin',
        'htmlmin'
    ]);

    grunt.registerTask('default', ['build']);
};
