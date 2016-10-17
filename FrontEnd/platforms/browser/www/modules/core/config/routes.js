'use strict';

/**
 * @ngdoc object
 * @name core.config
 * @requires ng.$stateProvider
 * @requires ng.$urlRouterProvider
 * @description Defines the routes and other config within the core module
 */
angular
    .module('core')
    .config(['$stateProvider',
        '$urlRouterProvider',
        function($stateProvider, $urlRouterProvider) {

            $urlRouterProvider.otherwise('/');

            /**
             * @ngdoc event
             * @name core.config.route
             * @eventOf core.config
             * @description
             *
             * Define routes and the associated paths
             *
             * - When the path is `'/'`, route to home
             * */
            $stateProvider
                .state('home', {
                    url: '/',
                    templateUrl: 'modules/core/views/home.html',
                    controller: 'HomeController'
                })
                .state('sign', {
                    url: '/sign',
                    templateUrl: 'modules/core/views/sign.html',
                    controller: 'SignController'
                })
                .state('visits', {
                    url: '/visits',
                    templateUrl: 'modules/core/views/visits.html',
                    controller: 'VisitsController'
                })
                .state('plan', {
                    url: '/plan',
                    templateUrl: 'modules/core/views/plan.html',
                    controller: 'PlanController'
                })
                .state('next_step', {
                    url: '/next',
                    templateUrl: 'modules/core/views/next_step.html',
                    controller: 'NextStepController'
                })
                .state('visit', {
                    url: '/visit',
                    templateUrl: 'modules/core/views/visit.html',
                    controller: 'VisitController'
                })
                .state('overview', {
                    url: '/overview',
                    templateUrl: 'modules/core/views/overview.html',
                    controller: 'OverviewController'
                })
                .state('city', {
                    url: '/city',
                    templateUrl: 'modules/core/views/city.html',
                    controller: 'CityController'
                });
        }
    ]);
