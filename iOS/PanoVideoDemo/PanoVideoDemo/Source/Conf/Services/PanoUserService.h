//
//  PanoUserService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoUserInfo.h"
#import "PanoRtc/PanoRtcEngineKit.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^PanoUserChangedBlock_t)(PanoUserInfo *user);

@protocol PanoUserDelegate <NSObject>

@optional

- (void)onUserStatusChanged:(PanoUserInfo *)user;

- (void)onUserRemoved:(PanoUserInfo *)user;

- (void)onUserAdded:(PanoUserInfo *)user;

- (void)onUserDidBeginSharingScreen:(PanoUserInfo *)user;

- (void)onUserDidEndShareingScreen:(PanoUserInfo *)user;

@end

@interface PanoUserService : PanoBaseService <PanoRtcEngineDelegate>

@property (nonatomic, copy) PanoUserChangedBlock_t userChangeBlock;

@property (nonatomic, weak) id <PanoUserDelegate> delegate;


- (PanoUserInfo *)me;

- (PanoUserInfo *)findUserWithId:(UInt64)userId;

- (NSArray<PanoUserInfo *> *)allUsers;



@end

@interface PanoUserService(Private)

- (void)onUserUpdated:(UInt64)userId message:(NSDictionary *)info;

@end

NS_ASSUME_NONNULL_END
