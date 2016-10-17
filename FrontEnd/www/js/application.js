'use strict';

angular
    .module(ApplicationConfiguration.applicationModuleName, ApplicationConfiguration.applicationModuleVendorDependencies);

angular
    .module(ApplicationConfiguration.applicationModuleName)
    .config(['$locationProvider',
        function($locationProvider) {
            $locationProvider.hashPrefix('!');
        }
    ]);

//Then define the init function for starting up the application
angular
    .element(document)
    .ready(function() {
        if (window.location.hash === '#_=_') {
            window.location.hash = '#!';
        }
        angular
            .bootstrap(document,
                [ApplicationConfiguration.applicationModuleName]);
    });


    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeConfig', function() {

      var config = null;

      return {
        set: function(data) {
          config = data;
        },
        get: function() {
          return config;
        }
      }
    });


    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeUser', function() {
      var user = null;
      var token = null

      return {
        setUser: function(item) {
          user = item;
        },
        setToken: function(item) {
          token = item;
        },
        getUser: function() {
          return user;
        },
        getToken: function() {
          return token;
        }
      }
    });


    angular.module(ApplicationConfiguration.applicationModuleName).factory('visitPlan', function() {
      var plan = {
        date            : "",
        user            : "",
        departure       : {},
        arrival         : {},
        departure_time  : "",
        arrival_time    : "",
        to_visit        : new Array(),
        visited         : new Array(),
        crowding        : 0.0,
        hash            : 0
      };


      return {
        setPlan: function(item) {
          plan = item;
        },
        setDate: function(item) {
          plan.date = item;
        },
        setUser: function(item) {
          plan.user = item;
        },
        setDeparture: function(item) {
          plan.departure = item;
        },
        setArrival: function(item) {
          plan.arrival = item;
        },
        setDepartureTime: function(item) {
          plan.departure_time = item;
        },
        setArrivalTime: function(item) {
          plan.arrival_time = item;
        },
        setToVisit: function(item) {
          plan.to_visit = item;
        },
        setVisited: function(item) {
          plan.visited = item;
        },
        removeToVisit: function(item) {
          plan.to_visit.pop(item);
        },
        addVisited: function(item) {
          plan.visited.push(item);
        },
        setCrowding: function(item) {
          plan.crowding = item;
        },
        getPlan: function() {
          return plan;
        },
        getDate: function() {
          return plan.date;
        },
        getUser: function() {
          return plan.user;
        },
        getDeparture: function() {
          return plan.departure;
        },
        getArrival: function() {
          return plan.arrival;
        },
        getDepartureTime: function() {
          return plan.departure_time;
        },
        getArrivalTime: function() {
          return plan.arrival_time;
        },
        getToVisit: function() {
          return plan.to_visit;
        },
        getVisited: function() {
          return plan.visited;
        },
        getCrowding: function() {
          return plan.crowding;
        },
        getHash: function() {
          return plan.hash;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('currentDeparture', function() {
      var departure = {};

      return {
        set: function(obj, time) {
          departure.activity = obj;
          departure.time = time;
        },
        get: function() {
          return departure;
        },
        reset: function() {
          departure = {};
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('currentDestination', function() {
      var destination = {};

      return {
        set: function(obj, time) {
          destination.activity = obj;
          destination.time = time;
        },
        get: function() {
          return destination;
        },
        reset: function() {
          destination = {};
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('loadedPOIs', function() {
      var loaded = false;

      return {
        set: function(bool) {
          loaded = bool;
        },
        get: function() {
          return loaded;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('storePOIs', function() {
      var POIS = {
        hotels : [],
        attractions : [],
        monuments : [],
        museums : [],
        restaurants : [],
        parks : [],
        historical : [],
        religious : []
      };

      return {
        addHotel: function(item) {
          POIS.hotels.push(item);
        },
        getHotels: function() {
          return POIS.hotels;
        },
        setHotels: function(items) {
          POIS.hotels = items;
        },
        addAttraction: function(item) {
          POIS.attractions.push(item);
        },
        getAttractions: function() {
          return POIS.attractions;
        },
        addMonument: function(item) {
          POIS.monuments.push(item);
        },
        getMonuments: function() {
          return POIS.monuments;
        },
        addMuseum: function(item) {
          POIS.museums.push(item);
        },
        getMuseums: function() {
          return POIS.museums;
        },
        addRestaurant: function(item) {
          POIS.restaurants.push(item);
        },
        getRestaurants: function() {
          return POIS.restaurants;
        },
        addPark: function(item) {
          POIS.parks.push(item);
        },
        getParks: function() {
          return POIS.parks;
        },
        addHistorical: function(item) {
          POIS.historical.push(item);
        },
        getHistorical: function() {
          return POIS.historical;
        },
        addReligious: function(item) {
          POIS.religious.push(item);
        },
        getReligious: function() {
          return POIS.religious;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('currentView', function() {

      var current = null;

      return {
        set: function(view) {
          current = view;
        },
        get: function() {
          return current;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('passData', function() {

      var savedData = [];

      return {
        set: function(data) {
          savedData = data;
        },
        get: function() {
          return savedData;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeTime', function() {

      var time = "00:00";

      return {
        set: function(data) {
          time = data;
        },
        get: function() {
          return time;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeDeparture', function() {

      var departure = null;

      var custom = {id : "0", lat : 0.0, lon : 0.0};

      return {
        set: function(item) {
          departure = item;
        },
        get: function() {
          return departure;
        },
        setCustom: function (val_lat, val_lng) {
          custom.lat = val_lat;
          custom.lon = val_lng;
          custom.id = "0";
        },
        initCustom: function () {
          custom = {id : "1", lat : 0.0, lon : 0.0};
        },
        getCustom: function() {
          return custom;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeSlider', function() {

      var value = 0;

      return {
        set: function(item) {
          value = item;
        },
        get: function() {
          return value;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('storeArrival', function() {

      var departure = null;

      var custom = {id : "0", lat : 0.0, lon : 0.0};

      return {
        set: function(item) {
          departure = item;
        },
        get: function() {
          return departure;
        },
        setCustom: function (val_lat, val_lng) {
          custom.lat = val_lat;
          custom.lon = val_lng;
          custom.id = "0";
        },
        initCustom: function () {
          custom = {id : "1", lat : 0.0, lon : 0.0};
        },
        getCustom: function() {
          return custom;
        }
      }
    });

    angular.module(ApplicationConfiguration.applicationModuleName).factory('selectedPOIs', function() {

      var savedPois = [];
      var attractions = [];
      var monuments = [];
      var museums = [];
      var parks = [];
      var historical_sites = [];
      var religious_sites = [];

      return {
        add: function(poi, category) {
          savedPois.push(poi);
          switch(category) {
            case "attractions": attractions.push(poi); break;
            case "monuments" : monuments.push(poi); break;
            case "museums" : museums.push(poi); break;
            case "parks" : parks.push(poi); break;
            case "historical_sites" : historical_sites.push(poi); break;
            case "religious_sites" : religious_sites.push(poi); break;
            default : break;
          }
        },
        remove: function(poi, category) {
          var to_rem = savedPois.indexOf(poi);
          savedPois.splice(to_rem,1);
          switch(category) {
            case "attractions": to_rem = attractions.indexOf(poi); attractions.splice(to_rem,1); break;
            case "monuments" : monuments.indexOf(poi); monuments.splice(to_rem,1); break;
            case "museums" : museums.indexOf(poi); museums.splice(to_rem,1); break;
            case "parks" : parks.indexOf(poi); parks.splice(to_rem,1); break;
            case "historical_sites" : historical_sites.indexOf(poi); historical_sites.splice(to_rem,1); break;
            case "religious_sites" : religious_sites.indexOf(poi); religious_sites.splice(to_rem,1); break;
            default : break;
          }
        },
        reset: function() {
          savedPois = [];
          attractions = [];
          monuments = [];
          museums = [];
          parks = [];
          historical_sites = [];
          religious_sites = [];
        },
        get: function(){
          return savedPois;
        },
        getAttractions: function(){
          return attractions;
        },
        getMonuments: function(){
          return monuments;
        },
        getMuseums: function(){
          return museums;
        },
        getParks: function(){
          return parks;
        },
        getHistorical_sites: function(){
          return historical_sites;
        },
        getReligious_sites: function(){
          return religious_sites;
        }
      }
    });

  angular.module(ApplicationConfiguration.applicationModuleName).factory('city', function() {
    var city;
    return {
      set: function(item) {
        city = item;
      },
      get: function() {
        return city;
      }
    }

  });
