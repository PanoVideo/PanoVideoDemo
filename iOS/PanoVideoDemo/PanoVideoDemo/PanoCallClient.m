//
//  PanoVideoDemoClient.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoCallClient.h"
#import "VideoFilterDelegate.h"

//https://developer.pano.video/getting-started/firstapp/#1-注册账号
//https://developer.pano.video/getting-started/firstapp/#14-%E7%94%9F%E6%88%90%E4%B8%B4%E6%97%B6token

#error("Enter the kDemoAppId and kDemoTempToken. Visit the above link for help.")
NSString * kDemoAppId = @"请输入AppID";
NSString * kDemoTempToken = @"请输入Token";
NSString * kDemoPanoServer = @"api.pano.video";


// Http Request
static NSString * kHttpHeader = @"http";
static NSString * kHttpsHeader = @"https";
static NSString * kUrlUsageRecordApi = @"sirius/pvc";

// Preference Keys
static NSString * kUserUniqueIDKey = @"UUID";
static NSString * kUserNameKey = @"UserName";
static NSString * kMobileNumberKey = @"MobileNumber";
static NSString * kAutoMuteKey = @"AutoMute";
static NSString * kAutoVideoKey = @"AutoVideo";
static NSString * kResolutionKey = @"Resolution";
static NSString * kAutoSpeakerKey = @"AutoSpeaker";
static NSString * kLeaveConfirmKey = @"LeaveConfirm";
static NSString * kFaceBeautifyKey = @"FaceBeautify";
static NSString * kBeautifyIntensityKey = @"BeautifyIntensity";
static NSString * kAdvancedBeautifyKey = @"AdvancedBeautify";
static NSString * kCheekThinningKey = @"CheekThinning";
static NSString * kEyeEnlargingKey = @"EyeEnlarging";

// Audio Dump
static NSString * kAudioDumpFileName = @"pano_audio.dump";
static SInt64 kMaxAudioDumpFileSize = 200 * 1024 * 1024;

@interface PanoCallClient () <PanoRtcEngineDelegate>

@property (strong, nonatomic) VideoFilterDelegate * videoFilter;

@end

@implementation PanoCallClient

+ (instancetype)sharedInstance {
    static dispatch_once_t onceToken;
    static PanoCallClient *sharedInstance = nil;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init {
    self = [super init];
    if (nil != self) {
        _host = NO;
        _roomId = nil;
        _userName = nil;
        _userId = 0;
        _mobileNumber = nil;
        _autoMute = NO;
        _autoVideo = YES;
        _resolution = kPanoProfileStandard;
        _autoSpeaker = YES;
        _leaveConfirm = YES;
        _faceBeautify = NO;
        _beautifyIntensity = 0.5;
        _advancedBeautify = NO;
        _cheekThinningIntensity = 0.0;
        _eyeEnlargingIntensity = 0.0;
        _debugMode = NO;
        
        _wbRole = kPanoWBRoleAttendee;
        _wbToolType = kPanoWBToolPath;
        _wbLineWidth = 5;
        _wbColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:1.0];
        _wbFontStyle = kPanoWBFontNormal;
        _wbFontSize = 20;
        
        _uuid = nil;
        [self checkUUID];
        
        [self loadPreferences];
        [self createEngineKit];
        _channelDelegate = nil;
    }
#if defined(DEBUG)
    NSLog(@"[PanoCallClient init], self = %@", self);
#endif
    return self;
}

- (void)dealloc {
#if defined(DEBUG)
    NSLog(@"[PanoCallClient dealloc], self = %@", self);
#endif
    [self destroyEngineKit];
    [self savePreferences];
}

- (void)setHost:(BOOL)host {
    if (_host != host) {
        _host = host;
        _wbRole = host ? kPanoWBRoleAdmin : kPanoWBRoleAttendee;
    }
}

