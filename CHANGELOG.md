## 2.1.2

- **iOS:**
  - Replace deprecated `UIApplication.shared.statusBarOrientation` with `UIWindowScene.interfaceOrientation` for the same UI heading compensation (portrait / upside-down / landscape).
  - Reduce Core Location heading spam: `headingFilter` raised from `0.1°` to `1.0°`.
  - Lower device-motion update rate to 15 Hz (attitude is still sampled on heading events only).
  - Throttle Flutter stream emissions to ~30 Hz to align with Android.

## 2.1.1

- **Android:**
  - Fix battery drain from unnecessary magnetometer registration when `TYPE_ROTATION_VECTOR` is available; register only the sensors required for the active path.
  - Unregister any previous listener before `onListen` and unregister the whole listener on cancel/detach (avoids duplicate registrations).
  - Throttle compass stream emissions (~33 ms) to match the sensor delay hint.
  - Implement real `headingForCameraMode` via new `HeadingUtils` (`remapCoordinateSystem(..., AXIS_X, AXIS_Z, ...)` per Android AR/camera docs). Accel/mag fallback uses `getRotationMatrix` for UI + camera when possible. Philipp Bobek `MathUtils` left unchanged.
- **Publishing:**
  - Restore `.pubignore` so build artifacts, Gradle wrappers, Pods, and tooling are excluded from the pub package.
- **License / docs:**
  - Document temporary **MIT + GPLv3** dual licensing (some Android model/util files from Philipp Bobek’s Compass). A clean-room **MIT-only** rewrite is planned for **2.2.0**.
  - Update README features, version pin, roadmap (`2.1.2` iOS, `2.2.0` MIT rewrite).

## 2.1.0

