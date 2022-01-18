//
//  PanoStatisticsService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoRtc/PanoRtcEngineKit.h"

NS_ASSUME_NONNULL_BEGIN


@protocol PanoStatisticsDelegate <NSObject>

- (void)onAudioStatisticsChanged;

@end

@interface PanoSystemStatsModel : NSObject

@property (nonatomic, copy) NSString *cpuDevice;

@property (nonatomic, copy) NSString *memoryDevice;

@property (assign, nonatomic) CGFloat totalCpuUsage;

@property (nonatomic, copy) NSString *totalPhysMemory;

@property (nonatomic, copy) NSString *workingSetSize;

@property (assign, nonatomic) CGFloat memoryUsage;


@end


/// 音视频统计核心接口
@interface PanoStatisticsService<T> : PanoBaseService<PanoRtcEngineDelegate>

@property (nonatomic, weak) id<PanoStatisticsDelegate> delegate;

@property (nonatomic, strong, readonly) NSMutableArray *overallDataSource;
@property (nonatomic, strong, readonly) NSMutableArray *audioDataSource;
@property (nonatomic, strong, readonly) NSMutableArray *videoDataSource;
@property (nonatomic, strong, readonly) NSMutableArray *screenDataSource;

@property (nonatomic, strong, readonly) PanoSystemStatsModel *statsModel;

@end

NS_ASSUME_NONNULL_END
