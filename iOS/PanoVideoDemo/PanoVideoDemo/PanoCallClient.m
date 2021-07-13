//
//  PanoVideoDemoClient.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoCallClient.h"
#import "VideoFilterDelegate.h"
#import "PanoServiceManager.h"
#import "PanoNetworkManager.h"
#import "PanoUserService.h"
#import "UIColor+Extension.h"

//https://developer.pano.video/getting-started/firstapp/#1-注册账号
//https://developer.pano.video/getting-started/firstapp/#14-%E7%94%9F%E6%88%90%E4%B8%B4%E6%97%B6token
NSString * kDemoAppId = <#T##请输入AppID，参考Pano开发者网站: String##String#>;
NSString * kDemoTempToken = <#T##请输入Token，参考Pano开发者网站: String##String#>;
NSString * kDemoPanoServer = @"api.pano.video";

// Http Request
static NSString * kHttpHeader = @"http";
static NSString * kHttpsHeader = @"https";
static NSString * kUrlUsageRecordApi = @"sirius/pvc";

// Preference Keys
static NSString * kUserUniqueIDKey = @"UUID";
static NSString * kUserNameKey = @"UserName";
static NSString * kRoomIdKey = @"RoomId";
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
@property (assign, nonatomic) PanoVideoProfileType maxResolution;
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
        _host = YES;
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
        _uuid = nil;
        [self checkUUID];
        
        _config = [[PanoConfig alloc] init];
        [self loadPreferences];
        [self createEngineKit];
        _channelDelegate = nil;
        [self startMonitoring];
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

- (void)setRoomId:(NSString *)roomId {
    if (![_roomId isEqualToString:roomId]) {
        _roomId = roomId;
        [self savePreference:roomId forKey:kRoomIdKey];
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

#pragma mark - Http Request

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
    [self queryDeviceRating];
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
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserJoinIndication:userId withName:userName];
    [_channelDelegate onUserJoinIndication:userId withName:userName];
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    [[PanoServiceManager serviceWithType:PanoVideoServiceType] onUserLeaveIndication:userId withReason:reason];
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserLeaveIndication:userId withReason:reason];
    [_channelDelegate onUserLeaveIndication:userId withReason:reason];
}

- (void)onUserAudioStart:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioStart:userId];
    [_channelDelegate onUserAudioStart:userId];
}

- (void)onUserAudioStop:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserAudioStop:userId];
    [_channelDelegate onUserAudioStop:userId];
}

- (void)onUserVideoStart:(UInt64)userId
          withMaxProfile:(PanoVideoProfileType)maxProfile {
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStart:userId withMaxProfile:maxProfile];
    [_channelDelegate onUserVideoStart:userId withMaxProfile:maxProfile];
}

- (void)onUserVideoStop:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserVideoStop:userId];
    [_channelDelegate onUserVideoStop:userId];
}

- (void)onUserScreenStart:(UInt64)userId {
    [_channelDelegate onUserScreenStart:userId];
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserScreenStart:userId];
}

- (void)onUserScreenStop:(UInt64)userId {
    [_channelDelegate onUserScreenStop:userId];
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserScreenStop:userId];
}

- (void)onUserAudioSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [_channelDelegate onUserAudioSubscribe:userId withResult:result];
}

- (void)onUserVideoSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [[PanoServiceManager serviceWithType:PanoVideoServiceType] onUserVideoSubscribe:userId withResult:result];
    [_channelDelegate onUserVideoSubscribe:userId withResult:result];
}

- (void)onUserScreenSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [_channelDelegate onUserScreenSubscribe:userId withResult:result];
    [[PanoServiceManager serviceWithType:PanoDesktopServiceType] onUserScreenSubscribe:userId withResult:result];
}

- (void)onUserAudioMute:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType]  onUserAudioMute:userId];
    [_channelDelegate onUserAudioMute:userId];
}

- (void)onUserAudioUnmute:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType]  onUserAudioUnmute:userId];
    [_channelDelegate onUserAudioUnmute:userId];
}

- (void)onUserVideoMute:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType]  onUserVideoMute:userId];
    [_channelDelegate onUserVideoMute:userId];
}

- (void)onUserVideoUnmute:(UInt64)userId {
    [[PanoServiceManager serviceWithType:PanoUserServiceType]  onUserVideoUnmute:userId];
    [_channelDelegate onUserVideoUnmute:userId];
}

- (void)onUserScreenMute:(UInt64)userId {
    [_channelDelegate onUserScreenMute:userId];
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserScreenMute:userId];
}

- (void)onUserScreenUnmute:(UInt64)userId {
    [_channelDelegate onUserScreenUnmute:userId];
    [[PanoServiceManager serviceWithType:PanoUserServiceType] onUserScreenUnmute:userId];
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

- (void)onNetworkQuality:(PanoQualityRating)quality withUser:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.channelDelegate onNetworkQuality:quality withUser:userId];
    });
}

