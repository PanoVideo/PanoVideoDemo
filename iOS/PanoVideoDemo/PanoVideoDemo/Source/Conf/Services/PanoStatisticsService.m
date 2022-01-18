//
//  PanoStatisticsService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoStatisticsService.h"

#import "PanoPoolService.h"
#import <sys/sysctl.h>
#import "PanoCallClient.h"


@implementation PanoSystemStatsModel
@end

@interface PanoStatisticsService()

@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) NSMutableArray<PanoRtcAudioRecvStats *> *audioReceiveArray;
@property (nonatomic, strong) NSMutableArray<PanoRtcVideoRecvStats *> *videoReceiveArray;
@property (nonatomic, strong) NSMutableArray<PanoRtcVideoRecvStats *> *screenReceiveArray;

@property (nonatomic, assign) NSInteger maxSendAudioLossRatio;
@property (nonatomic, assign) NSInteger maxSendVideoLossRatio;
@property (nonatomic, assign) NSInteger maxSendScreenLossRatio;
@property (nonatomic, assign) NSInteger maxReceiveAudioLossRatio;
@property (nonatomic, assign) NSInteger maxReceiveVideoLossRatio;
@property (nonatomic, assign) NSInteger maxReceiveScreenLossRatio;

@property (nonatomic, strong) dispatch_queue_t dataQueue;
@property (nonatomic, assign) BOOL isRunning;

@property (nonatomic, copy) NSString *sendBrandWidth;
@property (nonatomic, copy) NSString *receiveBrandWidth;
@property (nonatomic, strong) PanoRtcSystemStats *systemStats;

@end

@implementation PanoStatisticsService

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self initDataSource];
    }
    return self;
}

- (void)setDelegate:(id<PanoStatisticsDelegate>)delegate {
    _delegate = delegate;
    delegate ? [self addDelegate:delegate] : [self removeDelegate:delegate];
}

- (void)addDelegate:(id)delegate {
    [super addDelegate:delegate];
    if (self.delegatesCount > 0 && !_isRunning) {
        _isRunning = true;
        [self initDataSource];
        [self restartTimer];
    }
}

- (void)removeDelegate:(id)delegate {
    [super removeDelegate:delegate];
    if (self.delegatesCount <=0 && _isRunning) {
        _isRunning = false;
        [self stopTimer];
    }
}

- (void)restartTimer {
    _timer = [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(timerAction:) userInfo:nil repeats:YES];
    [self performSelector:@selector(timerAction:) withObject:nil afterDelay:0.2];
}

- (void)stopTimer {
    [_timer invalidate];
    _timer = nil;
}

