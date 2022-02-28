//
//  PanoVideoDemoClient.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoRtc/PanoRtcEngineKit.h"
#import "Reachability.h"
#import "PanoRtcService.h"
#import "PanoWhiteboardService.h"

#import "PanoPoolService.h"
#import "PanoAudioService.h"
#import "PanoVideoService.h"
#import "PanoDesktopService.h"
#import "PanoUserService.h"

#import "PanoStatisticsService.h"

#import "PanoConfig.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString * _Nullable kDemoAppId;
extern NSString * _Nullable kDemoTempToken;

@interface PanoCallClient : NSObject

@property (strong, nonatomic, readonly) NSString * uuid;

@property (assign, nonatomic) BOOL host;
@property (strong, nonatomic) NSString * roomId;
@property (strong, nonatomic) NSString * userName;
@property (assign, nonatomic) UInt64 userId;
@property (strong, nonatomic) NSString * mobileNumber;
@property (assign, nonatomic) BOOL autoMute;
@property (assign, nonatomic) BOOL autoVideo;
@property (assign, nonatomic) BOOL staticsFlag;
@property (assign, nonatomic) PanoVideoProfileType resolution;
@property (assign, nonatomic) PanoVideoFrameRateType frameRate;
@property (assign, nonatomic, readonly) PanoVideoProfileType videoProfile;
@property (assign, nonatomic) BOOL autoSpeaker;
@property (assign, nonatomic) BOOL leaveConfirm;
@property (assign, nonatomic) BOOL faceBeautify;
@property (assign, nonatomic) Float32 beautifyIntensity;
@property (assign, nonatomic) BOOL advancedBeautify;
@property (assign, nonatomic) Float32 cheekThinningIntensity;
@property (assign, nonatomic) Float32 eyeEnlargingIntensity;
@property (assign, nonatomic) BOOL debugMode;

@property (strong, nonatomic, readonly) PanoConfig *config;
@property (strong, nonatomic, readonly) PanoRtcEngineKit * engineKit;
@property (strong, nonatomic) id<PanoRtcEngineDelegate> _Nullable channelDelegate;
@property (strong, nonatomic) Reachability *reachability;
@property (strong, nonatomic, readonly) PanoRtcService<id<PanoRtcDelegate>> *rtcService;
@property (strong, nonatomic, readonly) PanoWhiteboardService<id<PanoWhiteboardDelegate>> *wb;
@property (strong, nonatomic, readonly) PanoPoolService<id<PanoPoolDelegate>> *pool;
@property (strong, nonatomic, readonly) PanoAudioService *audio;
@property (strong, nonatomic, readonly) PanoVideoService *video;
@property (strong, nonatomic, readonly) PanoDesktopService *screen;
@property (strong, nonatomic, readonly) PanoUserService *userMgr;
@property (strong, nonatomic, readonly) PanoStatisticsService *statistics;

+ (instancetype)shared;

- (instancetype)init NS_UNAVAILABLE;

- (void)start;

- (void)stop;

- (PanoResult)startAudioDump;

- (PanoResult)stopAudioDump;

- (void)updateAV1Encoding;

+ (NSString *)productName;

+ (NSString *)productVersion;

+ (NSString *)productShortVersion;

@end

NS_ASSUME_NONNULL_END
