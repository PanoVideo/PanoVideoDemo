//
//  PanoBaseMediaView.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseView.h"
#import "PanoViewInstance.h"
NS_ASSUME_NONNULL_BEGIN

typedef NSString * PanoMediaInfoKey;

extern PanoMediaInfoKey const PanoTopToolBarState;

extern PanoMediaInfoKey const PanoNetworkStatus;

extern PanoMediaInfoKey const PanoMediaViewPostionKey;

extern PanoMediaInfoKey const PanoFloatViewPostionKey;

typedef void(^PanoDoubleTapBlock)(PanoViewInstance *instance);

@interface PanoBaseMediaView : PanoBaseView

@property (nonatomic, strong, nullable) PanoViewInstance *instance;

@property (nonatomic, strong, readonly) UIView *contentView;

@property (nonatomic, copy) PanoDoubleTapBlock doubleClickBlock;

@property (nonatomic, assign) BOOL enableDoubleClick;

@property (nonatomic, assign) BOOL active;

@property (strong, nonatomic, readonly) UIView *annotationView;

- (void)start;

- (void)stop;

- (void)update:(NSDictionary<PanoMediaInfoKey,id> *)info;

- (void)startAnnotation:(PanoAnnotationItem *)annotation;

- (void)stopAnnotation:(PanoAnnotationItem *)annotation;

- (void)hideAnnotation:(PanoAnnotationItem *)annotation;
@end

NS_ASSUME_NONNULL_END
