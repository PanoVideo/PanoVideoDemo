//
//  VideoFilterDelegate.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoRtc/PanoRtcEngineKit.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoFilterDelegate : NSObject <PanoRtcVideoFilterDelegate>

- (void)setBeautifyIntensity:(Float32)intensity;
- (void)setCheekThinningIntensity:(Float32)intensity;
- (void)setEyeEnlargingIntensity:(Float32)intensity;

@end

NS_ASSUME_NONNULL_END
