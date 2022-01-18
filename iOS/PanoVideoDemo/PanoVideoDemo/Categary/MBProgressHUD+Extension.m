//
//  MBProgressHUD+Extension.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "MBProgressHUD+Extension.h"

@implementation MBProgressHUD (Extension)

+ (MBProgressHUD *)showMessage:(NSString *)message addedToView:(UIView *)view duration:(NSTimeInterval)duration {
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view animated:true];
    hud.detailsLabel.text = message ? message:@"加载中.....";
    hud.detailsLabel.font =[UIFont systemFontOfSize:15];
    hud.removeFromSuperViewOnHide = YES;
    hud.mode = MBProgressHUDModeText;
    if (duration > 0) {
        [hud hideAnimated:true afterDelay:duration];
    }
    return hud;
}

+ (MBProgressHUD *)showIndicatorViewWithMessage:(NSString *)message
                         addedToView:(UIView *)view
                            duration:(NSTimeInterval)duration {
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view animated:true];
    hud.detailsLabel.text = message;
    hud.detailsLabel.font =[UIFont systemFontOfSize:15];
    hud.removeFromSuperViewOnHide = YES;
    hud.mode = MBProgressHUDModeIndeterminate;
    if (duration > 0) {
        [hud hideAnimated:true afterDelay:duration];
    }
    return hud;
}
@end
