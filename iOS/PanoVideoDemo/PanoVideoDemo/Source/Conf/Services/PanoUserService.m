//
//  PanoUserService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserService.h"
#import "PanoCallClient.h"

@interface PanoUserService ()
@property (nonatomic, strong) NSMutableArray<PanoUserInfo *> *dataSource;
@end

@implementation PanoUserService

- (void)initService {
    _dataSource = [NSMutableArray array];
}

- (PanoUserInfo *)me {
    return self.dataSource.firstObject;
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

- (void)notifyRefresh:(PanoUserInfo *)user {
    if ([self.delegate respondsToSelector:@selector(onUserStatusChanged:)]) {
        [self.delegate onUserStatusChanged:user];
    }
    [self invokeWithAction:@selector(onUserStatusChanged:) completion:^(id <PanoUserDelegate> _Nonnull del) {
        [del onUserStatusChanged:user];
    }];
}

- (void)onUserUpdated:(UInt64)userId message:(NSDictionary *)info {
    PanoUserInfo *user = [self findUserWithId:userId];
    [user setValuesForKeysWithDictionary:info[@"palyload"]];
}

- (void)onUserJoinIndication:(UInt64)userId withName:(NSString * _Nullable)userName {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoUserInfo *user = [[PanoUserInfo alloc] initWithId:userId name:userName];
        if (user.userId == PanoCallClient.sharedInstance.userId) {
            [self.dataSource insertObject:user atIndex:0];
        } else {
            [self.dataSource addObject:user];
        }
        if ([self.delegate respondsToSelector:@selector(onUserAdded:)]) {
            [self.delegate onUserAdded:user];
        }
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
        if ([self.delegate respondsToSelector:@selector(onUserRemoved:)]) {
            [self.delegate onUserRemoved:user];
        }
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

@end


@implementation  PanoUserInfo (Add)

- (NSString *)videoUserName {
    NSString *name = PanoCallClient.sharedInstance.userId == self.userId ?
                     [NSString stringWithFormat:@"%@(Me)", self.userName] : self.userName;
    return name;
}

@end
