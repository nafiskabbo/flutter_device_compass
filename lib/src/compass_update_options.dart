/// Tuning for compass stream update rate and battery use.
///
/// Defaults match the historical / **fine** profile (`headingFilter: 0.1`),
/// which is what apps used before optional presets existed.
///
/// | Profile | [headingFilter] | Feel | Battery |
/// | --- | --- | --- | --- |
/// | [CompassUpdateOptions.fine] (`0.1°`, **default**) | Very smooth / chatty | Higher |
/// | [CompassUpdateOptions.balanced] (`0.5°`) | Smooth, fewer callbacks | Medium |
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

  /// Historical default: `0.1°` filter, 30 Hz motion, ~30 Hz emit cap.
  const CompassUpdateOptions({
    this.headingFilter = 0.1,
    this.motionUpdateHz = 30,
    this.updateIntervalMs = 33,
  });

  /// Very responsive (`0.1°`) — same as the constructor defaults.
  static const fine = CompassUpdateOptions();

  /// Fewer updates (`0.5°`). Good when Fine feels too chatty.
  static const balanced = CompassUpdateOptions(
    headingFilter: 0.5,
    motionUpdateHz: 20,
    updateIntervalMs: 50,
  );

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
