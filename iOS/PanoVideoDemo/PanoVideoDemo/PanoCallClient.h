//
//  PanoVideoDemoClient.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoRtc/PanoRtcEngineKit.h"

extern NSString * _Nullable kDemoAppId;
extern NSString * _Nullable kDemoPanoServer;
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
@property (assign, nonatomic) BOOL autoSpeaker;
@property (assign, nonatomic) BOOL leaveConfirm;
@property (assign, nonatomic) BOOL faceBeautify;
@property (assign, nonatomic) Float32 beautifyIntensity;
@property (assign, nonatomic) BOOL advancedBeautify;
@property (assign, nonatomic) Float32 cheekThinningIntensity;
@property (assign, nonatomic) Float32 eyeEnlargingIntensity;
@property (assign, nonatomic) BOOL debugMode;

@property (assign, nonatomic) PanoWBRoleType wbRole;
@property (assign, nonatomic) PanoWBToolType wbToolType;
@property (assign, nonatomic) UInt32 wbLineWidth;
@property (strong, nonatomic) UIColor * wbColor;
@property (assign, nonatomic) PanoWBFontStyle wbFontStyle;
@property (assign, nonatomic) UInt32 wbFontSize;

@property (strong, nonatomic, readonly) PanoRtcEngineKit * engineKit;
@property (strong, nonatomic) id<PanoRtcEngineDelegate> _Nullable channelDelegate;

+ (instancetype)sharedInstance;
- (instancetype)init NS_UNAVAILABLE;

- (PanoResult)startAudioDump;
- (PanoResult)stopAudioDump;

- (BOOL)recordUsage;

+ (NSString *)productName;
+ (NSString *)productVersion;

+ (void)updatePanoConfigWithAppId:(NSString *)appId
                         rtcServer:(NSString *)server
                             token:(NSString *)token;
@end

NS_ASSUME_NONNULL_END
