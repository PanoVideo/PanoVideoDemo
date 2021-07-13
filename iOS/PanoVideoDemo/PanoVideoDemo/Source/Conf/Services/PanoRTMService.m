//
//  PanoRTMService.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoRTMService.h"
#import "PanoCallClient.h"

@interface PanoRTMService() <PanoRtcMessageDelegate>
@property (assign, nonatomic) PanoMessageServiceState rtmState;
@end

@implementation PanoRTMService

- (PanoRtcMessage *)messageService {
    return PanoCallClient.sharedInstance.engineKit.messageService;
}

- (BOOL)sendMessageToUser:(UInt64)userId
                      msg:(NSDictionary<NSString *, id> *)msg {
    if (_rtmState == kPanoMessageServiceUnavailable) {
        return false;
    }
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:msg options:(0) error:&error];
    if (error) {
        NSLog(@"Error: %@", error);
        return false;
    }
    
    PanoResult res = [PanoCallClient.sharedInstance.engineKit.messageService sendMessageToUser:userId data:data];
    return res == kPanoResultOK;
}

- (BOOL)broadcastMessage:(NSDictionary<NSString *, id> *)msg
                sendBack:(BOOL)sendBack {
    if (_rtmState == kPanoMessageServiceUnavailable) {
        return false;
    }
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:msg options:(0) error:&error];
    if (error) {
        NSLog(@"Error: %@", error);
        return false;
    }
    PanoResult res = [PanoCallClient.sharedInstance.engineKit.messageService broadcastMessage:data sendBack:sendBack];
    return res == kPanoResultOK;
}

- (void)onServiceStateChanged:(PanoMessageServiceState)state reason:(PanoResult)reason {
    _rtmState = state;
    if (state == kPanoMessageServiceAvailable) {
        [self invokeWithAction:@selector(onRtmServiceAvailable) completion:^(id  _Nonnull del) {
            [del onRtmServiceAvailable];
        }];
    }
}

- (void)onUserMessage:(UInt64)userId data:(NSData *)data {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSError *error = nil;
        NSDictionary *msg = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
        if (error) {
            return;
        }
        [self invokeWithAction:@selector(onMessageReceived:fromUser:) completion:^(id <PanoRTMDelegate>  _Nonnull del) {
            [del onMessageReceived:msg fromUser:userId];
        }];
    });
}

- (void)onPropertyChanged:(NSArray<PanoPropertyAction *> *)props {
    for (PanoPropertyAction *action in props) {
        NSLog(@"action-> %@", action.propName);
    }
}

- (void)onSubscribeTopic:(NSString *)topic result:(PanoResult)result {
    NSLog(@"onSubscribeTopic-> %@ %ld", topic, (long)result);
}


- (void)onTopicMessage:(NSString *)topic userId:(UInt64)userId data:(NSData *)data {
    id s = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"onTopicMessage: %@  %llu %@", topic, userId, s);
}


@end