- (void)timerAction:(NSTimer *)timer {
    PanoPoolService *poolService = PanoCallClient.shared.pool;
    NSInteger activeID = poolService.activeAudioUserID;
    dispatch_async(_dataQueue, ^{
        
        NSInteger count = self.audioReceiveArray.count;
        NSString *audioBitrateStr = @"-";
        NSString *audioLossRatioStr = @"-";
        NSString *audioRttStr = @"-";
        
        PanoRtcAudioRecvStats * stats = self.audioReceiveArray.lastObject;
        for (NSInteger i=count-1; i>=0; i--) {
            PanoRtcAudioRecvStats * temp = self.audioReceiveArray[i];
            if (temp.userId == activeID) {
                stats = temp;
                break;
            }
        }
        if (stats) {
            audioBitrateStr = [self descriptionBitrate:stats.recvBitrate/count];
            audioLossRatioStr = [NSString stringWithFormat:@"%.0f%%", stats.lossRatio * 100 / (CGFloat)count];
        }
        NSString *videoBitrateRecvStr = @"-";
        NSString *videoLossRatioStr = @"-";
        NSString *videoRttStr = @"-";
        NSString *videoRelomun = @"-";
        NSString *videoFramerate = @"-";
        
        count = self.videoReceiveArray.count;
        if (count > 0) {
            PanoRtcVideoRecvStats * stats = self.videoReceiveArray.lastObject;
            for (NSInteger i=count-1; i>=0; i--) {
                PanoRtcVideoRecvStats * temp = self.videoReceiveArray[i];
                if (temp.userId == activeID) {
                    stats = temp;
                    break;
                }
            }
            videoBitrateRecvStr = [self descriptionBitrate:stats.recvBitrate];
            videoLossRatioStr = [NSString stringWithFormat:@"%.0f%%", stats.lossRatio * 100 ];
            videoRttStr = @"-";
            videoRelomun = [NSString stringWithFormat:@"%dx%d", (int)stats.width, (int)stats.height];
            videoFramerate = [NSString stringWithFormat:@"%dfps", (int)stats.framerate];
        }
        count = self.screenReceiveArray.count;
        NSString *screenBitrateRecvStr = @"-";
        NSString *screenLossRatioStr = @"-";
        NSString *screenRttStr = @"-";
        NSString *screenRelomun = @"-";
        NSString *screenFramerate = @"-";
        if (count > 0) {
            PanoRtcVideoRecvStats * stats = self.screenReceiveArray.lastObject;
            for (NSInteger i=count-1; i>=0; i--) {
                PanoRtcVideoRecvStats * temp = self.screenReceiveArray[i];
                if (temp.userId == activeID) {
                    stats = temp;
                    break;
                }
            }
            screenBitrateRecvStr = [self descriptionBitrate:stats.recvBitrate];
            screenLossRatioStr = [NSString stringWithFormat:@"%.0f%%", stats.lossRatio * 100 ];
            screenRelomun = [NSString stringWithFormat:@"%dx%d", (int)stats.width, (int)stats.height];
            screenFramerate = [NSString stringWithFormat:@"%dfps", (int)stats.framerate];
        }
        [self.audioReceiveArray removeAllObjects];
        [self.videoReceiveArray removeAllObjects];
        [self.screenReceiveArray removeAllObjects];
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *str = self.sendBrandWidth ?: @"";
            NSString *networkStatus = NSLocalizedString(@"Unknown", nil);
            switch ([PanoCallClient shared].reachability.currentReachabilityStatus) {
                case NotReachable:
                    networkStatus = NSLocalizedString(@"Not Reachable", nil);
                    break;
                case ReachableViaWWAN:
                    networkStatus = NSLocalizedString(@"WWAN", nil);
                    break;
                case ReachableViaWiFi:
                    networkStatus = NSLocalizedString(@"WiFi", nil);
                    break;
            }
            self.overallDataSource[0] = @[str, networkStatus];
            
            self.audioDataSource[1] = @[NSLocalizedString(@"Receive", nil), audioBitrateStr, audioLossRatioStr, audioRttStr];
            
            self.videoDataSource[1] = @[NSLocalizedString(@"Receive", nil), videoBitrateRecvStr, videoLossRatioStr, videoRttStr, videoRelomun, videoFramerate];
            
            self.screenDataSource[1] = @[NSLocalizedString(@"Receive", nil), screenBitrateRecvStr, screenLossRatioStr, screenRttStr, screenRelomun, screenFramerate];
            
            PanoSystemStatsModel *systemModel = [[PanoSystemStatsModel alloc] init];
            systemModel.cpuDevice = [NSString stringWithFormat:@"CPU %ld-core",(long)[[self class] deviceCPUNum]];
            systemModel.memoryDevice = [NSString stringWithFormat:@"%@ %ld GB", NSLocalizedString(@"Memory", nil),(long)[[self class] deviceCPUNum]];
            if (self.systemStats.totalPhysMemory > 0) {
                systemModel.memoryUsage = (CGFloat)self.systemStats.workingSetSize/(CGFloat)self.systemStats.totalPhysMemory;
            }
            systemModel.totalCpuUsage = self.systemStats.totalCpuUsage;
            systemModel.totalPhysMemory = [NSString stringWithFormat:@"%@ %d GB",NSLocalizedString(@"Memory", nil),(int)self.systemStats.totalPhysMemory/1024/1024];
            systemModel.workingSetSize = [NSString stringWithFormat:@"%d MB",(int)self.systemStats.workingSetSize/1024];
            self->_statsModel = systemModel;
            if ([self.delegate respondsToSelector:@selector(onAudioStatisticsChanged)]) {
                [self.delegate onAudioStatisticsChanged];
            }
        });
    });
}

