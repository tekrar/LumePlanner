/*global
    angular, conf, $
*/
'use strict';

angular
    .module('core')
    .controller('SignController', ['$scope', '$http', 'storeConfig', 'passData', '$location', 'storeUser', 'currentView', 'md5',
        function($scope, $http, storeConfig, passData, $location, storeUser, currentView, md5) {
          //$sce.trustAsUrl(storeConfig.get().dita_server);
          //$sceProvider.enabled(false);
          $scope.nonav=true;
          $scope.registered = false;
          $scope.already = false;
          $scope.wrong = false;
          $scope.notfound = false;

          if(storeConfig.get() === null) {
            storeConfig.set(conf);
          }
            $scope.user = {password: "", email: ""};
            $scope.$watch('user.email', function() {
              var string = ""+$scope.user.email;
              var patt = new RegExp(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/);
              var res = (string.indexOf('.') !== -1) ? patt.test(string) : (string.length < 3) ? true : false;
              $scope.valid_email = res;
              $scope.already = false;
              $scope.notfound = false;
            });
            $scope.$watch('user.password', function() {
              var string = ""+$scope.user.password;
              var patt = new RegExp(/([a-zA-Z0-9@*#])$/);
              var res = (string.length < 7) ? true : patt.test(string);
              $scope.valid_password = res;
              $scope.wrong = false;
            });



          $scope.login = function(user_obj) {
            var hashed_user = {id: "", email: user_obj.email.toLowerCase(), password: md5.createHash(user_obj.password)};
            $scope.load = true;
            console.log(user_obj.email);
            console.log(user_obj.password);
            $http.post(storeConfig.get().dita_server+'signin', hashed_user).
              success(function(data) {
                console.log(data);
                $scope.load = false;
                if (data==='2' || data === 2) { //a visiting plan exists
                  storeUser.setUser(hashed_user);
                  passData.set(data);
                  currentView.set('Visiting Plan');
                  $( "div" ).removeClass("background");
                  $location.path('plan');
                } else if(data==='1' || data === 1) { //first login or a visiting plan doesn't exist
                  storeUser.setUser(hashed_user);
                  currentView.set('home');
                  $( "div" ).removeClass("background");
                  $location.path('home');
                } else if (data==='0' || data === 0) { //wrong password
                  $scope.wrong = true;
                  $scope.registered = false;
                  $scope.already = false;
                  $scope.notfound = false;
                } else if (data==='-1' || data === -1) { //email not found
                  $scope.notfound = true;
                  $scope.registered = false;
                  $scope.already = false;
                  $scope.wrong = false;
                }
              }).
              error(function(data) {
                //error
                $scope.load = false;
                console.log("error");
                console.log(data);
              });
          };

            $scope.signup = function(user_obj) {
		$scope.load = true;
		var hashed_user = {email: user_obj.email.toLowerCase(), password: md5.createHash(user_obj.password)};
		console.log(JSON.stringify(hashed_user));

		$http.post(storeConfig.get().dita_server+'signup', hashed_user).then(function(data) {
		    $scope.load = false;

		    if (data.data === "true") {
			$scope.registered = true;
			$scope.already = false;
			$scope.wrong = false;
			$scope.notfound = false;
		    } else if (data.data === "false") {
			$scope.registered = false;
			$scope.already = true;
			$scope.wrong = false;
			$scope.notfound = false;
		    }
		}, function(data) {
		    //error
		    $scope.load = false;
		    console.log("error");
		    console.log('new string, just to check');
		    console.log(JSON.stringify(data));
		});
            };

            $scope.load = false;
        }
    ]);
