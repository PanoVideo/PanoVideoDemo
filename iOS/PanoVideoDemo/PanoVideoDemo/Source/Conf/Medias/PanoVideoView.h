//
//  PanoVideoView.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseMediaView.h"

NS_ASSUME_NONNULL_BEGIN

/// 视频 View
@interface PanoVideoView : PanoBaseMediaView

@property (strong, nonatomic, readonly) UILabel * userName;

@property (strong, nonatomic, readonly) UIImageView * micIcon;

@property (strong, nonatomic, readonly) UIImageView * avatarImageView;

@property (strong, nonatomic, readonly) UIImageView * signalView;

@end

NS_ASSUME_NONNULL_END