- (NSString *)descriptionBytes:(SInt64)bytes {
    NSInteger reuslt = bytes / 1024;
    if (reuslt < 1024) {
        return [NSString stringWithFormat:@"%ldKB", (long)reuslt];
    } else {
        CGFloat res = reuslt / 1024;
        return [NSString stringWithFormat:@"%.2fMB", res];
    }
}

- (NSString *)descriptionBitrate:(SInt64)bitrate {
    NSInteger reuslt = bitrate/1000;
    return [NSString stringWithFormat:@"%ldkb/s", (long)reuslt];
}

- (void)initDataSource {
    _overallDataSource = [@[[self dataWithCount:2 firstItem:@""]] mutableCopy];
    _audioDataSource = [@[[self dataWithCount:5 firstItem:NSLocalizedString(@"Send", nil)], [self dataWithCount:5 firstItem:NSLocalizedString(@"Receive", nil)]] mutableCopy];
    _videoDataSource = [@[[self dataWithCount:6 firstItem:NSLocalizedString(@"Send", nil)], [self dataWithCount:6 firstItem:NSLocalizedString(@"Receive", nil)]] mutableCopy];
    _screenDataSource = [@[[self dataWithCount:6 firstItem:NSLocalizedString(@"Send", nil)], [self dataWithCount:6 firstItem:NSLocalizedString(@"Receive", nil)]] mutableCopy];
    
    _dataQueue = dispatch_queue_create("com.pvc.statistics", NULL);
    _audioReceiveArray = [NSMutableArray array];
    _videoReceiveArray = [NSMutableArray array];
    _screenReceiveArray = [NSMutableArray array];
}

- (NSMutableArray *)dataWithCount:(NSInteger)count firstItem:(NSString *)item {
    NSMutableArray *mArray = [NSMutableArray array];
    for (NSInteger i=0; i<count; i++) {
        if (i == 0 && item != nil) {
            [mArray addObject:item];
            continue;
        }
        [mArray addObject:@"-"];
    }
    return mArray;
}

#pragma mark Statistics Delegate Methods

/**
 * @brief 回调发送音频的统计。
 * @param stats   已发送音频的统计信息。
 */
- (void)onAudioSendStats:(PanoRtcAudioSendStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    NSString *bytesSent = [self descriptionBitrate:stats.sendBitrate];
    NSString *lossRatio = [NSString stringWithFormat:@"%2.f%%",stats.lossRatio * 100];
    NSString *rtt = [NSString stringWithFormat:@"%lldms",stats.rtt];
    dispatch_async(_dataQueue, ^{
        if (stats.lossRatio > self.maxSendAudioLossRatio) {
            self.maxSendAudioLossRatio = stats.lossRatio;
        }
        self.audioDataSource[0] = @[NSLocalizedString(@"Send", nil), bytesSent, lossRatio, rtt];
    });
}

/**
 * @brief 回调接收音频的统计。
 * @param stats   已接收音频的统计信息。
 */
- (void)onAudioRecvStats:(PanoRtcAudioRecvStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    dispatch_async(_dataQueue, ^{
        [self.audioReceiveArray addObject:stats];
    });
}

/**
 * @brief 回调发送视频的统计。
 * @param stats   已发送视频的统计信息。
 */
