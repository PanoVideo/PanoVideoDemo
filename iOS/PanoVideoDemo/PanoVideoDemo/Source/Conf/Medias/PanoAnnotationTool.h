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
extern PanoAnnotationToolKey PanoToolKeyColor;
extern PanoAnnotationToolKey PanoToolKeyLineWidth;
extern PanoAnnotationToolKey PanoToolKeyFontSize;
extern PanoAnnotationToolKey PanoToolKeyFill;

@protocol PanoAnnotationToolDelegate <NSObject>

@required
- (void)annotationToolDidChooseType:(PanoWBToolType)type
                           options:(NSDictionary<PanoAnnotationToolKey, id> *)options;

@end


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

@end

NS_ASSUME_NONNULL_END
