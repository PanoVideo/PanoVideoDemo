//
//  YBPopupMenuPath.h
//  YBPopupMenu
//
//  
//  Copyright © 2017年 lyb. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, TCPopupViewArrowDirection) {
    YBPopupMenuArrowDirectionTop = 0,  //箭头朝上
    YBPopupMenuArrowDirectionBottom,   //箭头朝下
    YBPopupMenuArrowDirectionLeft,     //箭头朝左
    YBPopupMenuArrowDirectionRight,    //箭头朝右
    YBPopupMenuArrowDirectionNone      //没有箭头
};

typedef void(^DrawWillCompletion)(UIBezierPath *bezierPath, CGContextRef context);

@interface TCPopupViewPath : NSObject

+ (CAShapeLayer *)yb_maskLayerWithRect:(CGRect)rect
                            rectCorner:(UIRectCorner)rectCorner
                          cornerRadius:(CGFloat)cornerRadius
                            arrowWidth:(CGFloat)arrowWidth
                           arrowHeight:(CGFloat)arrowHeight
                         arrowPosition:(CGFloat)arrowPosition
                        arrowDirection:(TCPopupViewArrowDirection)arrowDirection;

+ (UIBezierPath *)yb_bezierPathWithRect:(CGRect)rect
                             rectCorner:(UIRectCorner)rectCorner
                           cornerRadius:(CGFloat)cornerRadius
                            borderWidth:(CGFloat)borderWidth
                            borderColor:(UIColor *)borderColor
                        backgroundColor:(UIColor *)backgroundColor
                             arrowWidth:(CGFloat)arrowWidth
                            arrowHeight:(CGFloat)arrowHeight
                          arrowPosition:(CGFloat)arrowPosition
                         arrowDirection:(TCPopupViewArrowDirection)arrowDirection;

+ (UIBezierPath *)yb_bezierPathWithRect:(CGRect)rect
                             rectCorner:(UIRectCorner)rectCorner
                           cornerRadius:(CGFloat)cornerRadius
                            borderWidth:(CGFloat)borderWidth
                            borderColor:(UIColor *)borderColor
                        backgroundColor:(UIColor *)backgroundColor
                             arrowWidth:(CGFloat)arrowWidth
                            arrowHeight:(CGFloat)arrowHeight
                          arrowPosition:(CGFloat)arrowPosition
                         arrowDirection:(TCPopupViewArrowDirection)arrowDirection
                             completion:(DrawWillCompletion)completion;
@end
