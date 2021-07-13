//
//  PanoPoolService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoViewInstance.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoPoolProtocols.h"
#import "PanoAnnotationService.h"

NS_ASSUME_NONNULL_BEGIN

@interface PanoPoolService : PanoBaseService <PanoTurnPageDelegate, PanoRtcEngineDelegate>

@property (nonatomic, weak) id <PanoPoolDelegate> delegate;

/// ASL 用户，目前已经删除
@property (nonatomic, strong, readonly) PanoViewInstance *activeSpeakerUser;

@property (nonatomic, assign, readonly) BOOL enableRender;

/// 当前所在的页码 （从 0 开始）
@property (nonatomic, assign, readonly) PageIndex currentIndex;

///  总共的页码数
@property (nonatomic, assign, readonly) PageIndex numbersOfIndexs;

/// 所有的桌面批注
@property (strong, nonatomic, readonly) NSDictionary<NSNumber *, PanoAnnotationItem*> *shareAnnotations;

/// 所有的视频批注
@property (strong, nonatomic, readonly) NSDictionary<NSNumber *, PanoAnnotationItem*> *videoAnnotations;

@property (strong, nonatomic, readonly) PanoAnnotationService *annotationService;

/// 正在开启的批注（桌面批注或者视频批注）
@property (strong, nonatomic, readonly) PanoAnnotationItem *activeAnnotation;

/// 是否正在进行批注
@property (nonatomic, assign, readonly) NSUInteger isAnnotating;

- (void)start;


///  切换视频布局
/// @param instance 要切换的View
- (void)switchInstance:(PanoViewInstance *)instance;


/// 订阅或者取消订阅某个人的view
/// @param instance  view实例
- (void)togglePinViewInstance:(PanoViewInstance *)instance completion:(void(^)(void))completion;

/// 取消订阅某个人的view
/// @param instance view实例
- (void)cancelPinViewInstance:(PanoViewInstance *)instance completion:(void(^)(void))completion;;

- (void)startRender;

- (void)stopRender;

/// 是否开启了共享
- (BOOL)isSharing;

- (void)startAnnotation;

- (void)stopAnnotation;
@end

NS_ASSUME_NONNULL_END
