/*global
    angular, $
*/
'use strict';

angular
    .module('core')
    .controller('VisitsController', ['$scope', 'city', 'passData', 'selectedPOIs', 'currentView', '$location',
        function($scope, city, passData, selectedPOIs, currentView, $location) {

          $scope.view_name = currentView.get();

          switch($scope.view_name) {
            case "attractions": $scope.activities = selectedPOIs.getAttractions(); break;
            case "monuments" :$scope.activities = selectedPOIs.getMonuments(); break;
            case "museums" : $scope.activities = selectedPOIs.getMuseums(); break;
            case "parks" : $scope.activities = selectedPOIs.getParks(); break;
            case "historical" : $scope.activities = selectedPOIs.getHistorical_sites(); break;
            case "religious" : $scope.activities = selectedPOIs.getReligious_sites(); break;
            default : console.log("Invalid view:"+view);
          }

          $('#iconified').on('keyup', function() {
            var input = $(this);
            if(input.val().length === 0) {
              input.addClass('iconplace');
            } else {
              input.removeClass('iconplace');
            }
          });

          $scope.isSelected = function (id) {
            if ($.inArray(id, $scope.activities) === -1) {
              return false;
            }
            return true;
          };
          $scope.items = passData.get().sort(function(item1, item2) {
            return (item1.display_name > item2.display_name) - (item1.display_name < item2.display_name);
          });

          $scope.$watch('searchActivities', function() {
            angular.forEach($scope.items, function(value) {
              if ($scope.searchActivities === undefined) {
                value.match = true;
              } else if (value.display_name.split(',')[0].toLowerCase().indexOf($scope.searchActivities.toLowerCase()) > -1) {
                value.match = true;
              } else {
                value.match = false;
              }
            });

          });

          $scope.selectPOI = function(item_id, category) {
            if ($.inArray(item_id, selectedPOIs.get())===-1) {
              selectedPOIs.add(item_id, category);
              angular.element('#'+item_id).addClass("list-group-item-info");
            } else {
              selectedPOIs.remove(item_id, category);
              angular.element('#'+item_id).removeClass("list-group-item-info");
            }
          };

          $scope.go_home = function() {
            console.log("Items selected after "+ currentView.get()+":"+selectedPOIs.get().length);
            $location.path('home');
          };



        }
    ]);