- (void)onVideoSendStats:(PanoRtcVideoSendStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    NSString *sendBitrate = [self descriptionBitrate:stats.sendBitrate];
    NSString *lossRatio = [NSString stringWithFormat:@"%2.f%%",stats.lossRatio * 100];
    NSString *rtt = [NSString stringWithFormat:@"%lldms",stats.rtt];
    NSString *frame = [NSString stringWithFormat:@"%dx%d",stats.width, stats.height];
    NSString *framerate = [NSString stringWithFormat:@"%dfps",stats.framerate];
    dispatch_async(_dataQueue, ^{
        if (stats.lossRatio > self.maxSendVideoLossRatio) {
            self.maxSendVideoLossRatio = stats.lossRatio;
        }
        self.videoDataSource[0] = @[NSLocalizedString(@"Send", nil), sendBitrate, lossRatio, rtt, frame, framerate];
    });
    
}

/**
 * @brief 回调接收视频的统计。
 * @param stats   已接收视频的统计信息。
 */
- (void)onVideoRecvStats:(PanoRtcVideoRecvStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    dispatch_async(_dataQueue, ^{
        [self.videoReceiveArray addObject:stats];
    });
}


/**
 * @brief 回调发送屏幕共享的统计。
 * @param stats   已发送屏幕共享的统计信息。
 */
- (void)onScreenSendStats:(PanoRtcScreenSendStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    NSString *sendBitrate = [self descriptionBitrate:stats.sendBitrate];
    NSString *lossRatio = [NSString stringWithFormat:@"%2.f%%",stats.lossRatio * 100];
    NSString *frame = [NSString stringWithFormat:@"%dx%d",stats.width, stats.height];
    NSString *rtt = [NSString stringWithFormat:@"%lldms",stats.rtt];
    NSString *screenFramerate = [NSString stringWithFormat:@"%dfps", (int)stats.framerate];
    dispatch_async(_dataQueue, ^{
        if (stats.lossRatio > self.maxSendScreenLossRatio) {
            self.maxSendScreenLossRatio = stats.lossRatio;
        }
        self.screenDataSource[0] = @[NSLocalizedString(@"Send", nil), sendBitrate, lossRatio, rtt, frame, screenFramerate];
    });
}

/**
 * @brief 回调接收屏幕共享的统计。
 * @param stats   已接收屏幕共享的统计信息。
 */
- (void)onScreenRecvStats:(PanoRtcScreenRecvStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    dispatch_async(_dataQueue, ^{
        [self.screenReceiveArray addObject:stats];
    });
}

/**
 * @brief 回调发送视频的带宽评估。
 * @param stats   发送视频的带宽评估信息。
 * @note 包含发送视频和屏幕共享的总共带宽评估。
 */
- (void)onVideoSendBweStats:(PanoRtcVideoSendBweStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    dispatch_async(_dataQueue, ^{
        self.sendBrandWidth = [NSString stringWithFormat:@"%@(%@)",[self descriptionBitrate:stats.transmitBitrate], NSLocalizedString(@"Send", nil)];
    });
}

/**
 * @brief 回调接收视频的带宽评估。
 * @param stats   接收视频的带宽评估信息。
 * @note 包含接收视频和屏幕共享的总共带宽评估。
 */
- (void)onVideoRecvBweStats:(PanoRtcVideoRecvBweStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
}

/**
 * @brief 回调系统统计信息。
 * @param stats   当前的系统统计信息。
 */
- (void)onSystemStats:(PanoRtcSystemStats * _Nonnull)stats {
    if (!_isRunning) {
        return;
    }
    dispatch_async(_dataQueue, ^{
        self.systemStats = stats;
    });
}

+ (NSInteger)deviceCPUNum{
    unsigned int ncpu;
    size_t len = sizeof(ncpu);
    sysctlbyname("hw.ncpu", &ncpu, &len, NULL, 0);
    NSInteger cpuNum = ncpu;
    return cpuNum;
}

+ (CGFloat)deviceTotalMemory{
    return [NSProcessInfo processInfo].physicalMemory/(1024.0*1024.0);
}

- (PanoStatisticsService<id<PanoRtcEngineDelegate>> *)service {
    return self;
}

@end