- (void)setUserName:(NSString *)userName {
    if (_userName.length != userName.length
            || [_userName compare:userName] != NSOrderedSame) {
        _userName = userName;
        [self savePreference:_userName forKey:kUserNameKey];
    }
}

- (void)setMobileNumber:(NSString *)mobileNumber {
    if (_mobileNumber.length != mobileNumber.length
            || [_mobileNumber compare:mobileNumber] != NSOrderedSame) {
        _mobileNumber = mobileNumber;
        [self savePreference:_mobileNumber forKey:kMobileNumberKey];
    }
}

- (void)setAutoMute:(BOOL)autoMute {
    if (_autoMute != autoMute) {
        _autoMute = autoMute;
        [self savePreference:[NSNumber numberWithBool:_autoMute] forKey:kAutoMuteKey];
    }
}

- (void)setAutoVideo:(BOOL)autoVideo {
    if (_autoVideo != autoVideo) {
        _autoVideo = autoVideo;
        [self savePreference:[NSNumber numberWithBool:_autoVideo] forKey:kAutoVideoKey];
    }
}

- (void)setResolution:(PanoVideoProfileType)resolution {
    if (_resolution != resolution) {
        _resolution = resolution;
        [self savePreference:[NSNumber numberWithInteger:_resolution] forKey:kResolutionKey];
    }
}

- (void)setAutoSpeaker:(BOOL)autoSpeaker {
    if (_autoSpeaker != autoSpeaker) {
        _autoSpeaker = autoSpeaker;
        [self savePreference:[NSNumber numberWithBool:_autoSpeaker] forKey:kAutoSpeakerKey];
    }
}

- (void)setLeaveConfirm:(BOOL)leaveConfirm {
    if (_leaveConfirm != leaveConfirm) {
        _leaveConfirm = leaveConfirm;
        [self savePreference:[NSNumber numberWithBool:_leaveConfirm] forKey:kLeaveConfirmKey];
    }
}

- (void)setFaceBeautify:(BOOL)faceBeautify {
    if (_faceBeautify != faceBeautify) {
        _faceBeautify = faceBeautify;
        [self savePreference:[NSNumber numberWithBool:_faceBeautify] forKey:kFaceBeautifyKey];
        [self updateFaceBeautifyOption];
    }
}

- (void)setBeautifyIntensity:(Float32)beautifyIntensity {
    if (_beautifyIntensity != beautifyIntensity) {
        _beautifyIntensity = beautifyIntensity;
        [self savePreference:[NSNumber numberWithFloat:_beautifyIntensity] forKey:kBeautifyIntensityKey];
        if (_advancedBeautify) {
            [_videoFilter setBeautifyIntensity:_beautifyIntensity];
        } else {
            [self updateInternalFaceBeautify];
        }
    }
}

- (void)setAdvancedBeautify:(BOOL)advancedBeautify {
    if (_advancedBeautify != advancedBeautify) {
        _advancedBeautify = advancedBeautify;
        [self savePreference:@(_advancedBeautify) forKey:kAdvancedBeautifyKey];
        [self updateFaceBeautifyOption];
    }
}

- (void)setCheekThinningIntensity:(Float32)cheekThinningIntensity {
    if (_cheekThinningIntensity != cheekThinningIntensity) {
        _cheekThinningIntensity = cheekThinningIntensity;
        [self savePreference:@(_cheekThinningIntensity) forKey:kCheekThinningKey];
        [_videoFilter setCheekThinningIntensity:_cheekThinningIntensity];
    }
}

- (void)setEyeEnlargingIntensity:(Float32)eyeEnlargingIntensity {
    if (_eyeEnlargingIntensity != eyeEnlargingIntensity) {
        _eyeEnlargingIntensity = eyeEnlargingIntensity;
        [self savePreference:@(_eyeEnlargingIntensity) forKey:kEyeEnlargingKey];
        [_videoFilter setEyeEnlargingIntensity:_eyeEnlargingIntensity];
    }
}

