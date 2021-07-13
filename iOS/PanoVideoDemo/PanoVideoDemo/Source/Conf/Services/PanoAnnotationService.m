//
//  PanoAnnotationService.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAnnotationService.h"
#import "PanoCallClient.h"
#import "PanoUserService.h"
#import "PanoServiceManager.h"

@interface PanoAnnotationService ()
@end

@implementation PanoAnnotationService

#pragma mark -- Public Methods
- (void)startAnnotationWithItem:(PanoAnnotationItem *)item {
    PanoRtcAnnotation *annotation = [self rtcAnnotationWithItem:item];
    if (!annotation) {
        return;
    }
    [annotation setVisible:true];
    PanoResult res = [annotation startAnnotation:item.view];
    if (res != kPanoResultOK) {
        NSLog(@"startAnnotation result: %zd",res);
        return;
    }
    if (item.type == PanoViewInstance_Desktop) {
        [PanoCallClient.sharedInstance.engineKit updateScreenScaling:item.userId withRatio:kPanoScalingFitRatio];
    }
    [PanoCallClient.sharedInstance.engineKit updateScreenScaling:item.userId withRatio:kPanoScalingFitRatio];
    NSLog(@"res = %zd", res);
}

- (PanoWBColor *)convertWBColor:(UIColor *)color {
    CGFloat red = 0.0, green = 0.0, blue = 0.0, alpha = 0.0;
    [color getRed:&red green:&green blue:&blue alpha:&alpha];
    PanoWBColor * wbColor = [PanoWBColor new];
    wbColor.red = red;
    wbColor.green = green;
    wbColor.blue = blue;
    wbColor.alpha = alpha;
    return wbColor;
}

- (void)stopAnnotationWithItem:(PanoAnnotationItem *)item {
    PanoRtcAnnotation *annotation = [self rtcAnnotationWithItem:item];
    PanoResult res = [annotation stopAnnotation];
    NSLog(@"stopAnnotation-> %zd",res);
}

- (void)hideAnnotationWithItem:(PanoAnnotationItem *)item {
    PanoRtcAnnotation *annotation = [self rtcAnnotationWithItem:item];
    [annotation setVisible:false];
}

- (void)pauseAnnotationWithItem:(PanoAnnotationItem *)item {
    PanoRtcAnnotation *annotation = [self rtcAnnotationWithItem:item];
    [annotation setToolType:kPanoWBToolNone];
}

- (PanoRtcAnnotation *)rtcAnnotationWithItem:(PanoAnnotationItem *)item {
    PanoRtcAnnotationManager *annotationMgr =  PanoCallClient.sharedInstance.engineKit.annotationManager;
    PanoRtcAnnotation *annotation = nil;
    if (item.type == PanoViewInstance_Video) {
        annotation = [annotationMgr videoAnnotation:item.userId stream:item.streamId];
    } else if (item.type == PanoViewInstance_Desktop) {
        annotation = [annotationMgr shareAnnotation:item.userId];
    }
    return annotation;
}

- (PanoRtcAnnotation *)rtcAnnotationWithUserId:(NSUInteger)userId {
    PanoAnnotationItem *item = [[PanoAnnotationItem alloc] init];
    item.userId = userId;
    item.streamId = 0;
    return [self rtcAnnotationWithItem:item];
}

#pragma mark -- PanoRtcAnnotationManagerDelegate
- (void)onShareAnnotationStart:(UInt64)userId {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
        if (![userService findUserWithId:userId]) {
            return;
        }
        PanoAnnotationItem *item = [PanoAnnotationItem new];
        item.userId = userId;
        item.type = PanoViewInstance_Desktop;
        if ([self.delegate respondsToSelector:@selector(onAnnotationStart:)]) {
            [self.delegate onAnnotationStart:item];
        }
    });
}

- (void)onShareAnnotationStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"onShareAnnotationStop %llu",userId);
        if ([self.delegate respondsToSelector:@selector(onAnnotationStop:)]) {
            PanoAnnotationItem *item = [PanoAnnotationItem new];
            item.userId = userId;
            item.type = PanoViewInstance_Desktop;
            [self.delegate onAnnotationStop:item];
        }
    });
}

- (void)onVideoAnnotationStart:(UInt64)userId stream:(SInt32)streamId {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSLog(@"onVideoAnnotationStart %llu",userId);
        PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
        if (![userService findUserWithId:userId]) {
            return;
        }
        PanoAnnotationItem *item = [PanoAnnotationItem new];
        item.userId = userId;
        item.streamId = streamId;
        item.type = PanoViewInstance_Video;
        if ([self.delegate respondsToSelector:@selector(onAnnotationStart:)]) {
            [self.delegate onAnnotationStart:item];
        }
    });
}

- (void)onVideoAnnotationStop:(UInt64)userId stream:(SInt32)streamId {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"onVideoAnnotationStop %llu",userId);
        if ([self.delegate respondsToSelector:@selector(onAnnotationStop:)]) {
            PanoAnnotationItem *item = [PanoAnnotationItem new];
            item.userId = userId;
            item.streamId = streamId;
            item.type = PanoViewInstance_Video;
            [self.delegate onAnnotationStop:item];
        }
    });
}

@end
