//
//  PanoVideoService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoVideoService.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoCallClient.h"
#import "PanoViewInstance.h"


@interface PanoVideoService()

@property (nonatomic, strong) NSMutableDictionary<NSNumber *, NSNumber *>* videoStatus;
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, NSNumber *>* videoScalingModes;

@end

@implementation PanoVideoService

- (NSMutableDictionary<NSNumber *,NSNumber *> *)videoScalingModes {
    if (!_videoScalingModes) {
        _videoScalingModes = [[NSMutableDictionary alloc] init];
    }
    return _videoScalingModes;
}

- (NSMutableDictionary<NSNumber *,NSNumber *> *)videoStatus {
    if (!_videoStatus) {
        _videoStatus = [NSMutableDictionary dictionary];
    }
    return _videoStatus;
}

- (BOOL)startVideo {
    PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
    renderConfig.profileType = PanoCallClient.shared.videoProfile;
    renderConfig.scalingMode = kPanoScalingCropFill;
    renderConfig.mirror = [PanoCallClient.shared.engineKit isFrontCamera];
    PanoResult result = [PanoCallClient.shared.engineKit startVideoWithView:nil config:renderConfig];
    return result == kPanoResultOK;
}

- (void)startVideoWithView:(UIView * _Nonnull)view
                  instance:(PanoViewInstance *)instance {
    PanoUserInfo *user = instance.user;
    if ([self.videoStatus objectForKey:@(user.userId)].integerValue == PanoUserVideo_Unmute) {
        PanoVideoProfileType profileType = [self profileWithMode:instance.mode];
        if (user.userId == PanoCallClient.shared.userId) {
            return;
        } else if (profileType == [self.videoScalingModes[@(instance.userId)] integerValue]) {
            return;
        }
    }
    // 1. 开启自己的视频
    if (user.userId == PanoCallClient.shared.userId) {
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = PanoCallClient.shared.videoProfile;
        renderConfig.scalingMode = kPanoScalingCropFill;
        renderConfig.mirror = [PanoCallClient.shared.engineKit isFrontCamera];
        PanoResult result = [PanoCallClient.shared.engineKit startVideoWithView:view config:renderConfig];
        if (result != kPanoResultOK) {
            [PanoCallClient.shared.userMgr onUserVideoStop:user.userId];
        }
        NSNumber *staus = result == kPanoResultOK ? @(PanoUserVideo_Unmute) : @(PanoUserVideo_None);
        [_videoStatus setObject:staus forKey:@(user.userId)];
        
    } else {
        // 2. 开启别人的视频
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = [self profileWithMode:instance.mode];
        renderConfig.scalingMode = kPanoScalingFit;
        PanoResult result = [PanoCallClient.shared.engineKit subscribeVideo:user.userId withView:view config:renderConfig];
        NSNumber *staus = result == kPanoResultOK ? @(PanoUserVideo_Unmute) : @(PanoUserVideo_None);
        [_videoStatus setObject:staus forKey:@(user.userId)];
        [self.videoScalingModes setObject:@(renderConfig.profileType) forKey:@(user.userId)];
    }
}

- (PanoVideoProfileType)profileWithMode:(PanoViewInstanceMode)mode {
    PanoVideoProfileType profile = kPanoProfileStandard;
    if (mode == PanoViewInstance_Float || mode == PanoViewInstance_Avg) {
        profile = kPanoProfileLow;
    } else if (mode == PanoViewInstance_Max) {
        profile = kPanoProfileHD1080P;
    }
    return profile;
}

- (void)unsubscribe:(UInt64)userId {
    if ([self.videoStatus objectForKey:@(userId)].integerValue != PanoUserVideo_Unmute) {
        return;
    }
    if (userId == PanoCallClient.shared.userId) {
        [self.videoStatus removeObjectForKey:@(userId)];
    } else {
        [PanoCallClient.shared.engineKit unsubscribeVideo:userId];
        [self.videoStatus setObject:@(PanoUserVideo_None) forKey:@(userId)];
        [self.videoScalingModes removeObjectForKey:@(userId)];
    }
}


- (void)switchVideoEnable:(BOOL)enable {
    if (enable) {
        // 模拟加入了一路新视频
        [PanoCallClient.shared.userMgr onUserVideoStart:PanoCallClient.shared.userId withMaxProfile:PanoCallClient.shared.videoProfile];
    } else {
        PanoUserInfo *user = [PanoCallClient.shared.userMgr findUserWithId:PanoCallClient.shared.userId];
        [PanoCallClient.shared.engineKit stopVideo];
        [PanoCallClient.shared.userMgr onUserVideoStop:user.userId];
    }
}

- (void)switchCamera {
    PanoUserService *userService = PanoCallClient.shared.userMgr;
    PanoUserInfo *user = [userService findUserWithId:PanoCallClient.shared.userId];
    if (user.videoStaus == PanoUserVideo_Unmute) {
        [PanoCallClient.shared.engineKit switchCamera];
        [userService onUserVideoStart:PanoCallClient.shared.userId withMaxProfile:PanoCallClient.shared.videoProfile];
    }
}

- (void)updateMyVideoWithView:(UIView * _Nonnull)view
                     config:(PanoRtcRenderConfig * _Nullable)config {
    if (!config) {
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = PanoCallClient.shared.videoProfile;
        renderConfig.scalingMode = kPanoScalingCropFill;
        renderConfig.mirror = [PanoCallClient.shared.engineKit isFrontCamera];
        config = renderConfig;
    }
    [PanoCallClient.shared.engineKit startVideoWithView:view config:config];
}


#pragma mark --
- (void)onUserVideoSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserVideo_Unmute) : @(PanoUserVideo_None);
    [_videoStatus setObject:staus forKey:@(userId)];
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.videoStatus removeObjectForKey:@(userId)];
        [self.videoScalingModes removeObjectForKey:@(userId)];
    });
}

@end
