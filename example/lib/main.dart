import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_device_compass/flutter_device_compass.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _hasPermissions = false;
  bool? _hasSensors;
  CompassEvent? _lastRead;
  DateTime? _lastReadAt;
  CompassUpdateOptions _options = CompassUpdateOptions.balanced;

  @override
  void initState() {
    super.initState();
    _checkHardwareAndPermissions();
  }

  Future<void> _checkHardwareAndPermissions() async {
    final hasSensors = await FlutterCompass.hasSensors;
    if (mounted) {
      setState(() => _hasSensors = hasSensors);
    }
    _fetchPermissionStatus();
  }

  void _selectOptions(CompassUpdateOptions options) {
    FlutterCompass.resetEvents();
    setState(() => _options = options);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Device Compass'),
          elevation: 2,
        ),
        body: Builder(
          builder: (context) {
            if (_hasSensors == false) {
              return const Center(
                child: Padding(
                  padding: EdgeInsets.all(24.0),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.sensors_off, size: 64, color: Colors.grey),
                      SizedBox(height: 16),
                      Text(
                        'Device does not have compass sensors!',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              );
            }

            if (_hasPermissions) {
              return Column(
                children: <Widget>[
                  _buildSensorStatusCard(),
                  _buildUpdateProfilePicker(),
                  _buildManualReader(),
                  Expanded(child: _buildCompass()),
                ],
              );
            } else {
              return _buildPermissionSheet();
            }
          },
        ),
      ),
    );
  }

  Widget _buildSensorStatusCard() {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(
        color: _hasSensors == true ? Colors.green.shade50 : Colors.amber.shade50,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: _hasSensors == true ? Colors.green.shade300 : Colors.amber.shade300,
        ),
      ),
      child: Row(
        children: [
          Icon(
            _hasSensors == true ? Icons.check_circle : Icons.help_outline,
            color: _hasSensors == true ? Colors.green.shade700 : Colors.amber.shade800,
            size: 20,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              _hasSensors == true
                  ? 'Hardware Sensor: Available'
                  : 'Checking sensor hardware...',
              style: TextStyle(
                color: _hasSensors == true ? Colors.green.shade900 : Colors.amber.shade900,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildUpdateProfilePicker() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Update profile (headingFilter)',
            style: Theme.of(context).textTheme.titleSmall,
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              ChoiceChip(
                label: const Text('Fine 0.1°'),
                selected: _options == CompassUpdateOptions.fine,
                onSelected: (_) => _selectOptions(CompassUpdateOptions.fine),
              ),
              ChoiceChip(
                label: const Text('Balanced 0.5°'),
                selected: _options == CompassUpdateOptions.balanced,
                onSelected: (_) => _selectOptions(CompassUpdateOptions.balanced),
              ),
              ChoiceChip(
                label: const Text('Battery 1.0°'),
                selected: _options == CompassUpdateOptions.batterySaver,
                onSelected: (_) => _selectOptions(CompassUpdateOptions.batterySaver),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            'filter ${_options.headingFilter}° · '
            'motion ${_options.motionUpdateHz.toStringAsFixed(0)} Hz · '
            'emit ≥${_options.updateIntervalMs} ms',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey.shade700),
          ),
        ],
      ),
    );
  }

  Widget _buildManualReader() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Card(
        elevation: 1,
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Row(
            children: <Widget>[
              ElevatedButton.icon(
                onPressed: () async {
                  final CompassEvent tmp =
                      await FlutterCompass.eventsFor(_options)!.first;
                  setState(() {
                    _lastRead = tmp;
                    _lastReadAt = DateTime.now();
                  });
                },
                icon: const Icon(Icons.refresh),
                label: const Text('Read Once'),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Text(
                      _lastRead != null
                          ? 'Heading: ${_lastRead?.heading?.toStringAsFixed(1)}°\n'
                              'Camera: ${_lastRead?.headingForCameraMode?.toStringAsFixed(1)}°\n'
                              'Accuracy: ${_lastRead?.accuracy != null ? "±${_lastRead?.accuracy?.toStringAsFixed(1)}°" : "N/A"}'
                          : 'No snapshot taken yet',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                    if (_lastReadAt != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        'Read at: ${_lastReadAt!.toLocal().toIso8601String().substring(11, 19)}',
                        style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.grey),
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCompass() {
    return StreamBuilder<CompassEvent>(
      key: ValueKey(_options),
      stream: FlutterCompass.eventsFor(_options),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return Center(child: Text('Error reading heading: ${snapshot.error}'));
        }

        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        final double? direction = snapshot.data?.heading;

        if (direction == null) {
          return const Center(
            child: Text('Device does not have sensors or values unavailable!'),
          );
        }

        final double? cameraDirection = snapshot.data?.headingForCameraMode;
        final double? accuracy = snapshot.data?.accuracy;

        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              '${direction.toStringAsFixed(1)}°',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Colors.deepPurple,
                  ),
            ),
            const SizedBox(height: 4),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                if (cameraDirection != null)
                  Text(
                    'Camera: ${cameraDirection.toStringAsFixed(1)}°   ',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.grey.shade700),
                  ),
                Text(
                  'Accuracy: ${accuracy != null ? "±${accuracy.toStringAsFixed(1)}°" : "Uncalibrated"}',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.grey.shade700),
                ),
              ],
            ),
            const SizedBox(height: 24),
            Material(
              shape: const CircleBorder(),
              clipBehavior: Clip.antiAlias,
              elevation: 4.0,
              child: Container(
                padding: const EdgeInsets.all(16.0),
                alignment: Alignment.center,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                ),
                child: Transform.rotate(
                  angle: direction * (math.pi / 180) * -1,
                  child: Image.asset(
                    'assets/compass.jpg',
                    width: 260,
                    height: 260,
                  ),
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildPermissionSheet() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          const Icon(Icons.location_on, size: 48, color: Colors.deepPurple),
          const SizedBox(height: 12),
          const Text(
            'Location Permission Required',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 32.0),
            child: Text(
              'iOS and Android location permissions are needed to read compass heading data.',
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () {
              Permission.locationWhenInUse.request().then((_) {
                _fetchPermissionStatus();
              });
            },
            child: const Text('Request Permissions'),
          ),
          const SizedBox(height: 8),
          TextButton(
            onPressed: () {
              openAppSettings();
            },
            child: const Text('Open App Settings'),
          ),
        ],
      ),
    );
  }

  void _fetchPermissionStatus() {
    Permission.locationWhenInUse.status.then((status) {
      if (mounted) {
        setState(() => _hasPermissions = status == PermissionStatus.granted);
      }
    });
  }
}
