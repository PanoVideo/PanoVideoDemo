//
//  PanoDesktopService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoDesktopService.h"
#import "PanoCallClient.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoUserInfo.h"
#import "PanoNotificationCenter.h"

@interface PanoDesktopService ()
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, NSNumber *>* screenStatuses;
@end


@implementation PanoDesktopService

- (NSMutableDictionary<NSNumber *,NSNumber *> *)screenStatuses {
    if (!_screenStatuses) {
        _screenStatuses = [NSMutableDictionary dictionary];
    }
    return _screenStatuses;
}

- (void)subscribe:(UInt64)userId WithView:(UIView * _Nonnull)view {
    if ([self.screenStatuses objectForKey:@(userId)].integerValue == PanoUserScreen_Unmute) {
        return;
    }
    PanoResult result = [PanoCallClient.shared.engineKit subscribeScreen:userId withView:view];
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserScreen_Unmute) : @(PanoUserScreen_None);
    [_screenStatuses setObject:staus forKey:@(userId)];
}

- (void)unsubscribe:(UInt64)userId {
    if ([self.screenStatuses objectForKey:@(userId)].integerValue != PanoUserVideo_Unmute) {
        return;
    }
    PanoResult result = [PanoCallClient.shared.engineKit unsubscribeScreen:userId];
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserScreen_None) : (_screenStatuses[@(userId)] ?: @(PanoUserVideo_None));
    [self.screenStatuses setObject:staus forKey:@(userId)];
}

- (void)onUserScreenSubscribe:(UInt64)userId withResult:(PanoSubscribeResult)result {
    NSLog(@"onUserScreenSubscribe-> userId-> %ld result-> %ld", (long)userId,(long)result);
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserScreen_Unmute) : @(PanoUserScreen_None);
    [self.screenStatuses setObject:staus forKey:@(userId)];
}

- (void)onScreenCaptureStateChanged:(PanoScreenCaptureState)state reason:(PanoResult)reason {
    NSLog(@"onScreenCaptureStateChanged: %zd, %zd",state, reason);
    _isSharingScreen = state == kPanoScreenCaptureNormal;
    if (state == kPanoScreenCaptureStopped) {
        [self start];
    }
}

- (void)start{
    if (@available(iOS 11.0, *)) {
        PanoResult res = [PanoCallClient.shared.engineKit startScreenWithAppGroupId:@"group.video.pano.PanoCall"];
        NSLog(@"startShare: %zd",res);
    }
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.screenStatuses removeObjectForKey:@(userId)];
    });
}

- (void)stopShare {
    [PanoCallClient.shared.engineKit stopScreen];
    _isSharingScreen = false;
    [self start];
    [self removeObservers];
}

- (void)removeObservers {
    [[PanoNotificationCenter defaultCenter] stopListeningWithIdentifier:@"ShareScreenFinishedNotification"];
    [[PanoNotificationCenter defaultCenter] stopListeningWithIdentifier:@"ShareScreenStartNotification"];
}

- (void)dealloc {
    [self removeObservers];
}

@end
