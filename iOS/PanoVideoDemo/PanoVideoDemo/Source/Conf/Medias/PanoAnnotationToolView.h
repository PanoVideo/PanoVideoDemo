//
//  PanoAnnotationToolView.h
//  PanoVideoDemo
//
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



/**
 标注工具栏
 @note: 使用UIStackView 构建，默认是水平布局，每个Item 尽可能平均分布
 */
@interface PanoAnnotationToolView : UIView

@property (nonatomic, strong, readonly) UIView *annotationView;
@property (nonatomic, strong, readonly) NSArray<PanoAction *> *items;

/// 设置当前选中的Item
@property (nonatomic, assign) NSUInteger selectedIndex;
@property (weak, nonatomic) id <PanoAnnotationViewDelegate> delegate;


/// 初始化工具栏
/// @param items 工具数组
- (instancetype)initWithItems:(NSArray<PanoAction *> *)items;

- (void)show;

- (void)hide;

- (BOOL)isVisible;

@end

NS_ASSUME_NONNULL_END


