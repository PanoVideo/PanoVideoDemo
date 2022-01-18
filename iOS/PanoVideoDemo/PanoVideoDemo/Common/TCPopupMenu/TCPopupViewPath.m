//
//  YBPopupMenuPath.m
//  YBPopupMenu
//
//  
//  Copyright © 2017年 lyb. All rights reserved.
//

#import "TCPopupViewPath.h"
#import "TCRectConst.h"

@implementation TCPopupViewPath

+ (CAShapeLayer *)yb_maskLayerWithRect:(CGRect)rect
                            rectCorner:(UIRectCorner)rectCorner
                          cornerRadius:(CGFloat)cornerRadius
                            arrowWidth:(CGFloat)arrowWidth
                           arrowHeight:(CGFloat)arrowHeight
                         arrowPosition:(CGFloat)arrowPosition
                        arrowDirection:(TCPopupViewArrowDirection)arrowDirection
{
    CAShapeLayer *shapeLayer = [CAShapeLayer layer];
    shapeLayer.path = [self yb_bezierPathWithRect:rect rectCorner:rectCorner cornerRadius:cornerRadius borderWidth:0 borderColor:nil backgroundColor:nil arrowWidth:arrowWidth arrowHeight:arrowHeight arrowPosition:arrowPosition arrowDirection:arrowDirection].CGPath;
    return shapeLayer;
}


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
{
    return [self yb_bezierPathWithRect:rect rectCorner:rectCorner cornerRadius:cornerRadius borderWidth:borderWidth borderColor:borderColor backgroundColor:backgroundColor arrowWidth:arrowWidth arrowHeight:arrowHeight arrowPosition:arrowPosition arrowDirection:arrowDirection completion:^(UIBezierPath *bezierPath, CGContextRef context) {
    }];
}

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
                             completion:(DrawWillCompletion)completion {
    CGContextRef context = UIGraphicsGetCurrentContext();
    UIBezierPath *bezierPath = [UIBezierPath bezierPath];
    if (borderColor) {
        [borderColor setStroke];
    }
    if (backgroundColor) {
        [backgroundColor setFill];
    }
    bezierPath.lineWidth = borderWidth;
    rect = CGRectMake(borderWidth / 2, borderWidth / 2, TCRectWidth(rect) - borderWidth, TCRectHeight(rect) - borderWidth);
    CGFloat topRightRadius = 0,topLeftRadius = 0,bottomRightRadius = 0,bottomLeftRadius = 0;
    CGPoint topRightArcCenter,topLeftArcCenter,bottomRightArcCenter,bottomLeftArcCenter;
    
    if (rectCorner & UIRectCornerTopLeft) {
        topLeftRadius = cornerRadius;
    }
    if (rectCorner & UIRectCornerTopRight) {
        topRightRadius = cornerRadius;
    }
    if (rectCorner & UIRectCornerBottomLeft) {
        bottomLeftRadius = cornerRadius;
    }
    if (rectCorner & UIRectCornerBottomRight) {
        bottomRightRadius = cornerRadius;
    }
    
    if (arrowDirection == YBPopupMenuArrowDirectionTop) {
        topLeftArcCenter = CGPointMake(topLeftRadius + TCRectX(rect), arrowHeight + topLeftRadius + TCRectX(rect));
        topRightArcCenter = CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect), arrowHeight + topRightRadius + TCRectX(rect));
        bottomLeftArcCenter = CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) - bottomLeftRadius + TCRectX(rect));
        bottomRightArcCenter = CGPointMake(TCRectWidth(rect) - bottomRightRadius + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius + TCRectX(rect));
        if (arrowPosition < topLeftRadius + arrowWidth / 2) {
            arrowPosition = topLeftRadius + arrowWidth / 2;
        }else if (arrowPosition > TCRectWidth(rect) - topRightRadius - arrowWidth / 2) {
            arrowPosition = TCRectWidth(rect) - topRightRadius - arrowWidth / 2;
        }
        [bezierPath moveToPoint:CGPointMake(arrowPosition - arrowWidth / 2, arrowHeight + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(arrowPosition, TCRectTop(rect) + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(arrowPosition + arrowWidth / 2, arrowHeight + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - topRightRadius, arrowHeight + TCRectX(rect))];
        [bezierPath addArcWithCenter:topRightArcCenter radius:topRightRadius startAngle:M_PI * 3 / 2 endAngle:2 * M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius - TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomRightArcCenter radius:bottomRightRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) + TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomLeftArcCenter radius:bottomLeftRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectX(rect), arrowHeight + topLeftRadius + TCRectX(rect))];
        [bezierPath addArcWithCenter:topLeftArcCenter radius:topLeftRadius startAngle:M_PI endAngle:M_PI * 3 / 2 clockwise:YES];
        
    }else if (arrowDirection == YBPopupMenuArrowDirectionBottom) {
        topLeftArcCenter = CGPointMake(topLeftRadius + TCRectX(rect),topLeftRadius + TCRectX(rect));
        topRightArcCenter = CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect), topRightRadius + TCRectX(rect));
        bottomLeftArcCenter = CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) - bottomLeftRadius + TCRectX(rect) - arrowHeight);
        bottomRightArcCenter = CGPointMake(TCRectWidth(rect) - bottomRightRadius + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius + TCRectX(rect) - arrowHeight);
        if (arrowPosition < bottomLeftRadius + arrowWidth / 2) {
            arrowPosition = bottomLeftRadius + arrowWidth / 2;
        }else if (arrowPosition > TCRectWidth(rect) - bottomRightRadius - arrowWidth / 2) {
            arrowPosition = TCRectWidth(rect) - bottomRightRadius - arrowWidth / 2;
        }
        [bezierPath moveToPoint:CGPointMake(arrowPosition + arrowWidth / 2, TCRectHeight(rect) - arrowHeight + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(arrowPosition, TCRectHeight(rect) + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(arrowPosition - arrowWidth / 2, TCRectHeight(rect) - arrowHeight + TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) - arrowHeight + TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomLeftArcCenter radius:bottomLeftRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectX(rect), topLeftRadius + TCRectX(rect))];
        [bezierPath addArcWithCenter:topLeftArcCenter radius:topLeftRadius startAngle:M_PI endAngle:M_PI * 3 / 2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect), TCRectX(rect))];
        [bezierPath addArcWithCenter:topRightArcCenter radius:topRightRadius startAngle:M_PI * 3 / 2 endAngle:2 * M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius - TCRectX(rect) - arrowHeight)];
        [bezierPath addArcWithCenter:bottomRightArcCenter radius:bottomRightRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
        
    }else if (arrowDirection == YBPopupMenuArrowDirectionLeft) {
        topLeftArcCenter = CGPointMake(topLeftRadius + TCRectX(rect) + arrowHeight,topLeftRadius + TCRectX(rect));
        topRightArcCenter = CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect), topRightRadius + TCRectX(rect));
        bottomLeftArcCenter = CGPointMake(bottomLeftRadius + TCRectX(rect) + arrowHeight, TCRectHeight(rect) - bottomLeftRadius + TCRectX(rect));
        bottomRightArcCenter = CGPointMake(TCRectWidth(rect) - bottomRightRadius + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius + TCRectX(rect));
        if (arrowPosition < topLeftRadius + arrowWidth / 2) {
            arrowPosition = topLeftRadius + arrowWidth / 2;
        }else if (arrowPosition > TCRectHeight(rect) - bottomLeftRadius - arrowWidth / 2) {
            arrowPosition = TCRectHeight(rect) - bottomLeftRadius - arrowWidth / 2;
        }
        [bezierPath moveToPoint:CGPointMake(arrowHeight + TCRectX(rect), arrowPosition + arrowWidth / 2)];
        [bezierPath addLineToPoint:CGPointMake(TCRectX(rect), arrowPosition)];
        [bezierPath addLineToPoint:CGPointMake(arrowHeight + TCRectX(rect), arrowPosition - arrowWidth / 2)];
        [bezierPath addLineToPoint:CGPointMake(arrowHeight + TCRectX(rect), topLeftRadius + TCRectX(rect))];
        [bezierPath addArcWithCenter:topLeftArcCenter radius:topLeftRadius startAngle:M_PI endAngle:M_PI * 3 / 2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - topRightRadius, TCRectX(rect))];
        [bezierPath addArcWithCenter:topRightArcCenter radius:topRightRadius startAngle:M_PI * 3 / 2 endAngle:2 * M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius - TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomRightArcCenter radius:bottomRightRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(arrowHeight + bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) + TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomLeftArcCenter radius:bottomLeftRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
        
    }else if (arrowDirection == YBPopupMenuArrowDirectionRight) {
        topLeftArcCenter = CGPointMake(topLeftRadius + TCRectX(rect),topLeftRadius + TCRectX(rect));
        topRightArcCenter = CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect) - arrowHeight, topRightRadius + TCRectX(rect));
        bottomLeftArcCenter = CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) - bottomLeftRadius + TCRectX(rect));
        bottomRightArcCenter = CGPointMake(TCRectWidth(rect) - bottomRightRadius + TCRectX(rect) - arrowHeight, TCRectHeight(rect) - bottomRightRadius + TCRectX(rect));
        if (arrowPosition < topRightRadius + arrowWidth / 2) {
            arrowPosition = topRightRadius + arrowWidth / 2;
        }else if (arrowPosition > TCRectHeight(rect) - bottomRightRadius - arrowWidth / 2) {
            arrowPosition = TCRectHeight(rect) - bottomRightRadius - arrowWidth / 2;
        }
        [bezierPath moveToPoint:CGPointMake(TCRectWidth(rect) - arrowHeight + TCRectX(rect), arrowPosition - arrowWidth / 2)];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) + TCRectX(rect), arrowPosition)];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - arrowHeight + TCRectX(rect), arrowPosition + arrowWidth / 2)];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - arrowHeight + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius - TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomRightArcCenter radius:bottomRightRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) + TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomLeftArcCenter radius:bottomLeftRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectX(rect), arrowHeight + topLeftRadius + TCRectX(rect))];
        [bezierPath addArcWithCenter:topLeftArcCenter radius:topLeftRadius startAngle:M_PI endAngle:M_PI * 3 / 2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect) - arrowHeight, TCRectX(rect))];
        [bezierPath addArcWithCenter:topRightArcCenter radius:topRightRadius startAngle:M_PI * 3 / 2 endAngle:2 * M_PI clockwise:YES];
        
    }else if (arrowDirection == YBPopupMenuArrowDirectionNone) {
        topLeftArcCenter = CGPointMake(topLeftRadius + TCRectX(rect),  topLeftRadius + TCRectX(rect));
        topRightArcCenter = CGPointMake(TCRectWidth(rect) - topRightRadius + TCRectX(rect),  topRightRadius + TCRectX(rect));
        bottomLeftArcCenter = CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) - bottomLeftRadius + TCRectX(rect));
        bottomRightArcCenter = CGPointMake(TCRectWidth(rect) - bottomRightRadius + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius + TCRectX(rect));
        [bezierPath moveToPoint:CGPointMake(topLeftRadius + TCRectX(rect), TCRectX(rect))];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) - topRightRadius, TCRectX(rect))];
        [bezierPath addArcWithCenter:topRightArcCenter radius:topRightRadius startAngle:M_PI * 3 / 2 endAngle:2 * M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectWidth(rect) + TCRectX(rect), TCRectHeight(rect) - bottomRightRadius - TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomRightArcCenter radius:bottomRightRadius startAngle:0 endAngle:M_PI_2 clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(bottomLeftRadius + TCRectX(rect), TCRectHeight(rect) + TCRectX(rect))];
        [bezierPath addArcWithCenter:bottomLeftArcCenter radius:bottomLeftRadius startAngle:M_PI_2 endAngle:M_PI clockwise:YES];
        [bezierPath addLineToPoint:CGPointMake(TCRectX(rect), arrowHeight + topLeftRadius + TCRectX(rect))];
        [bezierPath addArcWithCenter:topLeftArcCenter radius:topLeftRadius startAngle:M_PI endAngle:M_PI * 3 / 2 clockwise:YES];
    }
    if (completion) {
        completion(bezierPath, context);
    }
    [bezierPath fill];
    [bezierPath stroke];
    [bezierPath closePath];
    UIGraphicsEndImageContext();
    return bezierPath;
}

@end
