//
//  PanoWhiteBoradView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseMediaView.h"

NS_ASSUME_NONNULL_BEGIN
@protocol PanoRoleDelegate;
/**
 白板VIew
 */
@interface PanoWhiteboardView : PanoBaseMediaView

@property (nonatomic, weak) id <PanoRoleDelegate> delegate;

@property (nonatomic, assign) BOOL enable;

@end

NS_ASSUME_NONNULL_END
