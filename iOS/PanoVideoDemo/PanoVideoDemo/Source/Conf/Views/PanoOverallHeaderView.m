//
//  PanoOverallHeaderView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoOverallHeaderView.h"



@implementation PanoOverallProgressView

- (void)initViews {
    
    self.progressView = [[UIProgressView alloc] initWithProgressViewStyle:UIProgressViewStyleDefault];
    self.progressView.progressTintColor = [UIColor pano_colorWithHexString:@"#2F71E2"];
    self.progressView.progress = 0.3;
    [self addSubview:self.progressView];
    
    _descLabel = [[UILabel alloc] init];
    _descLabel.textAlignment = NSTextAlignmentRight;
    _descLabel.textColor = [UIColor blackColor];
    [_descLabel setFont:[UIFont systemFontOfSize:PanoFontSize_Min]];
    [self addSubview:_descLabel];
}

- (void)initConstraints {
    
    [self.descLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.right.mas_equalTo(self);
        make.height.mas_equalTo(15);
    }];
    
    [self.progressView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.height.mas_equalTo(3);
        make.top.mas_equalTo(self.descLabel.mas_bottom).mas_offset(5);
    }];
}

@end

@interface PanoOverallHeaderView ()

@property (nonatomic, strong) UIStackView *topStackView;
@property (nonatomic, strong) UIStackView *bottomStackView;

@end

@implementation PanoOverallHeaderView

- (void)initViews {
    
    _topStackView = [[UIStackView alloc] init];
    _topStackView.axis = UILayoutConstraintAxisHorizontal;
    _topStackView.distribution = UIStackViewDistributionFillEqually;
    [self addSubview:_topStackView];
    
    _cpuLabel = [[UILabel alloc] init];
    _cpuLabel.textAlignment = NSTextAlignmentCenter;
    _cpuLabel.textColor = [UIColor blackColor];
    [_cpuLabel setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
    [_topStackView addArrangedSubview:_cpuLabel];
    
    _memoryLabel = [[UILabel alloc] init];
    _memoryLabel.textAlignment = NSTextAlignmentCenter;
    _memoryLabel.textColor = [UIColor blackColor];
    [_memoryLabel setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
    [_topStackView addArrangedSubview:_memoryLabel];
    
    _bottomStackView = [[UIStackView alloc] init];
    _bottomStackView.spacing = 30;
    _bottomStackView.axis = UILayoutConstraintAxisHorizontal;
    _bottomStackView.distribution = UIStackViewDistributionFillEqually;
    [self addSubview:_bottomStackView];
    
    _cpuProgressView = [[PanoOverallProgressView alloc] init];
    [_bottomStackView addArrangedSubview:_cpuProgressView];
    
    _memoryProgressView = [[PanoOverallProgressView alloc] init];
    [_bottomStackView addArrangedSubview:_memoryProgressView];
    
    
}

- (void)initConstraints {
    [_topStackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.right.mas_equalTo(self).insets(UIEdgeInsetsMake(0, 20, 0, 20));
        make.height.mas_equalTo(50);
    }];
    [_bottomStackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.bottom.right.mas_equalTo(self).insets(UIEdgeInsetsMake(0, 20, 0, 20));;
        make.height.mas_equalTo(40);
    }];
}

@end
