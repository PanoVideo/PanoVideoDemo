//
//  UIImage+IconFont.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

IB_DESIGNABLE
@interface UIImage (IconFont)

/**
 通过IconFont的形式创建图片
 * 例如 [UIImage imageWithIconFontName:@"iconfont" fontSize:100 text:@"\U0000e603" color:[UIColor greenColor]]

 @param iconFontName iconFont的name
 @param fontSize 字体的大小
 @param text 文本信息<unicode>
 @param color 颜色
 @return 创建的图片
 */
+ (UIImage *)imageWithIconFontName:(NSString *)iconFontName
                          fontSize:(CGFloat)fontSize
                              text:(NSString *)text
                             color:(UIColor *)color;

/**
 通过IconFont的形式创建图片
 @param fontSize 字体的大小
 @param text 文本信息<unicode>
 @param color 颜色
 @return 创建的图片
 */
+ (UIImage *)imageWithIconFontSize:(CGFloat)fontSize
                              text:(NSString *)text
                             color:(UIColor *)color;
@end

#define IconFontImage(s, t, c) [UIImage imageWithIconFontSize:s text:t color:c]

NS_ASSUME_NONNULL_END
