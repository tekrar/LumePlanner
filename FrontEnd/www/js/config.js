'use strict';

var conf;
// Init the application configuration module for AngularJS application
var ApplicationConfiguration = (function() {
    // Init module configuration options
    var applicationModuleName = 'Venice On Your Fingers';
    var applicationModuleVendorDependencies = ['ngResource', 'ngCookies', 'ngAnimate', 'ngTouch', 'ngSanitize', 'ui.router', 'ui.bootstrap', 'ui.utils', 'leaflet-directive', 'angular-md5'];

    // Add a new vertical module
    var registerModule = function(moduleName) {
        // Create angular module
        angular
            .module(moduleName, []);

        // Add the module to the AngularJS configuration file
        angular
            .module(applicationModuleName)
            .requires
            .push(moduleName);
    };

    

    conf = {
    "dita_server" : "http://lume.morselli.unimore.it/DITA/",
    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter"
    }

    return {
        applicationModuleName: applicationModuleName,
        applicationModuleVendorDependencies: applicationModuleVendorDependencies,
        registerModule: registerModule
    };
})();
