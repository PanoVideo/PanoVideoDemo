//
//  PanoAnnotationColorView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAnnotationPenView.h"
#import "UIColor+Extension.h"
#import "UIImage+Extension.h"
#import "TCPopupViewPath.h"
#import "UIImage+IconFont.h"


@interface PanoAnnotationPenView () <PanoAnnotationColorViewDelegate>

@property(nonatomic, strong) UIView *contentView;
@property(nonatomic, strong) UIView *topView;
@property(nonatomic, strong) UIImageView *imageIcon;
@property(nonatomic, strong) UIView *lineView;
@property(nonatomic, strong) UILabel *lineWidthLabel;
@property(nonatomic, strong) UISlider *slider;
@property(nonatomic, strong) PanoAnnotationColorView *colorView;
@property(nonatomic, strong) UIStackView *shapeView;
@property(nonatomic, assign) PanoAnnotationToolOption option;
@property(nonatomic, strong) NSArray<NSArray<id> *> *shapes;
@end

@implementation PanoAnnotationPenView

- (instancetype)initWithOption:(PanoAnnotationToolOption)option
{
    self = [super initWithFrame:CGRectMake(0, 0, 300, 100)];
    if (self) {
        _option = option;
        [self initViews];
        [self initConstraints];
    }
    return self;
}

- (void)initViews {
    self.backgroundColor = [UIColor clearColor];
    _bgColor = [UIColor whiteColor];
    _contentView = [[UIView alloc] init];
    _contentView.backgroundColor = _bgColor;
    _contentView.layer.cornerRadius = 5;
    _contentView.layer.masksToBounds = true;
    [self addSubview:_contentView];
    
    _topView = [[UIView alloc] init];
    _topView.backgroundColor = [UIColor clearColor];
    [_contentView addSubview:_topView];
    
    if (_option & (PanoAnnotationToolPath | PanoAnnotationToolText)) {
        NSString *imageName = _option == PanoAnnotationToolPath ?
                              @"\U0000e78a" : @"\U0000e792";
        _imageIcon = [[UIImageView alloc] initWithImage:[UIImage imageWithIconFontSize:24 text:imageName color:appMainColor()]];
        [_topView addSubview:_imageIcon];
        
        _lineWidthLabel = [[UILabel alloc] initWithFrame:CGRectZero];
        _lineWidthLabel.font = [UIFont systemFontOfSize:14];
        [_lineWidthLabel setTextAlignment:NSTextAlignmentCenter];
        _lineWidthLabel.textColor = [UIColor blackColor];
        _lineWidthLabel.text = @"3";
        [_lineWidthLabel sizeToFit];
        [_topView addSubview:_lineWidthLabel];
        
        _slider = [[UISlider alloc] init];
        _slider.minimumValue = 1;
        _slider.maximumValue = 20;
        _slider.tintColor = appHighlightedColor();
        [_slider setThumbImage:[UIImage imageNamed:@"annotation.slider"] forState:UIControlStateNormal];
        [_slider addTarget:self action:@selector(chooseLineWidth:) forControlEvents:UIControlEventValueChanged];
        [_topView addSubview:_slider];
    } else if (_option & PanoAnnotationToolShape) {
        [_topView addSubview:self.shapeView];
    }
    
    _lineView = [[UIView alloc] init];
    _lineView.backgroundColor = [UIColor pano_colorWithHexString:@"e5e5e5"];
    [_contentView addSubview:_lineView];
    
    _colorView = [PanoAnnotationColorView new];
    _colorView.delegate = self;
    [_contentView addSubview:_colorView];
}

- (UIStackView *)shapeView {
    if (!_shapeView) {
        _shapeView = [[UIStackView alloc] init];
        _shapeView.backgroundColor = [UIColor clearColor];
        _shapeView.axis = UILayoutConstraintAxisHorizontal;
        _shapeView.distribution = UIStackViewDistributionFillEqually;
    }
    return _shapeView;
}

- (void)setShapes:(NSArray<NSArray<id> *> *)shapes {
    _shapes = shapes;
    if (shapes.count < 5) {
        [_shapeView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.width.mas_equalTo(_shapeView.superview).multipliedBy(0.6);
        }];
    }
    for (NSInteger i=0; i<shapes.count; i++) {
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        UIImage *normalImage = [UIImage imageWithIconFontSize:24 text:shapes[i][0] color:appMainColor()];
        UIImage *selectedImage = [UIImage imageWithIconFontSize:24 text:shapes[i][0] color:appHighlightedColor()];
        [button setImage:normalImage forState:UIControlStateNormal];
        [button setImage:selectedImage forState:UIControlStateSelected];
        [button addTarget:self action:@selector(chooseShape:) forControlEvents:UIControlEventTouchUpInside];
        button.tag = i;
        [self.shapeView addArrangedSubview:button];
    }
}

