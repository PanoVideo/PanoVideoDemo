//
//  PanoUserService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserService.h"
#import "PanoCallClient.h"

NSString * PropHostID = @"host";

@interface PanoUserService ()
@property (nonatomic, strong) NSMutableArray<PanoUserInfo *> *dataSource;
@property (nonatomic, assign) BOOL applyFlag; // 申请主持人Flag
@property (nonatomic, assign) NSUInteger propHostId;
@end

@implementation PanoUserService

- (void)initService {
    _dataSource = [NSMutableArray array];
}

- (PanoUserInfo *)me {
    return self.dataSource.firstObject;
}

- (PanoUserInfo *)host {
    return [self findUserWithId:self.hostId];
}

- (PanoUserInfo *)findUserWithId:(UInt64)userId {
    for (PanoUserInfo * userInfo in self.dataSource) {
        if (userInfo.userId == userId) {
            return userInfo;
        }
    }
    return nil;
}

- (NSArray<PanoUserInfo *> *)allUsers {
    return self.dataSource.copy;
}

- (void)setMeetingHostId:(UInt64)hostId {
    NSDictionary *props = @{ @"id" : @(hostId).stringValue};
    [PanoCallClient.shared.rtcService setProperty:props forKey:PropHostID];
}

- (NSUInteger)hostId {
    PanoUserInfo *host = [self findUserWithId:self.propHostId];
    if (host) {
        return self.propHostId;
    }
    return 0;
}

- (void)notifyHostChanged {
    [self invokeWithAction:@selector(onPresenterDidChanged) completion:^(id <PanoUserDelegate> _Nonnull del) {
        [del onPresenterDidChanged];
    }];
}

- (void)notifyRefresh:(PanoUserInfo *)user {
    if ([self.delegate respondsToSelector:@selector(onUserStatusChanged:)]) {
        [self.delegate onUserStatusChanged:user];
    }
    [self invokeWithAction:@selector(onUserStatusChanged:) completion:^(id <PanoUserDelegate> _Nonnull del) {
        [del onUserStatusChanged:user];
    }];
    [self notifyUserListChanged];
}

- (void)notifyUserListChanged {
    [self invokeWithAction:@selector(onUserListChanged) completion:^(id <PanoUserDelegate> _Nonnull del) {
        [del onUserListChanged];
    }];
}

- (void)onUserUpdated:(UInt64)userId message:(NSDictionary *)info {
    PanoUserInfo *user = [self findUserWithId:userId];
    [user setValuesForKeysWithDictionary:info[@"palyload"]];
}

- (void)onPropertyChanged:(id)value forKey:(NSString *)key {
    if ([key isEqualToString:PropHostID] && value) {
        NSUInteger hostId = [value[@"id"] integerValue];
        _propHostId = hostId;
        NSLog(@"onPropertyChanged: %lu", (unsigned long)hostId);
        [self notifyHostChanged];
    }
}

- (void)onRtmServiceAvailable {
    if (!_applyFlag) {
        _applyFlag = true;
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (self.hostId == 0 && self.propHostId == 0) {
                [self setMeetingHostId:[PanoCallClient shared].userId];
            }
        });
    }
}

- (void)onUserJoinIndication:(UInt64)userId withName:(NSString * _Nullable)userName {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [[PanoUserInfo alloc] initWithId:userId name:userName];
        if (user.userId == PanoCallClient.shared.userId) {
            [self.dataSource insertObject:user atIndex:0];
        } else {
            [self.dataSource addObject:user];
        }
        if (userId == self.propHostId) {
            [self notifyHostChanged];
        }
        if ([self.delegate respondsToSelector:@selector(onUserAdded:)]) {
            [self.delegate onUserAdded:user];
        }
        [self notifyUserListChanged];
        [self invokeWithAction:@selector(onUserAdded:) completion:^(id <PanoUserDelegate>  _Nonnull del) {
            [del onUserAdded:user];
        }];
    });
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [[PanoUserInfo alloc] initWithId:userId name:nil];
        [self.dataSource removeObject:user];
        if (userId == self.propHostId) {
            [self notifyHostChanged];
        }
        if ([self.delegate respondsToSelector:@selector(onUserRemoved:)]) {
            [self.delegate onUserRemoved:user];
        }
        [self notifyUserListChanged];
        [self invokeWithAction:@selector(onUserRemoved:) completion:^(id <PanoUserDelegate>  _Nonnull del) {
            [del onUserRemoved:user];
        }];
    });
}

