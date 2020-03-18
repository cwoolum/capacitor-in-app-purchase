
  Pod::Spec.new do |s|
    s.name = 'CapacitorInAppPurchase'
    s.version = '0.0.1'
    s.summary = 'A plugin for making in app purchases'
    s.license = 'MIT'
    s.homepage = 'https://github.com/cwoolum/capacitor-in-app-purchase'
    s.author = 'Christopher Woolum'
    s.source = { :git => 'https://github.com/cwoolum/capacitor-in-app-purchase', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end