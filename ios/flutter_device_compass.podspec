#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_device_compass.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_device_compass'
  s.version          = '2.1.0'
  s.summary          = 'A Flutter compass using device sensors (0-360°, north = 0).'
  s.description      = <<-DESC
A Flutter compass plugin using device sensors. Heading varies from 0-360, 0 being north.
                       DESC
  s.homepage         = 'https://github.com/nafiskabbo/flutter_device_compass'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Nafis Kabbo' => 'nafiskabbo30@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'flutter_device_compass/Sources/flutter_device_compass/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
  }
  s.swift_version = '5.0'
end
