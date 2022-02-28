//
//  PanoUserService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserService.h"
#import "PanoCallClient.h"
#import "NSArray+Extension.h"

NSString * PropHostID = @"host";

@interface PanoUserService ()
@property (nonatomic, strong) NSMutableArray<PanoUserInfo *> *dataSource;
@property (nonatomic, assign) BOOL applyFlag; // 申请主持人Flag
@property (nonatomic, assign) NSUInteger propHostId;
@property (nonatomic, strong) NSMutableArray<NSNumber *> *screens;
@end

@implementation PanoUserService

- (void)initService {
    _dataSource = [NSMutableArray array];
    _screens = [NSMutableArray array];
}

- (PanoUserInfo *)me {
    return self.dataSource.firstObject;
}

- (PanoUserInfo *)host {
    return [self findUserWithId:self.hostId];
}

- (BOOL)isHost {
    return self.hostId == PanoCallClient.shared.userId &&
           self.hostId != 0;
}

- (PanoUserInfo *)findUserWithId:(UInt64)userId {
    for (PanoUserInfo * userInfo in self.dataSource) {
        if (userInfo.userId == userId) {
            return userInfo;
        }
    }
    return nil;
}

- (PanoUserInfo *)findGroupUserWithId:(UInt64)userId {
    for (PanoUserInfo * userInfo in self.allUsers) {
        if (userInfo.userId == userId) {
            return userInfo;
        }
    }
    return nil;
}

- (NSArray<PanoUserInfo *> *)allUsers {
    return self.dataSource.copy;
}

- (NSArray<NSNumber *> *)allScreens {
    NSArray *allUsers = [self.allUsers arrayByMappingObjectsUsingBlock:^NSNumber * _Nullable(PanoUserInfo * _Nonnull user) {
        return @(user.userId);
    }];
    return [self.screens filteredArrayUsingBlock:^BOOL(NSNumber * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        return [allUsers containsObject:obj];
    }];
}

- (PanoUserInfo *)sharingUser {
    if (PanoCallClient.shared.screen.isSharingScreen) {
        return [self me];
    }
    return [self findUserWithId:[self allScreens].firstObject.unsignedLongLongValue];
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
    if (PanoCallClient.shared.userId == self.hostId) {
        [PanoCallClient.shared.engineKit.whiteboardEngine setRoleType:kPanoWBRoleAdmin];
    }
}

- (void)notifyRefresh:(PanoUserInfo *)user {
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
    if (!_applyFlag && self.firstJoin) {
        _applyFlag = true;
        [self setMeetingHostId:[PanoCallClient shared].userId];
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
        [self.screens removeObject:@(userId)];
        if (userId == self.propHostId) {
            [self notifyHostChanged];
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
        if (![self.screens containsObject:@(userId)]) {
            [self.screens insertObject:@(userId) atIndex:0];
        }
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Unmute;
        [self invokeWithAction:@selector(onUserDidBeginSharingScreen:) completion:^(id  _Nonnull del) {
            [del onUserDidBeginSharingScreen:user];
        }];
    });
}

- (void)onUserScreenStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.screens removeObject:@(userId)];
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_None;
        [self invokeWithAction:@selector(onUserDidEndShareingScreen:) completion:^(id  _Nonnull del) {
            [del onUserDidEndShareingScreen:user];
        }];
    });
}

- (void)onUserScreenMute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Mute;
        [self invokeWithAction:@selector(onUserDidEndShareingScreen:) completion:^(id  _Nonnull del) {
            [del onUserDidEndShareingScreen:user];
        }];
    });
}

- (void)onUserScreenUnmute:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [self findUserWithId:userId];
        user.screenStatus = PanoUserScreen_Unmute;
        [self invokeWithAction:@selector(onUserDidBeginSharingScreen:) completion:^(id  _Nonnull del) {
            [del onUserDidBeginSharingScreen:user];
        }];
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

- (UIImage *)audioImage {
    NSString *name = self.audioStatus == PanoUserAudio_Unmute ? @"image.unmute.big" : @"image.mute.big";
    if (self.audioCallType != kPanoAudioCallTypeVoIP) {
        name = [NSString stringWithFormat:@"%@.pstn",name];
    }
    if (self.audioStatus == PanoUserAudio_Unmute && [self activeAudioImage] != nil) {
        return [self activeAudioImage];
    }
    return [UIImage imageNamed:name];
}

- (UIImage *)activeAudioImage {
    return nil;
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