- (void)setActiveShape:(PanoShapeType)activeShape {
    _activeShape = activeShape;
    for (NSInteger i=0; i<self.shapeView.subviews.count; i++) {
        if ([self.shapes[i][1] integerValue] == activeShape) {
            ((UIButton *)self.shapeView.subviews[i]).selected = true;
        }
    }
}

- (void)chooseShape:(UIButton *)sender {
    [_delegate annotationViewDidChooseShape:[_shapes[sender.tag][1] integerValue]];
    for (UIButton *btn in self.shapeView.subviews) {
        btn.selected = false;
    }
    sender.selected = true;
}

- (void)setColors:(NSArray *)colors {
    _colorView.colors = colors;
}

- (void)setActiveColor:(UIColor *)activeColor {
    [_colorView setActiveColor:activeColor];
}

- (void)setRange:(PanoRange)range {
    if (range.min > range.max || !_slider) {
        return;
    }
    _slider.minimumValue = range.min;
    _slider.maximumValue = range.max;
}

- (void)setLineWidth:(UInt32)lineWidth {
    _lineWidth = lineWidth;
    _slider.value = lineWidth;
    [self chooseLineWidth:_slider];
}

- (void)setArrowPosition:(CGFloat)arrowPosition {
    UIWindow *window = UIApplication.sharedApplication.keyWindow;
    CGFloat width = CGRectGetWidth(window.bounds);
    CGFloat selfWidth = MIN(CGRectGetWidth(window.frame), CGRectGetHeight(window.frame)) - 30;
    _arrowPosition = arrowPosition - (width - selfWidth) * 0.5;
    [self setNeedsLayout];
    [self layoutIfNeeded];
}

- (void)initConstraints {
    [_contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.top.mas_equalTo(self).insets(UIEdgeInsetsMake(1,1,1,1));
        make.height.mas_equalTo(98);
    }];
    
    [_topView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.mas_equalTo(self.contentView);
        make.height.mas_equalTo(49);
    }];
    
    [_imageIcon mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.contentView).insets(UIEdgeInsetsMake(10, 20, 0, 0));
        make.centerY.mas_equalTo(self.topView);
        make.height.mas_equalTo(20);
    }];

    [_lineWidthLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(_imageIcon);
        make.left.mas_equalTo(_imageIcon.mas_right).mas_offset(15);
    }];

    [_slider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(_imageIcon);
        make.left.mas_equalTo(_lineWidthLabel.mas_right).mas_offset(15);
        make.right.mas_equalTo(self.contentView).mas_offset(-15);
    }];

    [_lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self.contentView);
        make.height.mas_equalTo(1);
        make.top.mas_equalTo(_topView.mas_bottom);
    }];
    
    [_colorView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.contentView).mas_offset(15);
        make.right.mas_equalTo(self.contentView).mas_offset(-15);
        make.height.mas_equalTo(49);
        make.bottom.mas_equalTo(self.contentView);
    }];
    if (_shapeView) {
        [_shapeView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.top.bottom.width.mas_equalTo(_shapeView.superview);
        }];
    }
}

- (void)drawRect:(CGRect)rect {
    UIColor *borderColor = [UIColor pano_colorWithHexString:@"#F4F4F4"];
    [TCPopupViewPath yb_bezierPathWithRect:self.frame rectCorner:UIRectCornerAllCorners cornerRadius:5 borderWidth:1 borderColor:borderColor backgroundColor:[UIColor whiteColor] arrowWidth:18 arrowHeight:11 arrowPosition:_arrowPosition arrowDirection:YBPopupMenuArrowDirectionBottom completion:^(UIBezierPath *bezierPath, CGContextRef context) {
        CGContextSetShadowWithColor(context, CGSizeMake(1, 1), 2, [UIColor colorWithRed:4/255.0 green:0/255.0 blue:0/255.0 alpha:0.16].CGColor);
    }];
}

