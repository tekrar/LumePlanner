/*global
    angular, $
*/
'use strict';

angular
    .module('core')
    .controller('CityController', ['$scope', '$http', '$location', 'storeConfig', 'storeUser', 'visitPlan', 'passData', 'currentView','city',
      function($scope, $http, $location, storeConfig, storeUser, visitPlan, passData, currentView, city) {

        if (storeUser.getUser() === null) {
          currentView.set('Login page');
          $location.path('sign');
        }
        else {
          $scope.load = false;


          $scope.go = function(selected_city) {
            console.info(selected_city);
            city.set(selected_city);

            var city_user = {id: "", email: storeUser.getUser().email, city: selected_city};
            console.log(JSON.stringify(city_user));
            $http.post(storeConfig.get().dita_server+'selectcity', city_user).then(function(data) {
              currentView.set('home');
              $location.path('home');
            }, function(data) {
              //error
              $scope.load = false;
              console.log("error");
              console.log('new string, just to check');
              console.log(JSON.stringify(data));
            });
          }


        }
      }
    ]);


