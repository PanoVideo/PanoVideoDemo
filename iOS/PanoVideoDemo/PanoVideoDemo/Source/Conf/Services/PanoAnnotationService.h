//
//  PanoAnnotationService.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PanoViewInstance.h"
#import "PanoRtc/PanoRtcEngineKit.h"
#import "PanoPoolProtocols.h"

NS_ASSUME_NONNULL_BEGIN

@interface PanoAnnotationService : NSObject <PanoRtcAnnotationManagerDelegate>

@property (nonatomic, weak) id <PanoAnnotationDelegate> delegate;

- (void)startAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)stopAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)hideAnnotationWithItem:(PanoAnnotationItem *)item;

- (void)pauseAnnotationWithItem:(PanoAnnotationItem *)item;

- (PanoRtcAnnotation *)rtcAnnotationWithItem:(PanoAnnotationItem *)item;

- (PanoWBColor *)convertWBColor:(UIColor *)color;

@end

NS_ASSUME_NONNULL_END
