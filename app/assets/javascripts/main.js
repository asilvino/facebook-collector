// `main.js` is the file that sbt-web will use as an entry point
(function (requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named 'main.js"
    packages: ['common', 'home', 'user', 'dashboard'],
    shim: {
      'jsRoutes': {
        deps: [],
        // it's not a RequireJS module, so we have to tell it what var is returned
        exports: 'jsRoutes'
      },

      // Hopefully this all will not be necessary but can be fetched from WebJars in the future
      'fastclick': {
        deps: ['jquery'],
        exports: 'fastclick'
      },
      'demo': {
        deps: ['jquery'],
        exports: 'demo'
      },
      'underscore':{
        deps: ['jquery'],
        exports: 'underscore'
      },
      'slimscroll': {
        deps: ['jquery'],
        exports: 'slimscroll'
      },
      'timepicker':{
        deps: ['jquery'],
        exports: 'timepicker'
      },
      'daterangepicker':{
        deps: ['jquery','moment'],
        exports: 'timepicker'
      },
      'chosen': {
        deps: ['jquery'],
        exports: 'chosen'
      },
      'angular-chosen': {
        deps: ['angular','chosen'],
        exports: 'angular-chosen'
      },
      'angular-blockui': {
        deps: ['angular'],
        exports: 'angular-blockui'
      },
      'angular': {
        deps: ['jquery'],
        exports: 'angular'
      },
      'angular-route': ['angular'],
      'angular-resource': ['angular'],
      'angular-cookies': ['angular'],
      'bootstrap': ['jquery']
    },
    paths: {
      'requirejs': ['../lib/requirejs/require'],
      'underscore': ['../lib/underscorejs/underscore-min'],
      'jquery': ['../lib/jquery/jquery'],
      'angular': ['../lib/angularjs/angular'],
      'angular-chosen': ['../lib/angular-chosen/chosen'],
      'angular-resource': ['../lib/angularjs/angular-resource'],
      'angular-route': ['../lib/angularjs/angular-route'],
      'angular-cookies': ['../lib/angularjs/angular-cookies'],
      'angular-blockui': ['../lib/angular-block-ui/dist/angular-block-ui.min'],
      'bootstrap': ['../lib/bootstrap/js/bootstrap'],
      'timepicker':['../plugins/timepicker/bootstrap-timepicker.min'],
      'moment':['../plugins/daterangepicker/moment.min'],
      'daterangepicker':['../plugins/daterangepicker/daterangepicker'],
      'chosen':['../plugins/chosen/chosen.jquery.min'],
      'dataTables_bootstrap':['../plugins/datatables/dataTables.bootstrap.min'],
      'datatables':['../plugins/datatables/jquery.dataTables.min'],
      'fastclick':['../plugins/fastclick/fastclick.min'],
      'slimscroll':['../plugins/slimScroll/jquery.slimscroll.min'],
      'demo':['../dist/js/demo'],
      'jsRoutes': ['/jsroutes']
    }
  });

  requirejs.onError = function (err) {
    console.log(err);
  };

  // Load the app. This is kept minimal so it doesn't need much updating.
  require(['angular', 'angular-cookies','angular-resource','angular-route', 'jquery', 
    'bootstrap', './app','demo','fastclick','slimscroll','timepicker','daterangepicker','chosen','angular-chosen',
    'dataTables_bootstrap','datatables','angular-blockui','underscore'],
    function (angular) {
      angular.bootstrap(document, ['app']);
    }
  );
})(requirejs);
