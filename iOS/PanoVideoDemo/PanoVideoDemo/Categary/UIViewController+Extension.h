//
//  UIViewController+Extension.h
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD+Extension.h"

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (Extension)

- (UIViewController*)topViewController;

- (MBProgressHUD *)showMessage:(NSString *)message
                      duration:(NSTimeInterval)duration;

- (MBProgressHUD *)showMessage:(NSString *)message;
@end

NS_ASSUME_NONNULL_END
