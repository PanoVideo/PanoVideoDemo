//
//  PanoUserService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoUserInfo.h"
#import "PanoRtc/PanoRtcEngineKit.h"

extern NSString * _Nullable PropHostID;

NS_ASSUME_NONNULL_BEGIN

typedef void(^PanoUserChangedBlock_t)(PanoUserInfo *user);

/// 用户接口回调
@protocol PanoUserDelegate <NSObject>

@optional

- (void)onUserStatusChanged:(PanoUserInfo *)user;

- (void)onUserRemoved:(PanoUserInfo *)user;

- (void)onUserAdded:(PanoUserInfo *)user;

/// 用户列表发生改变
- (void)onUserListChanged;

- (void)onUserDidBeginSharingScreen:(PanoUserInfo *)user;

- (void)onUserDidEndShareingScreen:(PanoUserInfo *)user;

- (void)onPresenterDidChanged;

@end

/// 用户核心接口
@interface PanoUserService : PanoBaseService <PanoRtcEngineDelegate>

@property (nonatomic, copy) PanoUserChangedBlock_t userChangeBlock;

@property (assign, nonatomic, readonly) NSUInteger hostId;

@property (assign, nonatomic) BOOL firstJoin; ///<是否是第一个加入

- (PanoUserInfo *)me;

- (PanoUserInfo *)findUserWithId:(UInt64)userId;

- (PanoUserInfo *)findGroupUserWithId:(UInt64)userId;

- (PanoUserInfo *)host;

- (BOOL)isHost;

- (NSArray<PanoUserInfo *> *)allUsers;

- (NSArray<NSNumber *> *)allScreens;

- (PanoUserInfo *)sharingUser;

- (void)setMeetingHostId:(UInt64)hostId;

@end

@interface PanoUserService(Private)

- (void)onUserUpdated:(UInt64)userId message:(NSDictionary *)info;

- (void)onPropertyChanged:(id)value forKey:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
