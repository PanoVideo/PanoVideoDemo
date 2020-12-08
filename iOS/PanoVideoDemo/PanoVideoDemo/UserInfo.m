//
//  UserInfo.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "UserInfo.h"

@implementation UserInfo

- (instancetype)initWithId:(UInt64)userId name:(NSString *)userName {
    self = [super init];
    if (nil != self) {
        _userId = userId;
        _userName = userName;
        _audioEnable = NO;
        _audioMute = NO;
        _videoEnable = NO;
        _videoMute = NO;
        _userView = nil;
    }
    return self;
}

- (void)setAudioEnable:(BOOL)audioEnable {
    _audioEnable = audioEnable;
    if (_userView) {
        _userView.micIcon.image = [self micImage];
    }
}

- (void)setAudioMute:(BOOL)audioMute {
    _audioMute = audioMute;
    if (_userView) {
        _userView.micIcon.image = [self micImage];
    }
}

- (void)setUserView:(UserView *)userView {
    _userView = userView;
    if (_userView) {
        _userView.userName.text = _userName;
        _userView.micIcon.image = [self micImage];
        _userView.hidden = NO;
    }
}

- (UIImage *)micImage {
    UIImage * image = nil;
    if (_audioEnable) {
        BOOL small = [self isSmallView:_userView];
        if (_audioMute) {
            image = [UIImage imageNamed:small ? @"image.mute.small" : @"image.mute.big"];
        } else {
            image = [UIImage imageNamed:small ? @"image.unmute.small" : @"image.unmute.big"];
        }
    }
    return image;
}

- (BOOL)isSmallView:(UserView *)view {
    BOOL small = YES;
    if (view.bounds.size.width * view.bounds.size.height > 160 * 90) {
        small = NO;
    }
    return small;
}

@end


@interface UserManager ()

@property (strong, nonatomic) NSMutableArray<UserInfo *> * userInfos;

@end

@implementation UserManager

- (instancetype)init {
    self = [super init];
    if (nil != self) {
        self.userInfos = [NSMutableArray arrayWithCapacity:10];
    }
    return self;
}

- (NSUInteger)count {
    return self.userInfos.count;
}

- (UserInfo *)addUser:(UInt64)userId withName:(NSString *)userName {
    UserInfo * userInfo = [[UserInfo alloc] initWithId:userId name:userName];
    [self.userInfos addObject:userInfo];
    return userInfo;
}

- (UserInfo *)removeUser:(UInt64)userId {
    UserInfo * userInfo = nil;
    for (userInfo in self.userInfos) {
        if (userInfo.userId == userId) {
            [self.userInfos removeObject:userInfo];
            break;
        }
    }
    return userInfo;
}

- (UserInfo *)findUser:(UInt64)userId {
    UserInfo * userInfo = nil;
    for (userInfo in self.userInfos) {
        if (userInfo.userId == userId) {
            break;
        }
    }
    return userInfo;
}

- (UserInfo *)findUserWithIndex:(NSUInteger)index {
    UserInfo * userInfo = nil;
    if (index < self.userInfos.count) {
        userInfo = [self.userInfos objectAtIndex:index];
    }
    return userInfo;
}

- (UserInfo *)findUserWithView:(UserView *)view {
    UserInfo * userInfo = nil;
    for (userInfo in self.userInfos) {
        if (userInfo.userView == view) {
            break;
        }
    }
    return userInfo;
}

- (UserInfo *)findWatingUser {
    UserInfo * userInfo = nil;
    for (userInfo in self.userInfos) {
        if (userInfo.videoEnable && userInfo.userView == nil) {
            break;
        }
    }
    return userInfo;
}

@end
