//
//  PanoVideoDemoClient.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoRtc/PanoRtcEngineKit.h"
#import "Reachability.h"
#import "PanoRTMService.h"
#import "PanoConfig.h"

extern NSString * _Nullable kDemoAppId;
extern NSString * _Nullable kDemoTempToken;

NS_ASSUME_NONNULL_BEGIN

@interface PanoCallClient : NSObject

@property (strong, nonatomic, readonly) NSString * uuid;

@property (assign, nonatomic) BOOL host;
@property (strong, nonatomic) NSString * roomId;
@property (strong, nonatomic) NSString * userName;
@property (assign, nonatomic) UInt64 userId;
@property (strong, nonatomic) NSString * mobileNumber;
@property (assign, nonatomic) BOOL autoMute;
@property (assign, nonatomic) BOOL autoVideo;
@property (assign, nonatomic) PanoVideoProfileType resolution;
@property (assign, nonatomic, readonly) PanoVideoProfileType videoProfile;
@property (assign, nonatomic) BOOL autoSpeaker;
@property (assign, nonatomic) BOOL leaveConfirm;
@property (assign, nonatomic) BOOL faceBeautify;
@property (assign, nonatomic) Float32 beautifyIntensity;
@property (assign, nonatomic) BOOL advancedBeautify;
@property (assign, nonatomic) Float32 cheekThinningIntensity;
@property (assign, nonatomic) Float32 eyeEnlargingIntensity;
@property (assign, nonatomic) BOOL debugMode;

@property (strong, nonatomic) PanoConfig *config;
@property (strong, nonatomic, readonly) PanoRtcEngineKit * engineKit;
@property (strong, nonatomic) id<PanoRtcEngineDelegate> _Nullable channelDelegate;
@property (strong, nonatomic) Reachability *reachability;
@property (strong, nonatomic, readonly) PanoRTMService *rtmService;

+ (instancetype)sharedInstance;
- (instancetype)init NS_UNAVAILABLE;

- (PanoResult)startAudioDump;
- (PanoResult)stopAudioDump;

+ (NSString *)productName;
+ (NSString *)productVersion;

@end

NS_ASSUME_NONNULL_END
