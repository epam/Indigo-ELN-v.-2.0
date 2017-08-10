angular.module('indigoeln', [
    'ui.router',
    'ngResource',
    'ui.tree',
    'ui.bootstrap',
    'ngAnimate',
    'ngRoute',
    'ngIdle',
    'ct.ui.router.extras',
    'xeditable',
    'angularFileUpload',
    'ngTagsInput',
    'ngCookies',
    'prettyBytes',
    angularDragula(angular),
    'cgBusy',
    'angular.filter',
    'ngFileSaver',
    'ui.select',
    'ngSanitize',
    'datePicker',
    'monospaced.mousewheel',
    'ui.checkbox',
    'monospaced.elastic',
    'ui.bootstrap-slider',
    'LocalStorageModule',
    'angular-click-outside',
    'config',
    'angular-cache',
    'indigoeln.autorecovery'
])
    .run(function($rootScope, $window, $state, $uibModal, editableOptions, Auth, Principal, Idle, EntitiesBrowser) {
        $.mCustomScrollbar.defaults.advanced.autoScrollOnFocus = false;
        // idleTime: 30 minutes, countdown: 30 seconds
        var countdownDialog = null;
        var idleTime = 30;
        var countdown = 30;

        $rootScope.$on('$stateChangeStart', function(event, toState, toStateParams) {
            $rootScope.toState = toState;
            $rootScope.toStateParams = toStateParams;

            if (Principal.isIdentityResolved()) {
                Auth.authorize();
            }
            var tab = angular.copy(toState.data.tab);

            if (tab) {
                tab.params = toStateParams;
                if (tab.type && tab.type === 'entity') {
                    EntitiesBrowser.addTab(tab);
                }
            }
        });

        $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
            var titleKey = 'indigoeln';

            // Remember previous state unless we've been redirected to login or we've just
            // reset the state memory after logout. If we're redirected to login, our
            // previousState is already set in the authExpiredInterceptor. If we're going
            // to login directly, we don't want to be sent to some previous state anyway
            if (toState.name !== 'login') {
                $rootScope.previousStateName = fromState.name;
                $rootScope.previousStateParams = fromParams;
                Idle.watch();
            }

            // Set the page title key to the one configured in state or use default one
            if (toState.data.pageTitle) {
                titleKey = toState.data.pageTitle;
            }
            $window.document.title = titleKey;
        });
        $rootScope.$on('IdleStart', function() {
            if (!countdownDialog) {
                countdownDialog = $uibModal.open({
                    animation: false,
                    templateUrl: 'scripts/app/timer/timer-dialog.html',
                    controller: 'CountdownDialogController',
                    windowClass: 'modal-danger',
                    resolve: {
                        countdown: function() {
                            return countdown;
                        },
                        idleTime: function() {
                            return idleTime;
                        }
                    }
                });
            }
        });
        $rootScope.$on('IdleEnd', function() {
            if (countdownDialog) {
                countdownDialog.close();
                countdownDialog = null;
            }
        });
        $rootScope.$on('IdleTimeout', function() {
            if (countdownDialog) {
                countdownDialog.close();
                countdownDialog = null;
            }
            Auth.logout();
            $state.go('login');
        });
        $rootScope.$on('$stateChangeSuccess', function() {
            $('html, body').animate({
                scrollTop: 0
            }, 200);
        });
        $rootScope.back = function() {
            // If previous state is 'activate' or do not exist go to 'home'
            if ($rootScope.previousStateName === 'activate' || $state.get($rootScope.previousStateName) === null) {
                $state.go('experiment');
            } else {
                $state.go($rootScope.previousStateName, $rootScope.previousStateParams);
            }
        };
        // Theme for angular-xeditable. Can also be 'bs2', 'default'
        editableOptions.theme = 'bs3';
    })

    .config(function($stateProvider, $urlRouterProvider, $provide, $httpProvider, $compileProvider, IdleProvider, $animateProvider) {
        // enable CSRF
        $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
        $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';

        $urlRouterProvider.otherwise('/');
        $stateProvider.state('app_page', {
            abstract: true,
            views: {
                'app_page@': {
                    template: '<app-page></app-page>'
                }
            },
            resolve: {
                authorize: function(Auth) {
                    return Auth.authorize();
                },
                user: function(Principal) {
                    return Principal.identity();
                }
            }
        }).state('navbar', {
            abstract: true,
            parent: 'app_page',
            views: {
                'navbar@app_page': {
                    templateUrl: 'scripts/components/navbar/navbar.html',
                    controller: 'NavbarController',
                    controllerAs: 'vm'
                }
            }
        }).state('sidebar', {
            abstract: true,
            parent: 'navbar',
            views: {
                'sidebar@app_page': {
                    templateUrl: 'scripts/components/sidebar/sidebar.html',
                    controller: 'SidebarController',
                    controllerAs: 'vm'
                }
            }
        });
        $httpProvider.interceptors.push('errorHandlerInterceptor');
        $httpProvider.interceptors.push('notificationInterceptor');

        // 30 min of idleness
        IdleProvider.idle(30 * 60);
        // 30 sec to do something
        IdleProvider.timeout(30);
        // to allow file's export
        $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);

        $animateProvider.classNameFilter(/\banimated\b/);
    });
