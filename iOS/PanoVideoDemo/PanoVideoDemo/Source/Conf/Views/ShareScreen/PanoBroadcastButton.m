//
//  PanoBroadcastButton.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoBroadcastButton.h"

@implementation PanoBroadcastButton

- (instancetype)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
#ifndef  TC_TARGET_IPHONE_SIMULATOR
        NSString *pluginPath = [NSBundle mainBundle].builtInPlugInsPath;
        NSArray *contents = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:pluginPath error:nil];
        for (NSString *content in contents) {
            if (![content hasSuffix:@".appex"]) {
                continue;
            }
            NSBundle *bundle = [NSBundle bundleWithPath:[[NSURL fileURLWithPath:pluginPath] URLByAppendingPathComponent:content].path];
            if (bundle) {
                NSString *identifier = [bundle.infoDictionary valueForKeyPath:@"NSExtension.NSExtensionPointIdentifier"];
                if ([identifier isEqualToString:@"com.apple.broadcast-services-upload"]) {
                    self.preferredExtension = bundle.bundleIdentifier;
                }
            }
        }
        self.showsMicrophoneButton = false;
        for (UIView *view in self.subviews) {
            if ([view isKindOfClass:[UIButton class]]) {
                _innerBtn = (UIButton *)view;
            }
        }
#endif
    }
    return self;
}

- (void)showBroadcastView{
    if (_innerBtn) {
        [_innerBtn sendActionsForControlEvents:UIControlEventAllEvents];
    }
}

- (void)dealloc {
    NSLog(@"-> %s", __func__);
}

@end
