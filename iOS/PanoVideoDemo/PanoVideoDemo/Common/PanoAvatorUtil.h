//
//  PanoAvatorUtil.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface PanoAvatorUtil : NSObject

+ (UIImage *)avatorView:(NSString *) name width:(CGFloat)width;

+ (UIColor *)getRandomColor:(NSString *)name;

+ (NSString *)getAvatarName:(NSString *)name;
@end

NS_ASSUME_NONNULL_END
