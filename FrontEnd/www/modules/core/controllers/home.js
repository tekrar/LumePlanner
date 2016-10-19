/*global
    angular, conf

'use strict';
*/

/**
 * @ngdoc object
 * @name core.Controllers.HomeController
 * @description Home controller
 * @requires ng.$scope
 */
angular
    .module('core')
    .controller('HomeController', ['$scope', '$http', '$location', 'storeConfig', 'storeUser', 'visitPlan', 'passData', 'currentView', 'city' ,'selectedPOIs', 'storeDeparture', 'storeArrival', 'storeTime', 'loadedPOIs', 'storePOIs', 'storeSlider',
				   function($scope, $http, $location, storeConfig, storeUser, visitPlan, passData, currentView, city, selectedPOIs, storeDeparture, storeArrival, storeTime, loadedPOIs, storePOIs, storeSlider) {
				       $scope.load = true;
				       $scope.load_plan = false;
				       //load config
				       if(storeConfig.get() === null) {
					   //console.log(conf);
					   storeConfig.set(conf);
				       }

				       if (storeUser.getUser() === null) {
					   currentView.set('Login page');
					   $location.path('sign');
				       } else {

						console.log("-------"+city.get());
						   $scope.city = city.get();



						   console.log(loadedPOIs.get()+" != "+city.get()+"  "+(loadedPOIs.get() != city.get()));

					   //get list of POIs -------------------------------------------------------------------
					   if (loadedPOIs.get() != city.get()) {
					       $http.get(storeConfig.get().dita_server+'activities?city='+city.get()).
						   success(function(data) {
							   storePOIs.clear();
						       //save data into vars
						       angular.forEach(data, function(value) {
							   //value.display_name = value.display_name.replace(/[^\x00-\x7F]/g, "'"); //remove non-ASCII chars (done on the server)

							   switch (value.category) {
							   case "attractions": storePOIs.addAttraction(value); break;
							   case "monuments" : storePOIs.addMonument(value); break;
							   case "museums" : storePOIs.addMuseum(value); break;
							   case "eating" : storePOIs.addRestaurant(value); break;
							   case "parks" : storePOIs.addPark(value); break;
							   case "resting" : storePOIs.addHotel(value); break;
							   case "historical_sites" : storePOIs.addHistorical(value); break;
							   case "religious_sites" : storePOIs.addReligious(value); break;
							   default : console.log("Invalid category:"+value.category);
							   }
						       });

						       $scope.hotels = storePOIs.getHotels().sort(function(item1, item2) {
							   return (item1.display_name > item2.display_name) - (item1.display_name < item2.display_name);
						       });
						       $scope.hotels.unshift({display_name:"Current Location"});
						       //$scope.hotels.unshift({display_name:"Select"});
						       storePOIs.setHotels($scope.hotels);

						       //$scope.departure = $scope.hotels[0];
						       //$scope.arrival = $scope.hotels[0];

						       $scope.attractions = selectedPOIs.getAttractions().length;
						       console.log("selected attr:"+$scope.attractions);
						       $scope.monuments = selectedPOIs.getMonuments().length;
						       $scope.museums = selectedPOIs.getMuseums().length;
						       $scope.parks = selectedPOIs.getParks().length;
						       $scope.historical_sites = selectedPOIs.getHistorical_sites().length;
						       $scope.religious_sites = selectedPOIs.getReligious_sites().length;

						       $scope.load = false;
						       loadedPOIs.set(city.get());
						   }).
						   error(function(data) {
						       //error
						       console.log("error getting activities", data);
						   });

					   } else {
					       $scope.load = false;
					       $scope.hotels = storePOIs.getHotels();
					       $scope.departure = (storeDeparture.get() === null) ? ((storeDeparture.getCustom().id === "1" ) ? $scope.hotels[0] : $scope.hotels[1] ) : storeDeparture.get();
					       $scope.arrival = (storeArrival.get() === null) ? ((storeArrival.getCustom().id === "1" ) ? $scope.hotels[0] : $scope.hotels[1] ) : storeArrival.get();

					   }

					   $scope.attractions = selectedPOIs.getAttractions().length;
					   console.log("selected attr:"+$scope.attractions);
					   $scope.monuments = selectedPOIs.getMonuments().length;
					   $scope.museums = selectedPOIs.getMuseums().length;
					   $scope.parks = selectedPOIs.getParks().length;
					   $scope.historical_sites = selectedPOIs.getHistorical_sites().length;
					   $scope.religious_sites = selectedPOIs.getReligious_sites().length;

					   //$('select').selectpicker();
					   var d = new Date();

					   //storeTime.set(((d.getHours()<10) ? "0"+d.getHours() : d.getHours())+":"+((d.getMinutes()<10) ? "0"+d.getMinutes() : d.getMinutes()));
					   storeTime.set("09:00");
					   $scope.time = storeTime.get();


					   $scope.update_time = function() {
					       storeTime.set(angular.element('#timepicker').val());
					   };

					   $scope.select_hotel_d = function() {
					       if ($scope.departure.display_name === "Current Location") {
						   $scope.get_location_d();
					       } else {
						   storeDeparture.set($scope.departure);

					       }
					       $scope.arrival = $scope.departure;
					       storeArrival.set($scope.arrival);
					   };


					   $scope.get_location_d = function() {
					       navigator.geolocation.getCurrentPosition(function(position) {
						   //onSuccess
						   storeDeparture.set(null);
						   storeArrival.set(null);
						   storeDeparture.setCustom(position.coords.latitude, position.coords.longitude);
						   storeArrival.setCustom(position.coords.latitude, position.coords.longitude);
						   //test     storeDeparture.setCustom(45.4, 12.35);
						   console.log('custom location:'+ storeDeparture.getCustom().lat +", "+storeDeparture.getCustom().lon);
					       }, function(error) {
						   //onError
						   console.log('code: '+ error.code +' message: ' + error.message);
					       });
					   };

					   $scope.select_hotel_a = function() {
					       if ($scope.arrival.display_name === "Current Location") {
						   $scope.get_location_a();
					       } else {
						   storeArrival.set($scope.arrival);
					       }
					   };

					   $scope.get_location_a = function() {
					       navigator.geolocation.getCurrentPosition(function(position) {
						   //onSuccess
						   storeArrival.set(null);
						   storeArrival.setCustom(position.coords.latitude, position.coords.longitude);
						   console.log('custom location:'+ storeArrival.getCustom().lat +", "+storeArrival.getCustom().lon);
					       }, function(error) {
						   //onError
						   console.log('code: '+ error.code +' message: ' + error.message);
					       });
					   };


					   //https://github.com/seiyria/bootstrap-slider
					   var slider = new Slider('#ex1', {
					       tooltip: 'hide'
					   });
					   function sliderUpdater(){
					       var newValue = slider.getValue();
					       console.log('newValue:', newValue );

					       if (newValue < 1) {
						   $scope.sliderValue = "Fully Empty";
					       } else if (newValue < 2) {
						   $scope.sliderValue = "Mainly Empty";
					       } else if (newValue < 3) {
						   $scope.sliderValue = "Mainly Crowded";
					       } else {
						   $scope.sliderValue = "Fully Crowded";
					       }
					       $scope.$apply();
					   }

					   slider.on('slide', sliderUpdater);
					   slider.on('change', sliderUpdater);
					   slider.setValue(storeSlider.get());
					   $scope.sliderValue = "Fully Empty";



					   $scope.sliderUpdate = function(){
					       var slid_val = slider.getValue();
					       console.log(slid_val);
					       if (slid_val < 1) {
						   $scope.sliderValue = "Fully Empty";
					       } else if (slid_val < 2) {
						   $scope.sliderValue = "Mainly Empty";
					       } else if (slid_val < 3) {
						   $scope.sliderValue = "Mainly Crowded";
					       } else {
						   $scope.sliderValue = "Fully Crowded";
					       }
					   };



					   //http://jdewit.github.io/bootstrap-timepicker/
					   angular.element('#timepicker').timepicker({
					       minuteStep: 15,
					       appendWidgetTo: 'body',
					       showSeconds: false,
					       showMeridian: false,
					       defaultTime: 'current'
					   });

					   $scope.changeView = function(view) {
					       currentView.set(view);
					       switch (view) {
					       case "attractions": passData.set(storePOIs.getAttractions()); break;
					       case "monuments" :passData.set(storePOIs.getMonuments()); break;
					       case "museums" : passData.set(storePOIs.getMuseums()); break;
					       case "parks" : passData.set(storePOIs.getParks()); break;
					       case "historical" : passData.set(storePOIs.getHistorical()); break;
					       case "religious" : passData.set(storePOIs.getReligious()); break;
					       default : console.log("Invalid view:"+view);
					       }
					       $location.path('visits');
					   };


					   $scope.go_city_selection = function() {
						   console.log("go back");
						   $location.path('city');
					   };

					   $scope.requestPlan = function() {
					       if (!$scope.form.$valid || ($scope.attractions == 0 && $scope.monuments == 0 && $scope.museums == 0 && $scope.parks == 0 && $scope.historical_sites == 0 && $scope.religious_sites == 0)) {
						   console.log("err");
						   return;
					       }
					       //$scope.load = true;
					       $scope.load_plan = true;
					       storeSlider.set(slider.getValue());
					       var slid_val = slider.getValue();
					       if (slid_val < 1) {
						   slid_val = 1.0;
					       } else if (slid_val < 2) {
						   slid_val = 0.5;
					       } else if (slid_val < 3) {
						   slid_val = -0.5;
					       } else {
						   slid_val = -1.0;
					       }

					       console.log(slid_val);
					       var request = {
						   user : storeUser.getUser().email,
                           city: city.get(),
						   crowd_preference : slid_val,
					       	   start_time : storeTime.get(),
						   visits : selectedPOIs.get()
					       };
					       if (storeDeparture.get() === null) {
						   console.log('null selected');
						   request.start_place = {
						       display_name : 'Current Location',
						       place_id : storeDeparture.getCustom().id,
						       lat : storeDeparture.getCustom().lat,
						       lon : storeDeparture.getCustom().lon
						   };
					       } else {
						   console.log('null not selected ', storeDeparture.get());
						   request.start_place = {
						       display_name: storeDeparture.get().display_name,
						       place_id : storeDeparture.get().place_id,
						       lat : storeDeparture.get().geometry.coordinates[1],
						       lon : storeDeparture.get().geometry.coordinates[0]
						   };
					       }
					       if (storeArrival.get() === null) {
						   request.end_place = {
					       	       display_name: 'Current Location',
						       place_id : storeArrival.getCustom().id,
						       lat : storeArrival.getCustom().lat,
						       lon : storeArrival.getCustom().lon
						   }
					       } else {
						   request.end_place = {
					       	       display_name: storeArrival.get().display_name,
						       place_id : storeArrival.get().place_id,
						       lat : storeArrival.get().geometry.coordinates[1],
						       lon : storeArrival.get().geometry.coordinates[0]
						   }
					       }

					       $http.post(storeConfig.get().dita_server+'newplan', request).
						   then(function(data) {
						       visitPlan.setPlan(data.data); //data is VisitPlan
						       passData.set(data.data);
						       console.log(data.data);
						       currentView.set('Visiting Plan');
						       $location.path('plan');
						   }, function(data) {
						       //error
						       console.log("error requesting new plan ", JSON.stringify(data));
						   });

					   };
				       }
				   }
				  ]);
