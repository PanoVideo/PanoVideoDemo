//
//  PanoAudioService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoAudioService.h"
#import "PanoCallClient.h"
#import "PanoServiceManager.h"
#import "PanoUserService.h"

@implementation PanoAudioService

- (BOOL)muteAudio:(BOOL)mute {
    PanoResult result = -1;
    if (mute) {
        result = [PanoCallClient.sharedInstance.engineKit muteAudio];
        if (result == kPanoResultOK) {
            [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioMute:PanoCallClient.sharedInstance.userId];
        }
    } else {
        result = [PanoCallClient.sharedInstance.engineKit unmuteAudio];
        if (result == kPanoResultOK) {
            [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioUnmute:PanoCallClient.sharedInstance.userId];
        }
    }
    return  result == kPanoResultOK;
}

- (BOOL)switchAudioEnable:(BOOL)enable {
    PanoResult result = -1;
    if (enable) {
        result = [PanoCallClient.sharedInstance.engineKit startAudio];
        if (result == kPanoResultOK) {
            [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioStart:PanoCallClient.sharedInstance.userId];
        }
    } else {
        result = [PanoCallClient.sharedInstance.engineKit stopAudio];
        if (result == kPanoResultOK) {
            [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioStop:PanoCallClient.sharedInstance.userId];
        }
    }
    return  result == kPanoResultOK;
}

@end
