//
//  UIImage+Extension.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIImage (Extension)

+ (UIImage *)pano_imageWithColor:(UIColor *)color size:(CGSize)size;

+ (UIImage *)pano_imageWithColor:(UIColor *)color;

@end

NS_ASSUME_NONNULL_END