- (void)setDebugMode:(BOOL)debugMode {
    if (_debugMode != debugMode) {
        _debugMode = debugMode;
        if (debugMode) {
            [self startAudioDump];
        } else {
            [self stopAudioDump];
        }
    }
}

- (PanoResult)startAudioDump {
    return [_engineKit startAudioDumpWithFilePath:PanoCallClient.audioDumpFile maxFileSize:kMaxAudioDumpFileSize];
}

- (PanoResult)stopAudioDump {
    return [_engineKit stopAudioDump];
}

- (BOOL)recordUsage {
    NSString * requestStr = [NSString stringWithFormat:@"%@://%@/%@", kHttpsHeader, kDemoPanoServer, kUrlUsageRecordApi];
    NSURL *requestURL = [NSURL URLWithString:requestStr];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:requestURL];
    [request setValue:NSUUID.UUID.UUIDString forHTTPHeaderField:@"Tracking-Id"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"no-cache" forHTTPHeaderField:@"Cache-Control"];
    request.HTTPMethod = @"POST";
    NSMutableDictionary * dictBody = [NSMutableDictionary dictionaryWithCapacity:10];
    [dictBody setValue:PanoCallClient.productName forKey:@"product"];
    [dictBody setValue:UIDevice.currentDevice.systemName forKey:@"os"];
    [dictBody setValue:UIDevice.currentDevice.systemVersion forKey:@"osver"];
    [dictBody setValue:PanoRtcEngineKit.getSdkVersion forKey:@"sdkver"];
    [dictBody setValue:kDemoAppId forKey:@"appId"];
    [dictBody setValue:_roomId forKey:@"channelId"];
    [dictBody setValue:[NSString stringWithFormat:@"%llu", _userId] forKey:@"userId"];
    [dictBody setValue:_userName forKey:@"userName"];
    [dictBody setValue:_uuid forKey:@"uuid"];
    [dictBody setValue:@"13812345888" forKey:@"phone"];
    NSError * error = nil;
    request.HTTPBody = [NSJSONSerialization dataWithJSONObject:dictBody options:NSJSONWritingPrettyPrinted error:&error];
    if (nil != error) {
#if defined(DEBUG)
        NSLog(@"[PanoCallClient recordUsage], Create request body fail, error: %@", error);
#endif
        return NO;
    }

    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
#if defined(DEBUG)
        NSLog(@"[PanoCallClient recordUsage], Receive response: %ld, error: %@", (long)httpResponse.statusCode, error);
#endif
    }];
    [dataTask resume];
    return YES;
}

#pragma mark - PanoRtcEngineDelegate

- (void)onChannelJoinConfirm:(PanoResult)result {
    [_channelDelegate onChannelJoinConfirm:result];
}

- (void)onChannelLeaveIndication:(PanoResult)result {
    [_channelDelegate onChannelLeaveIndication:result];
}

- (void)onChannelFailover:(PanoFailoverState)state {
    [_channelDelegate onChannelFailover:state];
}

- (void)onChannelCountDown:(UInt32)remain {
    [_channelDelegate onChannelCountDown:remain];
}

- (void)onUserJoinIndication:(UInt64)userId withName:(NSString * _Nullable)userName {
    [_channelDelegate onUserJoinIndication:userId withName:userName];
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    [_channelDelegate onUserLeaveIndication:userId withReason:reason];
}

- (void)onUserAudioStart:(UInt64)userId {
    [_channelDelegate onUserAudioStart:userId];
}

- (void)onUserAudioStop:(UInt64)userId {
    [_channelDelegate onUserAudioStop:userId];
}

- (void)onUserVideoStart:(UInt64)userId
          withMaxProfile:(PanoVideoProfileType)maxProfile {
    [_channelDelegate onUserVideoStart:userId withMaxProfile:maxProfile];
}

