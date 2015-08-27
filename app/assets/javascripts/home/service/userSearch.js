define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('yourprefix.home.userSearch',[]);
  mod.factory('UserSearch', ['$resource',
		function($resource) {
			return $resource('api/users/',{},{'query': {
							method: 'GET', 
							isArray: false}});
		}
	]);
  return mod;
});
