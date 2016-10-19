/*global
    angular, $
*/
'use strict';

angular
    .module('core')
    .controller('CityController', ['$scope', '$http', 'city', '$location', 'storeConfig', 'storeUser', 'visitPlan', 'passData', 'currentView',
      function($scope, $http, city, $location, storeConfig, storeUser, visitPlan, passData, currentView) {

        if (storeUser.getUser() === null) {
          currentView.set('Login page');
          $location.path('sign');
        }
        else {
          $scope.load = false;


          $scope.go = function(selected_city) {
            console.info(selected_city);
            city.set(selected_city);


            currentView.set('home');
            $location.path('home');
          }
        }
      }
    ]);


