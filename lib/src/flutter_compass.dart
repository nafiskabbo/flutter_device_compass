import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'compass_event.dart';

/// [FlutterCompass] is a singleton class that provides assess to compass events
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

  /// Provides a [Stream] of compass events that can be listened to.
  static Stream<CompassEvent>? get events {
    if (kIsWeb) {
      return Stream.empty();
    }
    _stream ??= _compassChannel.receiveBroadcastStream().map(
          (dynamic data) => CompassEvent.fromList(
            data as List<dynamic>?,
          ),
        );
    return _stream;
  }
}
