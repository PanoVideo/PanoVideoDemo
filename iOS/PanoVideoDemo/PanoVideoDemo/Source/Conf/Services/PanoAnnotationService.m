//
//  PanoAnnotationService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAnnotationService.h"
#import "PanoCallClient.h"
#import "NSArray+Extension.h"

@interface PanoAnnotationService ()
/// 所有的桌面批注/视频批注
@property (strong, nonatomic) NSMutableArray<PanoAnnotationItem *> *annotations;
@end

@implementation PanoAnnotationService

- (NSMutableArray<PanoAnnotationItem *> *)annotations {
    if (!_annotations) {
        _annotations = [NSMutableArray array];
    }
    return _annotations;
}

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
        [PanoCallClient.shared.engineKit updateScreenScaling:item.userId withRatio:kPanoScalingFitRatio];
    }
    [PanoCallClient.shared.engineKit updateScreenScaling:item.userId withRatio:kPanoScalingFitRatio];
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
    PanoRtcAnnotationManager *annotationMgr =  PanoCallClient.shared.engineKit.annotationManager;
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

- (NSArray<PanoAnnotationItem *> *)allAnnotations {
    return [NSArray arrayWithArray:self.annotations];
}

- (PanoAnnotationItem *)lastAnnotation {
    return self.annotations.lastObject;
}

#pragma mark -- PanoRtcAnnotationManagerDelegate
- (void)onShareAnnotationStart:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        PanoAnnotationItem *item = [[PanoAnnotationItem alloc] initWithUserId:userId type:PanoViewInstance_Desktop];
        [self.annotations addObject:item];
        // 如果当前用户没有加入RTC，不需要往上层回调，等待加入RTC后在回调
        if ([PanoCallClient.shared.userMgr findUserWithId:userId]) {
            if ([self.delegate respondsToSelector:@selector(onAnnotationStart:)]) {
                [self.delegate onAnnotationStart:item];
            }
        }
    });
}


- (void)onShareAnnotationStop:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"onShareAnnotationStop %llu",userId);
        PanoAnnotationItem *item = [[PanoAnnotationItem alloc] initWithUserId:userId type:PanoViewInstance_Desktop];
        NSUInteger index = [self.annotations indexOfObject:item];
        if (index != NSNotFound) {
            [self.annotations removeObjectAtIndex:index];
            if ([self.delegate respondsToSelector:@selector(onAnnotationStop:)]) {
                [self.delegate onAnnotationStop:item];
            }
        }
    });
}

- (void)onVideoAnnotationStart:(UInt64)userId stream:(SInt32)streamId {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"onVideoAnnotationStart %llu",userId);
        PanoAnnotationItem *item = [[PanoAnnotationItem alloc] initWithUserId:userId type:PanoViewInstance_Video streamId:streamId];
        [self.annotations addObject:item];
        if ([PanoCallClient.shared.userMgr findUserWithId:userId]) {
            if ([self.delegate respondsToSelector:@selector(onAnnotationStart:)]) {
                [self.delegate onAnnotationStart:item];
            }
        }
    });
}

- (void)onVideoAnnotationStop:(UInt64)userId stream:(SInt32)streamId {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"onVideoAnnotationStop %llu",userId);
        PanoAnnotationItem *item = [[PanoAnnotationItem alloc] initWithUserId:userId type:PanoViewInstance_Video streamId:streamId];
        NSUInteger index = [self.annotations indexOfObject:item];
        if (index != NSNotFound) {
            [self.annotations removeObjectAtIndex:index];
            if ([self.delegate respondsToSelector:@selector(onAnnotationStop:)]) {
                [self.delegate onAnnotationStop:item];
            }
        }
    });
}



@end


@implementation PanoAnnotationService (User)

#pragma mark -- PanoUserDelegate
- (void)onUserAdded:(PanoUserInfo *)user {
    for (PanoAnnotationItem *item in self.annotations) {
        if (item.userId == user.userId) {
            // 如果发现当前用户开启了标注，需要回调App
            if ([self.delegate respondsToSelector:@selector(onAnnotationStart:)]) {
                [self.delegate onAnnotationStart:item];
            }
            break;
        }
    }
}

- (void)onUserRemoved:(PanoUserInfo *)user {
    [self.annotations removeObjectWithPredicate:^BOOL(PanoAnnotationItem * _Nonnull item, NSUInteger idx, BOOL * _Nonnull stop) {
        if (item.userId == user.userId) {
            // 如果发现当前用户关闭了标注，需要回调App
            if ([self.delegate respondsToSelector:@selector(onAnnotationStop:)]) {
                [self.delegate onAnnotationStop:item];
            }
            return true;
        };
        return false;
    }];
}

@end
