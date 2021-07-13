//
//  PanoAvatorUtil.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoAvatorUtil.h"
#import "UIColor+Extension.h"

@implementation PanoAvatorUtil

+(UIColor *)getRandomColor:(NSString *)name {
    int randomIndex = ([name hash] % 10);
    NSArray<NSString *>* colors = @[@"#2EB2B5",@"#5BB660",@"#FFC700",@"#FC9A2D",@"#BB8AD2",
                                    @"#EBA7CD",@"#47D1DE",@"#47A3DE",@"#F18162",@"#FFA577"];
    return [UIColor pano_colorWithHexString:colors[randomIndex]];
}

+(NSString *)getAvatarName:(NSString *)name {
    NSString *strName = [NSString stringWithFormat:@"%@",name];
    if (strName.length >=1) {
        return [strName substringToIndex:1];
    }
    return name;
}

+ (UIImage* )avatorView:(NSString *) name width:(CGFloat)width  {
    NSString *strName = [PanoAvatorUtil getAvatarName:name];
    UIButton *button  = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setTitle:strName forState:UIControlStateNormal];
    [button setTitle:strName forState:UIControlStateHighlighted];
    [button setTitleColor: [UIColor whiteColor] forState:UIControlStateNormal];
    [button setTitleColor: [UIColor whiteColor] forState:UIControlStateHighlighted];
    [button.titleLabel setFont:[UIFont fontWithName:@"Helvetica-Bold" size:15]];
    [button setBackgroundColor:[PanoAvatorUtil getRandomColor:name]];
    [button setClipsToBounds:YES];
    [button setEnabled:NO];
    [button setFrame:CGRectMake(0, 0, width, width)];
    button.layer.cornerRadius = 3;
    
    UIGraphicsBeginImageContextWithOptions(button.bounds.size, YES, 0.0);
    [button drawViewHierarchyInRect:button.bounds afterScreenUpdates:NO];
    UIImage* img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return img;
}

@end
