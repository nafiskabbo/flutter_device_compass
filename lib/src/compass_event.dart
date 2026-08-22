class CompassEvent {
  /// The heading, in degrees, of the device around its Z
  /// axis, or where the top of the device is pointing.
  final double? heading;

  /// The heading, in degrees, of the device around its X axis, or
  /// where the back of the device is pointing.
  final double? headingForCameraMode;

  /// The deviation error, in degrees, plus or minus from the heading.
  /// NOTE: for iOS this is computed by the platform and is reliable. For
  /// Android several values are hard-coded, and the true error could be more
  /// or less than the value here.
  final double? accuracy;

  CompassEvent.fromList(List<dynamic>? data)
      : heading = (data != null && data.isNotEmpty && data[0] != null)
            ? (data[0] as num).toDouble()
            : null,
        headingForCameraMode =
            (data != null && data.length > 1 && data[1] != null)
                ? (data[1] as num).toDouble()
                : null,
        accuracy = (data != null &&
                data.length > 2 &&
                data[2] != null &&
                (data[2] as num).toDouble() != -1)
            ? (data[2] as num).toDouble()
            : null;

  @override
  String toString() {
    return 'heading: $heading\nheadingForCameraMode: $headingForCameraMode\naccuracy: $accuracy';
  }
}
