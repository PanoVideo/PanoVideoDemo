//
//  PanoAudioService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoAudioService.h"
#import "PanoCallClient.h"

@implementation PanoAudioService

- (BOOL)muteAudio:(BOOL)mute {
    PanoResult result = kPanoResultFailed;
    if (mute) {
        result = [PanoCallClient.shared.engineKit muteAudio];
        if (result == kPanoResultOK) {
            [PanoCallClient.shared.userMgr onUserAudioMute:PanoCallClient.shared.userId];
        }
    } else {
        result = [PanoCallClient.shared.engineKit unmuteAudio];
        if (result == kPanoResultOK) {
            [PanoCallClient.shared.userMgr onUserAudioUnmute:PanoCallClient.shared.userId];
        }
    }
    return  result == kPanoResultOK;
}

- (BOOL)switchAudioEnable:(BOOL)enable {
    PanoResult result = -1;
    if (enable) {
        result = [PanoCallClient.shared.engineKit startAudio];
        if (result == kPanoResultOK) {
            [PanoCallClient.shared.userMgr onUserAudioStart:PanoCallClient.shared.userId];
        }
    } else {
        result = [PanoCallClient.shared.engineKit stopAudio];
        if (result == kPanoResultOK) {
            [PanoCallClient.shared.userMgr onUserAudioStop:PanoCallClient.shared.userId];
        }
    }
    return  result == kPanoResultOK;
}

@end
