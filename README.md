![AirMap: The Airspace Platform for Developers](AirMap.png)
[![Bintray](https://img.shields.io/bintray/v/airmapio/maven/com.airmap.airmapsdk.svg)](http://jcenter.bintray.com/com/airmap/airmapsdk/airmapsdk/)
[![license](https://img.shields.io/github/license/airmap/AirMapSDK-Android.svg)](https://github.com/airmap/AirMapSDK-Android/blob/master/LICENSE)

Create Flights, Send Telemetry Data, Get Realtime Traffic Alerts.

## Requirements
* Minimum Andorid SDK Level 18 or higher
* Contextual Airspace (Rules API, Advisory API, and Flight Plan API) is currently in developer PREVIEW for testing and is subject to change. Contact us for more information.

### Sign up for an [AirMap Developer Account.](https://dashboard.airmap.io/developer/)

 [https://dashboard.airmap.io/developer](https://dashboard.airmap.io/developer)
 
 
### Read Getting Started Guide
[https://developers.airmap.com/v2.1/docs/getting-started-with-airmap](https://developers.airmap.com/v2.1/docs/getting-started-with-airmap)

## Setup

Start by adding the Android SDK to your project:

* Add 
```groovy
implementation 'com.airmap.sdk:airmapsdk:2.0.0-beta.6'
``` 

to your module level `build.gradle` file

* Add 
```groovy
maven { url "https://jitpack.io" }
``` 

to your application-level `build.gradle` file under the `allprojects.repositories` block

### Initalizing The SDK

Simply add this line in your Application or Activity's `onCreate`

```java
AirMap.init(MainActivity.this);
```

### Documentation
Visit [https://developers.airmap.com/](https://developers.airmap.com/) for the full documentation

# License
See [LICENSE](https://raw.githubusercontent.com/airmap/AirMapSDK-Android/master/LICENSE) for details.
