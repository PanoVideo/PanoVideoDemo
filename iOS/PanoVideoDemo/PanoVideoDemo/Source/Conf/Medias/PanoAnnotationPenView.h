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

typedef NS_ENUM(NSInteger, PanoAnnotationToolOption) {
    PanoAnnotationToolSelect = 1,
    PanoAnnotationToolPath = 1 << 1,
    PanoAnnotationToolText  = 1 << 2,
    PanoAnnotationToolShape = 1 << 3,
    PanoAnnotationToolBrush = 1 << 4,
    PanoAnnotationToolWhiteBoardFlag = 1 << 16,
    PanoAnnotationToolAnnotationFlag = 1 << 17,
    PanoAnnotationToolAll = PanoAnnotationToolSelect | PanoAnnotationToolPath |
                            PanoAnnotationToolText | PanoAnnotationToolShape |
                            PanoAnnotationToolBrush,
    PanoAnnotationToolWhiteBoard = PanoAnnotationToolAll | PanoAnnotationToolWhiteBoardFlag,
    PanoAnnotationToolAnnotation = PanoAnnotationToolAll | PanoAnnotationToolAnnotationFlag,
};

static const NSInteger PanoShapeFillFlag = 1 << 16;

typedef NS_ENUM(NSInteger, PanoShapeType) {
    PanoShapeLine = kPanoWBToolLine,
    PanoShapeEllipse = kPanoWBToolEllipse ,
    PanoShapeRect = kPanoWBToolRect,
    PanoShapeFillEllipse = kPanoWBToolEllipse | PanoShapeFillFlag,
    PanoShapeFillRect = kPanoWBToolRect | PanoShapeFillFlag,
};

@class PanoAnnotationPenView;

@protocol PanoAnnotationColorViewDelegate <NSObject>

- (void)annotationColorViewDidChooseColor:(UIColor *)color;

@end

@protocol PanoAnnotationPenViewDelegate <PanoAnnotationColorViewDelegate>

- (void)annotationViewDidChooseLineWidth:(UInt32)width;

- (void)annotationViewDidChooseShape:(PanoShapeType)shape;

@end

typedef struct _PanoRange {
    UInt32 min;
    UInt32 max;
} PanoRange;

NS_INLINE PanoRange PanoMakeRange(UInt32 min, UInt32 max) {
    PanoRange r;
    r.min = min;
    r.max = max;
    return r;
}


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

@interface PanoAnnotationColorView : PanoBaseView

@property(nonatomic, strong) NSArray<NSString *> *colors;

@property(nonatomic, strong) UIColor *activeColor;

@property(nonatomic, weak) id<PanoAnnotationColorViewDelegate>delegate;

@end

NS_ASSUME_NONNULL_END
