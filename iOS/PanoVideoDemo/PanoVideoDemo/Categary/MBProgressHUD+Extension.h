//
//  MBProgressHUD+Extension.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "MBProgressHUD.h"

NS_ASSUME_NONNULL_BEGIN

@interface MBProgressHUD (Extension)

+ (void)showMessage:(NSString *)message
        addedToView:(UIView *)view
           duration:(NSTimeInterval)duration;


@end

NS_ASSUME_NONNULL_END
