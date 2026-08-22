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
  static bool _streamPassedArgs = false;

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

  /// Compass stream using the legacy listen path (null platform arguments).
  ///
  /// Same call site as ≤2.1.1 — **no breaking change**. Platform uses built-in
  /// fine (`0.1°`) defaults when arguments are null (historical behavior).
  static Stream<CompassEvent>? get events => eventsFor();

  /// Compass stream with optional [CompassUpdateOptions].
  ///
  /// - `eventsFor()` / [events] → `null` EventChannel arguments (legacy-compatible).
  /// - `eventsFor(CompassUpdateOptions.balanced)` (or any non-null options) → passes a
  ///   map of nullable-friendly fields; native ignores missing keys and keeps defaults.
  ///
  /// Additive API only. Cancel existing subscriptions (or call [resetEvents]) before
  /// switching options so the platform restarts cleanly.
  static Stream<CompassEvent>? eventsFor([
    CompassUpdateOptions? options,
  ]) {
    if (kIsWeb) {
      return Stream.empty();
    }

    final resolved = options ?? const CompassUpdateOptions();
    final passArgs = options != null;

    if (_stream == null ||
        _streamOptions != resolved ||
        _streamPassedArgs != passArgs) {
      _streamOptions = resolved;
      _streamPassedArgs = passArgs;
      // Null args when [options] is omitted — identical to older plugin versions.
      _stream = _compassChannel.receiveBroadcastStream(options?.toMap()).map(
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
    _streamPassedArgs = false;
  }
}
