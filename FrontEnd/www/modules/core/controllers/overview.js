/*global
    angular
*/
'use strict';

angular
    .module('core')
    .controller('OverviewController', ['$scope', '$http', 'city', 'storeConfig', 'visitPlan', 'currentDeparture', 'currentDestination', 'passData', 'storeDeparture', 'storeSlider', 'storeArrival', 'selectedPOIs', '$location', 'storeUser', 'currentView',
        function($scope, $http, city, storeConfig, visitPlan, currentDeparture, currentDestination, passData, storeDeparture, storeSlider, storeArrival, selectedPOIs, $location, storeUser, currentView) {

          console.info("overview ---- "+city.get());

          $scope.view_name = currentView.get();

          $scope.crowd = function (val) {

            if (val===0) {
              $scope.crowd0 = true;
              $scope.crowd1 = false;
            } else {
              $scope.crowd1 = true;
              $scope.crowd0 = false;
            }

            var request = {
              user      : storeUser.getUser().email,
              choice    : val,
              city      : city.get(),
              crowding  : visitPlan.getCrowding()
            };

            console.log(request);
            $http.post(storeConfig.get().dita_server+'ov_crowding_fdbk', request).
              success(function(data) {
                console.log(data); //data = boolean
              }).
              error(function(data) {
                //error
                console.log("error posting overall crowding fedback ", data);
              });
          };

          $scope.seq = function (val) {

            if (val===0) {
              $scope.like0 = true;
              $scope.like1 = false;
            } else {
              $scope.like1 = true;
              $scope.like0 = false;
            }

            var request = {
              user  : storeUser.getUser().email,
              choice  : val,
              city: city.get(),
              crowding  : visitPlan.getCrowding()
            };

            $http.post(storeConfig.get().dita_server+'ov_plan_fdbk', request).
              success(function(data) {
                console.log(data); //data = boolean
              }).
              error(function(data) {
                //error
                console.log("error posting overall plan feedback ", data);
              });
          };

          $scope.restart = function () {
            var request = {
              user  : storeUser.getUser().email,
              email      : storeUser.getUser().email,
              city      : city.get()
            };

            console.log(JSON.stringify(request));
            $http.post(storeConfig.get().dita_server+'finish', request).
              success(function(data) {
                console.log("User plan deleted:"+data);
                visitPlan.setPlan({});
                currentDeparture.reset();
                currentDestination.reset();
                passData.set([]);
                storeDeparture.set(null);
                storeDeparture.setCustom(0.0, 0.0);
                storeSlider.set(0);
                storeArrival.set(null);
                storeArrival.setCustom(0.0, 0.0);
                selectedPOIs.reset();
                currentView.set('home');
                $location.path('home');

              }).error(function(data){
                console.log("Error Finishing Visiting Plan ", data);
              });
          };

        }
    ]);
