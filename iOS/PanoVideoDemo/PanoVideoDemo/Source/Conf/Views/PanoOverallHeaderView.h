//
//  PanoOverallHeaderView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseView.h"

NS_ASSUME_NONNULL_BEGIN

@interface PanoOverallProgressView : PanoBaseView

@property (nonatomic, strong) UILabel *descLabel;
@property (nonatomic, strong) UIProgressView *progressView;

@end


@interface PanoOverallHeaderView : PanoBaseView



@property (nonatomic, strong) UILabel *cpuLabel;
@property (nonatomic, strong) UILabel *memoryLabel;
@property (nonatomic, strong) PanoOverallProgressView *cpuProgressView;
@property (nonatomic, strong) PanoOverallProgressView *memoryProgressView;

@end

NS_ASSUME_NONNULL_END