- (void)onUserVideoStop:(UInt64)userId {
    [_channelDelegate onUserVideoStop:userId];
}

- (void)onUserScreenStart:(UInt64)userId {
    [_channelDelegate onUserScreenStart:userId];
}

- (void)onUserScreenStop:(UInt64)userId {
    [_channelDelegate onUserScreenStop:userId];
}

- (void)onUserAudioSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [_channelDelegate onUserAudioSubscribe:userId withResult:result];
}

- (void)onUserVideoSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [_channelDelegate onUserVideoSubscribe:userId withResult:result];
}

- (void)onUserScreenSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [_channelDelegate onUserScreenSubscribe:userId withResult:result];
}

- (void)onUserAudioMute:(UInt64)userId {
    [_channelDelegate onUserAudioMute:userId];
}

- (void)onUserAudioUnmute:(UInt64)userId {
    [_channelDelegate onUserAudioUnmute:userId];
}

- (void)onUserVideoMute:(UInt64)userId {
    [_channelDelegate onUserVideoMute:userId];
}

- (void)onUserVideoUnmute:(UInt64)userId {
    [_channelDelegate onUserVideoUnmute:userId];
}

- (void)onUserScreenMute:(UInt64)userId {
    [_channelDelegate onUserScreenMute:userId];
}

- (void)onUserScreenUnmute:(UInt64)userId {
    [_channelDelegate onUserScreenUnmute:userId];
}

- (void)onFirstAudioDataReceived:(UInt64)userId {
    [_channelDelegate onFirstAudioDataReceived:userId];
}

- (void)onFirstVideoDataReceived:(UInt64)userId {
    [_channelDelegate onFirstVideoDataReceived:userId];
}

- (void)onFirstScreenDataReceived:(UInt64)userId {
    [_channelDelegate onFirstScreenDataReceived:userId];
}

- (void)onAudioSendStats:(PanoRtcAudioSendStats * _Nonnull)stats {
    [_channelDelegate onAudioSendStats:stats];
}

- (void)onAudioRecvStats:(PanoRtcAudioRecvStats * _Nonnull)stats {
    [_channelDelegate onAudioRecvStats:stats];
}

- (void)onVideoSendStats:(PanoRtcVideoSendStats * _Nonnull)stats {
    [_channelDelegate onVideoSendStats:stats];
}

- (void)onVideoRecvStats:(PanoRtcVideoRecvStats * _Nonnull)stats {
    [_channelDelegate onVideoRecvStats:stats];
}

- (void)onScreenSendStats:(PanoRtcScreenSendStats * _Nonnull)stats {
    [_channelDelegate onScreenSendStats:stats];
}

- (void)onScreenRecvStats:(PanoRtcScreenRecvStats * _Nonnull)stats {
    [_channelDelegate onScreenRecvStats:stats];
}

- (void)onVideoSendBweStats:(PanoRtcVideoSendBweStats * _Nonnull)stats {
    [_channelDelegate onVideoSendBweStats:stats];
}

- (void)onVideoRecvBweStats:(PanoRtcVideoRecvBweStats * _Nonnull)stats {
    [_channelDelegate onVideoRecvBweStats:stats];
}

- (void)onSystemStats:(PanoRtcSystemStats * _Nonnull)stats {
    [_channelDelegate onSystemStats:stats];
}

- (void)onWhiteboardAvailable {
    [_channelDelegate onWhiteboardAvailable];
}

- (void)onWhiteboardUnavailable {
    [_channelDelegate onWhiteboardUnavailable];
}

- (void)onWhiteboardStart {
    [_channelDelegate onWhiteboardStart];
}

- (void)onWhiteboardStop {
    [_channelDelegate onWhiteboardStop];
}

#pragma mark - Private

