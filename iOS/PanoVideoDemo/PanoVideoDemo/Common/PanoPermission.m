//
//  PanoPermission.m
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "PanoPermission.h"
#import <AVFoundation/AVFoundation.h>

@implementation PanoPermission

+ (BOOL)checkVideoPermission {
    return [self checkPermissionWithType:AVMediaTypeVideo];
}

+ (BOOL)checkAudioPermission {
    return [self checkPermissionWithType:AVMediaTypeAudio];
}

+ (BOOL)checkPermissionWithType:(AVMediaType)type {
    __block BOOL granted = true;
    dispatch_sync(dispatch_get_global_queue(0, 0), ^{
        dispatch_semaphore_t sema = dispatch_semaphore_create(0);
        [AVCaptureDevice requestAccessForMediaType:type completionHandler:^(BOOL _granted) {
            granted = _granted;
            dispatch_semaphore_signal(sema);
        }];
        dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER);
    });
    return granted;
}

@end
