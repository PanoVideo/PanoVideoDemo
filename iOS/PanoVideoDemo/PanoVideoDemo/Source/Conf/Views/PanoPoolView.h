//
//  PanoPoolView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseView.h"
#import "PanoViewInstance.h"
#import "PanoPoolProtocols.h"
#import "PanoBaseMediaView.h"

@protocol PanoPoolViewDelegate <PanoPoolActiveAudioDelegate, PanoAnnotationDelegate>

- (void)onPageTypeChanged:(PanoViewPageLayoutType)type;


/// 池子的 页码数 发生改变
/// @param index 当前的页码
/// @param totalIndexs  总共的页面数
- (void)onPageIndexChanged:(NSUInteger)index numbersOfIndexs:(NSUInteger)totalIndexs;

@end

NS_ASSUME_NONNULL_BEGIN

@class PanoRtcAnnotation;

@interface PanoPoolView : PanoBaseView

@property (nonatomic, strong, readonly) UIView *contentView;

@property (nonatomic, weak) id <PanoPoolViewDelegate> delegate;

@property (nonatomic, strong, readonly) NSArray<PanoBaseMediaView *> *medias;

@property (nonatomic, assign, readonly) enum PanoWBToolType toolType;

@property (nonatomic, assign, readonly) PanoViewPageLayoutType layoutType;

@property (nonatomic, assign, readonly) UInt64 mainUserId;
/**
 更新自己视频的分辨率
 */
- (void)updateVideoRenderConfig:(id __nullable)config;


/// 更新Media布局
/// @param info  布局的配置信息
- (void)updateMediaLayout:(NSDictionary<PanoMediaInfoKey, id> *)info;

- (void)startRender;

- (void)stopRender;
@end

NS_ASSUME_NONNULL_END
