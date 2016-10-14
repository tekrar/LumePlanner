/*global
    angular,L, less
*/
'use strict';

function computeDistance(lat1, lng1, lat2, lng2) {
  var earthRadius = 6371000; //meters
  var dLat = (lat2-lat1) * Math.PI / 180;
  var dLng = (lng2-lng1) * Math.PI / 180;
  var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
  Math.cos(lat1 * Math.PI / 180) *
  Math.cos(lat2 * Math.PI / 180) *
  Math.sin(dLng/2) * Math.sin(dLng/2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  var dist = (earthRadius * c);
  return dist;
}

function calculateAngle(lat1, lng1, lat2, lng2) {
  var dy = lat2 - lat1;
  var dx = Math.cos(Math.PI / 180 * lat1) * (lng2 - lng1);
  var angle = Math.atan2(dy, dx);
  return (360+angle*57.295779513)%360;
}

angular
    .module('core')
    .controller('NextStepController', ['$scope', '$http', 'currentDeparture', 'currentDestination', 'storeConfig', 'passData', 'currentView', '$location', 'leafletData',
        function($scope, $http, currentDeparture, currentDestination, storeConfig, passData, currentView, $location, leafletData) {

          $scope.ready = false;
          $scope.load = true;
          $scope.invalid = false;
          $scope.view_name = currentView.get();

          var items = passData.get();
          var new_plan = true;
          var map_center = {};

          var start;
          var end;
          if (items.visited === null || items.visited.length === 0) {
            start = items.departure.geometry.coordinates[1]+','+items.departure.geometry.coordinates[0];
            end = items.to_visit[0].visit.geometry.coordinates[1]+','+items.to_visit[0].visit.geometry.coordinates[0];
            currentDeparture.set(items.departure, items.departure_time);
            currentDestination.set(items.to_visit[0].visit, items.to_visit[0].arrival_time);
          } else if (items.to_visit.length > 0) {
            start = items.visited[items.visited.length-1].visit.geometry.coordinates[1]+','+items.visited[items.visited.length-1].visit.geometry.coordinates[0];
            end = items.to_visit[0].visit.geometry.coordinates[1]+','+items.to_visit[0].visit.geometry.coordinates[0];
            currentDeparture.set(items.visited[items.visited.length-1].visit, items.visited[items.visited.length-1].departure_time);
            currentDestination.set(items.to_visit[0].visit, items.to_visit[0].arrival_time);
          } else {
            start = items.visited[items.visited.length-1].visit.geometry.coordinates[1]+','+items.visited[items.visited.length-1].visit.geometry.coordinates[0];
            end = items.arrival.geometry.coordinates[1]+','+items.arrival.geometry.coordinates[0];
            currentDeparture.set(items.visited[items.visited.length-1].visit, items.visited[items.visited.length-1].departure_time);
            currentDestination.set(items.arrival, items.arrival.arrival_time);
          }
          console.log("destination:"+end);

          angular.extend($scope, {
            center : {
              lat : parseFloat(start.split(',')[0]),
              lng : parseFloat(start.split(',')[1]),
              zoom : 18
            },
            defaults: {
              scrollWheelZoom: 'center',
              doubleClickZoom: 'center',
              zoomControl: false,
              touchZoom: false,
              dragging: false,
              tileLayer: storeConfig.get().osm_tile,
              maxZoom: 18,
              //minZoom:18,
              attributionControl: false,
              path: {
                weight: 10,
                color: '#800000',
                opacity: 1
              }
            }
          });


          //GH alt_key: LijBPDQGfu7Iiq80w3HzwB4RUDJbMbhs6BU0dEnn
          $http.get('https://graphhopper.com/api/1/route?'+
           'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false'+
           '&point=' + start + '&point=' + end).
           success(function(data) { //routing API
             console.log(data.paths[0]);
             var geoJsonPoints = data.paths[0].points.coordinates;
             var time_tot = data.paths[0].time;

             var distances = [];
             var d_tot = 0.0;
             var i, p_from, p_to, d;
             for (i=0; i<geoJsonPoints.length-1; i+=1) {
               p_from = geoJsonPoints[i];
               p_to = geoJsonPoints[i+1];
               d = computeDistance(p_from[1], p_from[0], p_to[1], p_to[0]);
               distances.push(d);
               d_tot += d;
             }

             var d_rates = [];
             for (i=0;i<distances.length;i+=1) {
               d_rates.push(distances[i]/d_tot);
             }

             var start_time = new Date().getTime();
             var timings = [];
             timings.push(start_time);
             for (i=0;i<d_rates.length;i+=1) {
               start_time +=  parseInt(d_rates[i]*time_tot, 10);
               timings.push(start_time);
             }


             var adaptedGeoJsonData = {
                type: "Feature",
                geometry: {
                  type: "MultiPoint",
                  coordinates: geoJsonPoints
                },
                properties: {
                  time: timings
                }
             };

             console.log ({adaptedGeoJson : adaptedGeoJsonData});


            var playback = function(leafletData) {

               leafletData.getMap('map').then(function(map) {

                 var options = {
                   //tickLen : '', //millis [def:250]
                   speed : 1.0, //float multiplier [def:1]
                   tracksLayer : false,
                   marker: {
                     icon: L.divIcon({className: 'fa fa-dot-circle-o fa-3x fa-rotate-dyn'})
                   }
                 };
                 var v = new L.Playback(map, adaptedGeoJsonData, null, options);
                 v.start();
               });

             };
             playback(leafletData);

             var timed_update;
             var old_position;
             var updateCenter = function() {
               leafletData.getMap('map').then(
                 function(map) {

                   timed_update = setInterval(function() {
                     var latlng = map.layerPointToLatLng(map.getPanes().markerPane.childNodes[0]._leaflet_pos);
                     if (old_position && old_position===latlng) {
                       console.log("qui");
                     }
                     old_position = latlng;
                     if (computeDistance(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng) < 30) {
                       //changeView
                       console.log({arrive : currentDestination.get()});
                       passData.set(currentDestination.get());
                       clearInterval(timed_update);
                       if (items.to_visit.length > 0) {
                         currentView.set('visit');
                         $location.path('visit');
                       } else {
                         currentView.set('Your visiting is over!');
                         $location.path('overview');
                       }
                     } else {
                       //console.log("moving:"+computeDistance(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng));
                       map.panTo(L.latLng(latlng));
                     }
                   }, 100);
                 }
               );
             };
             updateCenter();




             var m = [];
             m[0] = {
               lat : parseFloat(start.split(',')[0]),
               lng: parseFloat(start.split(',')[1]),
               icon: {
                 type: "awesomeMarker",
                 icon: "glyphicon-home",
                 markerColor: "green"
              }
            };

            m[1] = {
               lat : parseFloat(end.split(',')[0]),
               lng: parseFloat(end.split(',')[1]),
               icon: {
                 type: "awesomeMarker",
                 icon: "glyphicon-flag",
                 markerColor: "red"
              }
            };


             angular.extend($scope, {
               geojson: {
                 data: data.paths[0].points,
                 style: {
                   fillColor: "green",
                   weight: 2,
                   opacity: 1,
                   color: 'blue',
                   dashArray: '3',
                   fillOpacity: 0.7
                 }
               },
               markers: m
             });


              $scope.go_visit = function () {
                passData.set(currentDestination.get());
                clearInterval(timed_update);
                if (items.to_visit.length > 0) {
                  currentView.set('visit');
                  $location.path('visit');
                } else {
                  currentView.set('Your visiting is over!');
                  $location.path('overview');
                }
              };

            }).
            error(function(data) {
		//error
		console.log("error GrapHopper request " + JSON.stringify( data));
            });

            $scope.ready = true;
            $scope.load = false;

        }
    ]);

    var format_name = function(name) {
      var name_array = name.split(',');
      if (!name_array[1]) {
        name_array[1] = "";
        name_array[2] = "";
      }
      return name_array[0]+"<br />"+name_array[1]+","+name_array[2];
    };
