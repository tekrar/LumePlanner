/*global
  angular,L
*/
'use strict';

angular
    .module('core')
    .controller('PlanController', ['$scope', '$http', 'storeConfig', 'passData', 'visitPlan', 'selectedPOIs', 'currentView', '$location', 'storeDeparture', 'storeArrival', 'storeUser',
				   function($scope, $http, storeConfig, passData, visitPlan, selectedPOIs, currentView, $location, storeDeparture, storeArrival, storeUser) {

				       $scope.newSequence = false;
				       $scope.ready = false;
				       $scope.load = true;
				       $scope.load_direction = false;
				       $scope.invalid = false;
				       $scope.view_name = currentView.get();

				       var items = passData.get(); //data is '2' if user has just logged in, or it contains the visitPlan if he comes from the 'home' view or from the 'visit' view
				       var new_plan = true;
				       var map_center = {};

				       function format_name(name) {
					   var name_array = name.split(',');

					   if (!name_array[1]) {
					       name_array[1] = "";
					       name_array[2] = "";
					   }
					   
					   return name_array[0]+"<br />"+name_array[1]+","+name_array[2];
					   }
					   function drawMap(items) {
					   console.log('path:', items.path);
					   $scope.geojson = {
					       data  : items.path.points,
					       style : {
						   weight: 2,
						   opacity: 1,
						   color: 'blue',
						   dashArray: '3',
						   fillOpacity: 0.7
					       }};

					   var m = {};

					   var map_center = {
					       lat : items.departure.geometry.coordinates[1],
					       lng: items.departure.geometry.coordinates[0],
					       zoom: 12
					   };

					   if ((items.departure.place_id === "0" && items.arrival.place_id === "00")
					       || items.departure.place_id === items.arrival.place_id ) {

					       m[items.departure.place_id] = {
						   lat : items.departure.geometry.coordinates[1],
						   lng: items.departure.geometry.coordinates[0],
						   message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Start:" + items.departure_time + "</b><br />" +
						       "<b>Finish:" + items.arrival_time + "</b><br />" +
						       format_name(items.arrival.display_name)+"</p>",
						   icon: {
						       type: 'div',
						       className: 'marker',
						       html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
						   }
					       };

					   } else {

					       m[items.arrival.place_id] = {
						   lat : items.arrival.geometry.coordinates[1],
						   lng: items.arrival.geometry.coordinates[0],
						   message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Finish:" + items.arrival_time + "</b><br />" +
						       format_name(items.arrival.display_name)+"</p>",
						   icon: {
						       type: 'div',
						       className: 'marker',
						       html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-flag-checkered fa-stack-2x\"></i></span>"
						   }
					       };

					       m[items.departure.place_id] = {
						   lat : items.departure.geometry.coordinates[1],
						   lng: items.departure.geometry.coordinates[0],
						   message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Departure:"+items.departure_time+"</b><br />" +
						       format_name(items.departure.display_name)+"</p>",
						   icon: {
						       type: 'div',
						       className: 'marker',
						       html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
						   }
					       };
					   }

					   var j;
					   for (j = 0; j < items.visited.length; j+=1) {
					       m[items.visited[j].visit.place_id] = {
						   lat : items.visited[j].visit.geometry.coordinates[1],
						   lng: items.visited[j].visit.geometry.coordinates[0],
						   message:  "<p style='background-color:white !important;color:#084265 !important;'><b>"+"Arrival:"+items.visited[j].arrival_time +
						       "</b><br /><b>Departure:" + items.visited[j].departure_time
						       +"</b><br />"+format_name(items.visited[j].visit.display_name)+"</p>",
						   icon: {
						       type: 'div',
						       className: 'marker',
						       html: "<span class=\"fa-col-red fa-stack fa-lg\"><i class=\"fa fa-circle fa-stack-2x\"></i><i class=\"fa fa-inverse fa-stack-1x\">"+(j+1)+"</i></span>"
						   }
					       };

					   }
					   var i;
					   for (i = j+1; (i-j-1) < items.to_visit.length; i+=1) {
					       m[items.to_visit[i-j-1].visit.place_id] = {
						   lat : items.to_visit[i-j-1].visit.geometry.coordinates[1],
						   lng: items.to_visit[i-j-1].visit.geometry.coordinates[0],
						   message:  "<p style='background-color:white !important;color:#084265 !important;'><b>"+"Arrival:"+items.to_visit[i-j-1].arrival_time +
						       "</b><br /><b>Departure:" + items.to_visit[i-j-1].departure_time
						       +"</b><br />"+format_name(items.to_visit[i-j-1].visit.display_name)+"</p>",
						   icon: {
						       type: 'div',
						       className: 'marker',
						       html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-circle fa-stack-2x\"></i><i class=\"fa fa-inverse fa-stack-1x\">"+i+"</i></span>"

						   }
					       };
					   }

					   angular.extend($scope, {
					       venice: map_center,
					       defaults: {
						   attributionControl: false,
						   scrollWheelZoom: 'center',
						   tileLayer: storeConfig.get().osm_tile,
						   maxZoom: 18,
						   path: {
						       weight: 10,
						       color: '#800000',
						       opacity: 1
						   }
					       },
					       markers: m
					   });
					   $scope.ready = true;
				       }

				       console.log('items:');
				       console.log(items);
				       if (items==='2' || items === 2) { //user comes from 'sign' view --> a visiting plan exists already
					   new_plan = false;
					   $http.post(storeConfig.get().dita_server+'plan', storeUser.getUser()).
					       success(function(data) {
						   console.log(data);
						   passData.set(data);
						   visitPlan.setPlan(data.crowd_related);
						   $scope.departure_time = data.greedy.departure_time;
						   $scope.greedyp = data.greedy.to_visit;
						   $scope.shortest = data.shortest.to_visit;
						   $scope.lesscrowded = data.crowd_related.to_visit;
						   $scope.greedyp_v = data.greedy.visited;
						   $scope.shortest_v = data.shortest.visited;
						   $scope.lesscrowded_v = data.crowd_related.visited;

						   $scope.greedy_a = data.greedy.arrival_time;
						   $scope.shortest_a = data.shortest.arrival_time;
						   $scope.lesscrowded_a = data.crowd_related.arrival_time;

						   console.log('g+s+l', $scope.greedy_a, $scope.shortes_a, $scope.lesscrowded_a);
						   
						   $scope.load = false;
						   $scope.load_direction = false;
						   $scope.crowd = true;
						   $scope.greedy=false;
						   $scope.short=false;
						   drawMap(data.crowd_related);

					       }).
					       error(function(data) {
						   //error
						   console.log("error retrieving plan", data);
					       });

				       } else if (items.crowd_related.departure.place_id === "0") {
					   $scope.load = false;
					   $scope.invalid = true;
					   $scope.error_title = "Plan not found. Your departure location is invalid.";
					   console.log($scope.error_title);
				       } else if (items.crowd_related.arrival.place_id === "00") {
					   $scope.load = false;
					   $scope.invalid = true;
					   $scope.error_title = "Plan not found. Your arrival location is invalid.";
					   console.log($scope.error_title);
				       } else if (!items.crowd_related.departure && !items.crowd_related.arrival ) {
					   $scope.load = false;
					   $scope.invalid = true;
					   $scope.error_title = "Unexpected error in computing your path. Please try again.";
				       } else {
					   if (visitPlan.getHash() !== 0) { //if it is a replan, check if the replanning generated a new plan

					       if (visitPlan.getHash() && visitPlan.getHash() !== items.crowd_related.hash) {
						   //display the message to inform of the new sequence
						   $scope.newSequence = true;
					       }
					       visitPlan.setPlan(items.crowd_related);
					   }
					   $scope.greedyp = items.greedy.to_visit;
					   $scope.shortest = items.shortest.to_visit;
					   $scope.lesscrowded = items.crowd_related.to_visit;
					   $scope.greedyp_v = items.greedy.visited;
					   $scope.shortest_v = items.shortest.visited;
					   $scope.lesscrowded_v = items.crowd_related.visited;
					   $scope.crowd = true;
					   $scope.greedy=false;
					   $scope.short=false;
					   $scope.load = false;

					   $scope.departure_time = items.greedy.departure_time;
					   $scope.greedy_a = items.greedy.arrival_time;
					   $scope.shortest_a = items.shortest.arrival_time;
					   $scope.lesscrowded_a = items.crowd_related.arrival_time;

					   console.log('g+s+l', $scope.greedy_a, $scope.shortes_a, $scope.lesscrowded_a);
					   
					   drawMap(items.crowd_related);
				       }

				       $scope.showLeastCrowded = function() {
					   $scope.crowd=true;
					   $scope.greedy=false;
					   $scope.short=false;
					   drawMap(passData.get().crowd_related);
				       };

				       $scope.showShortest = function() {
					   $scope.crowd=false;
					   $scope.greedy=false;
					   $scope.short=true;
					   drawMap(passData.get().shortest);
				       };

				       $scope.showGreedy = function() {
					   $scope.crowd=false;
					   $scope.greedy=true;
					   $scope.short=false;
					   drawMap(passData.get().greedy);
				       };


				       $scope.go_home = function() {
					   console.log("Items selected after "+ currentView.get()+":"+selectedPOIs.get().length);
					   selectedPOIs.reset();
					   storeDeparture.initCustom();
					   storeDeparture.set(null);
					   storeArrival.initCustom();
					   storeArrival.set(null);
					   $location.path('home');
				       };

				       $scope.accept_plan = function() {
					   items = passData.get();

					   $scope.load_direction = true;

					   if (new_plan) {
					       $http.post(storeConfig.get().dita_server+'accept_plan', items).
						   success(function() {
						       $scope.load = false;

						       passData.set(items.crowd_related);
						       currentView.set('Reach your next Stop');
						       $location.path('next');

						   }).
						   error(function(data) {
						       //error
						       console.log("error accepting visting plan", data);
						   });
					   } else {
					       passData.set(items.crowd_related);
					       currentView.set('Reach your next Stop');
					       $location.path('next');
					   }
				       };


				   }
					   ]);
