//
//  PanoVideoService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoVideoService.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoCallClient.h"
#import "PanoViewInstance.h"
#import "PanoServiceManager.h"
#import "PanoUserService.h"


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
- (void)startVideoWithView:(UIView * _Nonnull)view
                  instance:(PanoViewInstance *)instance {
    PanoUserInfo *user = instance.user;
    if ([self.videoStatus objectForKey:@(user.userId)].integerValue == PanoUserVideo_Unmute) {
        //NSLog(@"%llu(%@) video has started",instance.userId, instance.user.userName);
        PanoVideoProfileType profileType = [self profileWithMode:instance.mode];
        if (user.userId == PanoCallClient.sharedInstance.userId) {
            return;
        } else if (profileType == [self.videoScalingModes[@(instance.userId)] integerValue]) {
            return;
        }
    }
    // NSLog(@"startVideoWithView: %@",instance);
    // 1. 开启自己的视频
    if (user.userId == PanoCallClient.sharedInstance.userId) {
        NSLog(@"view-> %@",view);
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = PanoCallClient.sharedInstance.videoProfile;
        renderConfig.scalingMode = kPanoScalingCropFill;
        renderConfig.mirror = [PanoCallClient.sharedInstance.engineKit isFrontCamera];
        PanoResult result = [PanoCallClient.sharedInstance.engineKit startVideoWithView:view config:renderConfig];
        if (result != kPanoResultOK) {
            [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStop:user.userId];
        }
        NSNumber *staus = result == kPanoResultOK ? @(PanoUserVideo_Unmute) : @(PanoUserVideo_None);
        [_videoStatus setObject:staus forKey:@(user.userId)];
        
    } else {
        // 2. 开启别人的视频
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = [self profileWithMode:instance.mode];
        renderConfig.scalingMode = kPanoScalingFit;
        PanoResult result = [PanoCallClient.sharedInstance.engineKit subscribeVideo:user.userId withView:view config:renderConfig];
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

- (void)stopViewWithUser:(PanoUserInfo *)user {
    if ([self.videoStatus objectForKey:@(user.userId)].integerValue != PanoUserVideo_Unmute) {
//        NSLog(@"%llu(%@) video has stopped",user.userId, user.userName);
        return;
    }
    NSLog(@"stopViewWithUser: %@", user);
    PanoResult result = -1;
    if (user.userId == PanoCallClient.sharedInstance.userId) {
        [self.videoStatus removeObjectForKey:@(user.userId)];
    } else {
        [PanoCallClient.sharedInstance.engineKit unsubscribeVideo:user.userId];
        [self.videoStatus setObject:@(PanoUserVideo_None) forKey:@(user.userId)];
        [self.videoScalingModes removeObjectForKey:@(user.userId)];
    }
}


- (void)switchVideoEnable:(BOOL)enable {
    if (enable) {
        // 模拟加入了一路新视频
        [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStart:PanoCallClient.sharedInstance.userId withMaxProfile:PanoCallClient.sharedInstance.videoProfile];
    } else {
        PanoUserInfo *user = [[PanoServiceManager serviceWithType:PanoUserServiceType] findUserWithId:PanoCallClient.sharedInstance.userId];
        [PanoCallClient.sharedInstance.engineKit stopVideo];
        [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStop:user.userId];
    }
}

- (void)switchCamera {
    PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
    PanoUserInfo *user = [userService findUserWithId:PanoCallClient.sharedInstance.userId];
    if (user.videoStaus == PanoUserVideo_Unmute) {
        [PanoCallClient.sharedInstance.engineKit switchCamera];
        [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStart:PanoCallClient.sharedInstance.userId withMaxProfile:PanoCallClient.sharedInstance.videoProfile];
    }
}

- (void)updateMyVideoWithView:(UIView * _Nonnull)view
                     config:(PanoRtcRenderConfig * _Nullable)config {
    if (!config) {
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = PanoCallClient.sharedInstance.videoProfile;
        renderConfig.scalingMode = kPanoScalingCropFill;
        renderConfig.mirror = [PanoCallClient.sharedInstance.engineKit isFrontCamera];
        config = renderConfig;
    }
    [PanoCallClient.sharedInstance.engineKit startVideoWithView:view config:config];
}


#pragma mark --
- (void)onUserVideoSubscribe:(UInt64)userId withResult:(PanoSubscribeResult)result {
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
