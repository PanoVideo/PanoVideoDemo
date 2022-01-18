//
//  PanoAnnotationService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PanoViewInstance.h"
#import "PanoPoolProtocols.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoBaseService.h"
#import "PanoUserService.h"

NS_ASSUME_NONNULL_BEGIN

/// 共享/ 视频标注核心接口
@interface PanoAnnotationService<T> : PanoBaseService<T>

@property (nonatomic, weak) id <PanoAnnotationDelegate> delegate;

- (void)startAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)stopAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)hideAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)pauseAnnotationWithItem:(PanoAnnotationItem *)item;

- (PanoRtcAnnotation *)rtcAnnotationWithItem:(PanoAnnotationItem *)item;

- (PanoWBColor *)convertWBColor:(UIColor *)color;

/// 所有的视频标注、共享标注
- (NSArray<PanoAnnotationItem *> *)allAnnotations;

/// 最近的视频标注、共享标注
- (PanoAnnotationItem *)lastAnnotation;

@end


@interface PanoAnnotationService ()<PanoRtcAnnotationManagerDelegate>

@end

@interface PanoAnnotationService (User) <PanoUserDelegate>

@end

NS_ASSUME_NONNULL_END
