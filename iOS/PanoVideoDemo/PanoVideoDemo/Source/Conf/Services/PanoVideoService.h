//
//  PanoVideoService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import <UIKit/UIKit.h>
#import "PanoRtc/PanoRtcEngineKit.h"

@class PanoViewInstance;

NS_ASSUME_NONNULL_BEGIN

/// 视频核心接口
@interface PanoVideoService : PanoBaseService  <PanoRtcEngineDelegate>

/// 开启自己视频，但不会渲染自己的视频
- (BOOL)startVideo;

/// 订阅视频 (包含开启自己的视频)
- (void)startVideoWithView:(UIView * _Nonnull)view
                  instance:(PanoViewInstance *)instance;

/// 取消订阅视频
- (void)unsubscribe:(UInt64)userId;

///  打开或者关闭自己的视频
/// @param enable
- (void)switchVideoEnable:(BOOL)enable;


/// 切换自己的摄像头
- (void)switchCamera;


/// 更新自己视频的配置
- (void)updateMyVideoWithView:(UIView * _Nonnull)view
                       config:(PanoRtcRenderConfig * _Nullable)config;
@end

NS_ASSUME_NONNULL_END
