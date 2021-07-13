//
//  MBProgressHUD+Extension.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "MBProgressHUD+Extension.h"

@implementation MBProgressHUD (Extension)

+ (void)showMessage:(NSString *)message addedToView:(UIView *)view duration:(NSTimeInterval)duration {
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view animated:true];
    hud.detailsLabel.text = message ? message:@"加载中.....";
    hud.detailsLabel.font =[UIFont systemFontOfSize:15];
    hud.removeFromSuperViewOnHide = YES;
    hud.mode = MBProgressHUDModeText;
    [hud hideAnimated:true afterDelay:duration];
}

@end
