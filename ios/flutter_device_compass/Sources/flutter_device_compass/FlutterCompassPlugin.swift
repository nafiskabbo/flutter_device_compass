import CoreLocation
import CoreMotion
import Flutter
import simd
import UIKit

public class FlutterCompassPlugin: NSObject, FlutterPlugin, FlutterStreamHandler,
    CLLocationManagerDelegate
{
    private var eventSink: FlutterEventSink?
    private var location: CLLocationManager = CLLocationManager()
    private var motion: CMMotionManager = CMMotionManager()

    init(channel: FlutterEventChannel) {
        super.init()
        location.delegate = self
        location.headingFilter = 0.1
        channel.setStreamHandler(self)

        motion.deviceMotionUpdateInterval = 1.0 / 30.0
        motion.startDeviceMotionUpdates(using: .xMagneticNorthZVertical)
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterEventChannel(
            name: "hemanthraj/flutter_compass",
            binaryMessenger: registrar.messenger())
        _ = FlutterCompassPlugin(channel: channel)
    }

    public func onListen(
        withArguments arguments: Any?,
        eventSink events: @escaping FlutterEventSink
    ) -> FlutterError? {
        self.eventSink = events
        location.startUpdatingHeading()
        return nil
    }

    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        location.stopUpdatingHeading()
        return nil
    }

    public func locationManager(
        _ manager: CLLocationManager,
        didUpdateHeading newHeading: CLHeading
    ) {
        if newHeading.headingAccuracy > 0 {
            var trueHeading = newHeading.trueHeading
            if trueHeading < 0 {
                trueHeading = newHeading.magneticHeading
            }
            var headingForCameraMode = trueHeading
            if let data = self.motion.deviceMotion?.attitude {
                let r1 = double3x3(rows: [
                    simd_double3(0, 0, 1),
                    simd_double3(0, 1, 0),
                    simd_double3(-1, 0, 0),
                ])
                let r2 = double3x3(rows: [
                    simd_double3(0, -1, 0),
                    simd_double3(1, 0, 0),
                    simd_double3(0, 0, 1),
                ])
                let R = double3x3(rows: [
                    simd_double3(
                        data.rotationMatrix.m11, data.rotationMatrix.m12,
                        data.rotationMatrix.m13),
                    simd_double3(
                        data.rotationMatrix.m21, data.rotationMatrix.m22,
                        data.rotationMatrix.m23),
                    simd_double3(
                        data.rotationMatrix.m31, data.rotationMatrix.m32,
                        data.rotationMatrix.m33),
                ])
                let T = r2 * r1 * R
                let yaw = atan2(T[0, 1], T[1, 1]) + Double.pi / 2
                headingForCameraMode =
                    (yaw + Double.pi * 2).truncatingRemainder(dividingBy: Double.pi * 2)
                    * 180.0 / Double.pi
            }
            var headingForUI = trueHeading
            switch UIApplication.shared.statusBarOrientation {
            case .portrait:
                headingForUI = trueHeading
            case .portraitUpsideDown:
                headingForUI = trueHeading + 180
            case .landscapeRight:
                headingForUI = trueHeading + 90
            case .landscapeLeft:
                headingForUI = trueHeading - 90
            default:
                headingForUI = trueHeading
            }
            eventSink?([headingForUI, headingForCameraMode, newHeading.headingAccuracy])
        }
    }
}
