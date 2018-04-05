##`2.0.0 Beta (12/21/17)`
* New Rulesets API
* New Flight Plan API
* Update Advisory API
* Updated Mapbox to 5.3.2
* New AirMapMapView (beta)

##`1.0.10 (8/21/17)`
* Updated Mapbox to 5.1.3

##`1.0.9 (5/12/17)`
* Updated login/signup to use auth0 Lock library instead of webview
* Bug fixes

##`1.0.8 (3/28/17)`
* CreateFlightActivity can now take theme and layers to display on map
* Added Anonymous Login

##`1.0.7 (3/14/17)`
* Updated AirMap Server Certificate for Certificate Pinning

##`1.0.6 (2/16/17)`
* Added Telemetry support
* Updated Advisory UI
* Added new airspace layers
* Resolved some crashes
* No longer need to include separate maven repo

##`1.0.5 (12/19/16)`
* Added custom flight tools for Point, Path & Polygon
* Update Advisory UI
* Added new airspace layers
* Update Status models
* Added support for Imperial/Metric measurement system
* Resolved minor issues

##`1.0.4 (11/1/16)`
* Fixed refreshAccessToken bug
* Fixed memory leak
* Updated tests
* Bug fixes

##`1.0.3 (9/9/16)`
* Package names have changed. Classes will need to be re-imported
* Traffic Alerts are now compatible with Android 7.0
* Updated Create Flight UI
* Bug fixes

##`1.0.2 (9/1/16)`
* Renamed `getAllPublicAndAuthenticatedPilotFlights` to `getAllPublicFlights`
* Getting flights now has `startsAfterNow`, `startsBeforeNow`, `endsAfterNow`, and `endsBeforeNow` parameters
* Renamed `isCurrent` to `isActive` in `AirMapFlight`
* Added 15 second timeout to requests
* Added ability to explicitly pass an `enhance` option when getting a flight
* Updated Gradle, Support Library, and Build Tools Versions
* Updated Properites Model Classes
* Added a constructor to `Coordinate` to create from a MapBox `LatLng`
* Create Flight UI updates
* Removed some unncessary Toasts
* Performance improvements
* Fixed bugs/crashes

##`1.0.1 (8/13/16)`

* Fixed a Manifest merger bug

##`1.0.0 (8/12/16)`
Initial release