import 'package:flutter/services.dart';
import 'package:flutter_device_compass/flutter_device_compass.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('CompassEvent', () {
    test('parses full double list correctly', () {
      final event = CompassEvent.fromList([90.0, 45.0, 15.0]);
      expect(event.heading, 90.0);
      expect(event.headingForCameraMode, 45.0);
      expect(event.accuracy, 15.0);
    });

    test('parses integer values into doubles safely without type error', () {
      final event = CompassEvent.fromList([180, 90, 25]);
      expect(event.heading, 180.0);
      expect(event.headingForCameraMode, 90.0);
      expect(event.accuracy, 25.0);
    });

    test('treats -1.0 and -1 int accuracy as null (unreliable sensor)', () {
      final eventDouble = CompassEvent.fromList([0.0, 0.0, -1.0]);
      expect(eventDouble.accuracy, isNull);

      final eventInt = CompassEvent.fromList([0, 0, -1]);
      expect(eventInt.accuracy, isNull);
    });

    test('preserves 0.0 accuracy as valid non-null deviation', () {
      final event = CompassEvent.fromList([120.0, 60.0, 0.0]);
      expect(event.accuracy, 0.0);
    });

    test('handles null input gracefully', () {
      final event = CompassEvent.fromList(null);
      expect(event.heading, isNull);
      expect(event.headingForCameraMode, isNull);
      expect(event.accuracy, isNull);
    });

    test('handles empty list without throwing RangeError', () {
      final event = CompassEvent.fromList([]);
      expect(event.heading, isNull);
      expect(event.headingForCameraMode, isNull);
      expect(event.accuracy, isNull);
    });

    test('handles partial lists without throwing RangeError', () {
      final eventSingle = CompassEvent.fromList([270.5]);
      expect(eventSingle.heading, 270.5);
      expect(eventSingle.headingForCameraMode, isNull);
      expect(eventSingle.accuracy, isNull);

      final eventPair = CompassEvent.fromList([270.5, 135.0]);
      expect(eventPair.heading, 270.5);
      expect(eventPair.headingForCameraMode, 135.0);
      expect(eventPair.accuracy, isNull);
    });

    test('handles list containing null elements', () {
      final event = CompassEvent.fromList([null, null, null]);
      expect(event.heading, isNull);
      expect(event.headingForCameraMode, isNull);
      expect(event.accuracy, isNull);
    });

    test('toString formats all fields accurately', () {
      final event = CompassEvent.fromList([90.0, 45.0, 15.0]);
      final str = event.toString();
      expect(str, contains('heading: 90.0'));
      expect(str, contains('headingForCameraMode: 45.0'));
      expect(str, contains('accuracy: 15.0'));
    });
  });

  group('FlutterCompass', () {
    test('factory constructor returns singleton instance', () {
      final instance1 = FlutterCompass();
      final instance2 = FlutterCompass();
      expect(identical(instance1, instance2), isTrue);
    });

    test('events stream yields parsed CompassEvent from EventChannel', () async {
      const channelName = 'hemanthraj/flutter_compass';
      const standardCodec = StandardMethodCodec();
      Object? listenArgs = 'unset';

      FlutterCompass.resetEvents();

      // Mock EventChannel binary messenger responses
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(
        const MethodChannel(channelName),
        (MethodCall methodCall) async {
          if (methodCall.method == 'listen') {
            listenArgs = methodCall.arguments;
            // Send mock compass event through the event channel
            final ByteData eventData =
                standardCodec.encodeSuccessEnvelope([180.0, 90.0, 15.0]);
            await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
                .handlePlatformMessage(
              channelName,
              eventData,
              (ByteData? data) {},
            );
            return null;
          } else if (methodCall.method == 'cancel') {
            return null;
          }
          return null;
        },
      );

      final event = await FlutterCompass.events!.first;
      expect(event.heading, 180.0);
      expect(event.headingForCameraMode, 90.0);
      expect(event.accuracy, 15.0);
      // Default path stays backward compatible: no listen arguments.
      expect(listenArgs, isNull);
    });

    test('eventsFor passes fine preset options on listen', () async {
      const channelName = 'hemanthraj/flutter_compass';
      Map<dynamic, dynamic>? listenArgs;

      FlutterCompass.resetEvents();

      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(
        const MethodChannel(channelName),
        (MethodCall methodCall) async {
          if (methodCall.method == 'listen') {
            listenArgs = methodCall.arguments as Map<dynamic, dynamic>?;
            return null;
          } else if (methodCall.method == 'cancel') {
            return null;
          }
          return null;
        },
      );

      // Subscribe so listen is invoked.
      final sub = FlutterCompass.eventsFor(CompassUpdateOptions.fine)!.listen((_) {});
      await Future<void>.delayed(Duration.zero);
      expect(listenArgs?['headingFilter'], 0.1);
      expect(listenArgs?['motionUpdateHz'], 30);
      expect(listenArgs?['updateIntervalMs'], 33);
      await sub.cancel();
    });

    test('legacy events getter remains usable without options API', () {
      FlutterCompass.resetEvents();
      expect(FlutterCompass.events, isNotNull);
      expect(FlutterCompass.events, same(FlutterCompass.eventsFor()));
      expect(
        FlutterCompass.events,
        same(FlutterCompass.eventsFor(CompassUpdateOptions.balanced)),
      );
    });

    test('CompassUpdateOptions presets match documented defaults', () {
      expect(CompassUpdateOptions.balanced.headingFilter, 0.5);
      expect(CompassUpdateOptions.fine.headingFilter, 0.1);
      expect(CompassUpdateOptions.batterySaver.headingFilter, 1.0);
      expect(CompassUpdateOptions.balanced, const CompassUpdateOptions());
    });

    test('hasSensors and isSupported return true when platform reports available',
        () async {
      const methodsChannelName = 'hemanthraj/flutter_compass/methods';
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(
        const MethodChannel(methodsChannelName),
        (MethodCall methodCall) async {
          if (methodCall.method == 'hasSensors') {
            return true;
          }
          return null;
        },
      );

      expect(await FlutterCompass.hasSensors, isTrue);
      expect(await FlutterCompass.isSupported, isTrue);
    });

    test(
        'hasSensors and isSupported return false when platform reports unavailable',
        () async {
      const methodsChannelName = 'hemanthraj/flutter_compass/methods';
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(
        const MethodChannel(methodsChannelName),
        (MethodCall methodCall) async {
          if (methodCall.method == 'hasSensors') {
            return false;
          }
          return null;
        },
      );

      expect(await FlutterCompass.hasSensors, isFalse);
      expect(await FlutterCompass.isSupported, isFalse);
    });
  });
}


