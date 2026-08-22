import CoreLocation
import CoreMotion
import Flutter
import simd
import UIKit

public class FlutterCompassPlugin: NSObject, FlutterPlugin, FlutterStreamHandler,
    CLLocationManagerDelegate
{
    /// Default balanced profile (matches Dart [CompassUpdateOptions]).
    private static let defaultHeadingFilter: CLLocationDegrees = 0.5
    private static let defaultMotionUpdateHz: Double = 20
    private static let defaultUpdateIntervalMs: Double = 50

    private var eventSink: FlutterEventSink?
    private var location: CLLocationManager = CLLocationManager()
    private var motion: CMMotionManager = CMMotionManager()
    private var lastEmitTimestamp: TimeInterval = 0
    private var minEmitInterval: TimeInterval = defaultUpdateIntervalMs / 1000.0

    init(channel: FlutterEventChannel) {
        super.init()
        location.delegate = self
        location.headingFilter = Self.defaultHeadingFilter
        channel.setStreamHandler(self)
        motion.deviceMotionUpdateInterval = 1.0 / Self.defaultMotionUpdateHz
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let eventChannel = FlutterEventChannel(
            name: "hemanthraj/flutter_compass",
            binaryMessenger: registrar.messenger())
        let methodChannel = FlutterMethodChannel(
            name: "hemanthraj/flutter_compass/methods",
            binaryMessenger: registrar.messenger())
        let instance = FlutterCompassPlugin(channel: eventChannel)
        registrar.addMethodCallDelegate(instance, channel: methodChannel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "hasSensors":
            result(CLLocationManager.headingAvailable())
        default:
            result(FlutterMethodNotImplemented)
        }
    }

    public func onListen(
        withArguments arguments: Any?,
        eventSink events: @escaping FlutterEventSink
    ) -> FlutterError? {
        applyOptions(arguments)
        self.eventSink = events
        lastEmitTimestamp = 0
        location.startUpdatingHeading()
        motion.startDeviceMotionUpdates(using: .xMagneticNorthZVertical)
        return nil
    }

    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        location.stopUpdatingHeading()
        motion.stopDeviceMotionUpdates()
        lastEmitTimestamp = 0
        return nil
    }

    private func applyOptions(_ arguments: Any?) {
        let map = arguments as? [String: Any]

        let headingFilter =
            (map?["headingFilter"] as? NSNumber)?.doubleValue
            ?? Self.defaultHeadingFilter
        location.headingFilter = max(headingFilter, 0)

        let motionHz =
            (map?["motionUpdateHz"] as? NSNumber)?.doubleValue
            ?? Self.defaultMotionUpdateHz
        let clampedHz = max(motionHz, 1)
        motion.deviceMotionUpdateInterval = 1.0 / clampedHz

        let updateMs =
            (map?["updateIntervalMs"] as? NSNumber)?.doubleValue
            ?? Self.defaultUpdateIntervalMs
        minEmitInterval = max(updateMs, 1) / 1000.0
    }

    public func locationManager(
        _ manager: CLLocationManager,
        didUpdateHeading newHeading: CLHeading
    ) {
        guard newHeading.headingAccuracy > 0 else { return }

        let now = ProcessInfo.processInfo.systemUptime
        if lastEmitTimestamp > 0, now - lastEmitTimestamp < minEmitInterval {
            return
        }
        lastEmitTimestamp = now

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

        let headingForUI = uiHeading(from: trueHeading)
        eventSink?([headingForUI, headingForCameraMode, newHeading.headingAccuracy])
    }

    /// Same orientation compensation as before, using `UIWindowScene.interfaceOrientation`
    /// instead of deprecated `UIApplication.statusBarOrientation`.
    private func uiHeading(from trueHeading: CLLocationDirection) -> CLLocationDirection {
        switch currentInterfaceOrientation() {
        case .portrait:
            return trueHeading
        case .portraitUpsideDown:
            return trueHeading + 180
        case .landscapeRight:
            return trueHeading + 90
        case .landscapeLeft:
            return trueHeading - 90
        default:
            return trueHeading
        }
    }

    private func currentInterfaceOrientation() -> UIInterfaceOrientation {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        if let active = scenes.first(where: { $0.activationState == .foregroundActive }) {
            return active.interfaceOrientation
        }
        return scenes.first?.interfaceOrientation ?? .portrait
    }
}
