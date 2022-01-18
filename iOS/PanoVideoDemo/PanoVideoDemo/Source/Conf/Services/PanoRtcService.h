//
//  PanoRTMService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoBaseService.h"


NS_ASSUME_NONNULL_BEGIN

@protocol PanoRtcDelegate <NSObject>

@optional
/// 用户消息通知
- (void)onMessageReceived:(NSDictionary *)message fromUser:(UInt64)userId;

/// 消息服务状态可用通知
- (void)onRtmServiceAvailable;

/// 属性改变通知
- (void)onPropertyChanged:(id)value forKey:(NSString *_Nonnull)key;

@end

/// 消息服务核心接口 PanoRtcMessage 封装
@interface PanoRtcService<T> : PanoBaseService<T>

- (BOOL)sendMessageToUser:(UInt64)userId
                      msg:(NSDictionary<NSString *, id> *)msg;

- (BOOL)broadcastMessage:(NSDictionary<NSString *, id> *)msg
                sendBack:(BOOL)sendBack;

- (BOOL)setProperty:(id)value forKey:(NSString *)key;
@end

NS_ASSUME_NONNULL_END
