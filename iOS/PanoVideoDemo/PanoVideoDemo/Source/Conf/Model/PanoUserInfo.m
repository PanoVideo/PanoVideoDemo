//
//  PanoUserInfo.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserInfo.h"
#import "PanoCallClient.h"

@implementation PanoUserInfo

- (instancetype)initWithId:(UInt64)userId name:(NSString * _Nullable)userName {
    self = [super init];
    if (nil != self) {
        _userId = userId;
        _userName = userName;
        _audioStatus = PanoUserAudio_None;
        _videoStaus = PanoUserVideo_None;
        _screenStatus = PanoUserScreen_None;
    }
    return self;
}

- (BOOL)isEqual:(id)object {
    if (![object isKindOfClass:[PanoUserInfo class]]) {
        return false;
    }
    return self.userId == ((PanoUserInfo *)object).userId;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"%@ _userId: %lld _userName: %@ _audioStatus: %ld _videoStaus: %ld",[super description] ,_userId, _userName, (long)_audioStatus, (long)_videoStaus];
}

- (id)copyWithZone:(NSZone *)zone {
    PanoUserInfo *info = [[PanoUserInfo alloc] initWithId:_userId name:_userName];
    info.audioStatus = _audioStatus;
    info.videoStaus = _videoStaus;
    info.screenStatus = _screenStatus;
    return info;
}

- (void)setValue:(id)value forUndefinedKey:(NSString *)key {
    NSLog(@"forUndefinedKey: %@", key);
}

- (BOOL)isVoip {
    return self.audioCallType == kPanoAudioCallTypeVoIP;
}

- (BOOL)isAudioMuted {
    return self.audioStatus != PanoUserAudio_Unmute;
}

- (BOOL)isVideoMuted {
    return self.videoStaus != PanoUserVideo_Unmute;
}

- (PanoUserRole)role {
    if (_userId == PanoCallClient.shared.userMgr.hostId) {
        return PanoUserRole_Host;
    }
    return PanoUserRole_None;
}
@end
