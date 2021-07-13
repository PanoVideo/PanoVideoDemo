//
//  PanoAnnotationToolView.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAnnotationToolView.h"

@interface PanoAnnotationToolView ()
@property (strong, nonatomic) UIStackView *stackView;
@property (strong, nonatomic) NSArray<PanoAction *> *items;
- (void)resetButtons;
@end

@implementation PanoAnnotationToolView

- (void)initViews {
    self.backgroundColor = [UIColor clearColor];
    _stackView = [[UIStackView alloc] init];
    _stackView.backgroundColor = [UIColor whiteColor];
    _stackView.axis = UILayoutConstraintAxisHorizontal;
    _stackView.distribution = UIStackViewDistributionFillEqually;
    [self addSubview:_stackView];
    for (NSInteger i=0; i<self.items.count; i++) {
        PanoAction *action = self.items[i];
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setTitle:action.title forState:UIControlStateNormal];
        [button setTitle:action.title forState:UIControlStateHighlighted];
        button.titleLabel.font = [UIFont systemFontOfSize:12];
        [button setTitleColor:[UIColor pano_colorWithHexString:@"#333333"] forState:UIControlStateNormal];
        [button setTitleColor:[UIColor pano_colorWithHexString:@"#0899F9"] forState:UIControlStateSelected];
        [button setImage:action.imgIcon forState:UIControlStateNormal];
        [button setImage:action.selectedIcon forState:UIControlStateHighlighted];
        [button setImage:action.selectedIcon forState:UIControlStateSelected];
        [button addTarget:self action:@selector(clickAction:) forControlEvents:UIControlEventTouchUpInside];
        [button sizeToFit];
        button.selected = false;
        button.tag = i;
        static CGFloat space = 10.0;
        CGSize imageSize = button.imageView.frame.size;
        CGSize titleSize = button.titleLabel.frame.size;
        [button setImageEdgeInsets:UIEdgeInsetsMake(-titleSize.height - space/2.0, 0, 0, -titleSize.width)];
        [button setTitleEdgeInsets:UIEdgeInsetsMake(0, -imageSize.width, -imageSize.height - space/2.0, 0)];
        [_stackView addArrangedSubview:button];
    }
    
    [_stackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.mas_equalTo(self);
        make.height.mas_equalTo(70);
    }];
}

- (void)clickAction:(UIButton *)button {
    [self resetButtons];
    PanoAction *action = self.items[button.tag];
    button.selected = true;
    if (action) {
        action.handler(action);
    }
}

- (void)resetButtons {
    [_stackView.subviews enumerateObjectsUsingBlock:^(__kindof UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.selected = false;
    }];
}

#pragma mark -- Public Methods

- (instancetype)initWithItems:(NSArray<PanoAction *> *)items {
    PanoAnnotationToolView *annotationToolView = [[PanoAnnotationToolView alloc] init];
    annotationToolView.items = items;
    [annotationToolView initViews];
    return annotationToolView;
}

- (void)show {
    if (!self.isHidden) {
        return;
    }
    self.hidden = false;
    UIButton *button = _stackView.subviews[1];
    button.selected = true;
}

- (void)hide {
    [self resetButtons];
    self.hidden = true;
}

- (BOOL)isVisible {
    return !self.isHidden;
}
@end

