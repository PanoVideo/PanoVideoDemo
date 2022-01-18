//
//  PanoRTMService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoRtcService.h"
#import "PanoCallClient.h"

@interface PanoRtcService() <PanoRtcMessageDelegate>
@property (assign, nonatomic) PanoMessageServiceState rtmState;
@end

@implementation PanoRtcService

- (PanoRtcMessage *)messageService {
    return PanoCallClient.shared.engineKit.messageService;
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
    
    PanoResult res = [PanoCallClient.shared.engineKit.messageService sendMessageToUser:userId data:data];
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
    PanoResult res = [PanoCallClient.shared.engineKit.messageService broadcastMessage:data sendBack:sendBack];
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
        [self invokeWithAction:@selector(onMessageReceived:fromUser:) completion:^(id <PanoRtcDelegate>  _Nonnull del) {
            [del onMessageReceived:msg fromUser:userId];
        }];
    });
}

- (void)onPropertyChanged:(NSArray<PanoPropertyAction *> *)props {
    NSLog(@"onPropertyChanged:%@ count: %zd",props, props.count);
    dispatch_async(dispatch_get_main_queue(), ^{
        for (PanoPropertyAction *prop in props) {
            id value = nil;
            if (prop.propValue) {
                NSError *error = nil;
                value = [NSJSONSerialization JSONObjectWithData:prop.propValue options:0 error:&error];
                NSLog(@"value: %@, error: %@", value, error);
                if (error) {
                    return;
                }
            }
            [self invokeWithAction:@selector(onPropertyChanged:forKey:) completion:^(id  _Nonnull del) {
                [del onPropertyChanged:value forKey:prop.propName];
            }];
        }
    });
}

- (void)onSubscribeTopic:(NSString *)topic result:(PanoResult)result {
    NSLog(@"onSubscribeTopic-> %@ %ld", topic, (long)result);
}


- (void)onTopicMessage:(NSString *)topic userId:(UInt64)userId data:(NSData *)data {
    id s = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"onTopicMessage: %@  %llu %@", topic, userId, s);
}


- (BOOL)setProperty:(id)value forKey:(NSString *)key {
    if (self.rtmState == kPanoMessageServiceUnavailable) {
        return false;
    }
    NSData *data = nil;
    NSError *error = nil;
    if (value) {
        data = [NSJSONSerialization dataWithJSONObject:value options:0 error:&error];
    }
    if (error) {
        NSLog(@"setProperty error: %@", error);
        return false;
    }
    PanoResult result = [self.messageService setProperty:key value:data];
    return result == kPanoResultOK;
}

@end
