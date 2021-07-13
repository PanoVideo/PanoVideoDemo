//
//  SampleHandler.m
//  Broadcast
//
//  Copyright © 2021 Pano. All rights reserved.
//


#import "SampleHandler.h"
#import <PanoReplayKitExt/PanoReplayKitExt.h>

@interface SampleHandler () <PanoScreenSharingExtDelegate>

@end

@implementation SampleHandler

- (void)broadcastStartedWithSetupInfo:(NSDictionary<NSString *,NSObject *> *)setupInfo {
    // User has requested to start the broadcast. Setup info from the UI extension can be supplied but optional.
    // 1. 需要在 BroadcastExtension  和 PanoVideoDemo 配置 App Group 用于共享屏幕
    NSString *appGroup = <#T##请输入AppGroup: String##String#>;
    [PanoScreenSharingExt.sharedInstance setupWithAppGroup:appGroup delegate:self];
    PanoScreenSharingExt.sharedInstance.delegate = self;
    [self postNotificaitonWithIdentifier:@"ShareScreenStartNotification"];
}

- (void)broadcastPaused {
    // User has requested to pause the broadcast. Samples will stop being delivered.
}

- (void)broadcastResumed {
    // User has requested to resume the broadcast. Samples delivery will resume.
}

- (void)broadcastFinished {
    // User has requested to finish the broadcast.
    [PanoScreenSharingExt.sharedInstance finishScreenSharing];
    [self postNotificaitonWithIdentifier:@"ShareScreenFinishedNotification"];
}

- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType {
    
    switch (sampleBufferType) {
        case RPSampleBufferTypeVideo:
            // Handle video sample buffer
            [PanoScreenSharingExt.sharedInstance sendVideoSampleBuffer:sampleBuffer];
            break;
        case RPSampleBufferTypeAudioApp:
            // Handle audio sample buffer for app audio
            break;
        case RPSampleBufferTypeAudioMic:
            // Handle audio sample buffer for mic audio
            break;
            
        default:
            break;
    }
}

//MARK: - PanoScreenSharingExtDelegate

- (void)screenSharingFinished:(PanoScreenSharingResult)reason {
    NSLog(@"screenSharingFinished: %lu", (unsigned long)reason);
    [self postNotificaitonWithIdentifier:@"ShareScreenFinishedNotification"];
    NSString *log;
    switch (reason) {
        case PanoScreenSharingResultVersionMismatch:
            log = @"SDK版本不匹配";
            break;
        case PanoScreenSharingResultCloseByHost:
            log = NSLocalizedString(@"Screen share is closed by host app", nil);
            break;
        case PanoScreenSharingResultDisconnected:
            log = NSLocalizedString(@"The connect disconnected", nil);
            break;
        default:
            break;
    }
    NSError *error = [NSError errorWithDomain:NSStringFromClass([self classForCoder]) code:0 userInfo:@{ NSLocalizedFailureReasonErrorKey : log }];
    [self finishBroadcastWithError:error];
}

- (void)postNotificaitonWithIdentifier:(NSString *)identifier {
    CFNotificationCenterRef notification = CFNotificationCenterGetDarwinNotifyCenter ();
    CFNotificationCenterPostNotification(notification, (__bridge CFStringRef)identifier, NULL,NULL, YES);
}

@end
