//
//  UserInfo.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "UserView.h"

NS_ASSUME_NONNULL_BEGIN

@interface UserInfo : NSObject

@property (assign, nonatomic, readonly) UInt64 userId;
@property (strong, nonatomic, readonly) NSString * _Nullable userName;
@property (assign, nonatomic) BOOL audioEnable;
@property (assign, nonatomic) BOOL audioMute;
@property (assign, nonatomic) BOOL videoEnable;
@property (assign, nonatomic) BOOL videoMute;
@property (strong, nonatomic) UserView * _Nullable userView;

- (instancetype)initWithId:(UInt64)userId name:(NSString *)userName;
- (instancetype)init NS_UNAVAILABLE;

@end


@interface UserManager : NSObject

@property (readonly) NSUInteger count;

- (UserInfo *)addUser:(UInt64)userId withName:(NSString *)userName;
- (UserInfo *)removeUser:(UInt64)userId;
- (UserInfo *)findUser:(UInt64)userId;

- (UserInfo *)findUserWithIndex:(NSUInteger)index;
- (UserInfo *)findUserWithView:(UserView *)view;
- (UserInfo *)findWatingUser;

@end

NS_ASSUME_NONNULL_END
