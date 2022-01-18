//
//  PanoStatisticsViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoStatisticsViewController.h"
#import "UIColor+Extension.h"
#import "Masonry.h"
#import "PanoDefine.h"
#import "PanoStatisticsView.h"
#import "PanoCallClient.h"
#import "PanoOverallHeaderView.h"

@interface PanoStatisticsViewController () <UIScrollViewDelegate, PanoStatisticsDelegate>

@property (nonatomic, strong) UISegmentedControl *segmentedControl;

@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic, strong) PanoStatisticsView *overallView;
@property (nonatomic, strong) PanoStatisticsView *audioView;
@property (nonatomic, strong) PanoStatisticsView *videoView;
@property (nonatomic, strong) PanoStatisticsView *screenView;
@property (nonatomic, strong) PanoOverallHeaderView *headerView;

@property (nonatomic, weak) PanoStatisticsService *statisticsService;


@end

@implementation PanoStatisticsViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.title = NSLocalizedString(@"Statistics", nil);
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"btn.back"] style:0 target:self action:@selector(dismiss)];
    
    NSArray *items = @[NSLocalizedString(@"Overall", nil), NSLocalizedString(@"Audio", nil),
                       NSLocalizedString(@"Video", nil), NSLocalizedString(@"Screen", nil)];
    self.segmentedControl = [[UISegmentedControl alloc] initWithItems:items];
    self.segmentedControl.backgroundColor = [UIColor pano_colorWithHexString:@"F2F0F7"];
    self.segmentedControl.selectedSegmentIndex = 0;
    [self.segmentedControl addTarget:self action:@selector(segmentedControlChanged) forControlEvents:UIControlEventValueChanged];
    self.segmentedControl.tintColor = [UIColor blueColor];
    if (@available(iOS 13.0, *)) {
        self.segmentedControl.selectedSegmentTintColor = [UIColor whiteColor];
    }
    [self.view addSubview:self.segmentedControl];
    
    self.scrollView = [[UIScrollView alloc] initWithFrame:CGRectZero];
    self.scrollView.pagingEnabled = true;
    self.scrollView.delegate = self;
    [self.view addSubview:self.scrollView];
    
    NSArray *statisticsItems = @[NSLocalizedString(@"Brandwidth", nil),
                            NSLocalizedString(@"Network Type", nil),
    ];
    
    self.headerView = [[PanoOverallHeaderView alloc] initWithFrame:CGRectZero];
    [self.scrollView addSubview:self.headerView];
    _overallView = [[PanoStatisticsView alloc] initWithItems:statisticsItems valuesCount:1];
    [self.scrollView addSubview:_overallView];
    
    statisticsItems = @[NSLocalizedString(@"Item Name", nil),
                        NSLocalizedString(@"Bitrate", nil),
                        NSLocalizedString(@"Lost Ratio", nil),
                        NSLocalizedString(@"RTT", nil)];
    _audioView = [[PanoStatisticsView alloc] initWithItems:statisticsItems valuesCount:2];
    [self.scrollView addSubview:_audioView];
    
    statisticsItems = @[NSLocalizedString(@"Item Name", nil),
                   NSLocalizedString(@"Bitrate", nil),
                   NSLocalizedString(@"Lost Ratio", nil),
                   NSLocalizedString(@"RTT", nil),
                   NSLocalizedString(@"Resolution", nil),
                   NSLocalizedString(@"Frame Rate", nil),
    ];
    
    _videoView = [[PanoStatisticsView alloc] initWithItems:statisticsItems valuesCount:2];
    [self.scrollView addSubview:_videoView];
    
    _screenView = [[PanoStatisticsView alloc] initWithItems:statisticsItems valuesCount:2];
    [self.scrollView addSubview:_screenView];
    

    _statisticsService = PanoCallClient.shared.statistics;
    _statisticsService.delegate = self;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(statusBarOrientationDidChange) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
    [self resetLayout];
}

- (void)statusBarOrientationDidChange {
    [self resetLayout];
}


- (void)resetLayout {
    NSLog(@"self.view.bounds-> %@", NSStringFromCGRect(self.view.bounds));
    CGFloat sgWidth = CGRectGetWidth(self.view.bounds) - LargeFixSpace * 2;
    self.segmentedControl.frame = CGRectMake(LargeFixSpace, statusBarHeight() + 44 + DefaultFixSpace*3, sgWidth, 34);
    
    CGFloat originY = CGRectGetMaxY(self.segmentedControl.frame) + 30;
    CGRect frame = CGRectMake(0, originY,  CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds) - originY);
    self.scrollView.frame = frame;
    
    self.scrollView.contentSize = CGSizeMake(CGRectGetWidth(self.scrollView.bounds) * 4, CGRectGetHeight(self.scrollView.bounds));
    
    CGRect headerFrame = CGRectMake(0, 0, CGRectGetWidth(self.scrollView.bounds), 90);
    self.headerView.frame = headerFrame;
    
    frame = self.scrollView.bounds;
    frame.origin.x = 0;
    frame.origin.y = headerFrame.size.height;
    frame.size.height = CGRectGetHeight(self.scrollView.bounds) - headerFrame.size.height;
    _overallView.frame = frame;
    
    frame.origin.x += CGRectGetWidth(self.scrollView.bounds);
    frame.origin.y = 0;
    _audioView.frame = frame;
    
    frame.origin.x += CGRectGetWidth(self.scrollView.bounds);
    _videoView.frame = frame;
    
    frame.origin.x += CGRectGetWidth(self.scrollView.bounds);
    _screenView.frame = frame;
    [self.view setNeedsLayout];
    [self.view updateConstraintsIfNeeded];
    [self.view layoutIfNeeded];
    [self segmentedControlChanged];
}

#pragma mark -- PanoStatisticsDelegate
- (void)onAudioStatisticsChanged {
    _overallView.values = self.statisticsService.overallDataSource.copy;
    _audioView.values = self.statisticsService.audioDataSource.copy;
    _videoView.values = self.statisticsService.videoDataSource.copy;
    _screenView.values = self.statisticsService.screenDataSource.copy;
    
    self.headerView.cpuLabel.text = self.statisticsService.statsModel.cpuDevice;
    self.headerView.memoryLabel.text = self.statisticsService.statsModel.totalPhysMemory;
    self.headerView.cpuProgressView.progressView.progress = self.statisticsService.statsModel.totalCpuUsage/100.0;
    self.headerView.cpuProgressView.descLabel.text = [NSString stringWithFormat:@"%@%%",@(self.statisticsService.statsModel.totalCpuUsage).stringValue];
    self.headerView.memoryProgressView.progressView.progress = self.statisticsService.statsModel.memoryUsage/100.0;
    self.headerView.memoryProgressView.descLabel.text = self.statisticsService.statsModel.workingSetSize;
}

- (void)segmentedControlChanged {
    NSInteger index = self.segmentedControl.selectedSegmentIndex;
    [self.scrollView setContentOffset:CGPointMake(CGRectGetWidth(self.scrollView.bounds) * index, 0) animated:true];
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    NSInteger index = self.scrollView.contentOffset.x / CGRectGetWidth(self.scrollView.bounds);
    self.segmentedControl.selectedSegmentIndex = index;
}

- (void)dismiss {
    [self dismissViewControllerAnimated:true completion:^{
    }];
}


- (void)dealloc {
    _statisticsService.delegate = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
