//
//  PanoStatisticsView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseView.h"

NS_ASSUME_NONNULL_BEGIN


@interface PanoStatisticsView : PanoBaseView

- (instancetype)initWithItems:(NSArray *)items valuesCount:(NSInteger)count;

@property (nonatomic, strong) NSArray<NSArray<NSString *> *>*values;

@end

NS_ASSUME_NONNULL_END
