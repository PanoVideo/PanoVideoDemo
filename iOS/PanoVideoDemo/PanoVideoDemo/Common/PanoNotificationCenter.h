//
//  PanoNotificationCenter.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PanoNotificationCenter : NSObject

+ (instancetype)defaultCenter;

- (void)listenWithIdentifier:(NSString *)identifier listener:(void (^)(id __nullable messageObject))listener;

- (void)stopListeningWithIdentifier:(NSString *)identifier;

- (void)sendNotificationWithIdentifier:(NSString *)identifier userInfo:(NSDictionary *)userInfo;

@end

NS_ASSUME_NONNULL_END
