/**
 * Main, shows the start page and provides controllers for the header and the footer.
 * This the entry module which serves as an entry point so other modules only have to include a
 * single module.
 */
define(['angular',  './routes', './controllers'], function(angular, routes, controllers) {
  'use strict';

  var mod = angular.module('yourprefix.home', ['ngRoute', 'home.routes']);
  mod.factory('UserSearch', ['$resource',
		function($resource) {
			return $resource('api/users/:id', {
							id: '@id',
							},{'query': {
									method: 'GET', 
									isArray: false
								},'get': {
									method: 'GET',
									isArray: false
								}
							});
		}
	]);
  mod.factory('Pages', ['$resource',
		function($resource) {
			return $resource('api/pages/', {
							},{'query': {
									method: 'GET', 
									isArray: true
								}
							});
		}
	]);
  mod.factory('Post', ['$resource',
		function($resource) {
			return $resource('api/posts/', {
							},{'query': {
									method: 'GET', 
									isArray: false
								}
							});
		}
	]);
  mod.factory('Page', ['$resource',
		function($resource) {
			return $resource('api/pages/',{},{'query': {
							method: 'GET', 
							isArray: true}});
		}
	]);
  mod.controller('HeaderCtrl', controllers.HeaderCtrl);
  mod.controller('FooterCtrl', controllers.FooterCtrl);
  
  return mod;
});
