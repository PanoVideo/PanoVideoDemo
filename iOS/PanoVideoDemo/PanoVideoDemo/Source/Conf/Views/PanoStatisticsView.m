//
//  PanoStatisticsView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoStatisticsView.h"

@interface PanoStatisticsView ()

@property (nonatomic, strong) UIView *contentBackgroudView;
@property (nonatomic, strong) UIStackView *contentView;
@property (nonatomic, strong) NSArray<NSString *> *items;
@property (nonatomic, assign) NSInteger valuesCount;
@property (nonatomic, strong) UIStackView *itemStackView;
@end

@implementation PanoStatisticsView

- (instancetype)initWithItems:(NSArray *)items valuesCount:(NSInteger)count {
    
    self.items = items;
    
    self.valuesCount = count;
    
    self = [super initWithFrame:CGRectZero];

    return self;
}

- (void)initViews {
    _contentBackgroudView = [[UIView alloc] initWithFrame:CGRectZero];
    _contentBackgroudView.layer.borderColor = [UIColor lightGrayColor].CGColor;
    _contentBackgroudView.layer.borderWidth = 0.5;
    _contentBackgroudView.layer.masksToBounds = true;
    _contentBackgroudView.layer.cornerRadius = 8;
    [self addSubview:_contentBackgroudView];
    
    _contentView = [[UIStackView alloc] initWithFrame:CGRectZero];
    _contentView.axis = UILayoutConstraintAxisHorizontal;
    _contentView.distribution = UIStackViewDistributionEqualSpacing;
    _contentView.spacing = 0;
    [_contentBackgroudView addSubview:_contentView];
    
    [_contentBackgroudView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.top.mas_equalTo(self).insets(UIEdgeInsetsMake(15, 20, 15, 20));
        make.height.mas_equalTo(30 * self.items.count + 15*2);
    }];
    
    [_contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.contentBackgroudView).insets(UIEdgeInsetsMake(15, 15, 15, 15));
    }];
    
    NSMutableArray *itemViews = [NSMutableArray array];
    for (NSString *title in self.items) {
        UILabel *itemLabel = [[UILabel alloc] init];
        itemLabel.textAlignment = NSTextAlignmentLeft;
        itemLabel.textColor = [UIColor pano_colorWithHexString:@"#999999"];
        [itemLabel setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
        itemLabel.text = title;
        [itemViews addObject:itemLabel];
    }
    _itemStackView = [[UIStackView alloc] initWithArrangedSubviews:itemViews];
    _itemStackView.axis = UILayoutConstraintAxisVertical;
    _itemStackView.distribution = UIStackViewDistributionFillEqually;
    _itemStackView.spacing = 15;
    [_contentView addArrangedSubview:_itemStackView];
    
    for (NSInteger i=0; i<self.valuesCount; i++) {
        NSMutableArray *itemViews = [NSMutableArray array];
        for (NSInteger i=0; i<self.items.count; i++) {
            UILabel *itemLabel = [[UILabel alloc] init];
            itemLabel.textAlignment = NSTextAlignmentLeft;
            itemLabel.textColor = [UIColor pano_colorWithHexString:@"#333333"];
            [itemLabel setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
            itemLabel.text = @"-";
            [itemLabel sizeToFit];
            [itemViews addObject:itemLabel];
        }
        UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:itemViews];
        stackView.axis = UILayoutConstraintAxisVertical;
        stackView.distribution = UIStackViewDistributionFillEqually;
        stackView.spacing = 15;
        [_contentView addArrangedSubview:stackView];
    }
}

- (void)initConstraints {
    
}


- (void)setValues:(NSArray<NSArray<NSString *> *> *)values {
    _values = values;
    for (NSInteger i=0; i<self.valuesCount; i++) {
        UIStackView *stackView = _contentView.arrangedSubviews[i+1];
        for (NSInteger j=0; j<stackView.arrangedSubviews.count; j++) {
            UILabel *label = stackView.arrangedSubviews[j];
            label.text = values[i][j];
        }
    }
}

@end
