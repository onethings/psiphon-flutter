#import "PsiphonPlugin.h"
#if __has_include(<psiphon/psiphon-Swift.h>)
#import <psiphon/psiphon-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "psiphon-Swift.h"
#endif

@implementation PsiphonPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftPsiphonPlugin registerWithRegistrar:registrar];
}
@end
