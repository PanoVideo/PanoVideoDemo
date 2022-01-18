//
//  PanoAnnotationTool.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PanoAnnotationPenView.h"
#import "PanoAnnotationToolView.h"
#import "PanoCallClient.h"


NS_ASSUME_NONNULL_BEGIN



@class PanoAnnotationTool;

typedef NSString* PanoAnnotationToolKey;
extern PanoAnnotationToolKey PanoToolKeyColor;     ///< 颜色
extern PanoAnnotationToolKey PanoToolKeyLineWidth; ///< 线宽
extern PanoAnnotationToolKey PanoToolKeyFontSize; ///< 字体大小
extern PanoAnnotationToolKey PanoToolKeyFill;     ///< 是否填充


/**
 标注工具栏的回调，默认会把 PanoAnnotationToolOption 转换成 PanoWBToolType,
  不能抓换的通过 ```annotationToolDidChoosed``` 回调
 */
@protocol PanoAnnotationToolDelegate <NSObject>

@required
- (void)annotationToolDidChooseType:(PanoWBToolType)type
                           options:(NSDictionary<PanoAnnotationToolKey, id> *)options;
@optional
- (void)annotationToolDidChoosed:(PanoAnnotationToolOption)option;

@end

/**
 标注工具类
 */
@interface PanoAnnotationTool : NSObject

@property (strong, nonatomic, readonly) PanoAnnotationToolView* annotationTool;

@property (assign, nonatomic, readonly) PanoAnnotationToolOption option;

@property (weak, nonatomic) id <PanoAnnotationToolDelegate> delegate;

@property (weak, nonatomic, readonly) PanoAnnotationPenView *penView;

@property (assign, nonatomic) PanoRange lineWidthRange;

@property (assign, nonatomic) PanoRange fontSizeRange;

- (instancetype)initWithView:(UIView *)parentView toolOption:(PanoAnnotationToolOption)option;

- (void)show;

- (void)hide;

- (BOOL)isVisible;

- (void)showToolInstruction;

- (void)hideToolInstruction;
@end

NS_ASSUME_NONNULL_END
