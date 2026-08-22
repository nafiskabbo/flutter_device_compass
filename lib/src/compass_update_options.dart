/// Tuning for compass stream update rate and battery use.
///
/// Defaults match the **balanced** profile (`headingFilter: 0.5`).
///
/// | Profile | [headingFilter] | Feel | Battery |
/// | --- | --- | --- | --- |
/// | [CompassUpdateOptions.fine] (`0.1°`) | Very smooth / chatty | Higher |
/// | [CompassUpdateOptions.balanced] (`0.5°`) | Smooth, practical default | Medium |
/// | [CompassUpdateOptions.batterySaver] (`1.0°`) | Coarser steps | Lower |
class CompassUpdateOptions {
  /// Minimum heading change in degrees before a new platform sample is useful.
  ///
  /// On **iOS** this maps to `CLLocationManager.headingFilter`.
  /// On **Android** emissions are skipped until the heading moves by at least
  /// this many degrees (shortest-arc) since the last emitted event.
  final double headingFilter;

  /// iOS `CMMotionManager.deviceMotionUpdateInterval` as samples per second.
  /// Used when sampling attitude for [CompassEvent.headingForCameraMode].
  /// Ignored on Android.
  final double motionUpdateHz;

  /// Minimum milliseconds between Flutter stream events on both platforms.
  final int updateIntervalMs;

  /// Balanced default: `0.5°` filter, 20 Hz motion, ~20 Hz emit cap.
  const CompassUpdateOptions({
    this.headingFilter = 0.5,
    this.motionUpdateHz = 20,
    this.updateIntervalMs = 50,
  });

  /// Very responsive (`0.1°`). More updates, higher battery use.
  /// Prefer for AR / camera overlays that need fine motion.
  static const fine = CompassUpdateOptions(
    headingFilter: 0.1,
    motionUpdateHz: 30,
    updateIntervalMs: 33,
  );

  /// Default balanced profile (`0.5°`). Good for most compass UIs.
  static const balanced = CompassUpdateOptions();

  /// Coarser updates (`1.0°`). Better battery, slightly steppier needle.
  static const batterySaver = CompassUpdateOptions(
    headingFilter: 1.0,
    motionUpdateHz: 15,
    updateIntervalMs: 66,
  );

  Map<String, dynamic> toMap() => <String, dynamic>{
        'headingFilter': headingFilter,
        'motionUpdateHz': motionUpdateHz,
        'updateIntervalMs': updateIntervalMs,
      };

  @override
  bool operator ==(Object other) {
    return other is CompassUpdateOptions &&
        other.headingFilter == headingFilter &&
        other.motionUpdateHz == motionUpdateHz &&
        other.updateIntervalMs == updateIntervalMs;
  }

  @override
  int get hashCode =>
      Object.hash(headingFilter, motionUpdateHz, updateIntervalMs);

  @override
  String toString() =>
      'CompassUpdateOptions(headingFilter: $headingFilter, '
      'motionUpdateHz: $motionUpdateHz, updateIntervalMs: $updateIntervalMs)';
}