- **Android:**
  - Fix fatal crash on `NaN` rotation-vector sensor samples by skipping non-finite samples before calculating azimuth ([#1](https://github.com/nafiskabbo/flutter_device_compass/pull/1) by [@mrweiland](https://github.com/mrweiland)).
  - Fix potential `NullPointerException` on `display!!` and add safe sensor manager unwrapping ([#4](https://github.com/nafiskabbo/flutter_device_compass/pull/4)).
  - Implement full local JVM Kotlin test suite for models and math utilities ([#4](https://github.com/nafiskabbo/flutter_device_compass/pull/4)).
- **iOS:**
  - Fix heading calculation by falling back to `magneticHeading` when `trueHeading < 0` ([#3](https://github.com/nafiskabbo/flutter_device_compass/pull/3) by [@aqinfh](https://github.com/aqinfh)).
  - Optimize battery consumption by strictly tying `CMMotionManager` updates to `onListen`/`onCancel` stream lifecycle.
- **Dart & API:**
  - Add `FlutterCompass.hasSensors` and `FlutterCompass.isSupported` APIs to detect hardware compass capabilities ([#4](https://github.com/nafiskabbo/flutter_device_compass/pull/4)).
  - Fix type casting for compass event data from the platform channel ([#2](https://github.com/nafiskabbo/flutter_device_compass/pull/2) by [@aqinfh](https://github.com/aqinfh)).
  - Add safe index bounds checking and `num` type flexibility to `CompassEvent.fromList` ([#4](https://github.com/nafiskabbo/flutter_device_compass/pull/4)).
  - Add comprehensive Dart unit and mock platform channel test suite ([#4](https://github.com/nafiskabbo/flutter_device_compass/pull/4)).
- **Documentation:**
  - Expand `README.md` with features overview, platform permission setup, practical `StreamBuilder`, sensor check, and one-off reading examples, plus complete API reference table.
- **Tooling & Build Configurations:**
  - Exclude build and platform directories from analyzer in `analysis_options.yaml` ([`f99fdac`](https://github.com/nafiskabbo/flutter_device_compass/commit/f99fdac2e4fccf27f86393a728c9af5dcf162c4c)).
  - Upgrade Android Gradle Plugin to 9.1.0, Kotlin to 2.4.0, and Gradle wrapper to 9.3.1.
  - Upgrade `permission_handler` to `^13.0.1` in example project.
  - Clean up legacy iOS Podfile / project configuration in example app.

## 2.0.1

- Documentation and README badge aligned with latest pub.dev release (`2.0.1`)
- No API changes from `2.0.0`

## 2.0.0

- **Package renamed** from `flutter_compass_v2` to [`flutter_device_compass`](https://pub.dev/packages/flutter_device_compass) for independent publication on pub.dev
- **Fork lineage:** community-maintained continuation of [medyas/flutter_compass_v2](https://github.com/medyas/flutter_compass_v2) (originally derived from [hemanthrajv/flutter_compass](https://github.com/hemanthrajv/flutter_compass))
- **Maintainer:** [Nafis Kabbo](mailto:nafiskabbo30@gmail.com)
- Migrates Android build to Kotlin DSL with built-in Kotlin support (Flutter 3.44+ and AGP 8/9 compatible)
- Adds iOS Swift Package Manager (SPM) support alongside CocoaPods
- Updates iOS deployment target to 13.0

## 1.2.0

- Community-maintained release on `flutter_compass_v2` (pre-rename to `flutter_device_compass`)
- Updated dependencies
- Update the Android implementation

## 1.1.0

- Updated dependencies
- Update the Android implementation

## 1.0.3

- Fix ios pod issue

## 1.0.1

- Fix ios pod issue

## 1.0.0

- Update the Android implementation

## 0.8.0

- Add guard on web (Web always returns an empty stream)
- Heading accounts for orientation on iOS
- Unregister listeners when detaching engine on Android

## 0.7.0

- Updates Android Embedding
- Resolves deprecations warnings
- New Android Implementation supporting Sensor.TYPE_ROTATION_VECTOR and Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR.
- Fixes multiple listeners on stream
- Fixes for null safety

## 0.6.1

- Flutter Compass no more depends on RxDart

## 0.6.0

- Migrate to null safety

## 0.5.1

Android:

- Remove roll from heading calculations
- Uses matrix reorientation to do a better job calculating heading "out the back of the phone"

iOS:

- Use the device motion orientation matrix to compute heading out the back of the device. This is much like what is done on Android, and yields a more stable and precise heading.

Dart:

- Add documentation to flutter_compass.dart.

## 0.5.0

**Breaking Change:** The `events` stream now gives you `CompassEvent` that consists of `heading`, `headingForCamera` and `accuracy`.

Android: Remove roll from heading calculations

## 0.4.3

Use geomagnetic rotation sensor as fallback on Android

## 0.4.2+1

Minor fix

## 0.4.2

Updated rxDart to 0.24.0

## 0.4.1

Updated README.md

## 0.4.0

**Breaking change:** Uses magnetic heading by default for iOS.

Older versions used True heading and which caused deviations.

## 0.3.7

- Sensor check added on android. `null` is returned as direction when no sensor available.

## 0.3.6

- Upgrade `rxdart` version to `0.23.1`

## 0.3.5

- Improve `README.md`

## 0.3.4

- Add `dispose` method

## 0.3.3

- Update `permission_handler` to 3.2.2
- Update `rxdart` to 0.22.3

## 0.3.2

- Android: The plugin will now remember the last read azimuth. This will be done across Isolates using a static variable. Additionally, the value is cached _within_ the isolate with the introduction of a RxDart `BehaviorSubject`. Reading the current azimuth using `await FlutterCompass.events.first` will therefore not hang anymore when the user has not moved the handset at all.
- Sample updated to cover the functional updates in Android.
- Added missing location permissions to the Android example which prevented the permission dialog from being shown.

## 0.3.1

- iOS: Remove permission request when Plugin is instantiated. Library users are responsible to request location permission by themselves.
- Request permission in the example directly.

## 0.3.0

- Replace kotlin code with simple java to reduce integration complexity

## 0.2.0

- Upgrade Android build components (Kotlin version)
  **Breaking change**. Migrate from the deprecated original Android Support Library to AndroidX. This shouldn't result in any functional changes, but it requires any Android apps using this plugin to [also migrate](https://developer.android.com/jetpack/androidx/migrate) if they're using the original support library.
- Upgrade iOS component to Swift 4.2

## 0.1.0

- Added example
- Added public api docs

## 0.0.3

- bug fixes

## 0.0.2

- Android emulator fix

## 0.0.1

- flutter compass plugin