- (void)onUserAudioStart:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.audioStatus = PanoUserAudio_Unmute;
        [self notifyRefresh:user];
    });
}

- (void)onUserAudioStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.audioStatus = PanoUserAudio_None;
        [self notifyRefresh:user];
    });
}

- (void)onUserAudioCallTypeChanged:(uint64_t)userId type:(PanoAudioCallType)type {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.audioCallType = type;
        [self notifyRefresh:user];
    });
}

- (void)onUserAudioMute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.audioStatus = PanoUserAudio_Mute;
        [self notifyRefresh:user];
    });
}

- (void)onUserAudioUnmute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.audioStatus = PanoUserAudio_Unmute;
        [self notifyRefresh:user];
    });
}

- (void)onUserVideoStart:(UInt64)userId
          withMaxProfile:(PanoVideoProfileType)maxProfile {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.videoStaus = PanoUserVideo_Unmute;
        [self notifyRefresh:user];
    });
}

- (void)onUserVideoStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.videoStaus = PanoUserVideo_None;
        [self notifyRefresh:user];
    });
}

- (void)onUserVideoMute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.videoStaus = PanoUserVideo_Mute;
        [self notifyRefresh:user];
    });
}

- (void)onUserVideoUnmute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.videoStaus = PanoUserVideo_Unmute;
        [self notifyRefresh:user];
    });
}

#pragma mark -- Screen
- (void)onUserScreenStart:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Unmute;
        if ([self.delegate respondsToSelector:@selector(onUserDidBeginSharingScreen:)]) {
            [self.delegate onUserDidBeginSharingScreen:user];
        }
    });
}

- (void)onUserScreenStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_None;
        if ([self.delegate respondsToSelector:@selector(onUserDidEndShareingScreen:)]) {
            [self.delegate onUserDidEndShareingScreen:user];
        }
    });
}

- (void)onUserScreenMute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Mute;
        if ([self.delegate respondsToSelector:@selector(onUserDidEndShareingScreen:)]) {
            [self.delegate onUserDidEndShareingScreen:user];
        }
    });
}

- (void)onUserScreenUnmute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Unmute;
        if ([self.delegate respondsToSelector:@selector(onUserDidBeginSharingScreen:)]) {
            [self.delegate onUserDidBeginSharingScreen:user];
        }
    });
}

#pragma mark -- PanoGroupDelegate
- (void)onGroupRosterChanged:(NSArray *)roster {
    [self notifyUserListChanged];
}

- (void)onGroupStatusChanged:(BOOL)on {
    [self notifyUserListChanged];
}
@end


@implementation  PanoUserInfo (Add)

- (NSString *)videoUserName {
    NSString *name = PanoCallClient.shared.userId == self.userId ?
                     [NSString stringWithFormat:@"%@(Me)", self.userName] : self.userName;
    return name;
}

- (NSString *)audioName {
    NSString *name = self.audioStatus == PanoUserAudio_Unmute ? @"image.unmute.big" : @"image.mute.big";
    if (self.audioCallType != kPanoAudioCallTypeVoIP) {
        name = [NSString stringWithFormat:@"%@.pstn",name];
    }
    return name;
}

- (UIImage *)userListAudioImage {
    NSString *name = self.audioStatus == PanoUserAudio_Unmute ?  @"userlist.audio.unmute" : @"userlist.audio.mute";
    if (self.audioCallType != kPanoAudioCallTypeVoIP) {
        name = [NSString stringWithFormat:@"%@.pstn",name];
    }
    return [UIImage imageNamed:name];
}

- (UIImage *)userListVideoImage {
    NSString *name = self.videoStaus == PanoUserAudio_Unmute ? @"userlist.video.open" : @"userlist.video.close";
    return [UIImage imageNamed:name];
}

@end
