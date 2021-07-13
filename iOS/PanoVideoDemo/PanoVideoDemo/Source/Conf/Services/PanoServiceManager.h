//
//  PanoServiceManager.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, PanoServiceType) {
    PanoPoolServiceType,
    PanoUserServiceType,
    PanoVideoServiceType,
    PanoAudioServiceType,
    PanoDesktopServiceType,
    PanoClientServiceType,
    PanoWhiteboardServiceType,
};

/**
 Service 管理类， 退出Room 后 需要 调用 `uninit`
 */
@interface PanoServiceManager : NSObject

+ (id)serviceWithType:(PanoServiceType)type;

+ (void)uninit;

@end

NS_ASSUME_NONNULL_END
