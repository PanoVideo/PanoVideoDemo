//
//  PanoPoolService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoViewInstance.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoPoolProtocols.h"

NS_ASSUME_NONNULL_BEGIN

/// 共享池核心接口
@interface PanoPoolService<T> : PanoBaseService<T>

@property (nonatomic, weak) id <PanoPoolDelegate> delegate;

/// 语音激励的用户，自己除外
@property (nonatomic, assign, readonly) NSInteger activeAudioUserID;

/// 语音激励的用户数组
@property (nonatomic, strong, readonly) NSArray<NSNumber *> *activeSpeakerList;

@property (nonatomic, strong, readonly) PanoViewInstance *activeSpeakerUser;

@property (nonatomic, assign, readonly) BOOL enableRender;

/// 当前所在的页码 （从 0 开始）
@property (nonatomic, assign, readonly) PageIndex currentIndex;

///  总共的页码数
@property (nonatomic, assign, readonly) PageIndex numbersOfIndexs;

/// 正在进行共享、视频标注 , 白板
@property (nonatomic, assign) BOOL isAnnotationing;


@property (strong, nonatomic) NSMutableDictionary<NSNumber *, NSNumber*> *speakingUsers;

///  Pin
@property (strong, nonatomic) PanoViewInstance *pinInstance;

/// 更新当前页面索引
- (void)updatePageIndex;

- (void)notify;

/// 优先级最高的桌面共享实例
- (PanoViewInstance *)desktopInstance;


- (void)start;


///  切换视频布局
/// @param instance 要切换的View
- (void)switchInstance:(PanoViewInstance *)instance;


/// 订阅或者取消订阅某个人的view
/// @param instance  view实例
- (void)togglePinViewInstance:(PanoViewInstance *)instance
                   completion:(void(^)(void))completion;

/// 取消订阅某个人的view
/// @param instance view实例
- (void)cancelPinViewInstance:(PanoViewInstance *)instance
                   completion:(void(^)(void))completion;;

- (void)startRender;

- (void)stopRender;

@end

@interface PanoPoolService (Pubilc) <PanoTurnPageDelegate, PanoRtcEngineDelegate>

@end

NS_ASSUME_NONNULL_END
