//
//  PanoPermission.h
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PanoPermission : NSObject

+ (BOOL)checkVideoPermission;

+ (BOOL)checkAudioPermission;

@end

NS_ASSUME_NONNULL_END
