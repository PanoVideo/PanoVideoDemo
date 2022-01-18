//
//  PanoAudioService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"

NS_ASSUME_NONNULL_BEGIN
/// 音频核心接口
@interface PanoAudioService : PanoBaseService

- (BOOL)muteAudio:(BOOL)mute;

- (BOOL)switchAudioEnable:(BOOL)enable;

@end

NS_ASSUME_NONNULL_END
