//
//  PanoDesktopService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoDesktopService.h"
#import "PanoCallClient.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoUserInfo.h"
#import "PanoNotificationCenter.h"

@interface PanoDesktopService ()
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, NSNumber *>* screenStatuses;

@property (nonatomic, copy) void(^handler)(BOOL);
@end


@implementation PanoDesktopService

- (NSMutableDictionary<NSNumber *,NSNumber *> *)screenStatuses {
    if (!_screenStatuses) {
        _screenStatuses = [NSMutableDictionary dictionary];
    }
    return _screenStatuses;
}

- (void)startScreenShareWithView:(UIView *)view user:(PanoUserInfo *)user {
    if ([self.screenStatuses objectForKey:@(user.userId)].integerValue == PanoUserScreen_Unmute) {
        return;
    }
    PanoResult result = [PanoCallClient.sharedInstance.engineKit subscribeScreen:user.userId withView:view];
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserScreen_Unmute) : @(PanoUserScreen_None);
    [_screenStatuses setObject:staus forKey:@(user.userId)];
}

- (void)stopScreenShareWithUser:(PanoUserInfo *)user {
    if ([self.screenStatuses objectForKey:@(user.userId)].integerValue != PanoUserVideo_Unmute) {
        return;
    }
    PanoResult result = [PanoCallClient.sharedInstance.engineKit unsubscribeScreen:user.userId];
    NSNumber *staus = result == kPanoResultOK ? @(PanoUserScreen_None) : (_screenStatuses[@(user.userId)] ?: @(PanoUserVideo_None));
    [_screenStatuses setObject:staus forKey:@(user.userId)];
}

- (void)onUserScreenSubscribe:(UInt64)userId withResult:(PanoSubscribeResult)result {
    NSLog(@"onUserScreenSubscribe-> userId-> %ld result-> %ld", (long)userId,(long)result);
}

- (void)onScreenStartResult:(PanoResult)result {
    NSLog(@"-> %@",[NSString stringWithFormat:@"Start Screen Sharing with result: %ld.", (long)result]);
}

- (void)startShareWithHandler:(void (^)(BOOL))handler{
    if (@available(iOS 11.0, *)) {
        self.handler = handler;
        [PanoCallClient.sharedInstance.engineKit startScreenWithAppGroupId:@"group.video.pano.PanoCall"];
    }
    [[PanoNotificationCenter defaultCenter] listenWithIdentifier:@"ShareScreenFinishedNotification" listener:^(id  _Nullable messageObject) {
        self->_isSharingScreen = false;
    }];
    [[PanoNotificationCenter defaultCenter] listenWithIdentifier:@"ShareScreenStartNotification" listener:^(id  _Nullable messageObject) {
        self->_isSharingScreen = true;
        if (self.handler) {
            self.handler(true);
        }
    }];
}

- (void)stopShare {
    [PanoCallClient.sharedInstance.engineKit stopScreen];
    [self removeObservers];
}

- (void)removeObservers {
    [[PanoNotificationCenter defaultCenter] stopListeningWithIdentifier:@"ShareScreenFinishedNotification"];
    [[PanoNotificationCenter defaultCenter] stopListeningWithIdentifier:@"ShareScreenStartNotification"];
}

//- (void)startAnnotationWithView:(UIView *)view user:(PanoUserInfo *)user {
//    PanoRtcAnnotationManager *annotationMgr =  PanoCallClient.sharedInstance.engineKit.annotationManager;
//    PanoRtcAnnotation *annotation = [annotationMgr shareAnnotation:user.userId];
//    PanoResult res = [annotation startAnnotation:view];
//    [annotation setToolType:kPanoWBToolPath];
//    PanoWBColor *wbColor = [self convertWBColor:UIColor.cyanColor];
//    [annotation setLineWidth:3.0];
//    [annotation setColor:wbColor];
//    [PanoCallClient.sharedInstance.engineKit updateScreenScaling:user.userId withRatio:kPanoScalingFitRatio];
//    NSLog(@"res = %zd", res);
//}
//
//- (PanoWBColor *)convertWBColor:(UIColor *)color {
//    CGFloat red = 0.0, green = 0.0, blue = 0.0, alpha = 0.0;
//    [color getRed:&red green:&green blue:&blue alpha:&alpha];
//    PanoWBColor * wbColor = [PanoWBColor new];
//    wbColor.red = red;
//    wbColor.green = green;
//    wbColor.blue = blue;
//    wbColor.alpha = alpha;
//    return wbColor;
//}
//
//- (void)stopAnnotationWithUser:(PanoUserInfo *)user {
//    PanoRtcAnnotationManager *annotationMgr =  PanoCallClient.sharedInstance.engineKit.annotationManager;
//    PanoRtcAnnotation *annotation = [annotationMgr shareAnnotation:user.userId];
//    PanoResult res = [annotation stopAnnotation];
//}

- (void)dealloc {
    NSLog(@"dealloc");
    [self removeObservers];
}

@end
