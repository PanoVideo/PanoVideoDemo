//
//  PanoClientService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"

NS_ASSUME_NONNULL_BEGIN

@interface PanoClientService : PanoBaseService

/// 主持人ID， 如果主持人没有入会，则为0
@property (nonatomic, assign, readonly) NSUInteger hostId;

//- (void)checkHost;

- (void)notifyAllUsersUploadLog;

+ (void)checkVersion;

@end

NS_ASSUME_NONNULL_END
