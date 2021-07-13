//
//  PanoAnnotationToolView.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoBaseView.h"
#import "PanoAction.h"

NS_ASSUME_NONNULL_BEGIN


@class PanoAnnotationToolView;

@protocol PanoAnnotationViewDelegate <NSObject>

- (void)annotationViewDidAppear:(PanoAnnotationToolView *)toolView;

- (void)annotationViewDidDisAppear:(PanoAnnotationToolView *)toolView;

@end



@interface PanoAnnotationToolView : UIView

@property (nonatomic, strong, readonly) UIView *annotationView;
@property (nonatomic, strong, readonly) NSArray<PanoAction *> *items;
@property (weak, nonatomic) id <PanoAnnotationViewDelegate> delegate;

- (instancetype)initWithItems:(NSArray<PanoAction *> *)items;

- (void)show;

- (void)hide;

- (BOOL)isVisible;

@end

NS_ASSUME_NONNULL_END


