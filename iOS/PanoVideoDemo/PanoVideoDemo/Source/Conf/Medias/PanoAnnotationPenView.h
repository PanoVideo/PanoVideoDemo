//
//  PanoAnnotationColorView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PanoBaseView.h"
#import "PanoCallClient.h"

NS_ASSUME_NONNULL_BEGIN

/// 标注工具栏选项
typedef NS_ENUM(NSInteger, PanoAnnotationToolOption) {
    PanoAnnotationToolSelect = 1,        ///  选择
    PanoAnnotationToolPath = 1 << 1,     /// 画笔
    PanoAnnotationToolText  = 1 << 2,    /// 文字
    PanoAnnotationToolShape = 1 << 3,    /// 形状
    PanoAnnotationToolBrush = 1 << 4,    /// 刷子
    PanoAnnotationToolUndo = 1 << 5,     /// 撤销
    PanoAnnotationToolRedo = 1 << 6,     /// 恢复
    PanoAnnotationToolClose = 1 << 7,    /// 关闭
    PanoAnnotationToolWhiteBoardFlag = 1 << 16, /// 白板的标志位
    PanoAnnotationToolAnnotationFlag = 1 << 17, /// 标注的标志位
    PanoAnnotationToolAll =
        PanoAnnotationToolSelect |
        PanoAnnotationToolPath |
        PanoAnnotationToolText |
        PanoAnnotationToolShape|
        PanoAnnotationToolBrush, /// 默认的工具类
    PanoAnnotationToolWhiteBoard =
        PanoAnnotationToolAll |
        PanoAnnotationToolWhiteBoardFlag |
        PanoAnnotationToolUndo |
        PanoAnnotationToolRedo |
        PanoAnnotationToolClose, /// 默认白板支持的类型
    PanoAnnotationToolAnnotation =
        PanoAnnotationToolAll |
        PanoAnnotationToolAnnotationFlag, /// 默认视频/共享标注支持的类型
};


/// 根据Option找到对应的工具类型
/// @param option 标注工具栏选项
static inline NSInteger firstBitFlag(PanoAnnotationToolOption option) {
    NSInteger i = 0;
    while (option > 0) {
        if (option & 1) {
            return i;
        }
        i++;
        option >>= 1;
    }
    return NSNotFound;
}

/// 形状是否填充标志位
static const NSInteger PanoShapeFillFlag = 1 << 16;

/// 形状的类型
typedef NS_ENUM(NSInteger, PanoShapeType) {
    PanoShapeLine = kPanoWBToolLine,         /// 直线
    PanoShapeEllipse = kPanoWBToolEllipse ,  /// 圆形
    PanoShapeRect = kPanoWBToolRect,         /// 方形
    PanoShapeFillEllipse =
        kPanoWBToolEllipse | PanoShapeFillFlag, /// 填充的圆形
    PanoShapeFillRect =
        kPanoWBToolRect | PanoShapeFillFlag,  /// 填充的方形
};

@class PanoAnnotationPenView;

@protocol PanoAnnotationColorViewDelegate <NSObject>

///  选择颜色的回调
/// @param color 颜色值
- (void)annotationColorViewDidChooseColor:(UIColor *)color;

@end

@protocol PanoAnnotationPenViewDelegate <PanoAnnotationColorViewDelegate>

/// 线宽改变回调
/// @param width: 宽度
- (void)annotationViewDidChooseLineWidth:(UInt32)width;


///  选择某个形状回调
/// @param shape: 形状类型
- (void)annotationViewDidChooseShape:(PanoShapeType)shape;

@end

typedef struct _PanoRange {
    UInt32 min;
    UInt32 max;
} PanoRange;

/// 初始化Range
NS_INLINE PanoRange PanoMakeRange(UInt32 min, UInt32 max) {
    PanoRange r;
    r.min = min;
    r.max = max;
    return r;
}


/// 画笔/ 文字 选择器
@interface PanoAnnotationPenView: UIView

@property(nonatomic, weak) id<PanoAnnotationPenViewDelegate>delegate;

@property(nonatomic, assign) CGFloat arrowPosition;

@property(nonatomic, assign) UInt32 lineWidth;

@property(nonatomic, strong) UIColor *activeColor;

@property(nonatomic, strong) UIColor *bgColor;

@property(nonatomic, assign) PanoRange range;

@property(nonatomic, assign) PanoShapeType activeShape;

+ (PanoAnnotationPenView *)showWithParentView:(UIView *)parentView
                         arrowPositionScreenX:(CGFloat)arrowPosition
                                   toolOption:(PanoAnnotationToolOption)option
                                       colors:(NSArray<NSString *> *)colors
                                       shapes:(NSArray<NSArray<id> *> *)shapes;

- (BOOL)isShowing;

- (void)dismiss;

@end


/// 标注颜色容器
@interface PanoAnnotationColorView : PanoBaseView

@property(nonatomic, strong) NSArray<NSString *> *colors;

@property(nonatomic, strong) UIColor *activeColor;

@property(nonatomic, weak) id<PanoAnnotationColorViewDelegate>delegate;

@end

NS_ASSUME_NONNULL_END
