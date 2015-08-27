/**
 * Home routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('home.routes', ['yourprefix.common']);
  mod.config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/',  {templateUrl: '/assets/javascripts/home/home.html', controller:controllers.HomeCtrl})
      .when('/posts',  {templateUrl: '/assets/javascripts/home/posts.html', controller:controllers.PostCtrl})
      .when('/user/:userId',  {templateUrl: '/assets/javascripts/home/single_user.html', controller:controllers.UserCtrl})
      .otherwise( {templateUrl: '/assets/javascripts/home/notFound.html'});
  }]);
  return mod;
});
