//
//  PanoClientService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoClientService.h"
#import "PanoCallClient.h"
#import "PanoUserService.h"
#import "PanoServiceManager.h"


NSString *const kUploadLog = @"uploadLog";
static NSTimeInterval kVersionCheckInterval = 24 * 3600;   // 24 hours
static NSString * kDefaultUpdateUrl = @"https://apps.apple.com/cn/app/pano-video-call/id1508485860";

static BOOL VersionChecked = false;
static NSTimeInterval VersionCheckTime = 0;

@interface PanoClientService ()
@end

@implementation PanoClientService

+ (void)checkVersion {}

+ (void)updateVersionAlert:(BOOL)forceupdate {
    NSString *title = forceupdate ? NSLocalizedString(@"upgradeTitleForce", nil) : NSLocalizedString(@"upgradeTitle", nil);
    NSString *msg = forceupdate ? NSLocalizedString(@"upgradeAlertForce", nil) : NSLocalizedString(@"upgradeAlert", nil);
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:title
                                                                    message:msg
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"upgrade", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [UIApplication.sharedApplication openURL:[NSURL URLWithString:kDefaultUpdateUrl]];
        if (forceupdate) {
            exit(EXIT_SUCCESS);
        }
    }];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"notUpgrade", nil)
                                                      style:UIAlertActionStyleDefault
                                                    handler:nil];
    if (!forceupdate) {
        [alert addAction:cancel];
    }
    [alert addAction:ok];
    UIViewController *vc = [[UIApplication sharedApplication] keyWindow].rootViewController;
    if (!vc.presentedViewController) {
        [vc presentViewController:alert animated:YES completion:^{
        }];
    }
}


#pragma mark -- PanoUserDelegate
- (void)onUserAdded:(PanoUserInfo *)user {
    // CHECK
    NSInteger me = PanoCallClient.sharedInstance.userId;
    if (me == _hostId && user.userId != me) {
        NSLog(@"onUserAddedSendMessage");
    }
}

/*
- (void)checkHost {
    if (PanoCallClient.sharedInstance.host) {
        [self performSelector:@selector(innerCheckHost) withObject:nil afterDelay:1];
    }
}

- (void)innerCheckHost {
    if (_message && _message[kHostId]) {
        NSInteger hostId = [_message[kHostId] integerValue];
        _hostId = hostId;
    } else if (PanoCallClient.sharedInstance.host) {
        // It is me host
        _hostId = PanoCallClient.sharedInstance.userId;
        [self broadcastMessage:@{kHostId : @(_hostId)}];
    }
} */

- (void)notifyAllUsersUploadLog {
    if (_hostId == PanoCallClient.sharedInstance.host) {
        
    }
}

- (void)uploadLog {
    PanoFeedbackInfo * info = [PanoFeedbackInfo new];
    info.type = kPanoFeedbackGeneral;
    info.productName = PanoCallClient.productName;
    info.detailDescription = @"主持人自动上传日志";
    info.contact = PanoCallClient.sharedInstance.mobileNumber;
    info.extraInfo = PanoCallClient.sharedInstance.uuid;
    info.uploadLogs = true;
    [PanoCallClient.sharedInstance.engineKit sendFeedback:info];
}

@end