#pragma mark - Private
- (void)queryDeviceRating {
    PanoDeviceRating deviceRating = [_engineKit queryDeviceRating];
    PanoVideoProfileType maxResolution = _resolution;
    switch (deviceRating) {
        case kPanoQualityVeryBad:
            maxResolution = kPanoProfileNone;
            break;
        case kPanoQualityBad:
            maxResolution = kPanoProfileLow;
            break;
        case kPanoQualityPoor:
            maxResolution = kPanoProfileStandard;
            break;
        case kPanoQualityGood:
            maxResolution = kPanoProfileHD720P;
            break;
        case kPanoQualityExcellent:
            maxResolution = kPanoProfileHD720P;
            break;
        default:
            break;
    }
    _maxResolution = maxResolution;
}

- (PanoVideoProfileType)videoProfile {
    if (_maxResolution == kPanoProfileNone) {
        return kPanoProfileNone;
    }
    if (_maxResolution < _resolution) {
        return _maxResolution;
    }
    return _resolution;
}

- (void)createEngineKit {
    [_engineKit destroy];
    _engineKit = nil;
    
    PanoRtcEngineConfig * engineConfig = [[PanoRtcEngineConfig alloc] init];
    engineConfig.appId = kDemoAppId;
    engineConfig.rtcServer = kDemoPanoServer;
    _engineKit = [PanoRtcEngineKit engineWithConfig:engineConfig delegate:self];
    engineConfig = nil;
    [PanoRtcEngineKit setLogLevel:kPanoLogInfo];
    
    // Update Options
    [self updateFaceBeautifyOption];
    
    // RTM
    _rtmService = [[PanoRTMService alloc] init];
    [self.engineKit.messageService setDelegate:(id<PanoRtcMessageDelegate>)_rtmService];
    [_rtmService addDelegate:self];
}

- (void)destroyEngineKit {
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    [_rtmService removeDelegate:self];
    [_engineKit.messageService setDelegate:nil];
    [_engineKit destroy];
    _engineKit = nil;
}

- (void)loadPreferences {
    NSString * userName = [[NSUserDefaults standardUserDefaults] stringForKey:kUserNameKey];
    if (userName) {
        _userName = userName;
    }
    NSString * roomId = [[NSUserDefaults standardUserDefaults] stringForKey:kRoomIdKey];
    if (roomId) {
        _roomId = roomId;
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

- (void)startMonitoring {
    self.reachability = [Reachability reachabilityWithHostname:@"www.google.com"];
    [self.reachability startNotifier];
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

@interface PanoCallClient (RTM) <PanoRTMDelegate>

@end

@implementation PanoCallClient (RTM)

- (void)onMessageReceived:(NSDictionary *)message fromUser:(UInt64)userId {
    NSLog(@"message-> %@ %lld %lld",message, userId, [PanoCallClient sharedInstance].userId);
    if ([message[@"type"] isEqualToString:@"command"]) {
        NSString *command = message[@"command"];
        if ([command isEqualToString:@"startDump"]) {
            [self startAudioDump];
            NSString *msg = message[@"description"];
            [self performSelector:@selector(uploadLogs:) withObject:msg afterDelay:60];
        }
    }else if ([message[@"type"] isEqualToString:@"UpdateUser"] &&
               [PanoCallClient sharedInstance].userId != userId ) {
        PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
        if (![userService findUserWithId:userId].os) {
            [_rtmService sendMessageToUser:userId msg:[self myUserInfo]];
        }
        [userService onUserUpdated:userId message:message];
    }
}

- (NSDictionary *)myUserInfo {
    NSDictionary *info = @{@"type": @"UpdateUser", @"palyload": @{ @"os": @"iOS"}};
    return info;
}
- (void)onRtmServiceAvailable {
    /*
     * Sync User Info
     * {
         "type": "UpdateUser",
         "palyload": {
            "os": "iOS" // iOS/Android/macOS/Windows/Web
          }
        }
     */
    [_rtmService broadcastMessage:[self myUserInfo] sendBack:false];
    //[_rtmService.messageService setProperty:@"123" value:[@"123" dataUsingEncoding:NSUTF8StringEncoding]];
}

- (void)uploadLogs:(NSString *)msg {
    [self stopAudioDump];
    PanoFeedbackInfo * info = [PanoFeedbackInfo new];
    info.type = kPanoFeedbackAudio;
    info.productName = PanoCallClient.productName;
    info.detailDescription = msg;
    info.extraInfo = PanoCallClient.sharedInstance.uuid;
    info.uploadLogs = true;
    PanoResult res = [PanoCallClient.sharedInstance.engineKit sendFeedback:info];
    NSLog(@"uploadLogs res-> %ld",(long)res);
}

@end
