import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'compass_event.dart';
import 'compass_update_options.dart';

/// [FlutterCompass] is a singleton class that provides access to compass events.
/// The heading varies from 0-360, 0 being north.
class FlutterCompass {
  static final FlutterCompass _instance = FlutterCompass._();

  factory FlutterCompass() {
    return _instance;
  }

  FlutterCompass._();

  static const EventChannel _compassChannel = EventChannel(
    'hemanthraj/flutter_compass',
  );
  static const MethodChannel _methodChannel = MethodChannel(
    'hemanthraj/flutter_compass/methods',
  );
  static Stream<CompassEvent>? _stream;
  static CompassUpdateOptions? _streamOptions;

  /// Checks if the device has compass/heading sensors available.
  /// Returns `true` if heading sensors are available on the device, `false` otherwise.
  static Future<bool?> get hasSensors async {
    if (kIsWeb) {
      return false;
    }
    return await _methodChannel.invokeMethod<bool>('hasSensors');
  }

  /// Alias for [hasSensors]. Checks if compass functionality is supported.
  static Future<bool?> get isSupported => hasSensors;

  /// Compass stream using platform defaults (same call site as before: no args).
  ///
  /// Equivalent to [eventsFor] with [CompassUpdateOptions.balanced].
  /// Existing apps that only use this getter do not need code changes.
  static Stream<CompassEvent>? get events => eventsFor();

  /// Compass stream with optional developer-tunable update options.
  ///
  /// This is an **additive** API. Prefer presets: [CompassUpdateOptions.fine],
  /// [CompassUpdateOptions.balanced], [CompassUpdateOptions.batterySaver].
  ///
  /// If you change [options] after listening, cancel existing subscriptions
  /// first (or call [resetEvents]) so the platform restarts with the new values.
  static Stream<CompassEvent>? eventsFor([
    CompassUpdateOptions options = const CompassUpdateOptions(),
  ]) {
    if (kIsWeb) {
      return Stream.empty();
    }
    if (_stream == null || _streamOptions != options) {
      _streamOptions = options;
      // Default/balanced uses null listen args (same as ≤2.1.1). Custom
      // profiles pass a map; native falls back to balanced when args are null.
      final Object? channelArgs =
          options == CompassUpdateOptions.balanced ? null : options.toMap();
      _stream = _compassChannel.receiveBroadcastStream(channelArgs).map(
            (dynamic data) => CompassEvent.fromList(
              data as List<dynamic>?,
            ),
          );
    }
    return _stream;
  }

  /// Drops the cached stream so the next [events] / [eventsFor] call can apply
  /// new [CompassUpdateOptions].
  static void resetEvents() {
    _stream = null;
    _streamOptions = null;
  }
}
