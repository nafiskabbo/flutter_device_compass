# flutter_device_compass

[![pub package](https://img.shields.io/pub/v/flutter_device_compass.svg?logo=dart)](https://pub.dev/packages/flutter_device_compass)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-blue.svg)](https://pub.dev/packages/flutter_device_compass)
[![License](https://img.shields.io/badge/license-MIT%20%2B%20GPLv3-yellow.svg)](https://github.com/nafiskabbo/flutter_device_compass/blob/master/LICENSE)

A Flutter compass plugin using device sensors. Provides real-time device heading, orientation-compensated azimuth (0°–360°, where 0° is North), camera-mode heading for AR/camera overlays, and sensor accuracy.

Community-maintained fork of [medyas/flutter_compass_v2](https://github.com/medyas/flutter_compass_v2) (lineage from [hemanthrajv/flutter_compass](https://github.com/hemanthrajv/flutter_compass)), republished as **`flutter_device_compass`** on pub.dev for ongoing maintenance and modern Flutter (3.24+ / 3.44+) & Gradle/Kotlin compatibility.

> **License note:** The package is currently **MIT + GPLv3** because some Android model/math files retain GPLv3 headers from Philipp Bobek’s Compass project. A clean-room MIT-only rewrite is planned for **2.2.0**. See [`LICENSE`](LICENSE).

---

## Features

- 🧭 **Heading Calculation:** 0° to 360°, where 0° is North.
- 📱 **Orientation-Aware:** Automatically compensates for device orientation changes (Portrait, Landscape Left, Landscape Right, Portrait Upside Down).
- 📷 **Camera Mode Heading:** Heading out the back of the device (`headingForCameraMode`), ideal for Augmented Reality (AR) or camera overlays. On Android this uses `SensorManager.remapCoordinateSystem(..., AXIS_X, AXIS_Z, ...)` per platform docs.
- 🎯 **Sensor Accuracy:** Exposes calibration and accuracy deviation in degrees (`accuracy`).
- 🔍 **Hardware Detection:** Proactively check sensor availability via `FlutterCompass.hasSensors` before mounting compass views.
- ⚡ **Robust Sensor Fallbacks:** Uses `Sensor.TYPE_ROTATION_VECTOR` on Android when available with low-pass filtered accelerometer + magnetometer fallback.
- 🔋 **Tunable updates:** `CompassUpdateOptions` presets (`fine` 0.1°, **`balanced` 0.5° default**, `batterySaver` 1.0°) control heading filter, motion rate, and emit interval.
- 🛡️ **Crash-Resistant:** Guarded against non-finite / `NaN` sensor readings and negative true headings.

---

## Getting Started

Add `flutter_device_compass` to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_device_compass: ^2.1.2
```

---

## Platform Setup

### iOS

iOS requires location permissions to deliver compass/heading updates via `CoreLocation`.

Add the following keys to your `ios/Runner/Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>This app needs access to heading and compass data to orient the map.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>This app needs access to heading and compass data to orient the map.</string>
```

> **Note:** Use a permission package like [`permission_handler`](https://pub.dev/packages/permission_handler) to request location permissions at runtime before listening to compass events on iOS.

### Android

No additional permissions are strictly required for sensor access. However, if your app utilizes fine location for geomagnetic declination or maps alongside the compass, add the following to `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

---

## Usage Examples

### 1. Basic Stream Subscription

```dart
import 'package:flutter_device_compass/flutter_device_compass.dart';

// Default balanced profile (headingFilter: 0.5°)
final subscription = FlutterCompass.events?.listen((CompassEvent event) {
  print('Heading: ${event.heading}°');
  print('Camera Mode Heading: ${event.headingForCameraMode}°');
  print('Accuracy Deviation: ±${event.accuracy}°');
});

// Or pick a preset / custom options:
// FlutterCompass.eventsFor(CompassUpdateOptions.fine)
// FlutterCompass.eventsFor(CompassUpdateOptions.batterySaver)
// FlutterCompass.eventsFor(const CompassUpdateOptions(
//   headingFilter: 0.5,
//   motionUpdateHz: 20,
//   updateIntervalMs: 50,
// ))

// Cancel when no longer needed
// subscription?.cancel();
```

#### Choosing `headingFilter` (and related knobs)

| Preset | `headingFilter` | Typical feel | Battery | Defaults applied |
| :--- | :--- | :--- | :--- | :--- |
| `CompassUpdateOptions.fine` | **0.1°** | Very smooth / chatty needle; good for AR | Higher | motion **30 Hz**, emit ≥**33 ms** |
| `CompassUpdateOptions.balanced` (**default**) | **0.5°** | Smooth enough for compass UIs | Medium | motion **20 Hz**, emit ≥**50 ms** |
| `CompassUpdateOptions.batterySaver` | **1.0°** | Slightly steppier; fewer callbacks | Lower | motion **15 Hz**, emit ≥**66 ms** |

- **iOS:** `headingFilter` → `CLLocationManager.headingFilter`; `motionUpdateHz` → CoreMotion sample rate (camera heading); `updateIntervalMs` caps Flutter events.
- **Android:** `headingFilter` is a minimum heading delta between emissions; `updateIntervalMs` sets emit throttle and sensor delay hint; `motionUpdateHz` is ignored.
- Call `FlutterCompass.resetEvents()` (or cancel listeners) before switching options so the platform restarts with the new map.

---

### 2. Building a Rotating Compass UI (`StreamBuilder`)

```dart
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:flutter_device_compass/flutter_device_compass.dart';

class CompassWidget extends StatelessWidget {
  const CompassWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<CompassEvent>(
      stream: FlutterCompass.events,
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return Center(child: Text('Error: ${snapshot.error}'));
        }

        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        final heading = snapshot.data?.heading;

        // On devices without compass sensors, heading will be null
        if (heading == null) {
          return const Center(
            child: Text('Device does not have compass sensors'),
          );
        }

        return Center(
          child: Transform.rotate(
            angle: (heading * (math.pi / 180) * -1),
            child: Image.asset(
              'assets/compass.png',
              width: 250,
              height: 250,
            ),
          ),
        );
      },
    );
  }
}
```

---

### 3. Check Sensor Availability

Before initiating compass-dependent features, you can verify if the device hardware supports compass sensors:

```dart
final bool? isSupported = await FlutterCompass.hasSensors;

if (isSupported == true) {
  print('Compass sensor is available on this device');
} else {
  print('Device does not support compass/heading sensors');
}
```

---

### 4. Read Current Heading Once

If you only need a single heading snapshot (e.g. when capturing a photo or logging location):

```dart
Future<void> logCurrentHeading() async {
  final CompassEvent event = await FlutterCompass.events!.first;
  print('Current heading is: ${event.heading}°');
}
```

---

## `CompassEvent` Reference

| Property | Type | Description |
| :--- | :--- | :--- |
| `heading` | `double?` | The device heading in degrees around its Z-axis (pointing direction of the top of the phone). Range: `0.0` to `360.0`, where `0.0` is North. `null` if sensor is unavailable. |
| `headingForCameraMode` | `double?` | The heading in degrees out the back of the phone (rear camera / AR). Ideal for camera overlays. |
| `accuracy` | `double?` | Estimated deviation error in degrees (±). `null` if uncalibrated or unavailable. |

## Releases

GitHub Releases / tags track pub.dev versions. Check [Releases](https://github.com/nafiskabbo/flutter_device_compass/releases) for changelogs per version.

## Roadmap

- **2.2.0** — Clean-room Android math rewrite under MIT (remove GPLv3 sources)

## Maintainer

Maintained with ❤️ by [Nafis Kabbo](mailto:nafiskabbo30@gmail.com).

Contributions, issues, and feature requests are welcome on [GitHub](https://github.com/nafiskabbo/flutter_device_compass)!
