//
//  PanoDesktopService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import <UIKit/UIKit.h>

@class PanoUserInfo;

NS_ASSUME_NONNULL_BEGIN
/// 卓曼共享的服务
@interface PanoDesktopService : PanoBaseService

@property (nonatomic, readonly, assign) BOOL isSharingScreen;

- (void)startScreenShareWithView:(UIView * _Nonnull)view
                            user:(PanoUserInfo *)user;

- (void)stopScreenShareWithUser:(PanoUserInfo *)user;


//- (void)startAnnotationWithView:(UIView * _Nonnull)view
//                           user:(PanoUserInfo *)user;
//
//- (void)stopAnnotationWithUser:(PanoUserInfo *)user;

- (void)startShareWithHandler:(void(^)(BOOL success))handler;

- (void)stopShare;


@end

NS_ASSUME_NONNULL_END