- (void)chooseLineWidth:(UISlider *)slider {
    if (!slider) return;
    UInt32 v = slider.value;
    _lineWidthLabel.text = [NSString stringWithFormat:@"%d",v];
    [self.delegate annotationViewDidChooseLineWidth:v];
}

#pragma mark -- Public Methods
static PanoAnnotationPenView *tipView = nil;
+ (PanoAnnotationPenView *)showWithParentView:(UIView *)parentView
                         arrowPositionScreenX:(CGFloat)arrowPosition
                                   toolOption:(PanoAnnotationToolOption)option
                                       colors:(NSArray<NSString *> *)colors
                                       shapes:(NSArray<NSArray<id> *> *)shapes {
    if (!tipView) {
        CGFloat width = MIN(CGRectGetWidth(parentView.frame), CGRectGetHeight(parentView.frame));
        CGFloat viewWidth = width-30;
        CGFloat viewHeight = 111;
        tipView = [[PanoAnnotationPenView alloc] initWithOption:option];
        tipView.colors = colors;
        tipView.shapes = shapes;
        tipView.arrowPosition = arrowPosition;
        [parentView addSubview:tipView];
        [tipView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.width.mas_equalTo(viewWidth);
            make.centerX.mas_equalTo(tipView.superview);
            make.bottom.mas_equalTo(tipView.superview).mas_offset(-(80 + pano_safeAreaInset(parentView).bottom));
            make.height.mas_equalTo(viewHeight);
        }];
    }
    tipView.alpha = 0.01;
    [UIView animateWithDuration:0.15 animations:^{
        tipView.alpha = 1;
    }];
    return tipView;
}

- (void)dismiss {
    if (tipView) {
        [UIView animateWithDuration:0.3 animations:^{
            tipView.alpha = 0.0;
        } completion:^(BOOL finished) {
            [tipView removeFromSuperview];
            tipView = nil;
        }];
    }
}

- (BOOL)isShowing {
    return tipView.alpha > 0.0;
}

#pragma mark -- PanoAnnotationColorViewDelegate
- (void)annotationColorViewDidChooseColor:(UIColor *)color {
    [self.delegate annotationColorViewDidChooseColor:color];
}

@end


@interface PanoAnnotationColorView ()
@property(nonatomic, strong) UIStackView *contentStackView;
@end

@implementation PanoAnnotationColorView

- (void)initViews {
    _contentStackView = [[UIStackView alloc] init];
    _contentStackView.backgroundColor = [UIColor clearColor];
    _contentStackView.axis = UILayoutConstraintAxisHorizontal;
    _contentStackView.distribution = UIStackViewDistributionFillEqually;
    [self addSubview:_contentStackView];
}

- (void)initConstraints {
    [_contentStackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self).insets(UIEdgeInsetsMake(9, 0, 9, 0));
    }];
}

- (void)setColors:(NSArray<NSString *> *)colors {
    _colors = colors;
    for (NSInteger i=0; i<_colors.count; i++) {
        NSString *colorString = self.colors[i];
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        UIColor *color = [UIColor pano_colorWithHexString:colorString];
        UIImage *image = [UIImage pano_imageWithColor:color size:CGSizeMake(20, 20)];
        [button setImage:image forState:UIControlStateNormal];
        [button setImage:image forState:UIControlStateHighlighted];
        button.tintColor = color;
        [button addTarget:self action:@selector(chooseColor:) forControlEvents:UIControlEventTouchUpInside];
        [_contentStackView addArrangedSubview:button];
        button.tag = i;
    }
}

- (void)chooseColor:(UIButton *)sender {
    NSString *colorString = self.colors[sender.tag];
    UIColor *color = [UIColor pano_colorWithHexString:colorString];
    _activeColor = color;
    [self updateColorButtons];
    [self.delegate annotationColorViewDidChooseColor:color];
}

- (void)setActiveColor:(UIColor *)activeColor {
    _activeColor = activeColor;
    [self updateColorButtons];
}
 
- (void)updateColorButtons {
    for (UIButton *button in _contentStackView.arrangedSubviews) {
        if (CGColorEqualToColor(button.tintColor.CGColor, _activeColor.CGColor)) {
            button.layer.cornerRadius = 3;
            button.layer.masksToBounds = true;
            button.layer.borderColor = [UIColor pano_colorWithHexString:@"#01BBF4"].CGColor;
            button.layer.borderWidth = 1;
        } else {
            button.layer.masksToBounds = false;
            button.layer.borderWidth = 0.0;
        }
    }
}

@end
