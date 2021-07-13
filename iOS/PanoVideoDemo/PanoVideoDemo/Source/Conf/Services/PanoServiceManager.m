//
//  PanoServiceManager.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoServiceManager.h"
#import "PanoPoolService.h"
#import "PanoVideoService.h"
#import "PanoUserService.h"
#import "PanoAudioService.h"
#import "PanoDesktopService.h"
#import "PanoClientService.h"
#import "PanoWhiteboardService.h"

static NSMutableDictionary<NSNumber *, PanoBaseService *> * g_services;

@implementation PanoServiceManager

+ (id)serviceWithType:(PanoServiceType)type {
    if (!g_services) {
        g_services = [NSMutableDictionary dictionary];
        [g_services setObject:[[PanoPoolService alloc] init] forKey:@(PanoPoolServiceType)];
        [g_services setObject:[[PanoUserService alloc] init] forKey:@(PanoUserServiceType)];
        [g_services setObject:[[PanoVideoService alloc] init] forKey:@(PanoVideoServiceType)];
        [g_services setObject:[[PanoAudioService alloc] init] forKey:@(PanoAudioServiceType)];
        [g_services setObject:[[PanoDesktopService alloc] init] forKey:@(PanoDesktopServiceType)];
        [g_services setObject:[[PanoClientService alloc] init] forKey:@(PanoClientServiceType)];
        [g_services setObject:[[PanoWhiteboardService alloc] init] forKey:@(PanoWhiteboardServiceType)];
    }
    return [g_services objectForKey:@(type)];
}

+ (void)uninit {
    [g_services removeAllObjects];
    g_services = nil;
}

@end
