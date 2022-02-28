//
//  UIViewController+Extension.m
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "UIViewController+Extension.h"

@implementation UIViewController (Extension)

- (UIViewController*)topViewController {
    UIViewController *vc = self;
    while (vc != NULL) {
        if ([vc isKindOfClass:[UITabBarController class]]) {
            UITabBarController* tabBarController = (UITabBarController*)vc;
            vc = tabBarController.selectedViewController;
        } else if ([vc isKindOfClass:[UINavigationController class]]) {
            UINavigationController* navigationController = (UINavigationController*)vc;
            vc = navigationController.visibleViewController;
        } else if (vc.presentedViewController) {
            vc = vc.presentedViewController;
        } else {
            return vc;
        }
    }
    return vc;
}

-(UIViewController*)topViewControllerExceptAlertController {
    UIViewController *vc = self;
    while (vc != NULL) {
        if ([vc isKindOfClass:[UITabBarController class]]) {
            UITabBarController* tabBarController = (UITabBarController*)vc;
            vc = tabBarController.selectedViewController;
        } else if ([vc isKindOfClass:[UINavigationController class]]) {
            UINavigationController* navigationController = (UINavigationController*)vc;
            vc = navigationController.visibleViewController;
        } else if (vc.presentedViewController && ![vc.presentedViewController isKindOfClass:[UIAlertController class]]) {
            vc = vc.presentedViewController;
        } else {
            return vc;
        }
    }
    return vc;
}


- (MBProgressHUD *)showMessage:(NSString *)message
                      duration:(NSTimeInterval)duration {
    if (!message) {
        return nil;
    }
    return [MBProgressHUD showMessage:message addedToView:self.topViewControllerExceptAlertController.view duration:duration];
}

- (MBProgressHUD *)showMessage:(NSString *)message {
    return [self showMessage:message duration:3];
}

@end