- (void)createEngineKit {
    [_engineKit destroy];
    _engineKit = nil;
    
    PanoRtcEngineConfig * engineConfig = [[PanoRtcEngineConfig alloc] init];
    engineConfig.appId = kDemoAppId;
    engineConfig.rtcServer = kDemoPanoServer;
    _engineKit = [PanoRtcEngineKit engineWithConfig:engineConfig delegate:self];
    engineConfig = nil;
    
    // Update Options
    [self updateFaceBeautifyOption];
}

- (void)destroyEngineKit {
    [_engineKit destroy];
    _engineKit = nil;
}

- (void)loadPreferences {
    NSString * userName = [[NSUserDefaults standardUserDefaults] stringForKey:kUserNameKey];
    if (userName) {
        _userName = userName;
    }
    NSString * mobileNumber = [[NSUserDefaults standardUserDefaults] stringForKey:kMobileNumberKey];
    if (mobileNumber) {
        _mobileNumber = userName;
    }
    NSNumber * autoMute = [[NSUserDefaults standardUserDefaults] objectForKey:kAutoMuteKey];
    if (autoMute) {
        _autoMute = autoMute.boolValue;
    }
    NSNumber * autoVideo = [[NSUserDefaults standardUserDefaults] objectForKey:kAutoVideoKey];
    if (autoVideo) {
        _autoVideo = autoVideo.boolValue;
    }
    NSNumber * resolution = [[NSUserDefaults standardUserDefaults] objectForKey:kResolutionKey];
    if (resolution) {
        _resolution = resolution.integerValue;
    }
    NSNumber * autoSpeaker = [[NSUserDefaults standardUserDefaults] objectForKey:kAutoSpeakerKey];
    if (autoSpeaker) {
        _autoSpeaker = autoSpeaker.boolValue;
    }
    NSNumber * leaveConfirm = [[NSUserDefaults standardUserDefaults] objectForKey:kLeaveConfirmKey];
    if (leaveConfirm) {
        _leaveConfirm = leaveConfirm.boolValue;
    }
    NSNumber * faceBeautify = [[NSUserDefaults standardUserDefaults] objectForKey:kFaceBeautifyKey];
    if (faceBeautify) {
        _faceBeautify = faceBeautify.boolValue;
    }
    NSNumber * beautifyIntensity = [[NSUserDefaults standardUserDefaults] objectForKey:kBeautifyIntensityKey];
    if (beautifyIntensity) {
        _beautifyIntensity = beautifyIntensity.floatValue;
    }
    NSNumber * advancedBeautify = [[NSUserDefaults standardUserDefaults] objectForKey:kAdvancedBeautifyKey];
    if (advancedBeautify) {
        _advancedBeautify = advancedBeautify.boolValue;
    }
    NSNumber * cheekThinning = [[NSUserDefaults standardUserDefaults] objectForKey:kCheekThinningKey];
    if (cheekThinning) {
        _cheekThinningIntensity = cheekThinning.floatValue;
    }
    NSNumber * eyeEnlarging = [[NSUserDefaults standardUserDefaults] objectForKey:kEyeEnlargingKey];
    if (eyeEnlarging) {
        _eyeEnlargingIntensity = eyeEnlarging.floatValue;
    }
}

- (void)savePreferences {
    [[NSUserDefaults standardUserDefaults] setObject:_userName forKey:kUserNameKey];
    [[NSUserDefaults standardUserDefaults] setObject:_mobileNumber forKey:kMobileNumberKey];
    [[NSUserDefaults standardUserDefaults] setBool:_autoMute forKey:kAutoMuteKey];
    [[NSUserDefaults standardUserDefaults] setBool:_autoVideo forKey:kAutoVideoKey];
    [[NSUserDefaults standardUserDefaults] setInteger:_resolution forKey:kResolutionKey];
    [[NSUserDefaults standardUserDefaults] setBool:_autoSpeaker forKey:kAutoSpeakerKey];
    [[NSUserDefaults standardUserDefaults] setBool:_leaveConfirm forKey:kLeaveConfirmKey];
    [[NSUserDefaults standardUserDefaults] setBool:_faceBeautify forKey:kFaceBeautifyKey];
    [[NSUserDefaults standardUserDefaults] setFloat:_beautifyIntensity forKey:kBeautifyIntensityKey];
    [[NSUserDefaults standardUserDefaults] setBool:_advancedBeautify forKey:kAdvancedBeautifyKey];
    [[NSUserDefaults standardUserDefaults] setFloat:_cheekThinningIntensity forKey:kCheekThinningKey];
    [[NSUserDefaults standardUserDefaults] setFloat:_eyeEnlargingIntensity forKey:kEyeEnlargingKey];
}

