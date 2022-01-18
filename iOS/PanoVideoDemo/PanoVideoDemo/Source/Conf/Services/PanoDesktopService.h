//
//  PanoDesktopService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import <UIKit/UIKit.h>
#import "PanoRtc/PanoRtcEngineKit.h"

@class PanoUserInfo;

NS_ASSUME_NONNULL_BEGIN
/// 桌面共享核心接口
@interface PanoDesktopService : PanoBaseService <PanoRtcEngineDelegate>

/// 是否正在共享手机屏幕
@property (nonatomic, readonly, assign) BOOL isSharingScreen;

/// 订阅屏幕
- (void)subscribe:(UInt64)userId WithView:(UIView * _Nonnull)view;

/// 取消订阅屏幕
- (void)unsubscribe:(UInt64)userId;

/// 启动桌面共享服务
- (void)start;

/// 停止共享
- (void)stopShare;


@end

NS_ASSUME_NONNULL_END
