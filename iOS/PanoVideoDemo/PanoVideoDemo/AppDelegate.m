//
//  AppDelegate.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "AppDelegate.h"
#import "UIViewController+Extension.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    return YES;
}


#pragma mark - UISceneSession lifecycle

- (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
    UIViewController *vc = [self topViewController];
    if (!vc) {
        return UIInterfaceOrientationMaskAllButUpsideDown;
    }
    return [vc supportedInterfaceOrientations];
}

- (UIViewController*)topViewController {
    return [[UIApplication sharedApplication].keyWindow.rootViewController topViewController];
}
@end