- (void)savePreference:(nullable id)value forKey:(NSString *)key {
    [[NSUserDefaults standardUserDefaults] setObject:value forKey:key];
}

- (void)checkUUID {
    NSString * uuid = [[NSUserDefaults standardUserDefaults] stringForKey:kUserUniqueIDKey];
    if (uuid) {
        _uuid = uuid;
    }
    if (nil == _uuid) {
        _uuid = [NSUUID UUID].UUIDString;
        [[NSUserDefaults standardUserDefaults] setObject:_uuid forKey:kUserUniqueIDKey];
    }
}

- (void)updateFaceBeautifyOption {
    [self updateInternalFaceBeautify];
    [self updateExternalFaceBeautify];
}

- (void)updateInternalFaceBeautify {
    BOOL internalBeautify = _faceBeautify && !_advancedBeautify;
    PanoFaceBeautifyOption * option = [PanoFaceBeautifyOption new];
    option.enable = internalBeautify;
    option.intensity = _beautifyIntensity;
    [_engineKit setOption:option forType:kPanoOptionFaceBeautify];
}

- (void)updateExternalFaceBeautify {
    BOOL externalBeautify = _faceBeautify && _advancedBeautify;
    if (externalBeautify) {
        if (nil == _videoFilter) {
            _videoFilter = [VideoFilterDelegate new];
        }
        [_videoFilter setBeautifyIntensity:_beautifyIntensity];
        [_videoFilter setCheekThinningIntensity:_cheekThinningIntensity];
        [_videoFilter setEyeEnlargingIntensity:_eyeEnlargingIntensity];
    }
    NSObject * processor = externalBeautify ? _videoFilter : nil;
    [_engineKit setMediaProcessor:kVideoPreprocessor processor:processor param:nil];
    PanoVideoFrameRateType frameRateType = externalBeautify ? kPanoFrameRateLow : kPanoFrameRateStandard;
    [_engineKit setOption:@(frameRateType) forType:kPanoOptionVideoFrameRate];
}

+ (NSString *)productName {
    return [NSBundle.mainBundle.infoDictionary objectForKey:@"CFBundleName"];
}

+ (NSString *)productShortVersion {
    return [NSBundle.mainBundle.infoDictionary objectForKey:@"CFBundleShortVersionString"];
}

+ (NSString *)productVersion {
    NSString * appVersion = [PanoCallClient productShortVersion];
    NSString * sdkVersion = [PanoRtcEngineKit getSdkVersion];
    return [NSString stringWithFormat:@"v%@ (%@)", appVersion, sdkVersion];
}

+ (NSString *)audioDumpFile {
    NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:kAudioDumpFileName];
    return filePath;
}

+ (NSString *)urlEncode:(NSString *)url {
    return [url stringByAddingPercentEncodingWithAllowedCharacters:NSCharacterSet.URLUserAllowedCharacterSet];
}

+ (void)updatePanoConfigWithAppId:(NSString *)appId
                         rtcServer:(NSString *)server
                             token:(NSString *)token
{
    kDemoAppId = appId;
    kDemoPanoServer = server;
    kDemoTempToken = token;
    [[PanoCallClient sharedInstance] createEngineKit];
}

@end
