//
//  PanoRTMService.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoBaseService.h"


NS_ASSUME_NONNULL_BEGIN

@protocol PanoRTMDelegate <NSObject>

- (void)onMessageReceived:(NSDictionary *)message fromUser:(UInt64)userId;

- (void)onRtmServiceAvailable;

@end

@class PanoRtcMessage;

@interface PanoRTMService : PanoBaseService

@property (nonatomic, weak) PanoRtcMessage *messageService;

- (BOOL)sendMessageToUser:(UInt64)userId
                            msg:(NSDictionary<NSString *, id> *)msg;

- (BOOL)broadcastMessage:(NSDictionary<NSString *, id> *)msg
                      sendBack:(BOOL)sendBack;

@end

NS_ASSUME_NONNULL_END
