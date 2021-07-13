//
//  PanoVideoService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import <UIKit/UIKit.h>
#import "PanoRtc/PanoRtcEngineKit.h"

@class PanoRtcRenderConfig;
@class PanoViewInstance;
@class PanoUserInfo;

NS_ASSUME_NONNULL_BEGIN

@interface PanoVideoService : PanoBaseService  <PanoRtcEngineDelegate>

- (void)startVideoWithView:(UIView * _Nonnull)view
                  instance:(PanoViewInstance *)instance;

- (void)stopViewWithUser:(PanoUserInfo *)user;


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
