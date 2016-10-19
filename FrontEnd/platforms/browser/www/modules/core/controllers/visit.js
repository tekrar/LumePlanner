/*global
    angular,L
*/
'use strict';

angular
    .module('core')
    .controller('VisitController', ['$scope', '$http', 'city', 'currentDeparture', 'visitPlan', 'currentDestination', 'storeConfig', 'passData', '$location', 'storeUser', 'currentView',
        function($scope, $http, city, currentDeparture, visitPlan, currentDestination, storeConfig, passData, $location, storeUser, currentView) {
          console.info("----------------"+city.get());

          var to_visit = passData.get().activity;
          $scope.rate = 0;
          $scope.rated = false;

          $scope.$watch('rate', function() {
            if ($scope.rate>0) {
              $scope.rated = true;
            }
          });
          $scope.view_name = to_visit.display_name.split(',')[0];

          var picture_query = to_visit.display_name.split(',')[0];
          function find_pic (picture_query) {
            $http.get('https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=9eab1c5e074fc0e1deb66c40d7535ad9&format=json&sort=relevance&page=1&per_page=1&nojsoncallback=1&text='+picture_query).
              success(function(data) {
                console.log(data);
                if (data.photos.photo.length > 0) {
                  var photo = data.photos.photo[0];
                  $scope.img_url = "https://farm"+photo.farm+".staticflickr.com/"+photo.server+"/"+photo.id+"_"+photo.secret+"_b.jpg";
                } else {
                  console.log("not found:"+picture_query);
                  if (picture_query.lastIndexOf(" ") !== -1) {
                    picture_query = picture_query.substr(0, picture_query.lastIndexOf(" "));
                    find_pic(picture_query);
                  } else {
                    console.log("picture not found");
                  }
                }
              }).error(function(){
                console.log("Error Flickr API");
            });
          }
          find_pic(picture_query);


            //CONTENT:
          // $http.get('https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&titles='+to_visit.display_name.split(',')[0]).
          //   success(function(data) {
          //     angular.forEach(data.responseData.results, function(value, key) {
          //       $scope.urls.push(value.url);
          //     });
          //
          //   }).error(function(data){
          //     console.log("Error Google Images API");
          //   });

          //https://www.googleapis.com/customsearch/v1?key=INSERT_YOUR_API_KEY&cx=017576662512468239146:omuauf_lfve&q=lectures

          $scope.crowd = function (val) {

            if (val===0) {
              $scope.crowd0 = true;
              $scope.crowd1 = false;
              $scope.crowd2 = false;
              $scope.crowd3 = false;

            } else if (val===1) {
              $scope.crowd1 = true;
              $scope.crowd0 = false;
              $scope.crowd2 = false;
              $scope.crowd3 = false;

            } else if (val===2) {
              $scope.crowd2 = true;
              $scope.crowd0 = false;
              $scope.crowd1 = false;
              $scope.crowd3 = false;

            } else {
              $scope.crowd3 = true;
              $scope.crowd0 = false;
              $scope.crowd1 = false;
              $scope.crowd2 = false;

            }

            var request = {
              user            : storeUser.getUser().email,
              departure       : currentDeparture.get().activity,
              departure_time  : currentDeparture.get().time,
              arrival         : currentDestination.get().activity,
              choice          : val,
              city            : city.get()
            };

            console.log(request);
            $http.post(storeConfig.get().dita_server+'crowding_fdbk', request).
              success(function(data) {
                console.log(data); //data = boolean
              }).
              error(function(data) {
                //error
                console.log("error posting crowding feedback: ", data);
              });
          };

          $scope.next = function () {
            var d = new Date().getTime();
            var request = {
              user    : storeUser.getUser().email,
              visited : currentDestination.get().activity,
              time    : d,
              rating  : $scope.rate,
              city    : city.get()
            };
            console.log(request);
            $http.post(storeConfig.get().dita_server+'visited', request).
              success(function(data) {
                //visitPlan.setPlan(data);
                passData.set(data);
                currentView.set('Visiting Plan');
                $location.path('plan');
              }).
              error(function(data) {
                //error
                console.log("error posting new completed visit: ", data);
              });

          };

        }
    ]);
